package com.xolo.swiftload.core;

import com.xolo.swiftload.models.DownloadListener;
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
import java.util.concurrent.atomic.AtomicLong;

public class DownloadManager implements DownloadListener {
    String url;
    Path downloadDir;
    Path savePath;
    int threads;
    long fileSize;
    private final AtomicLong totalDownloaded = new AtomicLong(0);
    public DownloadManager(String url, Path downloadDir, int threads){
        this.url = url;
        this.downloadDir = downloadDir;
        this.threads = threads;
    }

    public void start() throws IOException, InterruptedException, CancellationException, ExecutionException{
        MetaData meta = MetaDataFetcher.fetch(url);
        this.savePath = downloadDir.resolve(meta.fileName());
        this.fileSize = meta.fileSize();
        allocateFile(meta.fileSize());
        threads = meta.rangeSupport() ? threads : 1;
        System.out.println("Server supports rangeSupport: "+meta.rangeSupport());
        System.out.println("Threads being used: "+ threads);
        List<DownloadPart> parts = DownloadPartitioner.partition(meta.fileSize(),threads);
        try(ExecutorService executor = Executors.newFixedThreadPool(threads)){
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for(DownloadPart part : parts){
                tasks.add(new DownloadWorker(part,url,savePath, this));
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

    @Override
    public void onProgress(long bytesDownloaded) {
        long current = totalDownloaded.addAndGet(bytesDownloaded);
        double progress = ((double)current/fileSize) * 100;
        int progressStars = (int) (progress / 5);
        String bar = "=".repeat(progressStars) + " ".repeat(20 - progressStars);
        System.out.printf("\rDownloading: [%s] %.2f%%", bar, progress);
    }

    @Override
    public void onFailure(int partId, Throwable throwable) {
        System.err.println("Failed for partID: "+ partId);
    }

    @Override
    public void onComplete(int partId) {
        System.out.println("Completed partID: "+partId);

    }
}
