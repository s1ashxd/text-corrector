package ru.compot.corrector.application;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.compot.corrector.core.AnalyzedRegion;
import ru.compot.corrector.core.AnalyzerCore;
import ru.compot.corrector.core.AnalyzerOutput;
import ru.compot.corrector.utils.splash.SplashScreenUtils;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    /**
     * Главное окно
     */
    private final Stage primaryStage;
    /**
     * Поле ввода
     */
    @FXML
    private TextArea input;
    /**
     * Поле вывода
     */
    @FXML
    private TextArea output;
    /**
     * Текст на поле для вывода во время анализа
     */
    @FXML
    private Group analyzeState;
    /**
     * Контекстное меню поля для ввода
     */
    @FXML
    private ContextMenu outputContextMenu;
    /**
     * Группа со всеми подчеркиваниями
     */
    @FXML
    private Group linesGroup;
    /**
     * Кнопка открытия файла
     */
    @FXML
    private Button openButton;
    /**
     * Кнопка анализа
     */
    @FXML
    private Button analyzeButton;
    /**
     * Кнопка сохранения
     */
    @FXML
    private Button saveButton;
    /**
     * Чекбокс в меню Preferences отвечающий за включение разделения текста на абзацы
     */
    @FXML
    private CheckMenuItem paragraphState;
    /**
     * Текст с количеством предложений в абзаце
     */
    @FXML
    private MenuItem sentencesLabel;
    /**
     * Слайдер для изменения кол-ва предложений в абзаце
     */
    @FXML
    private Slider sentencesSlider;
    /**
     * Чекбокс, отвечающий за включение анализа немецкого языка
     */
    @FXML
    private CheckMenuItem germanCheck;
    /**
     * Чекбокс, отвечающий за включение анализа французкого языка
     */
    @FXML
    private CheckMenuItem frenchCheck;
    /**
     * Чекбокс, отвечающий за включение анализа английского языка
     */
    @FXML
    private CheckMenuItem englishCheck;
    private AnalyzerCore core;

    /**
     * Папка последнего открытого файла
     */
    private File savePath;
    /**
     * Название последнего открытого файла
     */
    private String saveFile = "corrector-output";

    /**
     * Пропустить следущее событие изменения поля для ввода?
     */
    private boolean skipOutputChangeEvent;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        core = new AnalyzerCore();
        // ---- загрузка сохраненной конфигурации ----
        sentencesLabel.setText("Предложений в абзаце: " + core.getSentencesInParagraph()); // устанавливаем текст над слайдером кол-ва предложений в абзаце в меню Preferences
        sentencesSlider.setValue(core.getSentencesInParagraph()); // устанавливаем сохраненное значение слайдеру
        paragraphState.setSelected(core.isParagraphsEnabled()); // устанавливаем сохраненное значение чекбоксу разделения текста на абзацы
        germanCheck.setSelected(core.isGermanEnabled()); // устанавливаем сохраненное значение чекбоксу анализа немецкого языка
        frenchCheck.setSelected(core.isFrenchEnabled()); // устангавливаем сохраненное значение чекбоксу анализа французкого языка
        englishCheck.setSelected(core.isEnglishEnabled()); // устангавливаем сохраненное значение чекбоксу анализа английского языка

        // ---- иницализация отловщиков событий ----
        output.textProperty().addListener(((observable, oldValue, newValue) -> { // отловщик события изменения текста в поле для вывода
            if (skipOutputChangeEvent) { // если мы скипаем событие обновления
                skipOutputChangeEvent = false;
                return; // скипаем его
            }
            if (core.getAnalyzedRegions().size() > 0) { // если список проанализируемых регионов не пуст
                core.getAnalyzedRegions().clear(); // очищаем его
                linesGroup.getChildren().clear(); // и группу с подчеркиваниями исправленных слов
            }
        }));
        output.scrollTopProperty().addListener(((observable, oldValue, newValue) -> { // отловщик события скролла поля для вывода
            AnalyzedRegion.yOffset = -newValue.doubleValue(); // изменяем смещение всех регионов на экране по оси y
            updateUnderlines(); // обновляем подчеркивания
        }));
        sentencesSlider.valueProperty().addListener((observable, oldValue, newValue) -> // отловщик события изменения слайдера
                sentencesLabel.setText("Предложений в абзаце: " + newValue.intValue())); // изменяем текст над слайдером
        Runtime.getRuntime().addShutdownHook(new Thread(() -> core.save())); // добавляем операцию (сохранение) после закрытия приложения
    }

    /**
     * Отловщик события нажатия мышкой на поле для вывода
     * @param event информация события
     */
    @FXML
    private void onOutputClick(MouseEvent event) {
        if (core.getAnalyzedRegions().size() < 1 || event.getButton() != MouseButton.SECONDARY) return; // если лист проанализированных регионов пустой или нажата не правая кнопка мыши
        List<AnalyzedRegion> regions = core.getAnalyzedRegions().stream() // создаем поток проанализированных регионов
                .filter(r -> output.getLayoutBounds().contains(r.x, r.y + AnalyzedRegion.yOffset, r.width, r.height)) // фильтруем только те регионы, которые видно на экране
                .filter(r -> {
                    double x = event.getSceneX() - output.getLayoutX();
                    double y = event.getSceneY() - output.getLayoutY();
                    return x >= r.x
                            && x <= r.x + r.width
                            && y >= r.y + AnalyzedRegion.yOffset
                            && y <= r.y + AnalyzedRegion.yOffset + r.height; // получаем те, на которые было произведено нажатие
                }).toList();
        outputContextMenu.getItems().clear(); // очищаем предыдущее контекстное меню
        regions.forEach(r -> { // проходимся по полученным регионам
            Menu menu = new Menu(r.replacement); // создаем меню
            MenuItem mi = addListenerToMenuItem(new MenuItem(r.source), r.source, r); // добавляем кнопку с исходным вариантом и вещаем на нее обработчик события нажатия
            menu.getItems().add(mi); // добавляем в контекстное меню
            r.allReplacements.forEach(rep -> menu.getItems().add(addListenerToMenuItem(new MenuItem(rep), rep, r))); // добавляем кнопки для всех возможным замен слова и вешаем на каждую обработчик события нажатия
            outputContextMenu.getItems().add(menu); // добавляем в контекстное меню
        });
    }

    /**
     * Вешает на кнопку в меню обработчик нажатия
     * @param mi кнопка
     * @param replacement замена
     * @param region регион
     * @return кнопку с обработчиком
     */
    private MenuItem addListenerToMenuItem(MenuItem mi, String replacement, AnalyzedRegion region) {
        mi.addEventHandler(ActionEvent.ANY, me -> {
            String oldReplacement = region.replacement; // получаем предыдущую замену
            region.replacement = replacement; // определяем новую замену в регионе

            String part1 = output.getText(0, region.from); // получаем текст до региона
            String part2 = output.getText(region.from + oldReplacement.length(), output.getLength()); // получаем текст после региона
            skipOutputChangeEvent = true; // скипаем след событие изменения поля для вывода
            output.setText(part1 + region.replacement + part2); // изменяем текст в поле для вывода

            Bounds areaBounds = output.lookup(".text").getBoundsInParent(); // получаем границы текстового поля дял вывода
            region.updatePosition(areaBounds, output.getFont(), part1); // обновляем позицию региона
            core.getAnalyzedRegions().stream() // создаем поток регионов
                    .filter(r1 -> r1.from >= region.from && r1 != region) // фильтруем другие регионы, которые начинаются после данного региона
                    .forEach(anotherRegion -> { // так же обновляем эти регионы
                        anotherRegion.from += region.replacement.length() - oldReplacement.length();
                        String anotherPart1 = output.getText(0, anotherRegion.from);
                        anotherRegion.updatePosition(areaBounds, output.getFont(), anotherPart1);
                    });
            updateUnderlines(); // обновляем все подчеркивания
        });
        return mi;
    }

    /**
     * Отловщик события нажатия мышкой на кнопку анализа
     */
    @FXML
    private void onAnalyzeClick() {
        setAnalyzeState(true); // говорим приложению, что анализ начался
        core.getAnalyzedRegions().clear(); // очищаем предыдущие регионы
        linesGroup.getChildren().clear(); // очищаем подчеркивания
        output.setText(input.getText()); // записываем в поле для вывода текст из поля для ввода
        Service<AnalyzerOutput> service = new Service<>() {
            @Override
            protected Task<AnalyzerOutput> createTask() {
                return new Task<>() {
                    @Override
                    protected AnalyzerOutput call() {
                        return core.analyze(input.getText()); // запускаем анализ в отдельном потоке
                    }
                };
            }
        };
        service.setOnSucceeded((event) -> { // если поток был выполнен успешно
            AnalyzerOutput out = service.getValue(); // получаем вывод
            if (out == null) { // если он пустой
                output.setText("Ошибка при анализе текста"); // это ошибка
                SplashScreenUtils.displayInfoScreen(primaryStage, "error.png", "Fail", "Не удалось анализировать текст"); // увдеомление
            } else { // иначе
                ConcurrentHashMap<Integer, Integer> offsets = new ConcurrentHashMap<>(); // создаем карту смещений символов
                calculateOffsetsAndApplyText(out, offsets); // заполняем карту смещений
                Bounds areaBounds = output.lookup(".text").getBoundsInParent(); // получаем границы
                core.applyAnalyzedRegions(out, offsets, output.getText(), areaBounds, output.getFont()); // вычисляем регионы
                updateUnderlines(); // обновляем подчеркивания
                SplashScreenUtils.displayInfoScreen(primaryStage, "success.png", "Success", "Текст проанализирован успешно!"); // увдобмление
            }
            setAnalyzeState(false); // говорим приложению, что анализ закончен
        });
        service.start(); // запускаем поток
    }

    /**
     * Метод обновления подчеркиваний
     */
    private void updateUnderlines() {
        linesGroup.getChildren().clear(); // очищаем лист с предыдущими
        core.getAnalyzedRegions().stream() // создаем поток с регионами
                .filter(r -> r.y + AnalyzedRegion.yOffset > 0 && r.y + AnalyzedRegion.yOffset < output.getHeight() - r.height) // фильтруем только те, которые видно на экране
                .forEach(r -> {
                    double y = r.y + AnalyzedRegion.yOffset + r.height; // вычисляем y
                    Line line = new Line(r.x, y, r.x + r.width, y); // создаем линию
                    line.setStroke(Color.RED); // указываем ее цвет
                    line.setStrokeWidth(3); // и толщину
                    linesGroup.getChildren().add(line); // добавляем в группу подчеркиваний
                });
    }

    /**
     * Метод вычисления смещений символов и изменения текста поля для вывода
     * @param out вывод анализатора
     * @param offsets карта смещений
     */
    private void calculateOffsetsAndApplyText(AnalyzerOutput out, ConcurrentHashMap<Integer, Integer> offsets) {
        out.matches().forEach(rm -> {
            int offset = AnalyzerCore.getOffset(offsets, rm.getFromPos()); // вычисляем все смещения перед регионом
            String source = input.getText(rm.getFromPos(), rm.getToPos()); // слово с ошибкой
            String replacement = rm.getSuggestedReplacements().size() > 0 ? rm.getSuggestedReplacements().get(0) : source; // замена
            String part1 = output.getText(0, rm.getFromPos() + offset); // текст перед словом
            String part2 = output.getText(rm.getToPos() + offset, output.getLength()); // текст после слова
            skipOutputChangeEvent = true; // скипаем событие изменения поля для вывода
            output.setText(part1 + replacement + part2); // записываем в поле для вывода текст с замененным словом
            int newOffset = replacement.length() - (rm.getToPos() - rm.getFromPos()); // вычисляем смещение этого региона
            if (newOffset == 0) return; // если оно 0, не добавляем в карту смещений
            if (offsets.containsKey(rm.getToPos())) { // если в карте смещений существует смещение по позиции этого региона
                offsets.replace(rm.getToPos(), offsets.get(rm.getToPos()) + newOffset); // складываем прошлое и текущее смещения
                return;
            }
            offsets.put(rm.getToPos(), newOffset); // иначе записываем новое смещение
        });
    }

    /**
     * Включает/выключает отображение элементов на экране, отвечающие за текущее состояние анализа текста
     * @param state анализируется ли сейчас текст или нет
     */
    private void setAnalyzeState(boolean state) {
        openButton.setDisable(state); // (раз)блокируем кнопку открытия файла
        analyzeButton.setDisable(state); // (раз)блокируем кнопку анализа
        saveButton.setDisable(state); // (раз)блокируем кнопку сохранения файла
        input.setDisable(state); // (раз)блокируем поле для ввода текста
        output.setDisable(state); // (раз)блокируем поле для вывода текста
        analyzeState.setVisible(state); // показываем/прячем текст анализируем.. над полем для вывода
    }

    /**
     * Отловщик события нажатия мышкой на кнопку открытия файла
     */
    @FXML
    private void onOpenClick() {
        openButton.setDisable(true); // блокируем кнопку открытия файла
        File file = new FileChooser().showOpenDialog(null); // открываем выбор файла
        openButton.setDisable(false); // разлокируем кнопку открытия файла
        if (file == null) return; // если файл не открыт, пропускаем действия
        try (BufferedReader br = new BufferedReader(new FileReader(file))) { // читаем его
            savePath = file.getParentFile(); // запоминаем папку открытого файла
            saveFile = file.getName(); // и название файла
            input.setText(br.lines().collect(Collectors.joining("\n"))); // задаем текст полю для ввода
        } catch (IOException e) { // при ошибке
            SplashScreenUtils.displayInfoScreen(primaryStage, "error.png", "Fail", "Не удалось открыть файл"); // уведомление
            e.printStackTrace();
        }
    }

    /**
     * Отловщик события нажатия мышкой на кнопку сохранения файла
     */
    @FXML
    private void onSaveClick() {
        saveButton.setDisable(true); // блокируем кнопку сохранения
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("Текстовый документ", "*.txt"); // задаем возможные расширения для сохранения файла
        fc.getExtensionFilters().add(ef);
        fc.setSelectedExtensionFilter(ef);
        fc.setInitialDirectory(savePath); // задаем папку предыдущего открытого файла
        fc.setInitialFileName(saveFile); // задаем название предыдущего открытого файла
        File file = fc.showSaveDialog(null); // получаем файл
        saveButton.setDisable(false); // включаем кнопку
        if (file == null) return; // если файл пустой, пропускаем
        try (FileWriter fw = new FileWriter(file)) { // записываем информацию в файл
            if (core.isParagraphsEnabled()) { // если разделение на абзацы включено
                String[] sentences = output.getText().split("[.?!]\s+"); // делим текст на предложения
                int countSentences = 0; // кол-во предложений
                StringBuilder newText = new StringBuilder("\t"); // создаем билдер строки
                for (String sentence : sentences) { // проходимся по каждому предложению
                    countSentences++; // плюсуем в кол-во предложений
                    if (countSentences > core.getSentencesInParagraph()) { // если кол-во предложений в абзаце, больше чем в настройке
                        countSentences = 0; // обнуляем кол-во
                        newText.append("\n\t").append(sentence).append(". "); // в билдер заносим текст с новым абзацем
                        continue;
                    }
                    newText.append(sentence).append(". "); // иначе просто заносим текст
                }
                fw.write(newText.toString()); // записываем в файл сбилженный текст
            } else fw.write(output.getText()); // иначе записываем просто весь текст разом
            fw.flush();
        } catch (IOException e) { // при ошибке
            SplashScreenUtils.displayInfoScreen(primaryStage, "error.png", "Fail", "Не удалось сохранить файл"); // уведомление
            e.printStackTrace();
        }
    }

    /**
     * Отловщик события нажатия мышкой на кнопку New в меню File
     */
    @FXML
    private void onNewClick() {
        input.setText("");
        output.setText("");
    }

    /**
     * Отловщик события нажатия мышкой на чекбокс Разделение на абзацы в меню Preferences
     */
    @FXML
    private void onParagraphStateClick() {
        core.setParagraphsEnabled(paragraphState.isSelected());
    }

    /**
     * Отловщик события нажатия мышкой на слайдер регулирования кол-ва предложений в абзаце в меню Preferences
     */
    @FXML
    private void onSliderClick() {
        int sentences = (int) sentencesSlider.getValue();
        core.setSentencesInParagraph(sentences);
    }

    /**
     * Отловщик события нажатия мышкой на чекбокс German в меню Preferences
     */
    @FXML
    private void onGermanClick() {
        core.setGermanEnabled(germanCheck.isSelected());
    }

    /**
     * Отловщик события нажатия мышкой на чекбокс French в меню Preferences
     */
    @FXML
    private void onFrenchClick() {
        core.setFrenchEnabled(frenchCheck.isSelected());
    }

    /**
     * Отловщик события нажатия мышкой на чекбокс English в меню Preferences
     */
    @FXML
    private void onEnglishClick() {
        core.setEnglishEnabled(englishCheck.isSelected());
    }
}