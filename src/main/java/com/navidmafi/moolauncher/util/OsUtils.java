package com.navidmafi.moolauncher.util;

public class OsUtils {
    public enum OSType {
        WINDOWS, LINUX, OSX, UNKNOWN
    }

    private static final String osName = System.getProperty("os.name").toLowerCase();

    public static OSType getOSType() {
        if (osName.contains("win")) return OSType.WINDOWS;
        if (osName.contains("mac")) return OSType.OSX;
        if (osName.contains("nux") || osName.contains("nix")) return OSType.LINUX;
        return OSType.UNKNOWN;
    }


    public static String getNativeClassifier() {
        return switch (getOSType()) {
            case WINDOWS -> "natives-windows";
            case LINUX   -> "natives-linux";
            case OSX     -> "natives-osx";
            default      -> "natives-unknown";
        };
    }

    public static String getMojangOsName() {
        return switch (getOSType()) {
            case WINDOWS -> "windows";
            case LINUX   -> "linux";
            case OSX     -> "osx";
            default      -> "unknown";
        };
    }
}
