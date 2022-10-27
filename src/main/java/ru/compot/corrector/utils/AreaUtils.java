package ru.compot.corrector.utils;

import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class AreaUtils {

    private AreaUtils() {
    }

    public static int getLinesInArea(Bounds areaBounds, Font font, String srcText) {
        String[] words = srcText.split(" ");
        int lines = 1;
        double lineWidth = 0d;
        for (int i = 0; i < words.length; i++) {
            StringBuilder word = new StringBuilder(words[i]);
            if (i < words.length - 1 || srcText.endsWith(" ")) word.append(' ');
            Text text = new Text(word.toString());
            text.setFont(font);
            Bounds textBounds = text.getBoundsInLocal();
            if (textBounds.getWidth() > areaBounds.getWidth()) {
                String line = word.toString();
                while (true) {
                    String substring = substringByWidth(font, line, areaBounds.getWidth());
                    line = line.substring(substring.length());
                    if (line.isEmpty()) {
                        Text substringText = new Text(substring);
                        substringText.setFont(font);
                        lineWidth = substringText.getBoundsInLocal().getWidth();
                        break;
                    }
                    lines++;
                }
                continue;
            }
            if (lineWidth + textBounds.getWidth() > areaBounds.getWidth()) {
                lines++;
                lineWidth = 0d;
            }
            lineWidth += textBounds.getWidth();
        }
        return lines;
    }

    public static double getLastLineWidth(Bounds areaBounds, Font font, String srcText) {
        String[] words = srcText.split(" ");
        double lineWidth = 0d;
        for (int i = 0; i < words.length; i++) {
            StringBuilder word = new StringBuilder(words[i]);
            if (i < words.length - 1 || srcText.endsWith(" ")) word.append(' ');
            Text text = new Text(word.toString());
            text.setFont(font);
            Bounds textBounds = text.getBoundsInLocal();
            if (textBounds.getWidth() > areaBounds.getWidth()) {
                String line = word.toString();
                while (true) {
                    String substring = substringByWidth(font, line, areaBounds.getWidth());
                    line = line.substring(substring.length());
                    if (line.isEmpty()) {
                        Text substringText = new Text(substring);
                        substringText.setFont(font);
                        lineWidth = substringText.getBoundsInLocal().getWidth();
                        break;
                    }
                }
                continue;
            }
            if (lineWidth + textBounds.getWidth() > areaBounds.getWidth()) lineWidth = 0d;
            lineWidth += textBounds.getWidth();
        }
        return lineWidth;
    }

    public static Bounds getTextSize(String srcText, Font font) {
        Text text = new Text(srcText);
        text.setFont(font);
        return text.getBoundsInLocal();
    }

    private static String substringByWidth(Font font, String src, double width) {
        Text srcText = new Text(src);
        srcText.setFont(font);
        if (srcText.getBoundsInLocal().getWidth() <= width)
            return src;

        double stringWidth = 0d;
        StringBuilder builder = new StringBuilder();
        for (char ch : src.toCharArray()) {
            Text charText = new Text(String.valueOf(ch));
            charText.setFont(font);
            if (stringWidth + charText.getBoundsInLocal().getWidth() > width)
                return builder.toString();
            stringWidth += charText.getBoundsInLocal().getWidth();
            builder.append(ch);
        }
        return src;
    }

}
