package com.navidmafi.moolauncher;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Bootstrap {
    public static void main(String[] args) {
        // 1) Configure L&F
        System.setProperty("flatlaf.uiScale", "1.5");
        FlatLightLaf.install();
        SwingUtilities.invokeLater(LauncherApp::new);
    }
}
