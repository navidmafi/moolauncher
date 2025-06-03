package com.navidmafi.moolauncher.downloader;

import java.nio.file.Path;

public class DownloadJob {
    private final String url;
    private final Path path;
    private volatile DownloadState state;
    public DownloadJob(String url, Path path) {
        this.url = url;
        this.path = path;
        this.state = DownloadState.QUEUED;
    }

    public String getUrl() {
        return url;
    }

    public Path getPath() {
        return path;
    }

    public DownloadState getState() {
        return state;
    }

    public void setState(DownloadState state) {
        this.state = state;
    }
}
