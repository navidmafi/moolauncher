package com.navidmafi.moolauncher.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.downloader.DownloadJob;
import com.navidmafi.moolauncher.downloader.DownloaderListener;
import com.navidmafi.moolauncher.downloader.IDownloader;
import com.navidmafi.moolauncher.downloader.MTDownloader;
import com.navidmafi.moolauncher.minecraft.api.VersionApi;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.OsUtils;
import com.navidmafi.moolauncher.util.UIProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;

public class Installer {

    public static void SetupVersion(String version, UIProgressListener listener) throws Exception {

        listener.onProgress(0, "Preparing to install version: " + version);

        MCStorage.createDirectories(version);

        Path versionJsonPath = MCStorage.getVersionJsonPath(version);
        String versionJson = VersionApi.getVersionJson(version);
        System.out.println("[Installer] downloaded: version.json");
        Files.writeString(versionJsonPath, versionJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("[Installer] wrote " + versionJsonPath);


        DownloaderListener downloaderListener = new DownloaderListener() {
            @Override
            public void onFinished(IDownloader downloader) {
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
            public void onError(IDownloader downloader, Exception exception) {
                listener.onFailure(exception.getMessage());
            }

            @Override
            public void onProgress(IDownloader downloader, int progress) {
                listener.onProgress(progress, "Downloading " + downloader.remainingItems() + " files");
            }
        };
        IDownloader downloader = new MTDownloader(8, 5, downloaderListener);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode versionNode = objectMapper.readTree(versionJson);
        JsonNode clientNode = versionNode.path("downloads").path("client");
        if (clientNode.isMissingNode()) {
            throw new RuntimeException("No client JAR in version.json");
        }
        String clientJarUrl = clientNode.get("url").asText();
        Path clientJarOut = MCStorage.getVersionJarPath(version);
        downloader.add(new DownloadJob(clientJarUrl, clientJarOut));


        JsonNode assetIndexNode = versionNode.path("assetIndex");
        if (assetIndexNode.isMissingNode()) {
            throw new RuntimeException("Asset index does not exist");
        }
        String assetIndexUrl = assetIndexNode.get("url").asText();
        Path assetIndexFile = MCStorage.getAssetIndexPath(version);
        String assetIndex = Networking.httpGetAsString(assetIndexUrl);
        Files.writeString(assetIndexFile, assetIndex, StandardOpenOption.CREATE);

        JsonNode assetIndexRootNode = objectMapper.readTree(assetIndex);
        Iterator<Map.Entry<String, JsonNode>> fields = assetIndexRootNode.path("objects").fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String hash = entry.getValue().get("hash").asText(); // 40‐char SHA1
            String prefix = hash.substring(0, 2);
            String assetUrl = "https://resources.download.minecraft.net/" + prefix + "/" + hash;
            Path outPath = MCStorage.getAssetsDirectory()
                    .resolve("objects").resolve(prefix).resolve(hash);
            downloader.add(new DownloadJob(assetUrl, outPath));
        }

        for (JsonNode lib : versionNode.get("libraries")) {
            if (lib.has("rules")) {
                boolean allowed = false;
                for (JsonNode rule : lib.get("rules")) {
                    String action = rule.get("action").asText();
                    JsonNode osNode = rule.get("os");
                    if (osNode == null) {
                        // applies to all OS
                        allowed = "allow".equals(action);
                    } else if (OsUtils.getMojangOsName().equals(osNode.get("name").asText())) {
                        allowed = "allow".equals(action);
                    }
                }
                if (!allowed) {
                    // Skip this library
                    continue;
                }
            }

            JsonNode downloadsNode = lib.path("downloads").path("artifact");
            if (downloadsNode.isMissingNode()) {
                throw new RuntimeException("Artifact does not exist");
            }
            String artifactUrl = downloadsNode.get("url").asText();
            String artifactPath = downloadsNode.get("path").asText();
            Path outPath = MCStorage.getLibrariesDirectory().resolve(artifactPath);
            downloader.add(new DownloadJob(artifactUrl, outPath));


            JsonNode loggingNode = versionNode.path("logging").path("client");
            if (loggingNode.isMissingNode()) {
                throw new RuntimeException("Logging does not exist");
            }
            String logConfigUrl = loggingNode.path("file").get("url").asText();
            Path logConfigFile = MCStorage.getLoggingConfigPath(version);
            MTDownloader.downloadFile(new DownloadJob(logConfigUrl, logConfigFile));


            // 5b.iii. Natives
            JsonNode classifiers = lib.path("downloads").path("classifiers");
            JsonNode nativeLibs = (classifiers != null) ? classifiers.get(OsUtils.getNativeClassifier()) : null;
            if (nativeLibs != null) {
                String nativeUrl = nativeLibs.get("url").asText();
                String nativePath = nativeLibs.get("path").asText();
                downloader.add(new DownloadJob(
                        nativeUrl,
                        MCStorage.getLibrariesDirectory().resolve(nativePath)
                ));
            }
        }

        downloader.start();

    }
}