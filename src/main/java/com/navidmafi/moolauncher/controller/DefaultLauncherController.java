package com.navidmafi.moolauncher.controller;

import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.config.Storage;
import com.navidmafi.moolauncher.listener.GameListener;
import com.navidmafi.moolauncher.minecraft.services.InstallationService;
import com.navidmafi.moolauncher.minecraft.model.LaunchConfig;
import com.navidmafi.moolauncher.minecraft.services.StorageService;

import java.util.Arrays;

public class DefaultLauncherController implements LauncherController {
    private final Storage storage;
    private final InstallationService installer;

    public DefaultLauncherController() {
        this.storage = new Storage(); // or inject a singleton
        this.installer = new InstallationService();
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
