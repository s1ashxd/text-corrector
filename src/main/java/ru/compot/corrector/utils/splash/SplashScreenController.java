package ru.compot.corrector.utils.splash;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контроллер окна с уведомлением
 */
public class SplashScreenController implements Initializable {

    /**
     * Текст уведомления
     */
    private final String body;
    /**
     * Окно уведомления
     */
    private final Stage stage;
    /**
     * Текст уведомления в окне
     */
    @FXML
    private Label label;

    public SplashScreenController(String body, Stage stage) {
        this.body = body;
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        label.setText(body);
    }

    /**
     * Отловщик события нажатия на кнопку Ok
     */
    @FXML
    private void onButtonAction() {
        stage.hide(); // прячем окно
    }
}
