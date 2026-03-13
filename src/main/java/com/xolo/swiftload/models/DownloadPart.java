package com.xolo.swiftload.models;

public record  DownloadPart(
    int partId,
    long startByte,
    long endByte
){
    public long getLength(){
        return (endByte - startByte)+1;
    }
}
