package com.navidmafi.moolauncher.config;

import java.util.ArrayList;

public class Config {
    public String username;
    public String version;
    public String[] installed_versions;

    public Config(String username, String version, String[] installed_versions) {
        this.username = username;
        this.version = version;
        this.installed_versions = installed_versions;
    }
}

