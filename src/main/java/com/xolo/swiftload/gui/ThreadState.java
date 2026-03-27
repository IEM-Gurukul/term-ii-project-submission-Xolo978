package com.xolo.swiftload.gui;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicLong;

public class ThreadState {
    final ProgressBar progressBar = new ProgressBar(0);
    final Label speedLabel;
    final AtomicLong downloaded = new AtomicLong(0);
    long downloadedAtLastSample = 0;
    final long totalBytes;

    ThreadState(VBox container, int partId, long totalBytes) {
        this.totalBytes = totalBytes;
        progressBar.setPrefWidth(160);
        speedLabel = new Label("0.00 MB/s");
        Label nameLabel = new Label("Thread " + partId);
        container.getChildren().add(new HBox(10, nameLabel, progressBar, speedLabel));
    }
}
