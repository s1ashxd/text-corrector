package ru.compot.corrector.core;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import ru.compot.corrector.utils.AreaUtils;

import java.util.List;

public class AnalyzedRegion {
    public static double yOffset;
    public int from;
    public String source, replacement;
    public List<String> allReplacements;
    public double x, y, width, height;

    public AnalyzedRegion(int from, String source, String replacement, List<String> allReplacements) {
        this.from = from;
        this.source = source;
        this.replacement = replacement;
        this.allReplacements = allReplacements;
    }

    public void updatePosition(Bounds outputAreaBounds, Font outputAreaFont, String beforeText) {
        Bounds textBounds = AreaUtils.getTextSize(replacement, outputAreaFont);
        Point2D p = AnalyzerCore.getMatchPosition(
                outputAreaBounds,
                outputAreaFont,
                beforeText,
                textBounds
        );
        this.x = p.getX();
        this.y = p.getY();
        this.width = textBounds.getWidth();
        this.height = textBounds.getHeight();
    }
}
