package com.navidmafi.moolauncher.util;

public interface UIProgressListener {
    void onProgress(int progress, String progressMessage);

    void onFailure(String errorMessage);

    void onFinished();
}