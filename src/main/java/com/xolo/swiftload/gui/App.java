package com.xolo.swiftload.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SwiftLoad Downloader");
        Label urlLabel = new Label("Download URL:");
        TextField urlField = new TextField("https://ash-speed.hetzner.com/100MB.bin");

        Label threadLabel = new Label("Threads:");
        Slider threadSlider = new Slider(1, 32, 4);
        threadSlider.setShowTickLabels(true);
        threadSlider.setShowTickMarks(true);
        threadSlider.setMajorTickUnit(8);

        Button startBtn = new Button("Start Download");
        startBtn.setMaxWidth(Double.MAX_VALUE);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label("Status: Idle");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
                urlLabel, urlField,
                threadLabel, threadSlider,
                startBtn, progressBar, statusLabel
        );

        Scene scene = new Scene(layout, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}