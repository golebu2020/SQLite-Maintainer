package main_window;

import java.io.IOException;
import java.util.Objects;

import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Splash extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {


        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/new_splash.fxml")));
        root.setStyle("-fx-background-radius: 20;" +
                "-fx-background-color:   #f19f48,   #332b33;" +
                "-fx-background-insets: 0, 0 3 3 0;");

        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        MainController.executor.shutdown();
    }
}
