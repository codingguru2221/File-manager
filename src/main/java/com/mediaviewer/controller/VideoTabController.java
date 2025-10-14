package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.ThumbnailGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Optional;

public class VideoTabController {
    
    private static final Logger LOGGER = Logger.getLogger(VideoTabController.class.getName());
    
    @FXML
    private FlowPane videoFlowPane;
    
    private DashboardController dashboardController;
    private ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(4);
    
    @FXML
    public void initialize() {
        // Ensure the FlowPane grows to fill available space
        if (videoFlowPane != null) {
            videoFlowPane.setPrefWrapLength(200); // Wrap at 200 pixels
            videoFlowPane.setHgap(10);
            videoFlowPane.setVgap(10);
        }
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateVideos(List<MediaFile> videoFiles) {
        if (videoFlowPane != null) {
            videoFlowPane.getChildren().clear();
            
            for (MediaFile mediaFile : videoFiles) {
                // Add placeholder immediately
                VBox card = createPlaceholderCard(mediaFile);
                videoFlowPane.getChildren().add(card);
                
                // Load thumbnail in background
                thumbnailExecutor.submit(() -> {
                    Image thumbnail = ThumbnailGenerator.generateVideoThumbnail(150, 150);
                    
                    // Update UI on JavaFX thread
                    Platform.runLater(() -> updateCardWithThumbnail(card, thumbnail, mediaFile));
                });
            }
        }
    }
    
    private VBox createPlaceholderCard(MediaFile mediaFile) {
        VBox card = new VBox();
        card.getStyleClass().add("media-card");
        card.setSpacing(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setMinWidth(170);
        card.setMinHeight(200);
        card.setPrefWidth(170);
        card.setPrefHeight(200);
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Set a default video placeholder
        setVideoPlaceholder(imageView);
        
        Label nameLabel = new Label(mediaFile.getFileName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        nameLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        card.getChildren().addAll(imageView, nameLabel);
        
        // Make right-click show context menu
        imageView.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                showContextMenu(imageView, mediaFile, card);
            }
        });
        
        return card;
    }
    
    private void setVideoPlaceholder(ImageView imageView) {
        // Set a colored background as a video placeholder
        imageView.setStyle("-fx-background-color: #e74c3c; -fx-border-color: #c0392b; -fx-border-width: 2px;");
    }
    
    private void updateCardWithThumbnail(VBox card, Image thumbnail, MediaFile mediaFile) {
        if (card.getChildren().size() > 0 && card.getChildren().get(0) instanceof ImageView) {
            ImageView imageView = (ImageView) card.getChildren().get(0);
            
            if (thumbnail != null) {
                imageView.setImage(thumbnail);
                imageView.setStyle(""); // Remove placeholder styling
            }
            
            // Make the image clickable to open the file
            imageView.setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    openFile(mediaFile);
                } else if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    showContextMenu(imageView, mediaFile, card);
                }
            });
        }
    }
    
    private void showContextMenu(ImageView imageView, MediaFile mediaFile, VBox card) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem favoriteItem = new MenuItem("Toggle Favorite");
        favoriteItem.setOnAction(event -> {
            mediaFile.setFavorite(!mediaFile.isFavorite());
            // Update UI to reflect favorite status
            if (mediaFile.isFavorite()) {
                card.setStyle("-fx-border-color: gold; -fx-border-width: 2px;");
            } else {
                card.setStyle("");
            }
        });
        
        MenuItem tagItem = new MenuItem("Add Tag");
        tagItem.setOnAction(event -> showTagDialog(mediaFile));
        
        contextMenu.getItems().addAll(favoriteItem, tagItem);
        contextMenu.show(imageView, javafx.geometry.Side.BOTTOM, 0, 0);
    }
    
    private void openFile(MediaFile mediaFile) {
        try {
            Desktop.getDesktop().open(mediaFile.getFilePath().toFile());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening file: " + mediaFile.getFileName(), e);
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
            // In a real application, you might want to refresh the tag panel
        });
    }
}