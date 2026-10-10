package com.navidmafi.moolauncher.minecraft.domain;

import java.nio.file.Path;

public class JarAsset {
    public final Path filePath;
    public final String sha1;
    public final String downloadURL;
    public final AssetType assetType;
    public final String groupID;
    public final  boolean skipVerify;
    public JarAsset(Path filePath, String sha1, String downloadURL,
                    AssetType assetType, String groupID) {
        this(filePath, sha1, downloadURL, assetType, groupID, false);
    }
    public JarAsset(Path filePath, String sha1, String downloadURL,
                    AssetType assetType, String groupID, boolean skipVerify) {
        this.filePath = filePath;
        this.sha1 = sha1;
        this.downloadURL = downloadURL;
        this.assetType = assetType;
        this.groupID = groupID;
        this.skipVerify = skipVerify;
    }
}
