package com.navidmafi.moolauncher.minecraft.services;

import com.navidmafi.moolauncher.util.OsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StorageService {

    /**
     *  .minecraft
     */
    public static Path getMCDirectory() {
        return Paths.get(".minecraft");
    }

    /**
     * “.minecraft/versions”
     */
    public static Path getVersionsDirectory() {
        Path dir = getMCDirectory().resolve("versions");
        return dir;
    }

    /**
     * “.minecraft/versions/<version>”
     */
    public static Path getVersionDirectory(String version) {
        Path dir = getVersionsDirectory().resolve(version);
        return dir;
    }

    /**
     * “.minecraft/versions/<version>/<version>.json”
     */
    public static Path getVersionJsonPath(String version) {
        Path versionDir = getVersionDirectory(version);
        return versionDir.resolve(version + ".json");
    }

    /**
     * “.minecraft/versions/<version>/<version>.jar”
     */
    public static Path getVersionJarPath(String version){
        Path versionDir = getVersionDirectory(version);
        return versionDir.resolve(version + ".jar");
    }

    /**
     * “.minecraft/libraries”
     */
    public static Path getLibrariesDirectory() {
       return getMCDirectory().resolve("libraries");
    }

    /**
     * “.minecraft/versions/<version>/natives”
     */
    public static Path getNativesDirectory(String version) {
      return getVersionDirectory(version).resolve("natives");
    }

    /**
     * “.minecraft/assets”
     */
    public static Path getAssetsDirectory() {
        Path dir = getMCDirectory().resolve("assets");
        return dir;
    }

    /**
     * “.minecraft/assets/indexes/<version>.json”
     */
    public static Path getAssetIndexPath(String version) {
        Path indexesDir = getAssetsDirectory().resolve("indexes");
        return indexesDir.resolve(version + ".json");
    }

    /**
     * Given the SHA1 hash of an asset, returns:
     *     “.minecraft/assets/objects/<first2chars>/<fullhash>”
     *
     * Make sure parent directories exist before writing.
     */
    public static Path getAssetObjectPath(String hash) throws IOException {
        String prefix = hash.substring(0, 2);
        Path objectsDir = getAssetsDirectory().resolve("objects").resolve(prefix);
        Files.createDirectories(objectsDir);
        return objectsDir.resolve(hash);
    }

    /**
     * “.minecraft/versions/<version>/<version>-client-logging.json”
     * (You could pick any naming convention; this is consistent with what the launcher expects.)
     */
    public static Path getLoggingConfigPath(String version) throws IOException {
        Path versionDir = getVersionDirectory(version);
        return versionDir.resolve(version + "-client-logging.json");
    }

    public static void createDirectories(String version) throws IOException {
        Files.createDirectories(getVersionDirectory(version));
        Files.createDirectories(getLibrariesDirectory());
        Files.createDirectories(getAssetsDirectory().resolve("indexes"));
        Files.createDirectories(getAssetsDirectory().resolve("objects"));
    }


    /**
     * Unzip a ZIP file (zipPath) into outputDir (creating all subdirectories).
     */
    public static void unzip(Path zipPath, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
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

    public static String computeSha1(Path file) throws IOException {
        try (InputStream fis = Files.newInputStream(file);
             DigestInputStream dis = new DigestInputStream(fis, MessageDigest.getInstance("SHA-1"))) {
            // Read the entire file to update the digest
            byte[] buffer = new byte[8 * 1024];
            while (dis.read(buffer) != -1) { /* no-op */ }
            byte[] shaBytes = dis.getMessageDigest().digest();

            // Convert to hex string
            try (Formatter formatter = new Formatter()) {
                for (byte b : shaBytes) {
                    formatter.format("%02x", b);
                }
                return formatter.toString();
            }
        } catch (Exception e) {
            throw new IOException("Could not compute SHA-1", e);
        }
    }
    public static void extractNatives(String version) throws IOException {
        System.out.println("[*] Extracting native libraries for version " + version);

        Path nativesDir = getNativesDirectory(version);
        Path librariesDir = getLibrariesDirectory();

        // Walk through every file under “.minecraft/libraries” looking for
        // *.jar files that end in “natives-<osName>.jar”.
        Files.walk(librariesDir)
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(OsUtils.getNativeClassifier() + ".jar");
                })
                .forEach(p -> {
                    try {
                        unzip(p, nativesDir);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });

        System.out.println("[*] Native extraction done.");
    }
}
