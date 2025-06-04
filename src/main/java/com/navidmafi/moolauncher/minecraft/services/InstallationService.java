package com.navidmafi.moolauncher.minecraft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.downloader.DownloadJob;
import com.navidmafi.moolauncher.downloader.DownloaderListener;
import com.navidmafi.moolauncher.downloader.IDownloader;
import com.navidmafi.moolauncher.downloader.MTDownloader;
import com.navidmafi.moolauncher.listener.InstallListener;
import com.navidmafi.moolauncher.listener.SwingProgressListener;
import com.navidmafi.moolauncher.minecraft.api.VersionApi;
import com.navidmafi.moolauncher.minecraft.domain.JarAsset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static com.navidmafi.moolauncher.minecraft.services.StorageService.computeSha1;

public class InstallationService {

    public static void installVersion(
            Config config,
            SwingProgressListener swingProgressListener,
            InstallListener installListener
    ) throws Exception {


        swingProgressListener.onProgress(0, "Preparing to install version: " + config.version);

        StorageService.createDirectories(config.version);

        Path versionJsonPath = StorageService.getVersionJsonPath(config.version);
        String versionJson = VersionApi.getVersionJson(config.version);
        System.out.println("[Installer] downloaded: version.json");
        Files.writeString(versionJsonPath, versionJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("[Installer] wrote " + versionJsonPath);


        DownloaderListener downloaderListener = new DownloaderListener() {
            public void onFinished(IDownloader downloader) {
                swingProgressListener.onProgress(100, "Extracting Native Libraries");
                try {
                    StorageService.extractNatives(config.version);
                    swingProgressListener.onProgress(100, "Installed Native Libraries");
                    installListener.onInstall();
                } catch (IOException e) {
                    swingProgressListener.onFailure(e.getMessage());
                }
            }

            public void onError(IDownloader downloader, Exception e) {
                swingProgressListener.onFailure(e.getMessage());
            }

            public void onProgress(IDownloader downloader, int progress) {
                swingProgressListener.onProgress(progress, "Downloading " + downloader.remainingItems() + " files");
            }
        };
        IDownloader downloader = new MTDownloader(8, 5, downloaderListener);

        var AllAssets = new ArrayList<JarAsset>();


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode versionNode = objectMapper.readTree(versionJson);
        AllAssets.add(AssetService.GetMainClient(versionNode));
        AllAssets.addAll(LibraryService.GetCompatibleLibs(versionNode));


        JsonNode assetIndexMetaNode = versionNode.path("assetIndex");
        if (assetIndexMetaNode.isMissingNode()) {
            throw new RuntimeException("Asset index does not exist");
        }
        String assetIndexUrl = assetIndexMetaNode.get("url").asText();
        Path assetIndexFile = StorageService.getAssetIndexPath(config.version);
        String assetIndex = NetworkingService.httpGetAsString(assetIndexUrl);
        Files.writeString(assetIndexFile, assetIndex, StandardOpenOption.CREATE);

        JsonNode assetIndexNode = objectMapper.readTree(assetIndex);
        AllAssets.addAll(AssetService.ExtractMCAssets(assetIndexNode));

        if (config.useFabric) {
            String manifestUrl =
                    "https://meta.fabricmc.net/v2/versions/loader/"
                            + config.version + "/" + config.fabricVersion;
            String manifestJson = NetworkingService.httpGetAsString(manifestUrl);
            String loaderFullVersionString = "fabric-loader-"+config.fabricVersion+"-"+config.version;
            Path fabricVerJson = StorageService.getVersionJsonPath(loaderFullVersionString);
            Files.createDirectories(fabricVerJson.getParent());
            Files.writeString(
                    fabricVerJson,
                    manifestJson,
                    StandardOpenOption.CREATE
            );
            JsonNode manifestNode = objectMapper.readTree(manifestJson);
            AllAssets.addAll(FabricService.GetFabricMainJars(manifestNode));
            AllAssets.addAll(FabricService.GetFabricLibs(manifestNode));
       }
        AllAssets.forEach(asset -> {
            if (needsDownload(asset)) {
                try {
                    Files.createDirectories(asset.filePath.getParent());
                    downloader.add(new DownloadJob(asset.downloadURL, asset.filePath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        downloader.start();

    }


    private static boolean needsDownload(JarAsset asset) {
        try {
            if (!Files.exists(asset.filePath)) return true;
            String actualSha1 = computeSha1(asset.filePath);
            return !actualSha1.equalsIgnoreCase(asset.sha1);

        } catch (IOException e) {
            return true;
        }
    }
}