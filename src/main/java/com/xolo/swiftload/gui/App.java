package com.xolo.swiftload.gui;

import com.xolo.swiftload.core.DownloadManager;
import com.xolo.swiftload.models.DownloadListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
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
        int availableThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        int defaultThreads = Math.min(4, availableThreads);

        Label urlLabel = new Label("Download URL:");
        TextField urlField = new TextField("https://ash-speed.hetzner.com/100MB.bin");
        urlField.setMaxWidth(Double.MAX_VALUE);

        Label threadLabel = new Label("Threads:");
        Slider threadSlider = new Slider(1, availableThreads, defaultThreads);
        threadSlider.setMajorTickUnit(1);
        threadSlider.setMinorTickCount(0);
        threadSlider.setBlockIncrement(1);
        threadSlider.setShowTickMarks(true);
        threadSlider.setShowTickLabels(availableThreads <= 32);
        threadSlider.setSnapToTicks(true);
        threadSlider.setMaxWidth(Double.MAX_VALUE);
        threadSlider.setTooltip(new Tooltip("Detected CPU threads: " + availableThreads));

        Button startBtn = new Button("Start Download");
        startBtn.setMaxWidth(Double.MAX_VALUE);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label("Status: Idle");

        threadContainer = new VBox(5);
        ScrollPane scrollPane = new ScrollPane(threadContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(260);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        layout.getChildren().addAll(
                urlLabel, urlField,
                threadLabel, threadSlider,
                startBtn, progressBar, statusLabel,
                scrollPane
        );

        Scene scene = new Scene(layout, 560, 520);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(420);
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