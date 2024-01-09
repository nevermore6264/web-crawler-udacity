package com.crawler.parser;

import com.crawler.qualifier.Profiled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface PageParser {

    @Profiled
    Result parse();

    final class Result {
        private final Map<String, Integer> wordCounts;
        private final List<String> links;

        private Result(Map<String, Integer> wordCounts, List<String> links) {
            this.wordCounts = Collections.unmodifiableMap(new HashMap<>(wordCounts));
            this.links = Collections.unmodifiableList(new ArrayList<>(links));
        }

        public Map<String, Integer> getWordCounts() {
            return wordCounts;
        }

        public List<String> getLinks() {
            return links;
        }

        static final class Builder {
            private final Map<String, Integer> wordCounts = new HashMap<>();
            private final Set<String> links = new HashSet<>();

            void addWord(String word) {
                Objects.requireNonNull(word);
                wordCounts.merge(word, 1, Integer::sum);
            }

            void addLink(String link) {
                links.add(Objects.requireNonNull(link));
            }

            Result build() {
                return new Result(wordCounts, new ArrayList<>(links));
            }
        }
    }
}