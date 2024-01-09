package com.crawler.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class CrawlResultWriter {
    private final CrawlResult result;

    public CrawlResultWriter(CrawlResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public void write(Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            write(writer);
        } catch (IOException e) {
            System.out.println("Failed to write crawling result to file");
            e.printStackTrace();
        }
    }

    public void write(Writer writer) {
        var objectMapper = new ObjectMapper();
        objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        try {
            objectMapper.writeValue(writer, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
