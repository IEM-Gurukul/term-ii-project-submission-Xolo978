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

public class DownloadManager  {
    String url;
    Path downloadDir;
    Path savePath;
    int threads;
    long fileSize;
    private final DownloadListener listener;
    public DownloadManager(String url, Path downloadDir, int threads, DownloadListener listener){
        this.url = url;
        this.downloadDir = downloadDir;
        this.threads = threads;
        this.listener = listener;
    }

    public void start() throws IOException, InterruptedException, CancellationException, ExecutionException{
        MetaData meta = MetaDataFetcher.fetch(url);
        this.savePath = downloadDir.resolve(meta.fileName());
        this.fileSize = meta.fileSize();
        if(listener!=null){
            listener.onMetaDataFetched(fileSize);
        }
        allocateFile(fileSize);
        threads = meta.rangeSupport() ? threads : 1;
        System.out.println("Server supports rangeSupport: "+meta.rangeSupport());
        System.out.println("Threads being used: "+ threads);
        List<DownloadPart> parts = DownloadPartitioner.partition(meta.fileSize(),threads);
        try(ExecutorService executor = Executors.newFixedThreadPool(threads)){
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for(DownloadPart part : parts){
                tasks.add(new DownloadWorker(part,url,savePath, listener));
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
