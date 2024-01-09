package com.crawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class Config {

    private final Path path;

    public Config(Path path) {
        this.path = Objects.requireNonNull(path);
    }


    private static CrawlerConfig read(Reader reader) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        CrawlerConfigBuilder config = null;
        try {
            config = objectMapper.readValue(reader, CrawlerConfigBuilder.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (config != null) ? config.build() : null;
    }

    public CrawlerConfig load() {
        CrawlerConfig config = null;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            config = read(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }
}
