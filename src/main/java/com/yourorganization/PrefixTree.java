package com.yourorganization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixTree {
    private static final Logger logger = LoggerFactory.getLogger(PrefixTree.class);
    private final Node root;

    public PrefixTree() {
        this.root = new Node();
    }

    /**
     * Метод для добавления строки в дерево с указанием номера строки.
     *
     * @param word      слово для добавления
     * @param rowNumber номер строки
     */
    public void insert(String word, int rowNumber) {
        String normalizedWord = word.toLowerCase().trim();
        logger.debug("Добавляю слово в дерево: {}", normalizedWord);

        Node current = root;
        for (char c : normalizedWord.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new Node());
            // Добавляем номер строки на каждом уровне дерева
            if (!current.rowNumbers.contains(rowNumber)) {
                current.rowNumbers.add(rowNumber);
            }
        }
        logger.debug("Слово \"{}\" успешно добавлено. Номера строк в конечном узле: {}", normalizedWord, current.rowNumbers);
    }

    /**
     * Метод для поиска по префиксу.
     *
     * @param prefix префикс для поиска
     * @return список номеров строк, соответствующих префиксу
     */
    public List<Integer> search(String prefix) {
        String normalizedPrefix = prefix.toLowerCase().trim();
        logger.debug("Ищу префикс: {}", normalizedPrefix);

        Node current = root;
        for (char c : normalizedPrefix.toCharArray()) {
            logger.trace("Проверяем символ: {}", c);
            current = current.children.get(c);
            if (current == null) {
                logger.debug("Символ \"{}\" не найден. Префикс отсутствует.", c);
                return new ArrayList<>();
            }
            logger.trace("Символ найден. Текущий узел содержит номера строк: {}", current.rowNumbers);
        }

        logger.debug("Префикс \"{}\" найден. Итоговые номера строк: {}", normalizedPrefix, current.rowNumbers);
        return current.rowNumbers;
    }

    /**
     * Внутренний класс для узла дерева.
     */
    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private final List<Integer> rowNumbers = new ArrayList<>();
    }
}
