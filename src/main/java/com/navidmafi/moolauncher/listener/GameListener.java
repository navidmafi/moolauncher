package com.navidmafi.moolauncher.listener;

public interface GameListener {
    void onExit(int exitCode);
    void onFailure(String errMessage);
    void onLaunch();
}
