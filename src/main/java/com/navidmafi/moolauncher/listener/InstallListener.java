package com.navidmafi.moolauncher.listener;

public interface InstallListener {
    void onFinished();
    void onFailure(String errMessage);
    void onProgress(int Progress, String Message);
}
