package org.example.musikplayer_doit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view.fxml"));
        // Create a scene with the loaded FXML
        Scene scene = new Scene(fxmlLoader.load(), 820, 540);
        // Set the stage title and scene, then show the stage
        stage.setTitle("Musikplayer s!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch();
    }



}