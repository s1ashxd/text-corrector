package ru.compot.corrector.core;

import org.languagetool.rules.RuleMatch;

import java.util.List;

/**
 * Данные после анализа
 * @param inputText входной текст
 * @param matches исправления
 */
public record AnalyzerOutput(String inputText, List<RuleMatch> matches) {
}
