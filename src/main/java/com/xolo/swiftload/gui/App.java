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
import java.util.concurrent.atomic.AtomicLong;

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

        startBtn.setOnAction(_ -> {
            String url = urlField.getText();
            int threads = (int) threadSlider.getValue();
            Path downloadDir = Paths.get("downloads");
            startBtn.setDisable(true);
            statusLabel.setText("Status: Starting download");
            progressBar.setProgress(0);
            DownloadListener listener = createListener(progressBar, statusLabel);
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

    private DownloadListener createListener(ProgressBar progressBar, Label status){
        AtomicLong totalDownloaded = new AtomicLong(0);
        return new DownloadListener() {
            long totalSize = 0;
            @Override
            public void onProgress(long bytesDownloaded) {
                long current = totalDownloaded.addAndGet(bytesDownloaded);
                if(totalSize>0){
                    double progress = (double) current/ totalSize;
                    Platform.runLater(()->progressBar.setProgress(progress));
                }
            }

            @Override
            public void onFailure(int partId, Throwable throwable) {
                Platform.runLater(()->status.setText("Error in partID: "+ partId));
            }

            @Override
            public void onMetaDataFetched(long fileSize) {
                this.totalSize = fileSize;
                Platform.runLater(() -> status.setText("Status: Downloading"));
            }

            @Override
            public void onComplete(int partId) {

            }
        };
    }
}