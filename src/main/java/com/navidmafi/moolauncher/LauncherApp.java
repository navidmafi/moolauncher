package com.navidmafi.moolauncher;

import com.navidmafi.moolauncher.components.TextPrompt;
import com.navidmafi.moolauncher.config.Config;
import com.navidmafi.moolauncher.config.Storage;
import com.navidmafi.moolauncher.listener.GameListener;
import com.navidmafi.moolauncher.listener.InstallListener;
import com.navidmafi.moolauncher.listener.SwingProgressListener;
import com.navidmafi.moolauncher.minecraft.api.VersionApi;
import com.navidmafi.moolauncher.minecraft.services.FabricService;
import com.navidmafi.moolauncher.minecraft.services.GameLaunchService;
import com.navidmafi.moolauncher.minecraft.services.InstallationService;
import com.navidmafi.moolauncher.minecraft.services.StorageService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class LauncherApp {


    private JFrame frame;
    private JTextField usernameField;
    private TextPrompt usernamePrompt;
    private JComboBox<String> versionComboBox;
    private JTextField heapSizeField;
    private TextPrompt heapSizePrompt;
    private JCheckBox useFabricCheckBox;
    private JCheckBox launchOfflineCheckBox;
    private JComboBox<String> fabricVersionComboBox;
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

        versionComboBox = new JComboBox<>();
        versionComboBox.setEditable(true);

        heapSizeField = new JTextField();
        heapSizePrompt = new TextPrompt("Heap size", heapSizeField);
        heapSizePrompt.changeAlpha(0.5f);

        useFabricCheckBox = new JCheckBox("Fabric");
        launchOfflineCheckBox = new JCheckBox("Offline");
        fabricVersionComboBox = new JComboBox<>();
        fabricVersionComboBox.setEditable(true);

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
        if (config.version != null && !config.version.isBlank()) {
            versionComboBox.addItem(config.version);
        }
        versionComboBox.setSelectedItem(config.version);
        heapSizeField.setText(config.heapSize);
        useFabricCheckBox.setSelected(config.useFabric);
        launchOfflineCheckBox.setSelected(config.launchOffline);
        if (config.fabricVersion != null && !config.fabricVersion.isBlank()) {
            fabricVersionComboBox.addItem(config.fabricVersion);
        }
        fabricVersionComboBox.setSelectedItem(config.fabricVersion);

        loadVersionList();
        loadFabricVersionList(config.version);
    }

    private void loadVersionList() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return VersionApi.getVersionIds();
            }

            @Override
            protected void done() {
                try {
                    List<String> versions = get();
                    String current = (String) versionComboBox.getEditor().getItem();
                    versionComboBox.removeAllItems();
                    for (String v : versions) {
                        versionComboBox.addItem(v);
                    }
                    if (current != null && !current.isBlank()) {
                        versionComboBox.setSelectedItem(current);
                    }
                } catch (Exception e) {
                    System.out.println("[Launcher] Could not fetch version list: " + e);
                    statusLabel.setText("Version fetch failed");
                    launchOfflineCheckBox.setSelected(true);

                    String current = (String) versionComboBox.getEditor().getItem();
                    versionComboBox.removeAllItems();
                    for (String v : StorageService.listInstalledVersions()) {
                        versionComboBox.addItem(v);
                    }
                    if (current != null && !current.isBlank()) {
                        versionComboBox.setSelectedItem(current);
                    }
                }
            }
        }.execute();
    }

    private void loadFabricVersionList(String mcVersion) {
        if (mcVersion == null || mcVersion.isBlank()) return;
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return FabricService.GetLoaderVersions(mcVersion);
            }

            @Override
            protected void done() {
                try {
                    List<String> loaderVersions = get();
                    String current = (String) fabricVersionComboBox.getEditor().getItem();
                    fabricVersionComboBox.removeAllItems();
                    for (String v : loaderVersions) {
                        fabricVersionComboBox.addItem(v);
                    }
                    if (current != null && !current.isBlank()) {
                        fabricVersionComboBox.setSelectedItem(current);
                    }
                } catch (Exception e) {
                    System.out.println("[Launcher] Could not fetch fabric loader list for " + mcVersion + ": " + e);
                    statusLabel.setText("Version fetch failed");
                    launchOfflineCheckBox.setSelected(true);

                    String current = (String) fabricVersionComboBox.getEditor().getItem();
                    fabricVersionComboBox.removeAllItems();
                    for (String v : StorageService.listInstalledFabricLoaderVersions(mcVersion)) {
                        fabricVersionComboBox.addItem(v);
                    }
                    if (current != null && !current.isBlank()) {
                        fabricVersionComboBox.setSelectedItem(current);
                    }
                }
            }
        }.execute();
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
        centerPanel.add(versionComboBox, gbc);


        gbc.gridx =0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        centerPanel.add(heapSizeField, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        centerPanel.add(useFabricCheckBox, gbc);
        gbc.gridx = 1;

        centerPanel.add(fabricVersionComboBox, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        centerPanel.add(launchOfflineCheckBox, gbc);
        gbc.gridx = 1;



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
        versionComboBox.addActionListener(e -> {
            String selectedVersion = (String) versionComboBox.getEditor().getItem();
            loadFabricVersionList(selectedVersion == null ? null : selectedVersion.trim());
        });
    }


    private void onPlayClicked(ActionEvent e) {
        String username = usernameField.getText().trim();
        Object versionItem = versionComboBox.getEditor().getItem();
        String version = versionItem == null ? "" : versionItem.toString().trim();

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
        versionComboBox.setEnabled(false);
        playButton.setEnabled(false);

        Object fabricVersionItem = fabricVersionComboBox.getEditor().getItem();
        String fabricVersion = fabricVersionItem == null ? "" : fabricVersionItem.toString().trim();

        this.config.username = username;
        this.config.version = version;
        this.config.heapSize = heapSizeField.getText();
        this.config.fabricVersion = fabricVersion;
        this.config.useFabric = useFabricCheckBox.isSelected();
        this.config.launchOffline = launchOfflineCheckBox.isSelected();
        storage.saveConfig(config);
        SwingProgressListener swingProgressListener = new SwingProgressListener(
                frame,
                progressBar,
                statusLabel,
                playButton,
                usernameField,
                versionComboBox
        );

        swingProgressListener.onProgress(0,"Loading...");


        var gameListener = new GameListener() {
            @Override
            public void onExit(int exitCode) {
                usernameField.setEnabled(true);
                versionComboBox.setEnabled(true);
                playButton.setEnabled(true);
                swingProgressListener.onProgress(0,"Game exited with code " + exitCode);
            }


            @Override
            public void onFailure(String errMessage) {
                swingProgressListener.onFailure(errMessage);
            }

            @Override
            public void onLaunch() {
                usernameField.setEnabled(false);
                versionComboBox.setEnabled(false);
                playButton.setEnabled(false);
                swingProgressListener.onProgress(100,"Welcome to Minecraft.");
            }
        };
        var installListener = new InstallListener() {
            @Override
            public void onFinished() {
                try {
                    GameLaunchService.launch(config,gameListener);
                } catch (Exception e) {
                    swingProgressListener.onFailure(e.toString());
                }
            }

            @Override
            public void onFailure(String errMessage) {
                swingProgressListener.onFailure(errMessage);
            }

            @Override
            public void onProgress(int Progress, String Message) {
                swingProgressListener.onProgress(Progress,Message);
            }

        };

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (config.launchOffline) {
                        System.out.println("[Launcher] Offline Launch Requested");
                        GameLaunchService.launch(config,gameListener);
                    } else{
                        System.out.println("[Launcher] Install Requested");
                        InstallationService.installVersion(config ,installListener);
                    }
                } catch (Exception ex) {
                    swingProgressListener.onFailure(ex.toString());
                }
                return null;
            }
        }.execute();;

    }
}
