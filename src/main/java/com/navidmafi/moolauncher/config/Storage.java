package com.navidmafi.moolauncher.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Storage {
    public static void saveConfig(Config config) throws RuntimeException {
        Properties props = new Properties();
        props.setProperty("username", config.username);
        props.setProperty("version", config.version);

        try (FileOutputStream out = new FileOutputStream("config.ini")) {
            props.store(out, "App Config");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Config readConfig() {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream("config.ini")) {
            props.load(in);
        } catch (IOException ignored) {
        }

        String username = props.getProperty("username"); // default: null
        String version = props.getProperty("version");
        return new Config(username, version);
    }

}
