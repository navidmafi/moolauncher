package com.navidmafi.moolauncher.minecraft.services;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MavenUrlMapper {
    public static String toMavenUrl(String coords, String baseUrl, String extension) {
        String[] parts = coords.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Maven coordinates: " + coords);
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];

        // Convert groupId to path
        String groupPath = groupId.replace('.', '/');

        // Build the full URL
        return String.format("%s/%s/%s/%s/%s-%s.%s",
                baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl,
                groupPath,
                artifactId,
                version,
                artifactId,
                version,
                extension);
    }
    public static Path toRelativePath(String coords) {
        String[] parts = coords.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Maven coordinates: " + coords);
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];

        // Convert groupId to path elements
        String[] groupParts = groupId.split("\\.");
        Path path = Paths.get("", groupParts)
                .resolve(artifactId)
                .resolve(version)
                .resolve(String.format("%s-%s.jar", artifactId, version));

        return path;
    }
}
