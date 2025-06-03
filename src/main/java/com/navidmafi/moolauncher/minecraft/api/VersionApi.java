package com.navidmafi.moolauncher.minecraft.api;

import com.navidmafi.moolauncher.AppConstants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VersionApi {
    private final HttpClient http = HttpClient.newHttpClient();

    public String fetchVersionJson(String version) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(AppConstants.PISTON_URL)).build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Failed to fetch version.json (HTTP " + resp.statusCode() + ")");
        }
        return resp.body();
    }
}
