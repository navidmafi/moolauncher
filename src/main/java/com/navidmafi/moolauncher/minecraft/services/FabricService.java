package com.navidmafi.moolauncher.minecraft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.minecraft.domain.AssetType;
import com.navidmafi.moolauncher.minecraft.domain.JarAsset;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricService {

    public static List<String> GetLoaderVersions(String mcVersion) throws IOException, InterruptedException {
        String url = "https://meta.fabricmc.net/v2/versions/loader/" + mcVersion;
        String json = NetworkingService.httpGetAsString(url);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode entries = objectMapper.readTree(json);

        List<String> versions = new ArrayList<>();
        for (JsonNode entry : entries) {
            versions.add(entry.path("loader").path("version").asText());
        }
        return versions;
    }

    public static ArrayList<JarAsset> GetFabricMainJars(JsonNode manifestNode, boolean offline) throws IOException, InterruptedException {
        System.out.println("[FabricService] GetFabricMainJars ");
        ArrayList<JarAsset> assets = new ArrayList<>();
        JsonNode intermediaryNode = manifestNode.path("intermediary");
        JsonNode loaderNode = manifestNode.path("loader");
        assets.add(GetMavenAsset(loaderNode.get("maven").asText(), offline));
        assets.add(GetMavenAsset(intermediaryNode.get("maven").asText(), offline));
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

    public static JarAsset GetMavenAsset(String mavenCoord, boolean offline) throws IOException, InterruptedException {
        String baseUrl = "https://maven.fabricmc.net/";
        String sha1Url = MavenService.toMavenUrl(mavenCoord, baseUrl, "jar.sha1");
        String jarUrl = MavenService.toMavenUrl(mavenCoord, baseUrl, "jar");
        String sha1 = "";
        if (!offline) {
             sha1 = NetworkingService.httpGetAsString(sha1Url);
        }
        Path downloadRelativePath = MavenService.toRelativePath(mavenCoord);
        return new JarAsset(
                StorageService.getLibrariesDirectory().resolve(downloadRelativePath),
                sha1,
                jarUrl,
                AssetType.MISC_ASSET,
                MavenService.stripVersion(mavenCoord),
                offline
        );

    }

}
