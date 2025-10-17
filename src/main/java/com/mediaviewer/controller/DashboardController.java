package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.FileScanner;
import com.mediaviewer.utils.ProjectExport;
import com.mediaviewer.utils.ProjectTemplateManager;
import com.mediaviewer.utils.ThumbnailGenerator;
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
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    
    // Executor for background tasks
    private ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2);
    
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
        CompletableFuture.runAsync(() -> {
            try {
                fileScanner.scanDirectory(directory.getAbsolutePath(), count -> {
                    // Update progress on UI thread less frequently to reduce UI updates
                    if (count % 100 == 0) {
                        javafx.application.Platform.runLater(() -> {
                            progressLabel.setText("Scanned " + count + " files...");
                        });
                    }
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
        }, backgroundExecutor);
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
        // Update each tab with the scanned data using background processing
        CompletableFuture.runAsync(() -> {
            List<MediaFile> images = fileScanner.getImageFiles();
            List<MediaFile> videos = fileScanner.getVideoFiles();
            List<MediaFile> documents = fileScanner.getDocumentFiles();
            List<MediaFile> projects = fileScanner.getProjectFiles();
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                if (imageTabController != null) {
                    imageTabController.updateImages(images);
                }
                if (videoTabController != null) {
                    videoTabController.updateVideos(videos);
                }
                if (documentTabController != null) {
                    documentTabController.updateDocuments(documents);
                }
                if (projectTabController != null) {
                    projectTabController.updateProjects(projects);
                    
                    // Set the dashboard controller for the project tab
                    projectTabController.setDashboardController(this);
                }
            });
        }, backgroundExecutor);
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        
        if (query.isEmpty()) {
            // If search is empty, show all files
            updateTabs();
            return;
        }
        
        // Filter files based on multiple criteria in background
        CompletableFuture.runAsync(() -> {
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
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
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
            });
        }, backgroundExecutor);
    }
    
    private void applyFilters() {
        String fileType = fileTypeFilter.getValue();
        String dateRange = dateRangeFilter.getValue();
        
        // Apply filters in background
        CompletableFuture.runAsync(() -> {
            List<MediaFile> initialImages = new ArrayList<>(fileScanner.getImageFiles());
            List<MediaFile> initialVideos = new ArrayList<>(fileScanner.getVideoFiles());
            List<MediaFile> initialDocuments = new ArrayList<>(fileScanner.getDocumentFiles());
            List<MediaFile> initialProjects = new ArrayList<>(fileScanner.getProjectFiles());
            List<MediaFile> initialFolders = new ArrayList<>(fileScanner.getNormalFolders()); // Get normal folders
            
            List<MediaFile> filteredImages = initialImages;
            List<MediaFile> filteredVideos = initialVideos;
            List<MediaFile> filteredDocuments = initialDocuments;
            List<MediaFile> filteredProjects = initialProjects;
            List<MediaFile> filteredFolders = initialFolders;
            
            // Apply file type filter
            if (fileType != null && !fileType.equals("All")) {
                switch (fileType) {
                    case "Images":
                        filteredVideos = new ArrayList<>();
                        filteredDocuments = new ArrayList<>();
                        filteredProjects = new ArrayList<>();
                        filteredFolders = new ArrayList<>();
                        break;
                    case "Videos":
                        filteredImages = new ArrayList<>();
                        filteredDocuments = new ArrayList<>();
                        filteredProjects = new ArrayList<>();
                        filteredFolders = new ArrayList<>();
                        break;
                    case "Documents":
                        filteredImages = new ArrayList<>();
                        filteredVideos = new ArrayList<>();
                        filteredProjects = new ArrayList<>();
                        filteredFolders = new ArrayList<>();
                        break;
                    case "Projects":
                        filteredImages = new ArrayList<>();
                        filteredVideos = new ArrayList<>();
                        filteredDocuments = new ArrayList<>();
                        filteredFolders = new ArrayList<>();
                        break;
                    case "Folders":
                        filteredImages = new ArrayList<>();
                        filteredVideos = new ArrayList<>();
                        filteredDocuments = new ArrayList<>();
                        filteredProjects = new ArrayList<>();
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
            
            // Store final results in final variables for lambda
            final List<MediaFile> finalImages = filteredImages;
            final List<MediaFile> finalVideos = filteredVideos;
            final List<MediaFile> finalDocuments = filteredDocuments;
            final List<MediaFile> finalProjects = filteredProjects;
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                // Update tabs with filtered results
                if (imageTabController != null) {
                    imageTabController.updateImages(finalImages);
                }
                if (videoTabController != null) {
                    videoTabController.updateVideos(finalVideos);
                }
                if (documentTabController != null) {
                    documentTabController.updateDocuments(finalDocuments);
                }
                if (projectTabController != null) {
                    projectTabController.updateProjects(finalProjects);
                }
            });
        }, backgroundExecutor);
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
        
        // Collect all tags from all files in background
        CompletableFuture.runAsync(() -> {
            Set<String> allTags = Stream.of(
                fileScanner.getImageFiles().stream(),
                fileScanner.getVideoFiles().stream(),
                fileScanner.getDocumentFiles().stream(),
                fileScanner.getProjectFiles().stream(),
                fileScanner.getNormalFolders().stream()
            )
            .flatMap(s -> s)
            .flatMap(file -> file.getTags().stream())
            .collect(Collectors.toSet());
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                for (String tag : allTags) {
                    Button tagButton = new Button(tag);
                    tagButton.setOnAction(e -> filterByTag(tag));
                    tagPanel.getChildren().add(tagButton);
                }
            });
        }, backgroundExecutor);
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
            } catch (IOException e) {
                e.printStackTrace();
                // In a real application, you might want to show an error dialog
            }
        }
    }
    
    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Write CSV header
            writer.println("Name,Type,Size,Modified Date");
            
            // Write image files
            for (MediaFile fileMedia : fileScanner.getImageFiles()) {
                writer.printf("%s,%s,%d,%s%n", 
                    fileMedia.getFileName(), 
                    fileMedia.getFileType(), 
                    fileMedia.getFileSize(),
                    fileMedia.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // Write video files
            for (MediaFile fileMedia : fileScanner.getVideoFiles()) {
                writer.printf("%s,%s,%d,%s%n", 
                    fileMedia.getFileName(), 
                    fileMedia.getFileType(), 
                    fileMedia.getFileSize(),
                    fileMedia.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // Write document files
            for (MediaFile fileMedia : fileScanner.getDocumentFiles()) {
                writer.printf("%s,%s,%d,%s%n", 
                    fileMedia.getFileName(), 
                    fileMedia.getFileType(), 
                    fileMedia.getFileSize(),
                    fileMedia.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // Write project files
            for (MediaFile fileMedia : fileScanner.getProjectFiles()) {
                writer.printf("%s,%s,%d,%s%n", 
                    fileMedia.getFileName(), 
                    fileMedia.getFileType(), 
                    fileMedia.getFileSize(),
                    fileMedia.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        } catch (IOException e) {
            e.printStackTrace();
            // In a real application, you might want to show an error dialog
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    // Cleanup method
    public void cleanup() {
        backgroundExecutor.shutdown();
        if (imageTabController != null) imageTabController.cleanup();
        if (videoTabController != null) videoTabController.cleanup();
        ThumbnailGenerator.shutdown();
    }
}