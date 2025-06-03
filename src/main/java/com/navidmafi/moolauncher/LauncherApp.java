package com.navidmafi.moolauncher;

import com.formdev.flatlaf.FlatLightLaf;
import com.navidmafi.moolauncher.components.TextPrompt;
import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.config.Storage;
import com.navidmafi.moolauncher.minecraft.ClientLauncher;
import com.navidmafi.moolauncher.minecraft.Installer;
import com.navidmafi.moolauncher.minecraft.LaunchConfig;
import com.navidmafi.moolauncher.minecraft.storage.MCStorage;
import com.navidmafi.moolauncher.util.UIProgressListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class LauncherApp {

    private JFrame frame;
    private JTextField usernameField;
    private TextPrompt usernamePrompt;
    private JTextField versionField;
    private TextPrompt versionPrompt;
    private JButton playButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public static void main(String[] args) {
        System.setProperty("flatlaf.uiScale", "1.5");

        FlatLightLaf.install();  // you can also use FlatDarkLaf, etc.
        SwingUtilities.invokeLater(() -> {
            new LauncherApp().createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        // 3) Create main frame
        frame = new JFrame("MooLauncher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = new Dimension(400, 400);
        frame.setSize(dimension);
        frame.setMinimumSize(dimension);
        frame.setMaximumSize(dimension);
        frame.setLocationRelativeTo(null); // center on screen


        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        frame.setContentPane(content);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try (InputStream imgStream = getClass().getResourceAsStream("/cow.png")) {
            Image logoImage = ImageIO.read(imgStream);
            frame.setIconImage(logoImage);
            Image scaled = logoImage.getScaledInstance(200, 50, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } catch (IOException e) {
            e.printStackTrace();
        }

        content.add(imageLabel, BorderLayout.NORTH);


        // --- Center panel: inputs + button
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;


        usernameField = new JTextField();
        usernamePrompt = new TextPrompt("Username", usernameField);
        usernamePrompt.changeAlpha(0.5f);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(usernameField, gbc);


        versionField = new JTextField();
        versionPrompt = new TextPrompt("Version", versionField);
        versionPrompt.changeAlpha(0.5f);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        centerPanel.add(versionField, gbc);

        // Play button (spans all columns)
        playButton = new JButton("Play");
        playButton.setBackground(new Color(39, 23, 231));
        playButton.setForeground(Color.WHITE);
        playButton.setBorder(null);
        playButton.setPreferredSize(new Dimension(100, 50));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        centerPanel.add(playButton, gbc);

        content.add(centerPanel, BorderLayout.CENTER);

        // --- South panel: progress bar + status label
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout(6, 6));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        southPanel.add(progressBar, BorderLayout.NORTH);

        statusLabel = new JLabel("Idle");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        content.add(southPanel, BorderLayout.SOUTH);

        playButton.addActionListener(this::onPlayClicked);

        Config config = Storage.readConfig();
        usernameField.setText(config.username);
        versionField.setText(config.version);

        frame.setVisible(true);
    }

    private void onPlayClicked(ActionEvent e) {
        String username = usernameField.getText().trim();
        String version = versionField.getText().trim();

        if (username.isEmpty() || version.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please enter both Username and Version.",
                    "Missing Input",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        usernameField.setEnabled(false);
        versionField.setEnabled(false);
        playButton.setEnabled(false);

        Config config = Storage.readConfig();
        LaunchConfig launchConfig = new LaunchConfig(
                MCStorage.getLibrariesDirectory(),
                MCStorage.getNativesDirectory(version),
                MCStorage.getAssetsDirectory(),
                MCStorage.getVersionDirectory(version),
                MCStorage.getMCDirectory(),
                username,
                version
        );


        try {
            Storage.saveConfig(new Config(username, version, config.installed_versions));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        progressBar.setValue(0);

        UIProgressListener progressListener = new UIProgressListener() {
            @Override
            public void onProgress(int progress, String progressMessage) {
                progressBar.setValue(progress);
                statusLabel.setText(progressMessage);
            }

            @Override
            public void onFailure(String errorMessage) {

                JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }

            @Override
            public void onFinished() {
                try {
                    ClientLauncher.Launch(launchConfig);
                } catch (Exception e){
                    this.onFailure(e.getMessage());
                }
            }
        };
        if (Arrays.asList(config.installed_versions).contains(version)) {
            System.out.println("Version is already installed");
            try {
                ClientLauncher.Launch(launchConfig);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

        }

        if (!Arrays.asList(config.installed_versions).contains(version)) {
            System.out.println("Version not installed. Installing version " + version);
            try {
                Installer.SetupVersion(version, progressListener);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }


    }
}
