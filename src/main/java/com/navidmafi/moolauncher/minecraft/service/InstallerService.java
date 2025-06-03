package com.navidmafi.moolauncher.minecraft.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.downloader.Downloader;
import com.navidmafi.moolauncher.downloader.MultiThreadedDownloader;
import com.navidmafi.moolauncher.downloader.DownloaderListener;
import com.navidmafi.moolauncher.minecraft.api.VersionApi;
import com.navidmafi.moolauncher.minecraft.domain.VersionManifest;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.UIProgressListener;

public class InstallerService {
    private final VersionApi versionApi = new VersionApi();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MCStorage mcStorage = new MCStorage();

    /**
     * Downloads and installs a given Minecraft "version" offline:
     *   1. Fetch version.json
     *   2. Save JSON to disk
     *   3. Parse to VersionManifest
     *   4. Download libraries + client JAR
     *   5. Optionally download assets
     *   6. Extract natives
     */
    public void installVersion(String version, UIProgressListener listener) throws Exception {
        // STEP 1: Fetch + Save version.json
        listener.onProgress(0, "Fetching version metadata");
        String rawJson = versionApi.fetchVersionJson(version);
        mcStorage.saveVersionJson(version, rawJson);
        listener.onProgress(10, "Parsing version metadata");

        // STEP 2: Parse JSON
        VersionManifest manifest = objectMapper.readValue(rawJson, VersionManifest.class);

        // (Optional) STEP 2a: Download assets
        if (manifest.assetIndex != null) {
            listener.onProgress(20, "Fetching asset index");
            String assetJson = versionApi.fetchAssetIndex(manifest.assetIndex.url);
            mcStorage.saveAssetIndex(version, assetJson);

            listener.onProgress(30, "Downloading assets");
            AssetsService assetSvc = new AssetsService();
            assetSvc.downloadAssets(
                    objectMapper.readValue(assetJson, AssetIndex.class),
                    listener
            );
        }

        // STEP 3: Download libraries + client JAR
        listener.onProgress(50, "Preparing downloads");
        DownloaderListener dlListener = new DownloaderListener() {
            @Override
            public void onFinished(Downloader downloader) {

            }

            @Override
            public void onError(Downloader downloader, Exception exception) {

            }

            @Override
            public void onProgress(Downloader downloader, int progress) {

            }
        };
        var downloader = new MultiThreadedDownloader(8, 5, dlListener);

        LibraryDownloadService libService = new LibraryDownloadService(downloader);
        libService.enqueueLibrariesDownload(manifest.libraries, version);

        listener.onProgress(60, "Downloading libraries");
        downloader.start();
    }
}
