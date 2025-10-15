package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.FileScanner;
import com.mediaviewer.utils.ProjectExport;
import com.mediaviewer.utils.ProjectTemplateManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

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
    private Label projectCountLabel;
    
    @FXML
    private Label folderCountLabel; // New label for folder count
    
    @FXML
    private Label imageSizeLabel;
    
    @FXML
    private Label videoSizeLabel;
    
    @FXML
    private Label documentSizeLabel;
    
    @FXML
    private Label projectSizeLabel;
    
    @FXML
    private Label folderSizeLabelUI; // New label for folder size (this was the issue)
    
    @FXML
    private ProgressBar scanProgressBar;
    
    @FXML
    private Label progressLabel;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private ComboBox<String> fileTypeFilter;
    
    @FXML
    private ComboBox<String> dateRangeFilter;
    
    @FXML
    private Slider sizeFilter;
    
    @FXML
    private FlowPane tagPanel;
    
    // References to the tab controllers through fx:include
    @FXML
    private ImageTabController imageTabController;
    
    @FXML
    private VideoTabController videoTabController;
    
    @FXML
    private DocumentTabController documentTabController;
    
    @FXML
    private ProjectTabController projectTabController;
    
    private FileScanner fileScanner;
    private ProjectTemplateManager templateManager;
    private Stage primaryStage;
    private File currentFolder;
    
    @FXML
    public void initialize() {
        fileScanner = new FileScanner();
        templateManager = new ProjectTemplateManager();
        
        // Set up event handlers
        selectFolderButton.setOnAction(event -> selectFolder());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        
        // Initialize filter components
        setupFilters();
        
        // Initialize with zero counts
        updateCounts();
    }
    
    private void setupFilters() {
        fileTypeFilter.getItems().addAll("All", "Images", "Videos", "Documents", "Projects", "Folders");
        dateRangeFilter.getItems().addAll("All Time", "Today", "This Week", "This Month");
        
        fileTypeFilter.setOnAction(e -> applyFilters());
        dateRangeFilter.setOnAction(e -> applyFilters());
        
        if (sizeFilter != null) {
            sizeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
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
        updateTagPanel();
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
        projectCountLabel.setText(String.valueOf(fileScanner.getProjectFilesCount()));
        folderCountLabel.setText(String.valueOf(fileScanner.getNormalFoldersCount())); // Set folder count
        
        // Update size labels
        imageSizeLabel.setText(formatFileSize(fileScanner.getImageFilesSize()));
        videoSizeLabel.setText(formatFileSize(fileScanner.getVideoFilesSize()));
        documentSizeLabel.setText(formatFileSize(fileScanner.getDocumentFilesSize()));
        projectSizeLabel.setText(formatFileSize(fileScanner.getProjectFilesSize()));
        folderSizeLabelUI.setText(formatFileSize(fileScanner.getNormalFoldersSize())); // Set folder size
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
        if (projectTabController != null) {
            projectTabController.updateProjects(fileScanner.getProjectFiles());
            
            // Set the dashboard controller for the project tab
            projectTabController.setDashboardController(this);
        }
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        
        if (query.isEmpty()) {
            // If search is empty, show all files
            updateTabs();
            return;
        }
        
        // Filter files based on multiple criteria
        List<MediaFile> filteredImages = fileScanner.getImageFiles().stream()
            .filter(file -> file.getFileName().toLowerCase().contains(query) || 
                           file.getExtension().toLowerCase().contains(query) ||
                           formatFileSize(file.getFileSize()).contains(query))
            .collect(Collectors.toList());
        
        List<MediaFile> filteredVideos = fileScanner.getVideoFiles().stream()
            .filter(file -> file.getFileName().toLowerCase().contains(query) || 
                           file.getExtension().toLowerCase().contains(query) ||
                           formatFileSize(file.getFileSize()).contains(query))
            .collect(Collectors.toList());
        
        List<MediaFile> filteredDocuments = fileScanner.getDocumentFiles().stream()
            .filter(file -> file.getFileName().toLowerCase().contains(query) || 
                           file.getExtension().toLowerCase().contains(query) ||
                           formatFileSize(file.getFileSize()).contains(query))
            .collect(Collectors.toList());
        
        List<MediaFile> filteredProjects = fileScanner.getProjectFiles().stream()
            .filter(file -> file.getFileName().toLowerCase().contains(query) || 
                           file.getFileType().toLowerCase().contains(query) ||
                           formatFileSize(file.getFileSize()).contains(query))
            .collect(Collectors.toList());
        
        // Update tabs with filtered results
        if (imageTabController != null) {
            imageTabController.updateImages(filteredImages);
        }
        if (videoTabController != null) {
            videoTabController.updateVideos(filteredVideos);
        }
        if (documentTabController != null) {
            documentTabController.updateDocuments(filteredDocuments);
        }
        if (projectTabController != null) {
            projectTabController.updateProjects(filteredProjects);
        }
    }
    
    private void applyFilters() {
        String fileType = fileTypeFilter.getValue();
        String dateRange = dateRangeFilter.getValue();
        
        List<MediaFile> filteredImages = new ArrayList<>(fileScanner.getImageFiles());
        List<MediaFile> filteredVideos = new ArrayList<>(fileScanner.getVideoFiles());
        List<MediaFile> filteredDocuments = new ArrayList<>(fileScanner.getDocumentFiles());
        List<MediaFile> filteredProjects = new ArrayList<>(fileScanner.getProjectFiles());
        List<MediaFile> filteredFolders = new ArrayList<>(fileScanner.getNormalFolders()); // Get normal folders
        
        // Apply file type filter
        if (fileType != null && !fileType.equals("All")) {
            switch (fileType) {
                case "Images":
                    filteredVideos.clear();
                    filteredDocuments.clear();
                    filteredProjects.clear();
                    filteredFolders.clear();
                    break;
                case "Videos":
                    filteredImages.clear();
                    filteredDocuments.clear();
                    filteredProjects.clear();
                    filteredFolders.clear();
                    break;
                case "Documents":
                    filteredImages.clear();
                    filteredVideos.clear();
                    filteredProjects.clear();
                    filteredFolders.clear();
                    break;
                case "Projects":
                    filteredImages.clear();
                    filteredVideos.clear();
                    filteredDocuments.clear();
                    filteredFolders.clear();
                    break;
                case "Folders":
                    filteredImages.clear();
                    filteredVideos.clear();
                    filteredDocuments.clear();
                    filteredProjects.clear();
                    break;
            }
        }
        
        // Apply date range filter
        if (dateRange != null && !dateRange.equals("All Time")) {
            LocalDateTime filterDate = calculateFilterDate(dateRange);
            
            filteredImages = filteredImages.stream()
                .filter(file -> file.getLastModified().isAfter(filterDate))
                .collect(Collectors.toList());
                
            filteredVideos = filteredVideos.stream()
                .filter(file -> file.getLastModified().isAfter(filterDate))
                .collect(Collectors.toList());
                
            filteredDocuments = filteredDocuments.stream()
                .filter(file -> file.getLastModified().isAfter(filterDate))
                .collect(Collectors.toList());
                
            filteredProjects = filteredProjects.stream()
                .filter(file -> file.getLastModified().isAfter(filterDate))
                .collect(Collectors.toList());
                
            filteredFolders = filteredFolders.stream()
                .filter(file -> file.getLastModified().isAfter(filterDate))
                .collect(Collectors.toList());
        }
        
        // Update tabs with filtered results
        if (imageTabController != null) {
            imageTabController.updateImages(filteredImages);
        }
        if (videoTabController != null) {
            videoTabController.updateVideos(filteredVideos);
        }
        if (documentTabController != null) {
            documentTabController.updateDocuments(filteredDocuments);
        }
        if (projectTabController != null) {
            projectTabController.updateProjects(filteredProjects);
        }
    }
    
    private LocalDateTime calculateFilterDate(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (dateRange) {
            case "Today":
                return now.minusDays(1);
            case "This Week":
                return now.minusWeeks(1);
            case "This Month":
                return now.minusMonths(1);
            default:
                return now.minusYears(100); // Effectively no filter
        }
    }
    
    private void updateTagPanel() {
        if (tagPanel == null) return;
        
        tagPanel.getChildren().clear();
        
        // Collect all unique tags
        List<String> allTags = new ArrayList<>();
        Stream.of(fileScanner.getImageFiles(), fileScanner.getVideoFiles(), fileScanner.getDocumentFiles(), fileScanner.getProjectFiles(), fileScanner.getNormalFolders())
            .flatMap(List::stream)
            .forEach(file -> allTags.addAll(file.getTags()));
        
        // Create tag buttons
        for (String tag : allTags) {
            Button tagButton = new Button(tag);
            tagButton.setOnAction(e -> filterByTag(tag));
            tagPanel.getChildren().add(tagButton);
        }
    }
    
    private void filterByTag(String tag) {
        List<MediaFile> filteredImages = fileScanner.getImageFiles().stream()
            .filter(file -> file.getTags().contains(tag))
            .collect(Collectors.toList());
            
        List<MediaFile> filteredVideos = fileScanner.getVideoFiles().stream()
            .filter(file -> file.getTags().contains(tag))
            .collect(Collectors.toList());
            
        List<MediaFile> filteredDocuments = fileScanner.getDocumentFiles().stream()
            .filter(file -> file.getTags().contains(tag))
            .collect(Collectors.toList());
            
        List<MediaFile> filteredProjects = fileScanner.getProjectFiles().stream()
            .filter(file -> file.getTags().contains(tag))
            .collect(Collectors.toList());
            
        List<MediaFile> filteredFolders = fileScanner.getNormalFolders().stream()
            .filter(file -> file.getTags().contains(tag))
            .collect(Collectors.toList());
        
        // Update tabs with filtered results
        if (imageTabController != null) {
            imageTabController.updateImages(filteredImages);
        }
        if (videoTabController != null) {
            videoTabController.updateVideos(filteredVideos);
        }
        if (documentTabController != null) {
            documentTabController.updateDocuments(filteredDocuments);
        }
        if (projectTabController != null) {
            projectTabController.updateProjects(filteredProjects);
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
    
    public List<MediaFile> getProjectFiles() {
        return fileScanner.getProjectFiles();
    }
    
    public List<MediaFile> getNormalFolders() {
        return fileScanner.getNormalFolders();
    }
    
    @FXML
    private void exportFileList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("media_files.csv");
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            exportToCSV(file);
        }
    }
    
    @FXML
    private void exportProjects() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("projects.csv");
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            String fileName = file.getName().toLowerCase();
            try {
                if (fileName.endsWith(".csv")) {
                    ProjectExport.exportProjectsToCSV(fileScanner.getProjectFiles(), file.getAbsolutePath());
                } else if (fileName.endsWith(".json")) {
                    ProjectExport.exportProjectsToJSON(fileScanner.getProjectFiles(), file.getAbsolutePath());
                }
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Projects exported successfully to " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to export projects: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Write CSV header
            writer.println("Name,Type,Extension,Size,Modified,Favorite,Tags");
            
            // Write all files
            Stream.of(fileScanner.getImageFiles(), 
                      fileScanner.getVideoFiles(), 
                      fileScanner.getDocumentFiles(),
                      fileScanner.getProjectFiles(),
                      fileScanner.getNormalFolders())
                .flatMap(List::stream)
                .forEach(mediaFile -> {
                    String tags = String.join(";", mediaFile.getTags());
                    writer.printf("\"%s\",%s,%s,%d,\"%s\",%s,\"%s\"%n", 
                        mediaFile.getFileName().replace("\"", "\"\""), 
                        mediaFile.getFileType(), 
                        mediaFile.getExtension(),
                        mediaFile.getFileSize(), 
                        mediaFile.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        mediaFile.isFavorite() ? "Yes" : "No",
                        tags.replace("\"", "\"\""));
                });
        } catch (Exception e) {
            // In a real application, you might want to show an error dialog
            e.printStackTrace();
        }
    }
}