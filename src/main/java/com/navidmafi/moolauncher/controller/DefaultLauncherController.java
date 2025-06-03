package com.navidmafi.moolauncher.controller;

import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.config.Storage;
import com.navidmafi.moolauncher.minecraft.ClientLauncher;
import com.navidmafi.moolauncher.minecraft.Installer;
import com.navidmafi.moolauncher.minecraft.LaunchConfig;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.UIProgressListener;

import java.util.Arrays;

public class DefaultLauncherController implements LauncherController {
    private final Storage storage;
    private final ClientLauncher clientLauncher;
    private final Installer installer;

    public DefaultLauncherController() {
        this.storage = new Storage(); // or inject a singleton
        this.clientLauncher = new ClientLauncher();
        this.installer = new Installer();
    }


    public void launchOrInstall(String username,
                                String version,
                                UIProgressListener uiListener) {
        Config config = storage.readConfig();

        config.username = username;
        config.version = version;
        storage.saveConfig(config);

        LaunchConfig lc;
        lc = new LaunchConfig(
                MCStorage.getLibrariesDirectory(),
                MCStorage.getNativesDirectory(version),
                MCStorage.getAssetsDirectory(),
                MCStorage.getVersionDirectory(version),
                MCStorage.getMCDirectory(),
                username,
                version
        );


        // 4) Check if version is already installed
        boolean isInstalled = Arrays.asList(config.installed_versions).contains(version);

        if (isInstalled) {
            // 4a) Already installed → launch immediately (on a background thread)
            new Thread(() -> {
                try {
                    clientLauncher.Launch(lc);
                    uiListener.onFinished();
                } catch (Exception e) {
                    uiListener.onFailure("Could not launch:\n" + e.getMessage());
                }
            }).start();

        } else {
            // 4b) Not installed → install first, then launch
            UIProgressListener installListener = new UIProgressListener() {
                @Override
                public void onProgress(int progress, String message) {
                    // Forward “download/installation” progress to the UI
                    uiListener.onProgress(progress, message);
                }

                @Override
                public void onFailure(String errorMessage) {
                    // If installer fails, report back immediately
                    uiListener.onFailure(errorMessage);
                }

                @Override
                public void onFinished() {
                    // Installation has finished successfully. Now launch (off the EDT).
                    new Thread(() -> {
                        try {
                            clientLauncher.Launch(lc);

                            // After a successful launch, mark version as installed
                            markVersionInstalled(version, storage.readConfig());

                            uiListener.onFinished();
                        } catch (Exception e) {
                            uiListener.onFailure("Launch after install failed:\n" + e.getMessage());
                        }
                    }).start();
                }
            };

            try {
                installer.SetupVersion(version, installListener);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void markVersionInstalled(String version, Config config) {
        // Avoid duplicates
        if (Arrays.asList(config.installed_versions).contains(version)) {
            return;
        }

        String[] oldArr = config.installed_versions;
        String[] newArr = Arrays.copyOf(oldArr, oldArr.length + 1);
        newArr[newArr.length - 1] = version;
        config.installed_versions = newArr;
        storage.saveConfig(config);

    }
}
