package ru.compot.corrector.core;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.French;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.Russian;
import org.languagetool.rules.RuleMatch;
import ru.compot.corrector.utils.AreaUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnalyzerCore {

    private static final String PREFERENCES_PATH = System.getProperty("user.home") + File.separator + "TextCorrector";

    private final JLanguageTool russianAnalyzer;
    private final JLanguageTool germanAnalyzer;
    private final JLanguageTool englishAnalyzer;
    private final JLanguageTool frenchAnalyzer;

    private final Properties properties = new Properties();

    private final List<AnalyzedRegion> analyzedRegions = new CopyOnWriteArrayList<>();

    public AnalyzerCore() {
        try (FileInputStream fis = new FileInputStream(PREFERENCES_PATH + File.separator + "preferences.properties")) {
            properties.load(fis);
        } catch (IOException ignored) {
        }
        russianAnalyzer = new JLanguageTool(new Russian());
        germanAnalyzer = new JLanguageTool(new GermanyGerman());
        englishAnalyzer = new JLanguageTool(new BritishEnglish());
        frenchAnalyzer = new JLanguageTool(new French());
    }

    public static int getOffset(Map<Integer, Integer> offsets, int fromPosition) {
        return offsets.keySet()
                .stream()
                .filter(i -> fromPosition >= i)
                .mapToInt(offsets::get)
                .sum();
    }

    public static Point2D getMatchPosition(Bounds outputAreaBounds, Font outputAreaFont, String beforeText, Bounds textBounds) {
        double regionX = outputAreaBounds.getMinX()
                + AreaUtils.getLastLineWidth(outputAreaBounds, outputAreaFont, beforeText);
        double regionY = outputAreaBounds.getMinY()
                + (AreaUtils.getLinesInArea(outputAreaBounds, outputAreaFont, beforeText) - 1)
                * (textBounds.getHeight() + 1);
        if (regionX + textBounds.getWidth() > outputAreaBounds.getWidth()) {
            regionX = outputAreaBounds.getMinX();
            regionY += textBounds.getHeight() + 1;
        }
        return new Point2D(regionX, regionY);
    }

    public boolean isParagraphsEnabled() {
        return Boolean.parseBoolean((String) properties.getOrDefault("paragraphs.enabled", "true"));
    }

    public void setParagraphsEnabled(boolean enabled) {
        properties.put("paragraphs.enabled", enabled);
    }

    public int getSentencesInParagraph() {
        int result = 3;
        try {
            result = Integer.parseInt((String) properties.getOrDefault("paragraphs.sentences", "3"));
        } catch (NumberFormatException e) {
            properties.remove("paragraphs.sentences");
        }
        return result;
    }

    public void setSentencesInParagraph(int sentences) {
        properties.put("paragraphs.sentences", String.valueOf(sentences));
    }

    public boolean isGermanEnabled() {
        return Boolean.parseBoolean((String) properties.getOrDefault("analyzers.german", "false"));
    }

    public void setGermanEnabled(boolean enabled) {
        properties.put("analyzers.german", String.valueOf(enabled));
    }

    public boolean isFrenchEnabled() {
        return Boolean.parseBoolean((String) properties.getOrDefault("analyzers.french", "false"));
    }

    public void setFrenchEnabled(boolean enabled) {
        properties.put("analyzers.french", String.valueOf(enabled));
    }

    public boolean isEnglishEnabled() {
        return Boolean.parseBoolean((String) properties.getOrDefault("analyzers.english", "false"));
    }

    public void setEnglishEnabled(boolean enabled) {
        properties.put("analyzers.english", String.valueOf(enabled));
    }

    public List<AnalyzedRegion> getAnalyzedRegions() {
        return analyzedRegions;
    }

    public AnalyzerOutput analyze(String input) {
        try {
            CopyOnWriteArrayList<RuleMatch> matches = new CopyOnWriteArrayList<>();
            russianAnalyzer.check(input, matches::add);
            if (isGermanEnabled()) germanAnalyzer.check(input, matches::add);
            if (isFrenchEnabled()) frenchAnalyzer.check(input, matches::add);
            if (isEnglishEnabled()) englishAnalyzer.check(input, matches::add);
            return new AnalyzerOutput(input, matches);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public void applyAnalyzedRegions(AnalyzerOutput output, Map<Integer, Integer> offsets, String outputText, Bounds outputAreaBounds, Font outputAreaFont) {
        for (RuleMatch rm : output.matches()) {
            int offset = AnalyzerCore.getOffset(offsets, rm.getFromPos());
            String part1 = outputText.substring(0, rm.getFromPos() + offset);
            String source = output.inputText().substring(rm.getFromPos(), rm.getToPos());
            String replacement = rm.getSuggestedReplacements().size() > 0 ? rm.getSuggestedReplacements().get(0) : source;
            AnalyzedRegion ar = new AnalyzedRegion(
                    rm.getFromPos() + offset,
                    source,
                    replacement,
                    rm.getSuggestedReplacements()
            );
            ar.updatePosition(outputAreaBounds, outputAreaFont, part1);
            analyzedRegions.add(ar);
        }
    }

    public void save() {
        try {
            File file = new File(PREFERENCES_PATH + File.separator + "preferences.properties");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
