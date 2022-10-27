package ru.compot.corrector.utils.splash;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashScreenController implements Initializable {

    private final String body;
    private final Stage stage;
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

    @FXML
    private void onButtonAction() {
        stage.hide();
    }
}
