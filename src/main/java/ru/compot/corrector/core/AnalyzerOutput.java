package ru.compot.corrector.core;

import org.languagetool.rules.RuleMatch;

import java.util.List;

public record AnalyzerOutput(String inputText, List<RuleMatch> matches) {
}
