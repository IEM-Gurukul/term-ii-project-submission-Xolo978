package com.xolo.swiftload.gui;

import com.xolo.swiftload.models.DownloadListener;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.xolo.swiftload.models.MetaData.formatBytes;

public class DownloadUiListener implements DownloadListener {
    private final VBox threadContainer;
    private final ProgressBar progressBar;
    private final Label status;
    private final AtomicLong totalDownloaded = new AtomicLong(0);
    private final AtomicBoolean isUiRefreshQueued = new AtomicBoolean(false);
    private final AtomicLong lastUiRefresh = new AtomicLong(0);
    private long totalSize = 0;
    private long lastTotalBytes = 0;
    private long lastTotalTs = 0;
    private long lastThreadSample = 0;
    private double speedMb = 0;
    private final Map<Integer, ThreadState> threads = new ConcurrentHashMap<>();

    public DownloadUiListener(VBox threadContainer, ProgressBar progressBar, Label status) {
        this.threadContainer = threadContainer;
        this.progressBar = progressBar;
        this.status = status;
    }

    @Override
    public void onPartStarted(int partId, long partSize) {
        Platform.runLater(() -> threads.put(partId, new ThreadState(threadContainer, partId, partSize)));
        requestUiRefresh(true);
    }

    @Override
    public void onProgress(int partId, long bytesDownloaded) {
        totalDownloaded.addAndGet(bytesDownloaded);
        ThreadState state = threads.get(partId);
        if (state != null) {
            state.downloaded.addAndGet(bytesDownloaded);
        }
        requestUiRefresh(false);
    }

    private void requestUiRefresh(boolean force) {
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
            double currentSpeed = bytesSinceLast / seconds / (1024 * 1024);
            speedMb = speedMb == 0 ? currentSpeed : (speedMb * 0.8) + (currentSpeed * 0.2);
        }
        lastTotalBytes = currentTotal;
        lastTotalTs = now;

        if (totalSize > 0 && speedMb > 0.01) {
            long remainingBytes = Math.max(0, totalSize - currentTotal);
            double eta = remainingBytes / (speedMb * 1024 * 1024);
            status.setText(String.format("Speed: %.2f MB/s | ETA: %.0fs", speedMb, eta));
        } else if (totalSize > 0) {
            status.setText("Status: Downloading...");
        }

        if (lastThreadSample == 0) {
            lastThreadSample = now;
        }
        double threadSeconds = Math.max(0.001, (now - lastThreadSample) / 1000.0);

        threads.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    ThreadState state = entry.getValue();
                    long partBytes = state.downloaded.get();

                    if (state.totalBytes > 0) {
                        state.progressBar.setProgress(Math.min(1.0, (double) partBytes / state.totalBytes));
                    }

                    double partSpeed = (partBytes - state.downloadedAtLastSample) / threadSeconds / (1024 * 1024);
                    state.downloadedAtLastSample = partBytes;

                    if (state.totalBytes > 0) {
                        state.speedLabel.setText(String.format("%.2f MB/s (%s/%s)",
                                partSpeed, formatBytes(partBytes), formatBytes(state.totalBytes)));
                    } else {
                        state.speedLabel.setText(String.format("%.2f MB/s (%s)",
                                partSpeed, formatBytes(partBytes)));
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
            ThreadState state = threads.get(partId);
            if (state != null) {
                state.progressBar.setProgress(1.0);
                state.speedLabel.setText("Done");
            }
        });
        requestUiRefresh(true);
    }
}

