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
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(SplashScreenUtils.class.getResource("splashscreen-view.fxml"));
        loader.setController(new SplashScreenController(body, stage));
        stage.getIcons().add(new Image(Objects.requireNonNull(SplashScreenUtils.class.getResourceAsStream(icon))));
        stage.setTitle(title);
        try {
            Scene scene = new Scene(loader.load());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
