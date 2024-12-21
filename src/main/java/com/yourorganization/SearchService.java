package com.yourorganization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для выполнения поиска по префиксному дереву.
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final PrefixTree prefixTree;

    public SearchService(PrefixTree prefixTree) {
        this.prefixTree = Objects.requireNonNull(prefixTree, "PrefixTree не должен быть null.");
    }

    /**
     * Выполняет поиск для каждого запроса в списке.
     *
     * @param queries список строк для поиска
     * @return список результатов поиска
     * @throws IllegalArgumentException если список запросов пустой или null
     */
    public List<SearchResult> search(List<String> queries) {
        if (queries == null || queries.isEmpty()) {
            throw new IllegalArgumentException("Список запросов не должен быть null или пустым.");
        }

        List<SearchResult> results = new ArrayList<>();
        for (String query : queries) {
            if (query == null || query.isBlank()) {
                logger.warn("Пустой или null-запрос пропущен.");
                continue;
            }

            String normalizedQuery = query.toLowerCase().trim();
            logger.info("Ищу по префиксу: \"{}\"", normalizedQuery);

            long startTime = System.nanoTime(); // Начало измерения времени
            List<Integer> matchedRows = prefixTree.search(normalizedQuery);
            long elapsedTimeNanos = System.nanoTime() - startTime; // Измеренное время в наносекундах

            double elapsedTimeMillis = elapsedTimeNanos / 1_000_000.0; // Конвертация в миллисекунды
            logger.debug("Результат поиска для \"{}\": {} (время: {} мс)", normalizedQuery, matchedRows, elapsedTimeMillis);

            results.add(new SearchResult(normalizedQuery, matchedRows, elapsedTimeMillis));
        }

        return results;
    }

    /**
     * DTO для хранения результата поиска.
     */
    public static class SearchResult {
        private final String search;
        private final List<Integer> result;
        private final double time;

        public SearchResult(String search, List<Integer> result, double time) {
            this.search = Objects.requireNonNull(search, "Поле search не должно быть null.");
            this.result = Objects.requireNonNull(result, "Поле result не должно быть null.");
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

        @Override
        public String toString() {
            return String.format("SearchResult{search='%s', result=%s, time=%.3f ms}", search, result, time);
        }
    }
}
