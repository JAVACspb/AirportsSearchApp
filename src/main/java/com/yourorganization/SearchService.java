package com.yourorganization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final PrefixTree prefixTree;

    public SearchService(PrefixTree prefixTree) {
        this.prefixTree = prefixTree;
    }

    public List<SearchResult> search(List<String> queries) {
        List<SearchResult> results = new ArrayList<>();
        for (String query : queries) {
            String normalizedQuery = query.toLowerCase().trim();
            logger.info("Ищу по префиксу: \"{}\"", normalizedQuery);

            long startTime = System.nanoTime(); // Начало измерения времени
            List<Integer> matchedRows = prefixTree.search(normalizedQuery);
            long elapsedTimeNanos = System.nanoTime() - startTime; // Измеренное время в наносекундах

            double elapsedTimeMillis = elapsedTimeNanos / 1_000_000.0; // Конвертация в миллисекунды (с плавающей точкой)
            logger.info("Результат поиска для \"{}\": {} (время: {} мс)", normalizedQuery, matchedRows, elapsedTimeMillis);

            results.add(new SearchResult(normalizedQuery, matchedRows, elapsedTimeMillis));
        }
        return results;
    }





    public static class SearchResult {
        private final String search;
        private final List<Integer> result;
        private final double time;

        public SearchResult(String search, List<Integer> result, double time) {
            this.search = search;
            this.result = result;
            this.time = time;
        }

        @JsonProperty
        public String getSearch() {
            return search;
        }

        @JsonProperty
        public List<Integer> getResult() {
            return result;
        }

        @JsonProperty
        public double getTime() {
            return time;
        }
    }
}
