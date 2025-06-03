package com.navidmafi.moolauncher.downloader;

public interface IDownloader {
    void add(DownloadJob job);

    int totalItems();

    int remainingItems();

    void start();

    void stop();
}