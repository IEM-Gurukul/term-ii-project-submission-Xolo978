package com.xolo.swiftload.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void download() throws IOException, ExecutionException, InterruptedException {
        String url = "https://raw.githubusercontent.com/IEM-Gurukul/term-ii-project-submission-Xolo978/main/README.md";
        int threads = 2;
        DownloadManager manager = new DownloadManager(url,tempDir,threads);
        manager.start();
        assertTrue(Files.exists(tempDir.resolve("README.md")),"File should exist");
    }
}
