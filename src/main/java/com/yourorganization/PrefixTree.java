package com.yourorganization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Префиксное дерево (Trie) для быстрого поиска строк по префиксу.
 */
public class PrefixTree {
    private static final Logger logger = LoggerFactory.getLogger(PrefixTree.class);
    private final Node root;

    public PrefixTree() {
        this.root = new Node();
    }

    /**
     * Добавляет слово в префиксное дерево с привязкой к номеру строки.
     *
     * @param word      слово для добавления
     * @param rowNumber номер строки
     * @throws IllegalArgumentException если слово пустое или null
     */
    public void insert(String word, int rowNumber) {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("Слово не может быть null или пустым.");
        }
        if (rowNumber < 0) {
            throw new IllegalArgumentException("Номер строки должен быть неотрицательным.");
        }

        String normalizedWord = word.toLowerCase().trim();
        logger.debug("Добавляю слово в дерево: {}", normalizedWord);

        Node current = root;
        for (char c : normalizedWord.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new Node());
            if (!current.rowNumbers.contains(rowNumber)) {
                current.rowNumbers.add(rowNumber);
            }
        }
        logger.debug("Слово \"{}\" успешно добавлено. Номера строк в конечном узле: {}", normalizedWord, current.rowNumbers);
    }

    /**
     * Выполняет поиск строк по заданному префиксу.
     *
     * @param prefix префикс для поиска
     * @return список номеров строк, соответствующих префиксу
     * @throws IllegalArgumentException если префикс пустой или null
     */
    public List<Integer> search(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Префикс не может быть null или пустым.");
        }

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
     * Внутренний класс, представляющий узел префиксного дерева.
     */
    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private final List<Integer> rowNumbers = new ArrayList<>();
    }
}
