package com.navidmafi.moolauncher.minecraft.services;

import com.navidmafi.moolauncher.listener.GameListener;
import com.navidmafi.moolauncher.minecraft.model.LaunchConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameLaunchService {

    public static void launch(LaunchConfig config, GameListener gameListener) throws IOException, InterruptedException {
        List<String> classpathEntries = new ArrayList<>();
        Path mcDir = StorageService.getMCDirectory();
        Path assetsDir = StorageService.getAssetsDirectory();
        Path librariesDir = StorageService.getLibrariesDirectory();
        Path versionDir = StorageService.getVersionDirectory(config.version);
        Path nativesDir = StorageService.getNativesDirectory(config.version);

        Files.walk(librariesDir)
                .filter(p -> p.toString().endsWith(".jar"))
                .forEach(p -> classpathEntries.add(p.toAbsolutePath().toString()));

        Path versionJarPath = versionDir.resolve(config.version + ".jar");
        classpathEntries.add(versionJarPath.toAbsolutePath().toString());

        String classpath = String.join(File.pathSeparator, classpathEntries);
        System.out.println("Classpath:\n" + classpath);

        List<String> javaArgs = new ArrayList<>();
        javaArgs.add("java");
        javaArgs.add("-Xmx3G");
        javaArgs.add("-Djava.library.path=" + nativesDir.toString());
        javaArgs.add("-cp");
        javaArgs.add(classpath);
        javaArgs.add("net.minecraft.client.main.Main");
        javaArgs.addAll(List.of(
                "--username",   config.username,
                "--version",    config.version,
                "--gameDir",    mcDir.toString(),
                "--assetsDir",  assetsDir.toString(),
                "--accessToken","0",
                "--uuid",       "00000000-0000-0000-0000-000000000000",
                "--userType",   "legacy"
        ));

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
