package com.navidmafi.moolauncher.minecraft;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ClientLauncher {
    public static void Launch(LaunchConfig config) throws IOException, InterruptedException {
        List<String> classpathEntries = new ArrayList<>();
        Files.walk(config.librariesDir)
                .filter(p -> p.toString().endsWith(".jar"))
                .forEach(p -> classpathEntries.add(p.toString()));
        Path versionJarPath = config.versionDir.resolve(config.version + ".jar");
        classpathEntries.add(versionJarPath.toString());
        String classpath = String.join(File.pathSeparator, classpathEntries);

        List<String> javaArgs = new ArrayList<>();
        javaArgs.add("java");
        javaArgs.add("-Xmx" + "3G");
        javaArgs.add("-Djava.library.path=" + config.nativesDir.toString());
        javaArgs.add("-cp");
        javaArgs.add(classpath);
        javaArgs.add("net.minecraft.client.main.Main");
        javaArgs.addAll(List.of(
                "--username", config.username,
                "--version",  config.version,
                "--gameDir",  config.mcDir.toString(),
                "--assetsDir", config.assetsDir.toString(),
                "--accessToken", "0",
                "--uuid",    "00000000-0000-0000-0000-000000000000",
                "--userType", "legacy"
        ));

        ProcessBuilder pb = new ProcessBuilder(javaArgs);
        pb.directory(config.mcDir.toFile());
        pb.inheritIO();
        Process proc = pb.start();
        int exitCode = proc.waitFor();
        System.out.println("[*] Minecraft exited with code " + exitCode);
        System.exit(exitCode);
    }
}