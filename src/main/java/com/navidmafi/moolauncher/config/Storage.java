package com.navidmafi.moolauncher.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Storage {
    ObjectMapper mapper = new ObjectMapper(); // or use GSON

    public void saveConfig(Config config) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("config.json"), config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Config readConfig() {
        try {
            return mapper.readValue(new File("config.json"), Config.class);
        } catch (IOException e) {
            var defaultConfig = new Config();
            defaultConfig.heapSize = "2G";
            defaultConfig.useFabric = false;
            return defaultConfig;
        }
    }


}
