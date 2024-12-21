package com.yourorganization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);

    /**
     * Парсит CSV-файл и извлекает данные из указанной колонки.
     *
     * @param filePath    путь к файлу CSV
     * @param columnIndex индекс колонки для извлечения данных (0-индексированный)
     * @return список записей аэропортов
     * @throws IOException если произошла ошибка чтения файла
     * @throws IllegalArgumentException если аргументы некорректны
     */
    public static List<AirportRecord> parseCsv(String filePath, int columnIndex) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Путь к файлу не должен быть null или пустым.");
        }
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Индекс колонки должен быть неотрицательным.");
        }

        List<AirportRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            if (!reader.ready()) {
                throw new IllegalArgumentException("Файл данных пуст.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    logger.warn("Пропущена пустая строка.");
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length <= columnIndex) {
                    logger.warn("Пропущена строка с недостаточным числом колонок: {}", line);
                    continue;
                }

                try {
                    int rowNumber = Integer.parseInt(fields[0].trim());
                    String searchField = fields[columnIndex].trim().replaceAll("^\"|\"$", "").toLowerCase();
                    records.add(new AirportRecord(rowNumber, searchField));
                    logger.debug("Успешный парсинг: строка {}, значение \"{}\"", rowNumber, searchField);
                } catch (NumberFormatException e) {
                    logger.warn("Ошибка парсинга номера строки: {}. Пропускаем строку.", line);
                }
            }
        }

        return records;
    }

    /**
     * Класс для хранения записи из CSV.
     */
    public static class AirportRecord {
        private final int rowNumber;
        private final String searchField;

        public AirportRecord(int rowNumber, String searchField) {
            if (searchField == null || searchField.isBlank()) {
                throw new IllegalArgumentException("Поле поиска не может быть null или пустым.");
            }
            this.rowNumber = rowNumber;
            this.searchField = searchField;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public String getSearchField() {
            return searchField;
        }

        @Override
        public String toString() {
            return String.format("AirportRecord{rowNumber=%d, searchField='%s'}", rowNumber, searchField);
        }
    }
}
