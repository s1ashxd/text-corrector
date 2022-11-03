package ru.compot.corrector.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml")); // загружаем разметку главного экрана
        fxmlLoader.setController(new MainController(stage)); // определяем контроллер
        stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("icon.png"))); // устанавливаем иконку
        Scene scene = new Scene(fxmlLoader.load(), 655, 440); // создаем сцену
        stage.setTitle("Text Corrector"); // задаем заголовок окна
        stage.setScene(scene); // задаем сцену окну
        stage.show(); // показать окно
    }
}