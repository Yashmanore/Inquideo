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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fetches YouTube transcripts using the YouTube InnerTube API.
 *
 * <p>This mirrors the {@code youtube-transcript} Node.js library's primary approach:
 * POST to the InnerTube player endpoint with an Android client context to get caption
 * track URLs that actually return transcript XML (unlike the web-page-scraped URLs
 * which now return empty bodies when {@code variant=gemini} is appended).
 */
@Service
@Slf4j
public class TranscriptServiceImpl implements TranscriptService {

    private static final String INNERTUBE_API_URL =
            "https://www.youtube.com/youtubei/v1/player?prettyPrint=false";
    private static final String INNERTUBE_CLIENT_VERSION = "20.10.38";
    private static final String INNERTUBE_USER_AGENT =
            "com.google.android.youtube/" + INNERTUBE_CLIENT_VERSION + " (Linux; U; Android 14)";
    private static final String BROWSER_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36,gzip(gfe)";

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
            // Step 1: Use InnerTube API (Android client) to get caption track list
            List<TranscriptItem> items = fetchViaInnerTube(videoId);
            if (items != null && !items.isEmpty()) {
                log.info("Fetched {} transcript items via InnerTube for video: {}", items.size(), videoId);
                return items;
            }

            // Step 2: Fallback to web page scraping
            log.warn("InnerTube returned no results for {}, falling back to web scrape", videoId);
            items = fetchViaWebPage(videoId);
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
     * Fetches transcript via the YouTube InnerTube API (Android client context).
     * This is the primary method — avoids the empty-body issue with variant=gemini URLs.
     */
    private List<TranscriptItem> fetchViaInnerTube(String videoId) {
        try {
            Map<String, Object> body = Map.of(
                "context", Map.of(
                    "client", Map.of(
                        "clientName", "ANDROID",
                        "clientVersion", INNERTUBE_CLIENT_VERSION
                    )
                ),
                "videoId", videoId
            );

            String responseBody = httpClient.post()
                    .uri(INNERTUBE_API_URL)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", INNERTUBE_USER_AGENT)
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
                log.debug("No caption tracks found via InnerTube for video: {}", videoId);
                return null;
            }

            // Pick the first English track, or the first available
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

            log.debug("Fetching transcript XML from InnerTube track URL for video: {}", videoId);
            return fetchAndParseXml(trackUrl, videoId);

        } catch (Exception e) {
            log.warn("InnerTube approach failed for video {}: {}", videoId, e.getMessage());
            return null;
        }
    }

    /**
     * Fallback: fetches the video page and scrapes the captionTracks JSON.
     */
    private List<TranscriptItem> fetchViaWebPage(String videoId) {
        String pageHtml = httpClient.get()
                .uri("https://www.youtube.com/watch?v=" + videoId)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept-Language", "en-US")
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
     * Fetches the transcript XML from a given URL and parses it into {@link TranscriptItem}s.
     */
    private List<TranscriptItem> fetchAndParseXml(String url, String videoId) {
        String transcriptXml = httpClient.get()
                .uri(url)
                .header("User-Agent", BROWSER_USER_AGENT)
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
     * Parses timed-text XML — supports both classic format and srv3 format.
     */
    private List<TranscriptItem> parseTranscriptXml(String xml) {
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        // Try srv3 format first: <p t="ms" d="ms">
        List<TranscriptItem> srv3 = new ArrayList<>();
        for (org.jsoup.nodes.Element p : doc.select("p[t][d]")) {
            long offsetMs  = Long.parseLong(p.attr("t"));
            long durMs     = Long.parseLong(p.attr("d"));
            // Collect text from <s> children or fall back to element text
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
