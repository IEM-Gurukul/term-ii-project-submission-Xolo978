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
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .HEAD()
                    .build();
            HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
            long size = res.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1L);
            String fileName = getFileName(uri,res);
            return new MetaData(fileName, size);
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
