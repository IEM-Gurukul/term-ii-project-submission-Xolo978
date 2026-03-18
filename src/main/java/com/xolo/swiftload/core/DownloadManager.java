package com.xolo.swiftload.core;

import com.xolo.swiftload.models.DownloadPart;
import com.xolo.swiftload.models.MetaData;
import com.xolo.swiftload.util.DownloadPartitioner;
import com.xolo.swiftload.util.MetaDataFetcher;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DownloadManager {
    String url;
    Path downloadDir;
    Path savePath;
    int threads;
    public DownloadManager(String url, Path downloadDir, int threads){
        this.url = url;
        this.downloadDir = downloadDir;
        this.threads = threads;
    }

    public void start() throws IOException, InterruptedException, CancellationException, ExecutionException{
        MetaData meta = MetaDataFetcher.fetch(url);
        this.savePath = downloadDir.resolve(meta.fileName());
        allocateFile(meta.fileSize());
        threads = meta.rangeSupport() ? threads : 1;
        List<DownloadPart> parts = DownloadPartitioner.partition(meta.fileSize(),threads);
        try(ExecutorService executor = Executors.newFixedThreadPool(threads)){
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for(DownloadPart part : parts){
                tasks.add(new DownloadWorker(part,url,savePath));
            }
            List<Future<Boolean>> futures = executor.invokeAll(tasks);
            for(Future<Boolean> i: futures){
                i.get();
            }
        }
    }

    private void allocateFile(long size) throws IOException{
        try(RandomAccessFile file = new RandomAccessFile(savePath.toFile(),"rw")){
            file.setLength(size);
        }
    }
}
