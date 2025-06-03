package com.navidmafi.moolauncher.minecraft.service;

import com.navidmafi.moolauncher.downloader.DownloadJob;
import com.navidmafi.moolauncher.downloader.Downloader;
import com.navidmafi.moolauncher.minecraft.domain.LibraryInfo;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.OsUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LibraryDownloadService {
    private final Downloader downloader;

    public LibraryDownloadService(Downloader downloader) {
        this.downloader = downloader;
    }

    public void enqueueLibrariesDownload(List<LibraryInfo> libraries, String version) {
        for (LibraryInfo lib : libraries) {
            if (!RuleEvaluator.isAllowed(lib)) {
                continue;
            }

            if (lib.downloads.artifact != null) {
                String artifactUrl  = lib.downloads.artifact.url;
                String artifactPath = lib.downloads.artifact.path;
                Path outPath = MCStorage.getLibrariesDirectory().resolve(artifactPath);
                downloader.add(new DownloadJob(artifactUrl, outPath));
            }

            // 2b. Download native for current OS:
            String classifierKey = OsUtils.getNativeClassifier(); // e.g. "natives-linux"
            if (lib.downloads.classifiers != null) {
                LibraryInfo.ArtifactInfo nativeInfo = lib.downloads.classifiers.get(classifierKey);
                if (nativeInfo != null) {
                    Path outPath = MCStorage.getLibrariesDirectory().resolve(nativeInfo.path);
                    downloader.add(new DownloadJob(nativeInfo.url, outPath));
                }
            }
        }

        Path clientTarget = MCStorage.getVersionJarPath(version);
        downloader.add(new DownloadJob(clientUrl, clientTarget));
    }
}
