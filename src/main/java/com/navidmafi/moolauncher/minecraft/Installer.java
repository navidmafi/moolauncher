package com.navidmafi.moolauncher.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.downloader.DownloadJob;
import com.navidmafi.moolauncher.downloader.Downloader;
import com.navidmafi.moolauncher.downloader.DownloaderListener;
import com.navidmafi.moolauncher.downloader.MultiThreadedDownloader;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.UIProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class Installer {
    private static String detectOS() {
        String name = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (name.contains("win")) return "windows";
        if (name.contains("mac")) return "osx";
        return "linux";
    }

    public static void SetupVersion(String version, UIProgressListener listener) throws Exception {

        System.out.println("[Installer] Installing version: " + version);
        Files.createDirectories(MCStorage.getVersionDirectory(version));



        Path versionJsonPath = MCStorage.getVersionJsonPath(version);
        String versionJson = MojangAPI.getVersionJson(version);
        System.out.println("[Installer] downloaded: version.json");
        Files.writeString(MCStorage.getVersionJsonPath(version), versionJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("[Installer] wrote: version.json");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode versionNode = objectMapper.readTree(versionJson);

        DownloaderListener downloaderListener = new DownloaderListener() {
            @Override
            public void onFinished(Downloader downloader) {
                listener.onProgress(100, "Extracting Native Libraries");
                try {
                    MCStorage.extractNatives(version);
                    listener.onFinished();
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e.getMessage());
                    throw new RuntimeException(e);
                }
            }


            @Override
            public void onError(Downloader downloader, Exception exception) {
                listener.onFailure(exception.getMessage());
            }

            @Override
            public void onProgress(Downloader downloader, int progress) {
                listener.onProgress(progress, "Downloading files");
            }
        };
        Downloader downloader = new MultiThreadedDownloader(8, 5, downloaderListener);

        JsonNode clientNode = versionNode.path("downloads").path("client");
        String clientJarUrl = clientNode.get("url").asText();
        downloader.add(new DownloadJob(clientJarUrl, MCStorage.getVersionJarPath(version)));

        JsonNode librariesArray = versionNode.get("libraries");

        for (JsonNode lib : librariesArray) {
            if (lib.has("rules")) {
                boolean allowed = false;
                for (JsonNode rule : lib.get("rules")) {
                    String action = rule.get("action").asText();
                    JsonNode osNode = rule.get("os");
                    if (osNode == null) {
                        // applies to all OS
                        allowed = "allow".equals(action);
                    } else if ("linux".equals(osNode.get("name").asText())) {
                        allowed = "allow".equals(action);
                    }
                }
                if (!allowed) {
                    // Skip this library
                    continue;
                }
            }

            // 5b.ii. Artifact (normal JAR)
            JsonNode downloadsNode = lib.path("downloads").path("artifact");
            if (!downloadsNode.isMissingNode()) {
                String artifactUrl = downloadsNode.get("url").asText();
                String artifactPath = downloadsNode.get("path").asText();
                Path outPath = MCStorage.getLibrariesDirectory().resolve(artifactPath);
                downloader.add(new DownloadJob(artifactUrl, outPath));
            }

            // 5b.iii. Natives (linux)
            JsonNode classifiers = lib.path("downloads").path("classifiers");
            JsonNode nativeLinux = (classifiers != null) ? classifiers.get("natives-linux") : null;
            if (nativeLinux != null) {
                String nativeUrl = nativeLinux.get("url").asText();
                String nativePath = nativeLinux.get("path").asText();
                Path outPath = MCStorage.getLibrariesDirectory().resolve(nativePath);
                downloader.add(new DownloadJob(nativeUrl, outPath));
            }
        }

        System.out.println("[Installer] starting downloader");
        downloader.start();

    }
}