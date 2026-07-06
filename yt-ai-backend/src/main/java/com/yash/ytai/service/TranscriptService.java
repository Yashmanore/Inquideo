package com.yash.ytai.service;

import com.yash.ytai.model.TranscriptItem;

import java.util.List;

/**
 * Fetches the raw YouTube transcript for a given video ID.
 */
public interface TranscriptService {

    /**
     * Fetches all transcript items for the given YouTube video ID.
     *
     * @param videoId the 11-character YouTube video ID
     * @return ordered list of transcript items with text, offset (ms), and duration (ms)
     * @throws com.yash.ytai.exception.TranscriptFetchException if the transcript cannot be retrieved
     */
    List<TranscriptItem> fetchTranscript(String videoId);
}
