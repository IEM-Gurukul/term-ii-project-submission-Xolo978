package com.xolo.swiftload.models;

public interface DownloadListener {
   default void onPartStarted(int partId, long partSize) {}
    void onProgress(int partId,long bytesDownloaded);
    void onMetaDataFetched(long fileSize);
    void onComplete(int partId);
}
