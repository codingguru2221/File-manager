package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.FileScanner;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class DashboardController {
    
    @FXML
    private Button selectFolderButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private VBox folderInfoBox;
    
    @FXML
    private Label folderNameLabel;
    
    @FXML
    private Label folderPathLabel;
    
    @FXML
    private Label folderSizeLabel;
    
    @FXML
    private Label imageCountLabel;
    
    @FXML
    private Label videoCountLabel;
    
    @FXML
    private Label documentCountLabel;
    
    @FXML
    private Label imageSizeLabel;
    
    @FXML
    private Label videoSizeLabel;
    
    @FXML
    private Label documentSizeLabel;
    
    @FXML
    private ProgressBar scanProgressBar;
    
    @FXML
    private Label progressLabel;
    
    @FXML
    private TabPane tabPane;
    
    // References to the tab controllers through fx:id
    @FXML
    private ImageTabController imageTabController;
    
    @FXML
    private VideoTabController videoTabController;
    
    @FXML
    private DocumentTabController documentTabController;
    
    private FileScanner fileScanner;
    private Stage primaryStage;
    private File currentFolder;
    
    @FXML
    public void initialize() {
        fileScanner = new FileScanner();
        
        // Set up event handlers
        selectFolderButton.setOnAction(event -> selectFolder());
        
        // Initialize with zero counts
        updateCounts();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    private void selectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Scan");
        
        if (primaryStage != null) {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                currentFolder = selectedDirectory;
                scanFolder(selectedDirectory);
            }
        }
    }
    
    private void scanFolder(File directory) {
        scanProgressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressLabel.setText("Scanning files...");
        
        // Run scanning in a background thread
        new Thread(() -> {
            try {
                fileScanner.scanDirectory(directory.getAbsolutePath(), count -> {
                    // Update progress on UI thread
                    javafx.application.Platform.runLater(() -> {
                        progressLabel.setText("Scanned " + count + " files...");
                    });
                });
                
                // Update UI when scanning is complete
                javafx.application.Platform.runLater(this::onScanComplete);
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    progressLabel.setText("Error scanning folder: " + e.getMessage());
                    scanProgressBar.setVisible(false);
                    progressLabel.setVisible(false);
                });
            }
        }).start();
    }
    
    private void onScanComplete() {
        scanProgressBar.setVisible(false);
        progressLabel.setVisible(false);
        
        updateFolderInfo();
        updateCounts();
        updateTabs();
    }
    
    private void updateFolderInfo() {
        if (currentFolder != null) {
            folderInfoBox.setVisible(true);
            folderNameLabel.setText("Folder: " + currentFolder.getName());
            folderPathLabel.setText("Location: " + currentFolder.getAbsolutePath());
            
            // Calculate total size
            long totalSize = fileScanner.getTotalFilesSize();
            folderSizeLabel.setText("Total Size: " + formatFileSize(totalSize));
        } else {
            folderInfoBox.setVisible(false);
        }
    }
    
    private void updateCounts() {
        imageCountLabel.setText(String.valueOf(fileScanner.getImageFilesCount()));
        videoCountLabel.setText(String.valueOf(fileScanner.getVideoFilesCount()));
        documentCountLabel.setText(String.valueOf(fileScanner.getDocumentFilesCount()));
        
        // Update size labels
        imageSizeLabel.setText(formatFileSize(fileScanner.getImageFilesSize()));
        videoSizeLabel.setText(formatFileSize(fileScanner.getVideoFilesSize()));
        documentSizeLabel.setText(formatFileSize(fileScanner.getDocumentFilesSize()));
    }
    
    private void updateTabs() {
        // Update each tab with the scanned data
        if (imageTabController != null) {
            imageTabController.updateImages(fileScanner.getImageFiles());
        }
        if (videoTabController != null) {
            videoTabController.updateVideos(fileScanner.getVideoFiles());
        }
        if (documentTabController != null) {
            documentTabController.updateDocuments(fileScanner.getDocumentFiles());
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    public List<MediaFile> getImageFiles() {
        return fileScanner.getImageFiles();
    }
    
    public List<MediaFile> getVideoFiles() {
        return fileScanner.getVideoFiles();
    }
    
    public List<MediaFile> getDocumentFiles() {
        return fileScanner.getDocumentFiles();
    }
}