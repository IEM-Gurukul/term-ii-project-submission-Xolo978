package com.xolo.swiftload.gui;

import com.xolo.swiftload.core.DownloadManager;
import com.xolo.swiftload.models.DownloadListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

    private DownloadListener createListener(ProgressBar progressBar, Label status) {
        AtomicLong totalDownloaded = new AtomicLong(0);
        AtomicBoolean isUiRefreshQueued = new AtomicBoolean(false);
        AtomicLong lastUiRefresh = new AtomicLong(0);
        return new DownloadListener() {
            long totalSize = 0;
            long lastTotalBytes = 0;
            long lastTotalTs = 0;
            long lastThreadSample = 0;
            double Speed = 0;

            final Map<Integer, ProgressBar> threadProgressBars = new HashMap<>();
            final Map<Integer, Label> threadSpeedLabels = new HashMap<>();
            final Map<Integer, Long> threadLastBytes = new HashMap<>();
            final Map<Integer, Long> threadTotalBytes = new ConcurrentHashMap<>();
            final Map<Integer, AtomicLong> threadBytes = new ConcurrentHashMap<>();

            @Override
            public void onPartStarted(int partId, long partSize) {
                threadTotalBytes.put(partId, partSize);
                threadBytes.computeIfAbsent(partId, _ -> new AtomicLong(0));
                requestUiRefresh(true);
            }

            @Override
            public void onProgress(int partId, long bytesDownloaded) {
                totalDownloaded.addAndGet(bytesDownloaded);
                threadBytes.computeIfAbsent(partId, _ -> new AtomicLong(0)).addAndGet(bytesDownloaded);
                requestUiRefresh(false);
            }

            void threadRow(int partId) {
                if (threadProgressBars.containsKey(partId)) {
                    return;
                }
                ProgressBar pb = new ProgressBar(0);
                pb.setPrefWidth(160);
                Label lbl = new Label("Thread " + partId);
                Label speedLbl = new Label("0.00 MB/s");
                threadContainer.getChildren().add(new HBox(10, lbl, pb, speedLbl));
                threadProgressBars.put(partId, pb);
                threadSpeedLabels.put(partId, speedLbl);
                threadLastBytes.put(partId, 0L);
            }

            void requestUiRefresh(boolean force) {
                long now = System.currentTimeMillis();
                long last = lastUiRefresh.get();
                if (!force && (now - last) < 400) {
                    return;
                }
                if (!force && !lastUiRefresh.compareAndSet(last, now)) {
                    return;
                }
                if (force) {
                    lastUiRefresh.set(now);
                }
                if (isUiRefreshQueued.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        try {
                            renderUi();
                        } finally {
                            isUiRefreshQueued.set(false);
                        }
                    });
                }
            }

            private void renderUi() {
                long now = System.currentTimeMillis();
                long currentTotal = totalDownloaded.get();

                if (totalSize > 0) {
                    progressBar.setProgress(Math.min(1.0, (double) currentTotal / totalSize));
                }

                if (lastTotalTs > 0 && now > lastTotalTs) {
                    long bytesSinceLast = currentTotal - lastTotalBytes;
                    double seconds = (now - lastTotalTs) / 1000.0;
                    double currSpeed = bytesSinceLast / seconds / (1024 * 1024);
                    Speed = Speed == 0 ? currSpeed : (Speed * 0.8) + (currSpeed * 0.2);
                }
                lastTotalBytes = currentTotal;
                lastTotalTs = now;

                if (totalSize > 0 && Speed > 0.01) {
                    long remainingBytes = Math.max(0, totalSize - currentTotal);
                    double eta = remainingBytes / (Speed * 1024 * 1024);
                    status.setText(String.format("Speed: %.2f MB/s | ETA: %.0fs", Speed, eta));
                } else if (totalSize > 0) {
                    status.setText("Status: Downloading...");
                }

                if (lastThreadSample == 0) {
                    lastThreadSample = now;
                }
                double threadSeconds = Math.max(0.001, (now - lastThreadSample) / 1000.0);
                //Result rendering
                threadBytes.keySet().stream()
                        .sorted(Comparator.naturalOrder())
                        .forEach(partId -> {
                            threadRow(partId);
                            long pBytes = threadBytes.get(partId).get();

                            ProgressBar threadProgress = threadProgressBars.get(partId);
                            Long partSize = threadTotalBytes.get(partId);
                            if (partSize != null && partSize > 0) {
                                threadProgress.setProgress(Math.min(1.0, (double) pBytes / partSize));
                            }

                            long previousBytes = threadLastBytes.getOrDefault(partId, 0L);
                            double speed = (pBytes - previousBytes) / threadSeconds / (1024 * 1024);
                            threadLastBytes.put(partId, pBytes);

                            Label speedLabel = threadSpeedLabels.get(partId);
                            if (partSize != null && partSize > 0) {
                                speedLabel.setText(String.format("%.2f MB/s (%s/%s)", speed, formatBytes(pBytes), formatBytes(partSize)));
                            } else {
                                speedLabel.setText(String.format("%.2f MB/s (%s)", speed, formatBytes(pBytes)));
                            }
                        });

                lastThreadSample = now;

            }

            @Override
            public void onMetaDataFetched(long fileSize) {
                this.totalSize = fileSize;
                Platform.runLater(() -> status.setText("Status: Downloading"));
                requestUiRefresh(true);
            }

            @Override
            public void onComplete(int partId) {
                Platform.runLater(() -> {
                    ProgressBar pb = threadProgressBars.get(partId);
                    if (pb != null) {
                        pb.setProgress(1.0);
                    }
                    Label speedLbl = threadSpeedLabels.get(partId);
                    if (speedLbl != null) {
                        speedLbl.setText("Done");
                    }
                });
                requestUiRefresh(true);

            }
        };
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}