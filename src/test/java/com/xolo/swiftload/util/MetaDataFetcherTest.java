package com.xolo.swiftload.util;

import com.xolo.swiftload.models.MetaData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MetaDataFetcherTest {
    @Test
    void testGDriveFetch() throws IOException, InterruptedException {
        String url = "https://drive.google.com/uc?export=download&id=1FqsW-_9k8nE-TkNQJy4VVFPQCRhKU_9M";
        MetaData meta = MetaDataFetcher.fetch(url);
        assertNotNull(meta);
        assertTrue(meta.fileName().contains("Avatar"));
        assertTrue(meta.fileSize()>0);
    }
    @Test
    void testUnreachableThrowException(){
       String url = "https://notexist.in/idk.txt";
       assertThrows(IOException.class , ()-> MetaDataFetcher.fetch(url));
    }
    @Test
    void testInvalidUrlThrowsException(){
        String url = "htttps://idk.com";
        assertThrows(IllegalArgumentException.class , () -> MetaDataFetcher.fetch(url));
    }
}

