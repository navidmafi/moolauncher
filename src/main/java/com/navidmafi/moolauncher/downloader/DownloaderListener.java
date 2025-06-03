package com.navidmafi.moolauncher.downloader;


public interface DownloaderListener {
    void onFinished(IDownloader downloader);

    void onError(IDownloader downloader, Exception exception);

    void onProgress(IDownloader downloader, int progress);
}