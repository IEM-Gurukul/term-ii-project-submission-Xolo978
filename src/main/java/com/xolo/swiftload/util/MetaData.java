package com.xolo.swiftload.util;

public record MetaData(
        String fileName,
        long fileSize
){
    public String getFileSize(){
        if(fileSize<=0){
            return "Unknown Size";
        }
        if(fileSize<1024) {
            return fileSize + "B";
        }
        int exp = (int) (Math.log(fileSize)/Math.log(1024));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.2f %cB", fileSize / Math.pow(1024, exp), pre); //Human redable format
    }
}