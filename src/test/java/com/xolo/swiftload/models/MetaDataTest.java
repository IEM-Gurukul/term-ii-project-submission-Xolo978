package com.xolo.swiftload.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaDataTest {
    @Test
    void testGetFileSizeKB(){
        MetaData data = new MetaData("f1.zip",1024);
        assertEquals("1.00 KB",data.getFileSize());
    }
    @Test
    void testGetFileSizeMB(){
        MetaData data = new MetaData("f1.zip",1024*1024);
        assertEquals("1.00 MB",data.getFileSize());
    }
}
