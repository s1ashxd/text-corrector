package ru.compot.corrector.core;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import ru.compot.corrector.utils.AreaUtils;

import java.util.List;

/**
 * Проанализируемый регион
 */
public class AnalyzedRegion {
    /**
     * Смещение всех регионов по оси y в следствии скролла поля для вывода
     */
    public static double yOffset;
    /**
     * Начало региона
     */
    public int from;
    /**
     * Исходный вариант и исправленный
     */
    public String source, replacement;
    /**
     * Другие исправления
     */
    public List<String> allReplacements;
    /**
     * Границы региона на экране
     */
    public double x, y, width, height;

    public AnalyzedRegion(int from, String source, String replacement, List<String> allReplacements) {
        this.from = from;
        this.source = source;
        this.replacement = replacement;
        this.allReplacements = allReplacements;
    }

    /**
     * Метод обновления границ региона на экране
     * @param outputAreaBounds границы поля для вывода
     * @param outputAreaFont шрифт поля для вывода
     * @param beforeText текст перед регионом
     */
    public void updatePosition(Bounds outputAreaBounds, Font outputAreaFont, String beforeText) {
        Bounds textBounds = AreaUtils.getTextSize(replacement, outputAreaFont); // получаем размеры текста на экране
        Point2D p = AnalyzerCore.getMatchPosition( // получаем позицию региона на экране
                outputAreaBounds,
                outputAreaFont,
                beforeText,
                textBounds
        );
        // ---- записываем значения ----
        this.x = p.getX();
        this.y = p.getY();
        this.width = textBounds.getWidth();
        this.height = textBounds.getHeight();
    }
}
