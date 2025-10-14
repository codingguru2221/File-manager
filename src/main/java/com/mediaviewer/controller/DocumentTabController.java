package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.ThumbnailGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DocumentTabController {
    
    @FXML
    private TableView<MediaFile> documentTableView;
    
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
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        sizeColumn.setCellValueFactory(cellData -> {
            long size = cellData.getValue().getFileSize();
            return new javafx.beans.property.SimpleStringProperty(formatFileSize(size));
        });
        modifiedColumn.setCellValueFactory(cellData -> {
            java.time.LocalDateTime modified = cellData.getValue().getLastModified();
            return new javafx.beans.property.SimpleStringProperty(modified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });
        
        // Make table rows clickable to open files
        documentTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<MediaFile> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    MediaFile mediaFile = row.getItem();
                    openFile(mediaFile);
                }
            });
            return row;
        });
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateDocuments(List<MediaFile> documentFiles) {
        documentTableView.getItems().clear();
        documentTableView.getItems().addAll(documentFiles);
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
}