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
        String[] words = srcText.split(" "); // разделяем текст по пробелам
        int lines = 1; // кол-во строк
        double lineWidth = 0d; // текущая длина строки
        for (int i = 0; i < words.length; i++) { // проходимся по каждому слову
            StringBuilder word = new StringBuilder(words[i]); // создаем билдер строки
            if (i < words.length - 1 || srcText.endsWith(" ")) word.append(' '); // если у нас не последнее слово в массиве или текст не оканчивается на пробел, добавляем слову пробел
            Text text = new Text(word.toString());       //
            text.setFont(font);                          // вычисляем границы слова на экране
            Bounds textBounds = text.getBoundsInLocal(); //
            if (textBounds.getWidth() > areaBounds.getWidth()) { // если слово больше чем текстовое поле
                String line = word.toString();
                while (true) {
                    String substring = substringByWidth(font, line, areaBounds.getWidth()); // обрезаем слово по длине текстового поля
                    line = line.substring(substring.length()); // обрезаем слово
                    if (line.isEmpty()) { // если остаток пуст
                        Text substringText = new Text(substring);
                        substringText.setFont(font);
                        lineWidth = substringText.getBoundsInLocal().getWidth(); // в текущую длину строки записываем длину остатка
                        break;
                    }
                    lines++; // плюсуем линию
                }
                continue;
            }
            if (lineWidth + textBounds.getWidth() > areaBounds.getWidth()) { // если длина слова + текущая длина строки больше чем длина текстовогшо поля
                lines++; // плюсуем линию
                lineWidth = 0d; // обнуляем текущую длину строки
            }
            lineWidth += textBounds.getWidth(); // прибавляем к текущей длине строки длину слова
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
        Text srcText = new Text(src); //
        srcText.setFont(font);        // получаем размеры текста
        if (srcText.getBoundsInLocal().getWidth() <= width) // если он и так меньше чем запршиваемая длина, вернем его
            return src;

        double stringWidth = 0d; // текущая длина
        StringBuilder builder = new StringBuilder(); // строка
        for (char ch : src.toCharArray()) { // проходимся по каждому символу
            Text charText = new Text(String.valueOf(ch)); //
            charText.setFont(font);                       // получаем длину символа на экране
            if (stringWidth + charText.getBoundsInLocal().getWidth() > width) // если длина символа + текущая длина строки больше чем запрашиваемая длина, вернем строку
                return builder.toString();
            stringWidth += charText.getBoundsInLocal().getWidth(); // иначе прибавим к текущей длине строки размер символа
            builder.append(ch); // и к строке добавим этот символ
        }
        return src;
    }

}
