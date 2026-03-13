package com.xolo.swiftload.util;

import com.xolo.swiftload.models.DownloadPart;

import java.util.ArrayList;
import java.util.List;

public class DownloadPartitioner {
    public static List<DownloadPart> partition(long totalSize, int threadCount){
       List<DownloadPart> parts = new ArrayList<>();
       long start = 0;
       long chunkSize = totalSize/threadCount;
       long end;
       for(int i =0;i<threadCount;i++){
           if(i==threadCount-1){
               end = totalSize-1;
           }
           else{
               end = start+chunkSize-1;
           }
           parts.add(new DownloadPart(i,start,end));
           start = end+1;
       }
       return parts;
    }
}
