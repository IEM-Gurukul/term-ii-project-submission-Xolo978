package com.xolo.swiftload.util;

import com.xolo.swiftload.models.MetaData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaDataFetcher {
    public static MetaData fetch(String url) throws IOException, InterruptedException, IllegalArgumentException {
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        ) {
            URI uri = URI.create(url);
            HttpRequest req;
             req = HttpRequest.newBuilder()
                    .uri(uri)
                    .HEAD()
                    .header("User-Agent", "Mozilla/5.0")
                    .build();
             HttpResponse<Void> res;
             try{
                 res = client.send(req, HttpResponse.BodyHandlers.discarding());
             }catch(IOException e){
                 req = HttpRequest.newBuilder()
                         .uri(uri)
                         .header("User-Agent", "Mozilla/5.0")
                         .header("Range", "bytes=0-1")
                         .GET()
                         .build();
                 res = client.send(req, HttpResponse.BodyHandlers.discarding());
             }

            long size = res.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1L);
             //Content range is returned when using get request
             if(size<=2){
                 String range = res.headers()
                         .firstValue("Content-Range")
                         .orElse("");
                 if (range.contains("/")) {
                     size = Long.parseLong(range.split("/")[1]);
                 }
             }
            String acceptRange = res.headers()
                    .firstValue("Accept-Ranges")
                    .orElse("")
                    .toLowerCase();
            String fileName = getFileName(uri,res);
            //For GET req the server would return 206 status code
            return new MetaData(fileName, size, acceptRange.contains("bytes") || res.statusCode() == 206);
        }
    }
    private static String getFileName(URI url, HttpResponse<?> res){
        String disposition = res.headers().firstValue("Content-Disposition").orElse("");
        if(disposition.contains("filename=")){
            String reg = "filename=\"?([^\";]+)\"?";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(disposition);
            if(matcher.find()) {
                return matcher.group(1);
            }
        }
        String path = url.getPath();
        if(path==null||path.isEmpty()){
            return "file";
        }
        //like https://xolo.com/file/1.pdf
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
