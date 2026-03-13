package com.xolo.swiftload.util;

import com.xolo.swiftload.models.DownloadPart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DownloadPartitionerTest {
    @Test
    void testSingleThread(){
        long fileSize = 1000;
        List<DownloadPart> parts = DownloadPartitioner.partition(fileSize,1);
        assertEquals(1,parts.size());
        assertEquals(0,parts.getFirst().startByte());
        assertEquals(999,parts.getFirst().endByte());
    }
}
