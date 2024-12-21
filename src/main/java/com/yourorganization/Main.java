package com.yourorganization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Приложение запущено");
        logger.info("Аргументы: {}", Arrays.toString(args));
        logger.info("Текущая рабочая директория: {}", System.getProperty("user.dir"));

        long startTime = System.currentTimeMillis();

        try {
            AppArguments appArgs = parseArguments(args);

            File dataFile = new File(appArgs.dataFile());
            File inputFile = new File(appArgs.inputFile());

            // Проверяем существование файлов
            validateFile(dataFile, "Файл данных не найден: ");
            validateFile(inputFile, "Файл с запросами не найден: ");

            // Парсинг CSV
            List<CsvParser.AirportRecord> records = CsvParser.parseCsv(dataFile.getAbsolutePath(), appArgs.columnIndex());
            if (records.isEmpty()) {
                throw new IllegalArgumentException("Файл данных не содержит подходящих записей.");
            }

            // Построение префиксного дерева
            PrefixTree prefixTree = buildPrefixTree(records);

            // Выполнение поиска
            List<String> queries = readQueries(appArgs.inputFile());
            if (queries.isEmpty()) {
                throw new IllegalArgumentException("Файл запросов пуст.");
            }

            SearchService searchService = new SearchService(prefixTree);
            List<SearchService.SearchResult> results = searchService.search(queries);

            // Сохранение результата
            long initTime = System.currentTimeMillis() - startTime;
            saveResults(appArgs.outputFile(), initTime, results);

        } catch (Exception e) {
            logger.error("Ошибка выполнения программы: {}", e.getMessage(), e);
        }
    }

    /**
     * Разбирает аргументы командной строки.
     */
    private static AppArguments parseArguments(String[] args) {
        if (args.length < 8) {
            throw new IllegalArgumentException("""
                Неверное количество параметров. Пример использования:
                --data airports.csv --indexed-column-id 2 --input-file input.txt --output-file output.json
                """);
        }

        return new AppArguments(
                args[1], // dataFile
                Integer.parseInt(args[3]) - 1, // columnIndex (переводим в 0-индекс)
                args[5], // inputFile
                args[7]  // outputFile
        );
    }

    /**
     * Проверяет существование файла.
     */
    private static void validateFile(File file, String errorMessage) {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(errorMessage + file.getAbsolutePath());
        }
    }

    /**
     * Создаёт префиксное дерево из записей.
     */
    private static PrefixTree buildPrefixTree(List<CsvParser.AirportRecord> records) {
        PrefixTree prefixTree = new PrefixTree();
        for (CsvParser.AirportRecord record : records) {
            logger.info("Добавляю в индекс: \"{}\" (строка {})", record.getSearchField(), record.getRowNumber());
            prefixTree.insert(record.getSearchField(), record.getRowNumber());
        }
        logger.info("Индекс построен. Готов к выполнению поиска.");
        return prefixTree;
    }

    /**
     * Читает запросы из файла.
     */
    private static List<String> readQueries(String inputFile) throws IOException {
        return Files.readAllLines(Paths.get(inputFile))
                .stream()
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Сохраняет результаты поиска в файл.
     */
    private static void saveResults(String outputFile, long initTime, List<SearchService.SearchResult> results) throws IOException {
        ResultJson resultJson = new ResultJson(initTime, results);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(outputFile).toFile(), resultJson);
        logger.info("Результаты поиска сохранены в файл: {}", outputFile);
    }

    /**
     * DTO для аргументов приложения.
     */
    private record AppArguments(String dataFile, int columnIndex, String inputFile, String outputFile) {
    }

    /**
     * DTO для результата выполнения программы.
     */
    private record ResultJson(long initTime, List<SearchService.SearchResult> result) {
    }
}
