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

    private final Stage primaryStage;
    @FXML
    private TextArea input;
    @FXML
    private TextArea output;
    @FXML
    private Group analyzeState;
    @FXML
    private ContextMenu outputContextMenu;
    @FXML
    private Group linesGroup;
    @FXML
    private Button openButton;
    @FXML
    private Button analyzeButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label openError;
    @FXML
    private Label analyzeError;
    @FXML
    private Label saveError;
    @FXML
    private CheckMenuItem paragraphState;
    @FXML
    private MenuItem sentencesLabel;
    @FXML
    private Slider sentencesSlider;
    @FXML
    private CheckMenuItem germanCheck;
    @FXML
    private CheckMenuItem frenchCheck;
    @FXML
    private CheckMenuItem englishCheck;
    private AnalyzerCore core;

    private File savePath;
    private String saveFile = "corrector-output";

    private boolean skipOutputChangeEvent;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        core = new AnalyzerCore();
        sentencesLabel.setText("Предложений в абзаце: " + core.getSentencesInParagraph());
        sentencesSlider.setValue(core.getSentencesInParagraph());
        paragraphState.setSelected(core.isParagraphsEnabled());
        germanCheck.setSelected(core.isGermanEnabled());
        frenchCheck.setSelected(core.isFrenchEnabled());
        englishCheck.setSelected(core.isEnglishEnabled());

        output.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (skipOutputChangeEvent) {
                skipOutputChangeEvent = false;
                return;
            }
            if (core.getAnalyzedRegions().size() > 0) {
                core.getAnalyzedRegions().clear();
                linesGroup.getChildren().clear();
            }
        }));

        openButton.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) openError.setVisible(false);
        }));
        analyzeButton.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) analyzeError.setVisible(false);
        }));
        saveButton.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) saveError.setVisible(false);
        }));

        output.scrollTopProperty().addListener(((observable, oldValue, newValue) -> {
            AnalyzedRegion.yOffset = -newValue.doubleValue();
            updateUnderlines();
        }));

        sentencesSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                sentencesLabel.setText("Предложений в абзаце: " + newValue.intValue()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> core.save()));
    }

    @FXML
    private void onOutputClick(MouseEvent event) {
        if (core.getAnalyzedRegions().size() < 1 || event.getButton() != MouseButton.SECONDARY) return;
        List<AnalyzedRegion> regions = core.getAnalyzedRegions().stream()
                .filter(r -> output.getLayoutBounds().contains(r.x, r.y + AnalyzedRegion.yOffset, r.width, r.height))
                .filter(r -> {
                    double x = event.getSceneX() - output.getLayoutX();
                    double y = event.getSceneY() - output.getLayoutY();
                    return x >= r.x
                            && x <= r.x + r.width
                            && y >= r.y + AnalyzedRegion.yOffset
                            && y <= r.y + AnalyzedRegion.yOffset + r.height;
                }).toList();
        outputContextMenu.getItems().clear();
        regions.forEach(r -> {
            Menu menu = new Menu(r.replacement);
            MenuItem mi = addListenerToMenuItem(new MenuItem(r.source), r.source, r);
            menu.getItems().add(mi);
            r.allReplacements.forEach(rep -> menu.getItems().add(addListenerToMenuItem(new MenuItem(rep), rep, r)));
            outputContextMenu.getItems().add(menu);
        });
    }

    private MenuItem addListenerToMenuItem(MenuItem mi, String replacement, AnalyzedRegion region) {
        mi.addEventHandler(ActionEvent.ANY, me -> {
            String oldReplacement = region.replacement;
            region.replacement = replacement;

            String part1 = output.getText(0, region.from);
            String part2 = output.getText(region.from + oldReplacement.length(), output.getLength());
            skipOutputChangeEvent = true;
            output.setText(part1 + region.replacement + part2);

            Bounds areaBounds = output.lookup(".text").getBoundsInParent();
            region.updatePosition(areaBounds, output.getFont(), part1);
            core.getAnalyzedRegions().stream()
                    .filter(r1 -> r1.from >= region.from && r1 != region)
                    .forEach(anotherRegion -> {
                        anotherRegion.from += region.replacement.length() - oldReplacement.length();
                        String anotherPart1 = output.getText(0, anotherRegion.from);
                        anotherRegion.updatePosition(areaBounds, output.getFont(), anotherPart1);
                    });
            updateUnderlines();
        });
        return mi;
    }

    @FXML
    private void onAnalyzeClick(ActionEvent ev) {
        setAnalyzeState(true);
        core.getAnalyzedRegions().clear();
        linesGroup.getChildren().clear();
        output.setText(input.getText());
        Service<AnalyzerOutput> service = new Service<>() {
            @Override
            protected Task<AnalyzerOutput> createTask() {
                return new Task<>() {
                    @Override
                    protected AnalyzerOutput call() {
                        return core.analyze(input.getText());
                    }
                };
            }
        };
        service.setOnSucceeded((event) -> {
            AnalyzerOutput out = service.getValue();
            if (out == null) {
                output.setText("Ошибка при анализе текста");
                SplashScreenUtils.displayInfoScreen(primaryStage, "checked.png", "Failed", "Не удалось анализировать текст");
            } else {
                ConcurrentHashMap<Integer, Integer> offsets = new ConcurrentHashMap<>();
                applyAnalyzedChanges(out, offsets);
                Bounds areaBounds = output.lookup(".text").getBoundsInParent();
                core.applyAnalyzedRegions(out, offsets, output.getText(), areaBounds, output.getFont());
                updateUnderlines();
            }
            SplashScreenUtils.displayInfoScreen(primaryStage, "checked.png", "Success", "Текст проанализирован успешно!");
            setAnalyzeState(false);
        });
        service.start();
    }

    private void updateUnderlines() {
        linesGroup.getChildren().clear();
        core.getAnalyzedRegions().stream()
                .filter(r -> r.y + AnalyzedRegion.yOffset > 0 && r.y + AnalyzedRegion.yOffset < output.getHeight() - r.height)
                .forEach(r -> {
                    double y = r.y + AnalyzedRegion.yOffset + r.height;
                    Line line = new Line(r.x, y, r.x + r.width, y);
                    line.setStroke(Color.RED);
                    line.setStrokeWidth(3);
                    linesGroup.getChildren().add(line);
                });
    }

    private void applyAnalyzedChanges(AnalyzerOutput out, ConcurrentHashMap<Integer, Integer> offsets) {
        out.matches().forEach(rm -> {
            int offset = AnalyzerCore.getOffset(offsets, rm.getFromPos());
            String source = input.getText(rm.getFromPos(), rm.getToPos());
            String replacement = rm.getSuggestedReplacements().size() > 0 ? rm.getSuggestedReplacements().get(0) : source;
            String part1 = output.getText(0, rm.getFromPos() + offset);
            String part2 = output.getText(rm.getToPos() + offset, output.getLength());
            skipOutputChangeEvent = true;
            output.setText(part1 + replacement + part2);
            int newOffset = replacement.length() - (rm.getToPos() - rm.getFromPos());
            if (newOffset == 0) return;
            if (offsets.containsKey(rm.getToPos())) {
                offsets.replace(rm.getToPos(), offsets.get(rm.getToPos()) + newOffset);
                return;
            }
            offsets.put(rm.getToPos(), newOffset);
        });
    }

    private void setAnalyzeState(boolean state) {
        openButton.setDisable(state);
        analyzeButton.setDisable(state);
        saveButton.setDisable(state);
        input.setDisable(state);
        output.setDisable(state);
        analyzeState.setVisible(state);
    }

    @FXML
    private void onOpenClick() {
        openButton.setDisable(true);
        File file = new FileChooser().showOpenDialog(null);
        openButton.setDisable(false);
        if (file == null) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            savePath = file.getParentFile();
            saveFile = file.getName();
            input.setText(br.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            openError.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveClick() {
        saveButton.setDisable(true);
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("Текстовый документ", "*.txt");
        fc.getExtensionFilters().add(ef);
        fc.setSelectedExtensionFilter(ef);
        fc.setInitialDirectory(savePath);
        fc.setInitialFileName(saveFile);
        File file = fc.showSaveDialog(null);
        saveButton.setDisable(false);
        if (file == null) return;
        try (FileWriter fw = new FileWriter(file)) {
            if (core.isParagraphsEnabled()) {
                String[] sentences = output.getText().split("[.?!]\s+");
                int countSentences = 0;
                StringBuilder newText = new StringBuilder("\t");
                for (String sentence : sentences) {
                    countSentences++;
                    if (countSentences > core.getSentencesInParagraph()) {
                        countSentences = 0;
                        newText.append("\n\t").append(sentence).append(". ");
                        continue;
                    }
                    newText.append(sentence).append(". ");
                }
                fw.write(newText.toString());
            } else fw.write(output.getText());
            fw.flush();
        } catch (IOException e) {
            openError.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewClick() {
        input.setText("");
        output.setText("");
    }

    @FXML
    private void onParagraphStateClick() {
        core.setParagraphsEnabled(paragraphState.isSelected());
    }

    @FXML
    private void onSliderClick() {
        int sentences = (int) sentencesSlider.getValue();
        core.setSentencesInParagraph(sentences);
    }

    @FXML
    private void onGermanClick() {
        core.setGermanEnabled(germanCheck.isSelected());
    }

    @FXML
    private void onFrenchClick() {
        core.setFrenchEnabled(frenchCheck.isSelected());
    }

    @FXML
    private void onEnglishClick() {
        core.setEnglishEnabled(englishCheck.isSelected());
    }
}