package com.xolo.swiftload.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetaDataFetcherTest {
    @Test
    void testGDriveFetch() throws IOException, InterruptedException {
        String url = "https://drive.google.com/uc?export=download&id=1FqsW-_9k8nE-TkNQJy4VVFPQCRhKU_9M";
        MetaData meta = MetaDataFetcher.fetch(url);
        assertNotNull(meta);
        assertTrue(meta.fileName().contains("Avatar"));
        assertTrue(meta.fileSize()>0);
    }
}