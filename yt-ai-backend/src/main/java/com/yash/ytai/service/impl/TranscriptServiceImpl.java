package com.yash.ytai.service.impl;

import com.yash.ytai.exception.TranscriptFetchException;
import com.yash.ytai.model.TranscriptItem;
import com.yash.ytai.service.TranscriptService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Fetches YouTube transcripts by scraping the YouTube timedtext endpoint.
 *
 * <p>This mirrors the Node.js {@code youtube-transcript} library approach —
 * no YouTube Data API key required. The approach:
 * <ol>
 *   <li>Fetch the video page HTML to extract the {@code captionTracks} JSON</li>
 *   <li>Find the English (or first available) {@code baseUrl}</li>
 *   <li>Fetch the timed-text XML from that URL</li>
 *   <li>Parse each {@code <text>} element into a {@link TranscriptItem}</li>
 * </ol>
 */
@Service
@Slf4j
public class TranscriptServiceImpl implements TranscriptService {

    private final WebClient httpClient;

    public TranscriptServiceImpl() {
        this.httpClient = WebClient.builder()
                .defaultHeader("User-Agent", "node")
                .defaultHeader("Accept-Language", "en-US")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    @Override
    public List<TranscriptItem> fetchTranscript(String videoId) {
        log.info("Fetching transcript for video: {}", videoId);
        try {
            // Step 1: Fetch the video page
            String pageHtml = httpClient.get()
                    .uri("https://www.youtube.com/watch?v=" + videoId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (pageHtml == null || pageHtml.isBlank()) {
                throw new TranscriptFetchException("Empty response from YouTube for video: " + videoId);
            }

            // Step 2: Extract captionTracks JSON block from the page source
            String captionBaseUrl = extractCaptionBaseUrl(pageHtml, videoId);

            // Step 3: Fetch the timed-text XML
            String transcriptXml = httpClient.get()
                    .uri(captionBaseUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (transcriptXml == null || transcriptXml.isBlank()) {
                throw new TranscriptFetchException("Empty transcript XML for video: " + videoId);
            }

            // Step 4: Parse XML into TranscriptItem list
            List<TranscriptItem> items = parseTranscriptXml(transcriptXml);
            log.info("Fetched {} transcript items for video: {}", items.size(), videoId);
            return items;

        } catch (TranscriptFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new TranscriptFetchException(
                    "Failed to fetch transcript for video '" + videoId + "': " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the caption track base URL from the YouTube video page HTML.
     * Searches for the "captionTracks" JSON blob embedded in the page script.
     */
    private String extractCaptionBaseUrl(String pageHtml, String videoId) {
        // Find the captionTracks segment in the ytInitialPlayerResponse JSON
        int captionIdx = pageHtml.indexOf("\"captionTracks\":");
        if (captionIdx == -1) {
            throw new TranscriptFetchException(
                    "No captions available for video: " + videoId +
                    ". The video may not have auto-generated subtitles.");
        }

        // Extract the baseUrl value — find first occurrence after captionTracks
        int baseUrlStart = pageHtml.indexOf("\"baseUrl\":\"", captionIdx);
        if (baseUrlStart == -1) {
            throw new TranscriptFetchException("Could not find caption baseUrl for video: " + videoId);
        }
        baseUrlStart += "\"baseUrl\":\"".length();
        int baseUrlEnd = pageHtml.indexOf("\"", baseUrlStart);

        String baseUrl = pageHtml.substring(baseUrlStart, baseUrlEnd)
                .replace("\\u0026", "&")
                .replace("\\/", "/");

        log.debug("Extracted caption URL for video {}", videoId);
        return baseUrl;
    }

    /**
     * Parses the timed-text XML into a list of {@link TranscriptItem}.
     *
     * <p>Each {@code <text start="..." dur="...">content</text>} element maps to:
     * <ul>
     *   <li>{@code offset} = {@code start} * 1000 (milliseconds)</li>
     *   <li>{@code duration} = {@code dur} * 1000 (milliseconds)</li>
     *   <li>{@code text} = decoded HTML entities in content</li>
     * </ul>
     */
    private List<TranscriptItem> parseTranscriptXml(String xml) {
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        return doc.select("text").stream()
                .map(element -> {
                    double startSec = Double.parseDouble(element.attr("start"));
                    double durSec   = Double.parseDouble(
                            element.hasAttr("dur") ? element.attr("dur") : "1.0");

                    // Decode HTML entities (YouTube XML uses &amp;, &#39;, etc.)
                    String text = Jsoup.parse(element.text()).text();

                    return TranscriptItem.builder()
                            .text(text)
                            .offset((long) (startSec * 1000))    // Convert seconds → ms
                            .duration((long) (durSec * 1000))    // Convert seconds → ms
                            .build();
                })
                .filter(item -> item.getText() != null && !item.getText().isBlank())
                .collect(Collectors.toList());
    }
}
