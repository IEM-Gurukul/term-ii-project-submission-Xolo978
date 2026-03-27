package com.xolo.swiftload.gui;

import com.xolo.swiftload.core.DownloadManager;
import com.xolo.swiftload.models.DownloadListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class App extends Application {
    private VBox threadContainer;
    private Path selectedDownloadPath;

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

        Label downloadPathLabel = new Label("Download Location:");
        Label pathDisplayLabel = new Label("downloads");
        pathDisplayLabel.setStyle("-fx-text-fill: #0078d4;");
        Button browseBtn = new Button("Browse");
        browseBtn.setPrefWidth(80);
        HBox pathSelectBox = new HBox(10, downloadPathLabel, pathDisplayLabel, browseBtn);
        pathSelectBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        selectedDownloadPath = Paths.get("downloads");

        browseBtn.setOnAction(_ -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Download Folder");
            File defaultDir = Files.exists(selectedDownloadPath)
                ? selectedDownloadPath.toFile() 
                : new File(System.getProperty("user.home"));
            dirChooser.setInitialDirectory(defaultDir);
            File selected = dirChooser.showDialog(primaryStage);
            if (selected != null) {
                selectedDownloadPath = selected.toPath();
                pathDisplayLabel.setText(selectedDownloadPath.toString());
            }
        });

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
                pathSelectBox,
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
            threadContainer.getChildren().clear();
            startBtn.setDisable(true);
            statusLabel.setText("Status: Starting download");
            progressBar.setProgress(0);
            DownloadListener listener = new DownloadUiListener(threadContainer, progressBar, statusLabel);
            new Thread(() -> {
                try {
                    if (!Files.exists(selectedDownloadPath)) {
                        Files.createDirectories(selectedDownloadPath);
                    }
                    DownloadManager manager = new DownloadManager(url, selectedDownloadPath, threads, listener);
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