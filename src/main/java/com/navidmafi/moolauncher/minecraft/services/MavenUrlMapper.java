package com.navidmafi.moolauncher.minecraft.services;

public class MavenUrlMapper {
    public static String toMavenUrl(String coords, String baseUrl) {
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
        return String.format("%s/%s/%s/%s/%s-%s.jar",
                baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl,
                groupPath,
                artifactId,
                version,
                artifactId,
                version);
    }

    public static void main(String[] args) {
        String coords = "net.fabricmc:intermediary:1.14.3-pre2";
        String baseUrl = "https://maven.fabricmc.net";

        String downloadUrl = toMavenUrl(coords, baseUrl);
        System.out.println(downloadUrl);
    }
}
