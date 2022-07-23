package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import main_window.HelperClass;
import main_window.Splash;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class SplashController implements Initializable {
    @FXML
    public Label lblMaintainer;
    @FXML
    private AnchorPane anchorPaneSplash;

    class SplashShow extends Thread{
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    Stage newStage = new Stage();
                    Parent root = null;
                    try {

                        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/main_window.fxml")));
                        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../resources/application.css")).toExternalForm());
                       // scene.getStylesheets().add(Objects.requireNonNull(Splash.class.getResource("../resources/java-keywords.css")).toExternalForm());

                        Scene scene = new Scene(root);

                        newStage.setScene(scene);
                        newStage.setTitle("SQLiteMaintainer");
                        newStage.show();//scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../resources/application.css")).toExternalForm());
                        anchorPaneSplash.getScene().getWindow().hide();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        OpenRecentDatabaseHelper.initDB();
        new SplashShow().start();
    }
}
