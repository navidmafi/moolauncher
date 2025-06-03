package com.navidmafi.moolauncher.controller;

public interface LauncherController {
    void launchOrInstall(
            String username,
            String version,
            UIProgressListener listener
    );
}
