package com.navidmafi.moolauncher.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionManifest {
    @JsonProperty("downloads")
    public Downloads downloads;

    @JsonProperty("libraries")
    public List<LibraryInfo> libraries;

    // You can nest inner classes or use separate files for clarity:
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Downloads {
        @JsonProperty("client")
        public ArtifactInfo client;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArtifactInfo {
        @JsonProperty("url")
        public String url;

        @JsonProperty("path")
        public String path;
    }
}
