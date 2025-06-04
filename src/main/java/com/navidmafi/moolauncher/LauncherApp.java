package com.navidmafi.moolauncher;

import com.navidmafi.moolauncher.components.TextPrompt;
import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.config.Storage;
import com.navidmafi.moolauncher.listener.GameListener;
import com.navidmafi.moolauncher.listener.InstallListener;
import com.navidmafi.moolauncher.listener.SwingProgressListener;
import com.navidmafi.moolauncher.minecraft.services.GameLaunchService;
import com.navidmafi.moolauncher.minecraft.services.InstallationService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

public class LauncherApp {


    private JFrame frame;
    private JTextField usernameField;
    private TextPrompt usernamePrompt;
    private JTextField versionField;
    private TextPrompt versionPrompt;
    private JTextField heapSizeField;
    private TextPrompt heapSizePrompt;
    private JCheckBox useFabricCheckBox;
    private JTextField fabricVersionField;
    private TextPrompt fabricVersionPrompt;
    private JButton playButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Config config;
    private Storage storage = new Storage();


    public LauncherApp() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        this.config = storage.readConfig();
        initFrame();
        initComponents();
        layoutComponents();
        attachListeners();

        frame.setVisible(true);
    }


    private void initFrame() {
        frame = new JFrame("MooLauncher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension dimension = new Dimension(400, 450);
        frame.setSize(dimension);
        frame.setMinimumSize(dimension);
        frame.setMaximumSize(dimension);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void initComponents() {
        usernameField = new JTextField();
        usernamePrompt = new TextPrompt("Username", usernameField);
        usernamePrompt.changeAlpha(0.5f);

        versionField = new JTextField();
        versionPrompt = new TextPrompt("Version", versionField);
        versionPrompt.changeAlpha(0.5f);

        heapSizeField = new JTextField();
        heapSizePrompt = new TextPrompt("Heap size", heapSizeField);
        heapSizePrompt.changeAlpha(0.5f);

        useFabricCheckBox = new JCheckBox("Fabric");
        fabricVersionField = new JTextField();
        fabricVersionPrompt = new TextPrompt("Fabric Version", fabricVersionField);
        fabricVersionPrompt.changeAlpha(0.5f);

        playButton = new JButton("Play");
        playButton.setBackground(new Color(39, 23, 231));
        playButton.setForeground(Color.WHITE);
        playButton.setBorder(null);
        playButton.setPreferredSize(new Dimension(100, 50));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        statusLabel = new JLabel("Idle");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        usernameField.setText(config.username);
        versionField.setText(config.version);
        heapSizeField.setText(config.heapSize);
        useFabricCheckBox.setSelected(config.useFabric);
        fabricVersionField.setText(config.fabricVersion);
    }

    private void layoutComponents() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        frame.setContentPane(content);

        // ----- NORTH: logo image
        JLabel imageLabel = loadLogoLabel();
        content.add(imageLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;


        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(usernameField, gbc);

        gbc.gridx = 1;
        centerPanel.add(versionField, gbc);


        gbc.gridx =0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        centerPanel.add(heapSizeField, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        centerPanel.add(useFabricCheckBox, gbc);
        gbc.gridx = 1;

        centerPanel.add(fabricVersionField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        centerPanel.add(playButton, gbc);
        content.add(centerPanel, BorderLayout.CENTER);

        // ----- SOUTH: progress + status
        JPanel southPanel = new JPanel(new BorderLayout(6, 6));
        southPanel.add(progressBar, BorderLayout.NORTH);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        content.add(southPanel, BorderLayout.SOUTH);
    }


    private JLabel loadLogoLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        try (InputStream imgStream = getClass().getResourceAsStream("/cow.png")) {
            Image logoImage = ImageIO.read(imgStream);
            frame.setIconImage(logoImage);
            Image scaled = logoImage.getScaledInstance(200, 50, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Cow is essential for our functionality
        }
        return label;
    }


    private void attachListeners() {
        playButton.addActionListener(this::onPlayClicked);
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

        this.config.username = username;
        this.config.version = version;
        this.config.heapSize = heapSizeField.getText();
        this.config.fabricVersion = fabricVersionField.getText();
        this.config.useFabric = useFabricCheckBox.isSelected();
        storage.saveConfig(config);
        SwingProgressListener swingProgressListener = new SwingProgressListener(
                frame,
                progressBar,
                statusLabel,
                playButton,
                usernameField,
                versionField
        );

        var gameListener = new GameListener() {
            @Override
            public void onExit(int exitCode) {
                usernameField.setEnabled(true);
                versionField.setEnabled(true);
                playButton.setEnabled(true);
                swingProgressListener.onProgress(0,"Game exited with code " + exitCode);
            }
        };
        var installListener = new InstallListener() {
            public void onInstall() {
                try {
                    swingProgressListener.onProgress(100,"Welcome to Minecraft.");
                    GameLaunchService.launch(config,gameListener);
                } catch (Exception e) {
                    swingProgressListener.onFailure(e.getMessage());
                }
            }
        };

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    InstallationService.installVersion(config ,swingProgressListener,installListener);
                } catch (Exception ex) {
                    swingProgressListener.onFailure(ex.getMessage());
                }
                return null;
            }
        }.execute();

    }
}
