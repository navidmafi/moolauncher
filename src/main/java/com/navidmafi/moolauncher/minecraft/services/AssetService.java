package com.navidmafi.moolauncher.minecraft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.navidmafi.moolauncher.minecraft.domain.AssetType;
import com.navidmafi.moolauncher.minecraft.domain.JarAsset;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AssetService {
    public static ArrayList<JarAsset> ExtractMCAssets(JsonNode assetsIndexNode) {
        ArrayList<JarAsset> assets = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = assetsIndexNode.path("objects").fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String hash = entry.getValue().get("hash").asText(); // 40‐char SHA1
            String prefix = hash.substring(0, 2);
            String assetUrl = "https://resources.download.minecraft.net/" + prefix + "/" + hash;
            Path outPath = StorageService.getAssetsDirectory()
                    .resolve("objects").resolve(prefix).resolve(hash);
            assets.add(new JarAsset(outPath, hash, assetUrl, AssetType.GAME_RESOURCE, null));
        }
        return assets;
    }

    public static JarAsset GetMainClient(JsonNode versionNode) {
        JsonNode clientNode = versionNode.path("downloads").path("client");
        if (clientNode.isMissingNode()) {
            throw new RuntimeException("No client JAR in version.json");
        }
        String clientJarUrl = clientNode.get("url").asText();
        String clientSha1 = clientNode.get("sha1").asText();
        Path clientJarOut = StorageService.getVersionJarPath(versionNode.get("id").asText());
        return new JarAsset(clientJarOut, clientSha1, clientJarUrl, AssetType.MISC_ASSET, versionNode.get("mainClass").asText());
    }
}
