package com.navidmafi.moolauncher.minecraft;

import java.nio.file.Path;

public class LaunchConfig {

    public final Path librariesDir;
    public final Path nativesDir;
    public final Path assetsDir;
    public final Path versionDir;
    public final Path mcDir;
    public final String username;
    public final String version;

    public LaunchConfig(Path librariesDir,
                        Path nativesDir,
                        Path assetsDir,
                        Path versionDir,
                        Path mcDir,
                        String username,
                        String version) {
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.assetsDir = assetsDir;
        this.versionDir = versionDir;
        this.mcDir = mcDir;
        this.username = username;
        this.version = version;
    }


}