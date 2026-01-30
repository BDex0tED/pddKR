package com.ivyro.pdd.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TextParserService {

    private final Logger logger = LoggerFactory.getLogger(TextParserService.class);

    private static final Pattern CHUNK_START_PATTERN = Pattern.compile("^(\\d+\\.\\d+(\\.\\d+)?\\.?|\\\"[А-Яа-я].+\\\"|\\d+\\.\\s).*");

    /**
     * Parses a raw document into semantic chunks based on PDD structure.
     */
    public List<Document> parse(Document sourceDoc) {
        if (sourceDoc == null) throw new NullPointerException("Provided document is null");

        String fullText = sourceDoc.getText();
        List<Document> chunks = new ArrayList<>();

        String[] lines = fullText.split("\\r?\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            if (isNewChunkStart(trimmedLine)) {
                if (currentChunk.length() > 0) {
                    chunks.add(new Document(currentChunk.toString(), Map.of("source", "pdd.docx")));
                    currentChunk.setLength(0);
                }
                currentChunk.append(trimmedLine);
            } else {
                currentChunk.append(" ").append(trimmedLine);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(new Document(currentChunk.toString(), Map.of("source", "pdd.docx")));
        }

        logger.info("Parsed document into {} semantic chunks.", chunks.size());
        return chunks;
    }

    private boolean isNewChunkStart(String line) {
        Matcher matcher = CHUNK_START_PATTERN.matcher(line);
        return matcher.matches();
    }
}