package com.navidmafi.moolauncher.downloader;


import java.io.IOException;

public interface DownloaderListener {
    void onFinished(Downloader downloader);

    void onError(Downloader downloader, Exception exception);

    void onProgress(Downloader downloader, int progress);
}