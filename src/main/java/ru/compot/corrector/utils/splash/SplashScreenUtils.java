package ru.compot.corrector.utils.splash;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SplashScreenUtils {

    private SplashScreenUtils() {
    }

    /**
     * Открывает уведомление в новом окне
     * @param owner главная сцена, из которой вызывается уведомление
     * @param icon иконка
     * @param title заголовок
     * @param body текст уведомления
     */
    public static void displayInfoScreen(Stage owner, String icon, String title, String body) {
        Stage stage = new Stage(); // создаем окно
        FXMLLoader loader = new FXMLLoader(SplashScreenUtils.class.getResource("splashscreen-view.fxml")); // загружаем его разметку
        loader.setController(new SplashScreenController(body, stage)); // задаем контроллер
        stage.getIcons().add(new Image(Objects.requireNonNull(SplashScreenUtils.class.getResourceAsStream(icon)))); // задаем иконку
        stage.setTitle(title); // задаем заголовок
        try {
            Scene scene = new Scene(loader.load()); // создаем сцену
            stage.initModality(Modality.APPLICATION_MODAL); // это будет модальное окно
            stage.initOwner(owner); // определение родительского экрана
            stage.setScene(scene); // определяем сцену
            stage.show(); // показываем окно
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
