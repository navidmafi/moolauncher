package com.navidmafi.moolauncher.controller;

import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.config.Storage;
import com.navidmafi.moolauncher.minecraft.ClientLauncher;
import com.navidmafi.moolauncher.minecraft.Installer;
import com.navidmafi.moolauncher.minecraft.LaunchConfig;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.UIProgressListener;

import java.io.IOException;
import java.util.Arrays;

public class DefaultLauncherController implements LauncherController {
    private final Storage storage; // to read/write config
    private final ClientLauncher clientLauncher;
    private final Installer installer;

    public DefaultLauncherController() {
        this.storage = new Storage(); // or inject a singleton
        this.clientLauncher = new ClientLauncher();
        this.installer = new Installer();
    }

    @Override
    public void launchOrInstall(String username, String version, UIProgressListener listener) {
        Config config = storage.readConfig();
        config.username = username;
        config.version = version;
        try {
            storage.saveConfig(config);
        } catch (IOException e) {
           listener.onFailure(e.getMessage());
        }

        LaunchConfig lc;
        try {
            lc = new LaunchConfig(
                    MCStorage.getLibrariesDirectory(),
                    MCStorage.getNativesDirectory(version),
                    MCStorage.getAssetsDirectory(),
                    MCStorage.getVersionDirectory(version),
                    MCStorage.getMCDirectory(),
                    username,
                    version
            );
        } catch (IOException ex) {
            listener.onFailure("Failed to build launch configuration:\n" + ex.getMessage());
            return;
        }

        String[] installed = config.installed_versions;
        boolean isInstalled = Arrays.asList(installed).contains(version);

        if (isInstalled) {
            new Thread(() -> {
                try {
                    clientLauncher.Launch(lc);
                    listener.onFinished();
                } catch (Exception e) {
                    listener.onFailure("Could not launch:\n" + e.getMessage());
                }
            }).start();

        } else {
            new Thread(() -> {
                try {
                    installer.SetupVersion(version, listener);
                    clientLauncher.Launch(lc);
                    listener.onFinished();
                } catch (Exception e) {
                    listener.onFailure("Installation/Launch failed:\n" + e.getMessage());
                }
            }).start();
        }
    }
}
