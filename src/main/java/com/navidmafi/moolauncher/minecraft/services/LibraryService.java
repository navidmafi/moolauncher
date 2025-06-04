package com.navidmafi.moolauncher.minecraft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.navidmafi.moolauncher.minecraft.domain.AssetType;
import com.navidmafi.moolauncher.minecraft.domain.JarAsset;
import com.navidmafi.moolauncher.util.OsUtils;

import java.nio.file.Path;
import java.util.ArrayList;


// Each library has both its own assets and native platform-specific assets
public class LibraryService {
    public static ArrayList<JarAsset> GetCompatibleLibs(JsonNode versionNode) {
        var Libs = new ArrayList<JarAsset>();
        for (JsonNode lib : versionNode.get("libraries")) {
            if (!IsCompatible(lib)) continue;
            JsonNode downloadsNode = lib.path("downloads").path("artifact");
            if (downloadsNode.isMissingNode()) {
                throw new RuntimeException("Artifact does not exist");
            }

            String artifactPath = downloadsNode.get("path").asText();
            String artifactSha1 = downloadsNode.get("sha1").asText();
            String artifactUrl = downloadsNode.get("url").asText();

            var RL = new JarAsset(
                    StorageService.getLibrariesDirectory().resolve(artifactPath),
                    artifactSha1,
                    artifactUrl,
                    AssetType.NON_NATIVE_LIBRARY);
            Libs.add(RL);

            JsonNode classifiers = lib.path("downloads").path("classifiers");
            JsonNode nativeLibs = (classifiers != null) ? classifiers.get(OsUtils.getNativeClassifier()) : null;
            if (nativeLibs != null) {
                String nativeUrl = nativeLibs.get("url").asText();
                String nativePath = nativeLibs.get("path").asText();
                String nativeSha1 = nativeLibs.get("sha1").asText();
                Path nativeOutPath = StorageService.getLibrariesDirectory().resolve(nativePath);
                var NRL = new JarAsset(nativeOutPath, nativeSha1, nativeUrl, AssetType.NON_NATIVE_LIBRARY);
                Libs.add(NRL);
            }
        }
        return Libs;
    }

    public static boolean IsCompatible(JsonNode libNode) {
        if (!libNode.has("rules")) {
            return true;
        }

        boolean allowed = false;
        for (JsonNode rule : libNode.get("rules")) {
            String action = rule.get("action").asText();
            JsonNode osNode = rule.get("os");
            if (osNode == null) {
                // Rule applies to all OS
                allowed = "allow".equals(action);
            } else if (OsUtils.getMojangOsName().equals(osNode.get("name").asText())) {
                // Rule specifically for current OS
                allowed = "allow".equals(action);
            }
            // Note: we do NOT break here, because a later rule might override
            // (Mojang format: last matching rule takes precedence) [2].
        }
        return allowed;
    }
}
