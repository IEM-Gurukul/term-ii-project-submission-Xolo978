package com.xolo.swiftload.gui;

import com.xolo.swiftload.core.DownloadManager;
import com.xolo.swiftload.models.DownloadListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends Application {
    private VBox threadContainer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SwiftLoad Downloader");
        Label urlLabel = new Label("Download URL:");
        TextField urlField = new TextField("https://ash-speed.hetzner.com/100MB.bin");

        Label threadLabel = new Label("Threads:");
        Slider threadSlider = new Slider(1, 16, 4);
        threadSlider.setMajorTickUnit(1);
        threadSlider.setMinorTickCount(0);
        threadSlider.setBlockIncrement(1);
        threadSlider.setShowTickMarks(true);
        threadSlider.setShowTickLabels(true);
        threadSlider.setSnapToTicks(true);

        Button startBtn = new Button("Start Download");
        startBtn.setMaxWidth(Double.MAX_VALUE);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label("Status: Idle");

        threadContainer = new VBox(5);
        ScrollPane scrollPane = new ScrollPane(threadContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
                urlLabel, urlField,
                threadLabel, threadSlider,
                startBtn, progressBar, statusLabel,
                scrollPane
        );

        Scene scene = new Scene(layout, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.show();

        startBtn.setOnAction(_ -> {
            String url = urlField.getText();
            int threads = (int) threadSlider.getValue();
            Path downloadDir = Paths.get("downloads");
            threadContainer.getChildren().clear();
            startBtn.setDisable(true);
            statusLabel.setText("Status: Starting download");
            progressBar.setProgress(0);
            DownloadListener listener = new DownloadUiListener(threadContainer, progressBar, statusLabel);
            new Thread(() -> {
                try {
                    if (!Files.exists(downloadDir)) {
                        Files.createDirectories(downloadDir);
                    }
                    DownloadManager manager = new DownloadManager(url, downloadDir, threads, listener);
                    manager.start();
                    Platform.runLater(() -> {
                        statusLabel.setText("Status: Finished");
                        startBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error: " + ex.getMessage());
                        startBtn.setDisable(false);
                    });
                }
            }).start();
        });
    }


}