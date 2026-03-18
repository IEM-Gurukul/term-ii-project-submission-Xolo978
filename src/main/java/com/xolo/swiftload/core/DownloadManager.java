package com.xolo.swiftload.core;

import com.xolo.swiftload.models.MetaData;
import com.xolo.swiftload.util.MetaDataFetcher;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class DownloadManager {
    String url;
    Path savePath;
    int threads;
    public DownloadManager(String url, Path savePath, int threads){
        this.url = url;
        this.savePath = savePath;
        this.threads = threads;
    }

    public void start() throws IOException, InterruptedException {
        MetaData meta = MetaDataFetcher.fetch(url);
        allocateFile(meta.fileSize());
    }

    private void allocateFile(long size) throws IOException{
        try(RandomAccessFile file = new RandomAccessFile(savePath.toFile(),"rw")){
            file.setLength(size);
        }
    }
}
