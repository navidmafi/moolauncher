package com.navidmafi.moolauncher.downloader;

public interface Downloader {
    void add(DownloadJob job);

    void start();

    void stop();
}