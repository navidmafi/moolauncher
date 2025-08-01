package com.navidmafi.moolauncher.listener;

import javax.swing.*;

public class SwingProgressListener {
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final JButton playButton;
    private final JFrame frame;
    private final JTextField usernameField;
    private final JTextField versionField;

    public SwingProgressListener(
            JFrame frame,
            JProgressBar progressBar,
            JLabel statusLabel,
            JButton playButton,
            JTextField usernameField,
            JTextField versionField
    ) {
        this.frame = frame;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
        this.playButton = playButton;
        this.usernameField = usernameField;
        this.versionField = versionField;
    }

    public void onProgress(int progress, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            statusLabel.setText(message);
        });
    }

    public void onFailure(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            usernameField.setEnabled(true);
            versionField.setEnabled(true);
            playButton.setEnabled(true);
            statusLabel.setText("Failed");
            progressBar.setValue(0);
        });
    }

}
