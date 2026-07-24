package com.yash.ytai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yash.ytai.exception.TranscriptFetchException;
import com.yash.ytai.model.TranscriptItem;
import com.yash.ytai.service.TranscriptService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fetches YouTube transcripts using the YouTube InnerTube API.
 *
 * <p>Tries multiple InnerTube client strategies in sequence to handle cloud-IP
 * restrictions (e.g., Render, Railway, Heroku servers) where YouTube may block
 * or return empty responses for certain client types. Falls back to web scraping
 * with consent-bypass headers if all InnerTube strategies fail.
 */
@Service
@Slf4j
public class TranscriptServiceImpl implements TranscriptService {

    private static final String INNERTUBE_API_URL =
            "https://www.youtube.com/youtubei/v1/player?prettyPrint=false";

    // Modern browser User-Agent — used for web-scrape fallback
    private static final String BROWSER_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";

    /**
     * InnerTube client strategies tried in order.
     * Each entry: { clientName, clientVersion, userAgent, [clientPlatform] }
     * Cloud IPs that fail ANDROID often succeed with TVHTML5 or IOS.
     */
    private static final List<Map<String, String>> CLIENT_STRATEGIES = List.of(
        Map.of(
            "clientName", "ANDROID",
            "clientVersion", "20.10.38",
            "userAgent", "com.google.android.youtube/20.10.38 (Linux; U; Android 14) gzip"
        ),
        Map.of(
            "clientName", "TVHTML5",
            "clientVersion", "7.20241010.18.00",
            "userAgent", BROWSER_USER_AGENT,
            "clientPlatform", "TV"
        ),
        Map.of(
            "clientName", "IOS",
            "clientVersion", "19.45.4",
            "userAgent", "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X)"
        ),
        Map.of(
            "clientName", "WEB_EMBEDDED_PLAYER",
            "clientVersion", "2.20230914.02.00",
            "userAgent", BROWSER_USER_AGENT
        )
    );

    private final WebClient httpClient;
    private final ObjectMapper objectMapper;

    public TranscriptServiceImpl() {
        this.httpClient = WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<TranscriptItem> fetchTranscript(String videoId) {
        log.info("Fetching transcript for video: {}", videoId);
        try {
            // Step 1: Try each InnerTube client strategy in order
            for (Map<String, String> strategy : CLIENT_STRATEGIES) {
                String clientName = strategy.get("clientName");
                try {
                    List<TranscriptItem> items = fetchViaInnerTube(videoId, strategy);
                    if (items != null && !items.isEmpty()) {
                        log.info("Fetched {} transcript items via InnerTube [{}] for video: {}",
                                items.size(), clientName, videoId);
                        return items;
                    }
                    log.debug("InnerTube [{}] returned no caption tracks for video: {}", clientName, videoId);
                } catch (Exception e) {
                    log.warn("InnerTube [{}] failed for video {}: {}", clientName, videoId, e.getMessage());
                }
            }

            // Step 2: Final fallback — web page scraping with consent-bypass headers
            log.warn("All InnerTube strategies failed for {}, falling back to web scrape", videoId);
            List<TranscriptItem> items = fetchViaWebPage(videoId);
            log.info("Fetched {} transcript items via web scrape for video: {}", items.size(), videoId);
            return items;

        } catch (TranscriptFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new TranscriptFetchException(
                    "Failed to fetch transcript for video '" + videoId + "': " + e.getMessage(), e);
        }
    }

    /**
     * Fetches transcript using a specific InnerTube client strategy.
     * Using multiple client types handles cloud-IP blocks — YouTube is more lenient
     * with TVHTML5 and IOS clients from datacenter IPs than with ANDROID.
     */
    private List<TranscriptItem> fetchViaInnerTube(String videoId, Map<String, String> strategy) {
        Map<String, Object> clientContext = new HashMap<>();
        clientContext.put("clientName", strategy.get("clientName"));
        clientContext.put("clientVersion", strategy.get("clientVersion"));
        clientContext.put("hl", "en");
        clientContext.put("gl", "US");
        if (strategy.containsKey("clientPlatform")) {
            clientContext.put("clientPlatform", strategy.get("clientPlatform"));
        }

        Map<String, Object> body = Map.of(
            "context", Map.of("client", clientContext),
            "videoId", videoId
        );

        String responseBody = httpClient.post()
                .uri(INNERTUBE_API_URL)
                .header("Content-Type", "application/json")
                .header("User-Agent", strategy.get("userAgent"))
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Origin", "https://www.youtube.com")
                .header("X-Youtube-Client-Name", getClientId(strategy.get("clientName")))
                .header("X-Youtube-Client-Version", strategy.get("clientVersion"))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode captionTracks = root
                .path("captions")
                .path("playerCaptionsTracklistRenderer")
                .path("captionTracks");

        if (!captionTracks.isArray() || captionTracks.isEmpty()) {
            return null;
        }

        // Prefer English track; fall back to first available
        String trackUrl = null;
        for (JsonNode track : captionTracks) {
            String lang = track.path("languageCode").asText("");
            if ("en".equals(lang)) {
                trackUrl = track.path("baseUrl").asText(null);
                break;
            }
        }
        if (trackUrl == null) {
            trackUrl = captionTracks.get(0).path("baseUrl").asText(null);
        }

        if (trackUrl == null || trackUrl.isBlank()) {
            return null;
        }

        log.debug("Fetching transcript XML from InnerTube [{}] track URL for: {}",
                strategy.get("clientName"), videoId);
        return fetchAndParseXml(trackUrl, videoId);
    }

    /**
     * Maps InnerTube client name to its numeric ID for the X-Youtube-Client-Name header.
     */
    private String getClientId(String clientName) {
        return switch (clientName) {
            case "ANDROID"             -> "3";
            case "TVHTML5"             -> "7";
            case "IOS"                 -> "5";
            case "WEB_EMBEDDED_PLAYER" -> "56";
            default                    -> "1"; // WEB
        };
    }

    /**
     * Fallback: fetches the YouTube video page HTML and scrapes the captionTracks URL.
     * Uses full browser headers and a consent cookie to bypass YouTube's consent wall —
     * this is what causes "captionTracks not found" on cloud/datacenter IPs.
     */
    private List<TranscriptItem> fetchViaWebPage(String videoId) {
        String pageHtml = httpClient.get()
                .uri("https://www.youtube.com/watch?v=" + videoId + "&hl=en")
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                // Bypasses YouTube's GDPR consent wall that cloud IPs commonly hit
                .header("Cookie", "CONSENT=YES+cb.20210328-17-p0.en+FX+294; SOCS=CAI=; YSC=bypass")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (pageHtml == null || pageHtml.isBlank()) {
            throw new TranscriptFetchException("Empty response from YouTube for video: " + videoId);
        }

        String captionBaseUrl = extractCaptionBaseUrl(pageHtml, videoId);
        return fetchAndParseXml(captionBaseUrl, videoId);
    }

    /**
     * Fetches the transcript XML from a caption track URL and parses it.
     */
    private List<TranscriptItem> fetchAndParseXml(String url, String videoId) {
        String transcriptXml = httpClient.get()
                .uri(url)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (transcriptXml == null || transcriptXml.isBlank()) {
            throw new TranscriptFetchException("Empty transcript XML for video: " + videoId);
        }

        return parseTranscriptXml(transcriptXml);
    }

    /**
     * Extracts the caption track base URL from the YouTube video page HTML.
     */
    private String extractCaptionBaseUrl(String pageHtml, String videoId) {
        int captionIdx = pageHtml.indexOf("\"captionTracks\":");
        if (captionIdx == -1) {
            throw new TranscriptFetchException(
                    "No captions available for video: " + videoId +
                    ". The video may not have auto-generated subtitles.");
        }

        int baseUrlStart = pageHtml.indexOf("\"baseUrl\":\"", captionIdx);
        if (baseUrlStart == -1) {
            throw new TranscriptFetchException("Could not find caption baseUrl for video: " + videoId);
        }
        baseUrlStart += "\"baseUrl\":\"".length();
        int baseUrlEnd = pageHtml.indexOf("\"", baseUrlStart);

        return pageHtml.substring(baseUrlStart, baseUrlEnd)
                .replace("\\u0026", "&")
                .replace("\\/", "/");
    }

    /**
     * Parses timed-text XML — supports both srv3 format and classic format.
     */
    private List<TranscriptItem> parseTranscriptXml(String xml) {
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        // Try srv3 format first: <p t="ms" d="ms">
        List<TranscriptItem> srv3 = new ArrayList<>();
        for (org.jsoup.nodes.Element p : doc.select("p[t][d]")) {
            long offsetMs = Long.parseLong(p.attr("t"));
            long durMs    = Long.parseLong(p.attr("d"));
            String text = p.select("s").stream()
                    .map(org.jsoup.nodes.Element::text)
                    .collect(Collectors.joining(""));
            if (text.isBlank()) text = p.text();
            text = Jsoup.parse(text).text().trim();
            if (!text.isBlank()) {
                srv3.add(TranscriptItem.builder()
                        .text(text)
                        .offset(offsetMs)
                        .duration(durMs)
                        .build());
            }
        }
        if (!srv3.isEmpty()) return srv3;

        // Fall back to classic format: <text start="s" dur="s">
        return doc.select("text").stream()
                .map(element -> {
                    double startSec = Double.parseDouble(element.attr("start"));
                    double durSec   = Double.parseDouble(
                            element.hasAttr("dur") ? element.attr("dur") : "1.0");
                    String text = Jsoup.parse(element.text()).text();
                    return TranscriptItem.builder()
                            .text(text)
                            .offset((long) (startSec * 1000))
                            .duration((long) (durSec * 1000))
                            .build();
                })
                .filter(item -> item.getText() != null && !item.getText().isBlank())
                .collect(Collectors.toList());
    }
}
