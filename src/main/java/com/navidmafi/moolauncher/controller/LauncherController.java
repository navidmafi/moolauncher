package com.navidmafi.moolauncher.controller;

import com.navidmafi.moolauncher.util.UIProgressListener;

public interface LauncherController {
    void launchOrInstall(
            String username,
            String version,
            UIProgressListener listener
    );
}
