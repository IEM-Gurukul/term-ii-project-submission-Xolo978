package com.xolo.swiftload.models;

public record MetaData(
        String fileName,
        long fileSize,
        boolean rangeSupport
){
}