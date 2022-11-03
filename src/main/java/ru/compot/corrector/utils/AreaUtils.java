package ru.compot.corrector.utils;

import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class AreaUtils {

    private AreaUtils() {
    }

    /**
     * Считает колчиество строк в текстовом поле
     * @param areaBounds границы поля
     * @param font шрифт поля
     * @param srcText текст в поле
     * @return кол-во строк в поле
     */
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

    /**
     * Считает длину последней строки в пикселях в поле
     * @param areaBounds границы поля
     * @param font шрифт поля
     * @param srcText текст в поле
     * @return длина последней строки в пикселях
     */
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

    /**
     * Считает размеры текста на экране
     * @param srcText текст
     * @param font шрифт текста
     * @return границы текста на экране
     */
    public static Bounds getTextSize(String srcText, Font font) {
        Text text = new Text(srcText);
        text.setFont(font);
        return text.getBoundsInLocal();
    }

    /**
     * Обрезает строку по длине в пикскелях
     * @param font шрифт строки
     * @param src строка
     * @param width ширина в пикселях
     * @return обрезанная строка по длине в пикселях
     */
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
