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

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Приложение запущено");
        System.out.println("Аргументы: " + Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        if (args.length < 8) {
            logger.error("Неверное количество параметров. Пример использования:");
            logger.error("--data airports.csv --indexed-column-id 2 --input-file input.txt --output-file output.json");
            return;
        }


        String dataFile = args[1];
        int columnId = Integer.parseInt(args[3]) - 1; // Преобразуем в индекс с 0
        String inputFile = args[5];
        String outputFile = args[7];

        // Проверяем существование файлов
        File dataFileCheck = new File(dataFile);
        File inputFileCheck = new File(inputFile);

        if (!dataFileCheck.exists()) {
            System.err.println("Файл данных не найден: " + dataFile);
            return;
        }
        if (!inputFileCheck.exists()) {
            System.err.println("Файл с запросами не найден: " + inputFile);
            return;
        }

        try {
            // Парсинг CSV
            List<CsvParser.AirportRecord> records = CsvParser.parseCsv(dataFile, columnId);
            if (records.isEmpty()) {
                logger.error("Файл данных не содержит подходящих записей.");
                return;
            }

            PrefixTree prefixTree = new PrefixTree();

            // Добавляем данные в дерево
            for (CsvParser.AirportRecord record : records) {
                logger.info("Добавляю в индекс: \"{}\" (строка {})", record.getSearchField(), record.getRowNumber());
                prefixTree.insert(record.getSearchField(), record.getRowNumber());
            }

            logger.info("Индекс построен. Готов к выполнению поиска.");

            // Чтение запросов
            List<String> queries = Files.readAllLines(Paths.get(inputFile));
            queries.removeIf(String::isBlank); // Удаляем пустые строки

            if (queries.isEmpty()) {
                logger.warn("Файл запросов пуст.");
                return;
            }

            // Выполнение поиска
            SearchService searchService = new SearchService(prefixTree);
            List<SearchService.SearchResult> results = searchService.search(queries);

            // Формирование JSON-результата
            long initTime = System.currentTimeMillis() - startTime;
            ResultJson resultJson = new ResultJson(initTime, results);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(outputFile).toFile(), resultJson);

            logger.info("Результаты поиска сохранены в файл: {}", outputFile);

        } catch (IOException e) {
            logger.error("Ошибка: {}", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Неверные данные: {}", e.getMessage());
        }
        System.out.println("Текущая рабочая директория: " + System.getProperty("user.dir"));

    }

    private static class ResultJson {
        private final long initTime;
        private final List<SearchService.SearchResult> result;

        public ResultJson(long initTime, List<SearchService.SearchResult> result) {
            this.initTime = initTime;
            this.result = result;
        }

        public long getInitTime() {
            return initTime;
        }

        public List<SearchService.SearchResult> getResult() {
            return result;
        }
    }
}
