package com.navidmafi.moolauncher.minecraft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.navidmafi.moolauncher.minecraft.domain.AssetType;
import com.navidmafi.moolauncher.minecraft.domain.JarAsset;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class FabricService {
    public static ArrayList<JarAsset> GetFabricMainJars(JsonNode manifestNode) throws IOException, InterruptedException {
        ArrayList<JarAsset> assets = new ArrayList<>();
        JsonNode intermediaryNode = manifestNode.path("intermediary");
        JsonNode loaderNode = manifestNode.path("loader");
        assets.add(GetMavenAsset(loaderNode.get("maven").asText()));
        assets.add(GetMavenAsset(intermediaryNode.get("maven").asText()));
        return assets;
    }

    public static ArrayList<JarAsset> GetFabricLibs(JsonNode manifestNode) {
        ArrayList<JarAsset> assets = new ArrayList<>();
        JsonNode commonLibs = manifestNode.path("launcherMeta").path("libraries").path("common");
        for (JsonNode libNode : commonLibs) {
            String libName = libNode.path("name").asText();
            String baseUrl = libNode.path("url").asText();
            assets.add(
                    new JarAsset(
                            StorageService.getLibrariesDirectory().resolve(MavenService.toRelativePath(libName)),
                            libNode.get("sha1").asText(),
                            MavenService.toMavenUrl(libName,baseUrl,"jar"),
                            AssetType.MISC_ASSET,
                            MavenService.stripVersion(libName)
                    )
            );
        }
        return assets;
    }

    public static JarAsset GetMavenAsset(String mavenCoord) throws IOException, InterruptedException {
        String baseUrl = "https://maven.fabricmc.net/";
        String sha1Url = MavenService.toMavenUrl(mavenCoord, baseUrl, "jar.sha1");
        String jarUrl = MavenService.toMavenUrl(mavenCoord, baseUrl, "jar");
        String sha1 = NetworkingService.httpGetAsString(sha1Url);
        Path downloadRelativePath = MavenService.toRelativePath(mavenCoord);
        return new JarAsset(
                StorageService.getLibrariesDirectory().resolve(downloadRelativePath),
                sha1,
                jarUrl,
                AssetType.MISC_ASSET,
                MavenService.stripVersion(mavenCoord)

        );

    }



}
