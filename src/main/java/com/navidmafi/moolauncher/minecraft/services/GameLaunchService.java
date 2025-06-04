package com.navidmafi.moolauncher.minecraft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.listener.GameListener;
import com.navidmafi.moolauncher.minecraft.domain.AssetType;
import com.navidmafi.moolauncher.minecraft.domain.JarAsset;
import com.navidmafi.moolauncher.util.OsUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameLaunchService {

    // Must use absolute paths for everything otherwise game might not launch or create nested dirs like .minecraft/.minecraft
    public static void launch(Config config, GameListener gameListener) throws IOException, InterruptedException {
        JsonMapper mapper = new JsonMapper();
        List<String> classpathEntries = new ArrayList<>();

        String mainClass = "net.minecraft.client.main.Main";
        Path mcDir = StorageService.getMCDirectory();
        Path assetsDir = StorageService.getAssetsDirectory();
        Path versionDir = StorageService.getVersionDirectory(config.version);
        Path nativesDir = StorageService.getNativesDirectory(config.version);
        if (config.useFabric) {
            String loaderFullVersionString = "fabric-loader-" + config.fabricVersion + "-" + config.version;
            String fabricManifest = Files.readString(
                    StorageService.getVersionJsonPath(loaderFullVersionString)
            );
            JsonNode fabricManifestNode = mapper.readTree(fabricManifest);
            for (JarAsset lib : FabricService.GetFabricLibs(fabricManifestNode)){
                System.out.println("Adding fabric lib " + lib.filePath + " to classpath");
                classpathEntries.add(lib.filePath.toAbsolutePath().toString());
            }
            for (JarAsset jar : FabricService.GetFabricMainJars(fabricManifestNode)){
                System.out.println("Adding fabric jar " + jar.filePath + " to classpath");
                classpathEntries.add(jar.filePath.toAbsolutePath().toString());
            }

            mainClass = fabricManifestNode.path("launcherMeta").path("mainClass").path("client").asText();

        }



        Path versionManifestPath = StorageService.getVersionJsonPath(config.version);
        String versionManifest = Files.readString(versionManifestPath);
       JsonNode versionManifestNode = mapper.readTree(versionManifest);
       for (JarAsset lib : LibraryService.GetCompatibleLibs(versionManifestNode)){
           if (lib.assetType != AssetType.NON_NATIVE_LIBRARY) continue;
           classpathEntries.add(lib.filePath.toAbsolutePath().toString());
       }

        Path versionJarPath = versionDir.resolve(config.version + ".jar");
        classpathEntries.add(versionJarPath.toAbsolutePath().toString());

        String classpath = String.join(File.pathSeparator, classpathEntries);

        List<String> javaArgs = new ArrayList<>();
        javaArgs.add("java");
        javaArgs.add("-Xmx" + config.heapSize);
        javaArgs.add("-Djava.library.path=" + nativesDir.toAbsolutePath().normalize().toString());

        javaArgs.add("-cp");
        javaArgs.add(classpath);

        javaArgs.add(mainClass);
        System.out.println("Launching " + mainClass);

        javaArgs.addAll(List.of(
                "--username", config.username,
                "--version", config.version,
                "--gameDir", mcDir.toAbsolutePath().normalize().toString(),
                "--assetIndex", config.version,
                "--assetsDir", assetsDir.toAbsolutePath().normalize().toString(),
                "--accessToken", "0",
                "--uuid", "00000000-0000-0000-0000-000000000000",
                "--userType", "legacy"
        ));

        if (config.useFabric) {
            javaArgs.add("--versionType");
            javaArgs.add("fabric");
        }

        ProcessBuilder pb = new ProcessBuilder(javaArgs);
        pb.directory(mcDir.toFile());
        pb.inheritIO(); // forward stdout/stderr to current console
        Process proc = pb.start();

//        gameListener.onLaunch();

        int exitCode = proc.waitFor();
        System.out.println("[*] Minecraft exited with code " + exitCode);
        gameListener.onExit(exitCode);

    }

}
