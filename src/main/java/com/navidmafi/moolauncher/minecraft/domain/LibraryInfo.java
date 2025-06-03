package com.navidmafi.moolauncher.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Maps a single entry from "libraries": [ ... ] in version.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryInfo {
    /** e.g. "com.mojang:patchy:1.1" */
    public String name;

    /** Contains "downloads": { "artifact": { … }, "classifiers": { … } } */
    public Downloads downloads;

    /** Optional "rules": [ { "action": "allow"/"disallow", "os": { "name": "linux" } }, … ] */
    public List<Rule> rules;

    // ====================
    // Nested types below
    // ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Downloads {
        /** Maps to "downloads.artifact":  { "sha1": "...", "size": ..., "url": "...", "path": "..." } */
        public ArtifactInfo artifact;

        /**
         * Maps to "downloads.classifiers": {
         *    "natives-windows": { "url": "...", "sha1": "...", "path": "..." },
         *    "natives-linux":   { "url": "...", "sha1": "...", "path": "..." },
         *    …
         * }
         */
        public Map<String, ArtifactInfo> classifiers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArtifactInfo {
        /** SHA-1 checksum (you may use it later to verify integrity). */
        public String sha1;

        /** Size in bytes (optional if you don’t need it). */
        public long size;

        /** Download URL, e.g. "https://libraries.minecraft.net/com/mojang/patchy/1.1/patchy-1.1.jar" */
        public String url;

        /** Relative path under "libraries/" for this artifact, e.g. "com/mojang/patchy/1.1/patchy-1.1.jar" */
        public String path;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rule {
        /** Either "allow" or "disallow" */
        public String action;

        /** Optional "os": { "name": "linux", "version": "^10\\..*" } */
        public Os os;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Os {
        /** E.g. "windows", "linux", or "osx" */
        public String name;

        /** A regex string for version matching, e.g. "^10\\..*" (often absent) */
        public String version;
    }
}
