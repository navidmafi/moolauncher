package com.navidmafi.moolauncher.minecraft.domain;

import java.nio.file.Path;

public class MJAsset {
    public final Path filePath;
    public final String sha1;
    public final String downloadURL;
    public final AssetType assetType;
    public MJAsset(Path filePath, String sha1, String downloadURL, AssetType assetType) {
        this.filePath = filePath;
        this.sha1 = sha1;
        this.downloadURL = downloadURL;
        this.assetType = assetType;
    }
}
