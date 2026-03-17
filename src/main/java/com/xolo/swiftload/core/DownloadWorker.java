package com.xolo.swiftload.core;

import com.xolo.swiftload.models.DownloadPart;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;

public class DownloadWorker implements Callable<Boolean> {
     DownloadPart part;
     URI uri;
     Path savePath;
    public DownloadWorker(DownloadPart part, String url, Path savePath){
        this.part = part;
        this.uri = URI.create(url);
        this.savePath = savePath;
    }
    @Override
    public Boolean call() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(2))
                .build()
        ){
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Range","bytes="+part.startByte()+"-"+part.endByte())
                    .GET()
                    .build();
            HttpResponse<InputStream> res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if(res.statusCode()!=206){
                throw new RuntimeException("Server did not support partial request");
            }
            copyToFile(res.body());
            return true;
        }
    }

    public void copyToFile(InputStream input) throws IOException {
        try(RandomAccessFile file = new RandomAccessFile(savePath.toFile(),"rw")){
           file.seek(part.startByte());
           //TODO: Optimize buffer size dynamically for large files
           byte[] buffer = new byte[8192];
           int i;
           while((i =input.read(buffer))!=-1){
               file.write(buffer,0,i);
           }

        }
    }
}
