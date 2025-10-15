package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProjectTabController {
    
    @FXML
    private TableView<MediaFile> projectTableView;
    
    @FXML
    private TableColumn<MediaFile, String> nameColumn;
    
    @FXML
    private TableColumn<MediaFile, String> typeColumn;
    
    @FXML
    private TableColumn<MediaFile, String> sizeColumn;
    
    @FXML
    private TableColumn<MediaFile, String> modifiedColumn;
    
    private DashboardController dashboardController;
    
    @FXML
    public void initialize() {
        // Set up table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        typeColumn.setCellValueFactory(cellData -> {
            String fileType = cellData.getValue().getFileType();
            return new javafx.beans.property.SimpleStringProperty(convertFileTypeToDisplayName(fileType));
        });
        sizeColumn.setCellValueFactory(cellData -> {
            long size = cellData.getValue().getFileSize();
            return new javafx.beans.property.SimpleStringProperty(formatFileSize(size));
        });
        modifiedColumn.setCellValueFactory(cellData -> {
            java.time.LocalDateTime modified = cellData.getValue().getLastModified();
            return new javafx.beans.property.SimpleStringProperty(modified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });
        
        // Make table rows clickable to open files
        projectTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<MediaFile> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    MediaFile mediaFile = row.getItem();
                    openFile(mediaFile);
                }
            });
            
            // Add context menu for additional actions
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem favoriteItem = new MenuItem("Toggle Favorite");
            favoriteItem.setOnAction(event -> {
                MediaFile mediaFile = row.getItem();
                if (mediaFile != null) {
                    mediaFile.setFavorite(!mediaFile.isFavorite());
                    projectTableView.refresh();
                }
            });
            
            MenuItem tagItem = new MenuItem("Add Tag");
            tagItem.setOnAction(event -> {
                MediaFile mediaFile = row.getItem();
                if (mediaFile != null) {
                    showTagDialog(mediaFile);
                }
            });
            
            contextMenu.getItems().addAll(favoriteItem, tagItem);
            row.setContextMenu(contextMenu);
            
            return row;
        });
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateProjects(List<MediaFile> projectFiles) {
        projectTableView.getItems().clear();
        projectTableView.getItems().addAll(projectFiles);
    }
    
    private String convertFileTypeToDisplayName(String fileType) {
        switch (fileType) {
            case "java-project":
                return "Java Project";
            case "javascript-project":
                return "JavaScript Project";
            case "python-project":
                return "Python Project";
            case "gradle-project":
                return "Gradle Project";
            case "intellij-project":
                return "IntelliJ Project";
            case "vscode-project":
                return "VS Code Project";
            case "generic-project":
                return "Generic Project";
            default:
                return fileType;
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    private void openFile(MediaFile mediaFile) {
        try {
            Desktop.getDesktop().open(mediaFile.getFilePath().toFile());
        } catch (IOException e) {
            e.printStackTrace();
            // In a real application, you might want to show an error dialog
        }
    }
    
    private void showTagDialog(MediaFile mediaFile) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Tag");
        dialog.setHeaderText("Add a tag to " + mediaFile.getFileName());
        dialog.setContentText("Tag:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(tag -> {
            mediaFile.addTag(tag);
            // Refresh the dashboard to update the tag panel
            if (dashboardController != null) {
                // We would need to call a method to refresh the tag panel
            }
        });
    }
}