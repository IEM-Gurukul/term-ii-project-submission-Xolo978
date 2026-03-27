package com.xolo.swiftload.models;

public interface DownloadListener {
    void onProgress(long bytesDownloaded);
    void onFailure(int partId, Throwable throwable);
    void onMetaDataFetched(long fileSize);
    void onComplete(int partId);
}
