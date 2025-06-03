package com.navidmafi.moolauncher.minecraft.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MCStorage {

    public static Path getMCDirectory() {
        return Paths.get(".minecraft");
    }

    public static Path getVersionsDirectory() {
        return getMCDirectory().resolve("versions");
    }

    public static Path getVersionDirectory(String version) {
        return getVersionsDirectory().resolve(version);
    }

    public static Path getVersionJsonPath(String version) {
        return getVersionsDirectory().resolve(version + ".json");
    }

    public static Path getVersionJarPath(String version) {
        return getVersionDirectory(version).resolve(version + ".jar");
    }

    public static Path getLibrariesDirectory() {
        return getMCDirectory().resolve("libraries");
    }

    public static Path getNativesDirectory(String version) {
        // Natives are version specific it seems
        return getVersionDirectory(version).resolve("natives");
    }

    public static Path getAssetsDirectory() {
        return getMCDirectory().resolve("assets");
    }



    public static void unzip(Path zipPath, Path outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path outPath = outputDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        Files.copy(is, outPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    public static void extractNatives(String version) throws IOException {
        System.out.println("[*] Extracting native libraries for version " + version);

        Files.walk(getLibrariesDirectory())
                .filter(p -> p.getFileName().toString().endsWith("natives-linux.jar"))
                .forEach(p -> {
                    try {
                        unzip(p, getNativesDirectory(version));
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
        System.out.println("[*] Native extraction done.");
    }
}