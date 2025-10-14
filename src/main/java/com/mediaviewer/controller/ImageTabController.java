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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Optional;

public class ImageTabController {
    
    private static final Logger LOGGER = Logger.getLogger(ImageTabController.class.getName());
    
    @FXML
    private FlowPane imageFlowPane;
    
    private DashboardController dashboardController;
    private ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(4);
    
    @FXML
    public void initialize() {
        // Ensure the FlowPane grows to fill available space
        if (imageFlowPane != null) {
            imageFlowPane.setPrefWrapLength(200); // Wrap at 200 pixels
            imageFlowPane.setHgap(10);
            imageFlowPane.setVgap(10);
        }
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateImages(List<MediaFile> imageFiles) {
        if (imageFlowPane != null) {
            imageFlowPane.getChildren().clear();
            
            for (MediaFile mediaFile : imageFiles) {
                // Add placeholder immediately
                VBox card = createPlaceholderCard(mediaFile);
                imageFlowPane.getChildren().add(card);
                
                // Load thumbnail in background
                thumbnailExecutor.submit(() -> {
                    Image thumbnail = ThumbnailGenerator.generateThumbnail(
                        mediaFile.getFilePath().toFile(), 150, 150);
                    
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
        
        // Set a placeholder
        setPlaceholderImage(imageView);
        
        Label nameLabel = new Label(mediaFile.getFileName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        nameLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        card.getChildren().addAll(imageView, nameLabel);
        
        // Add context menu
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
        
        // Make right-click show context menu
        imageView.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
            }
        });
        
        return card;
    }
    
    private void setPlaceholderImage(ImageView imageView) {
        // Set a colored background as a placeholder
        imageView.setStyle("-fx-background-color: #bdc3c7; -fx-border-color: #95a5a6; -fx-border-width: 1px;");
    }
    
    private void updateCardWithThumbnail(VBox card, Image thumbnail, MediaFile mediaFile) {
        if (card.getChildren().size() > 0 && card.getChildren().get(0) instanceof ImageView) {
            ImageView imageView = (ImageView) card.getChildren().get(0);
            
            if (thumbnail != null) {
                imageView.setImage(thumbnail);
                imageView.setStyle(""); // Remove placeholder styling
            } else {
                // If thumbnail generation failed, try loading directly
                try {
                    Image directImage = new Image(mediaFile.getFilePath().toUri().toString(), 
                                                150, 150, true, true, true);
                    if (!directImage.isError()) {
                        imageView.setImage(directImage);
                        imageView.setStyle(""); // Remove placeholder styling
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to load image: " + mediaFile.getFileName(), e);
                }
            }
            
            // Make the image clickable to open the file
            imageView.setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    openFile(mediaFile);
                } else if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    // Show context menu
                    ContextMenu contextMenu = new ContextMenu();
                    
                    MenuItem favoriteItem = new MenuItem("Toggle Favorite");
                    favoriteItem.setOnAction(e -> {
                        mediaFile.setFavorite(!mediaFile.isFavorite());
                        // Update UI to reflect favorite status
                        if (mediaFile.isFavorite()) {
                            card.setStyle("-fx-border-color: gold; -fx-border-width: 2px;");
                        } else {
                            card.setStyle("");
                        }
                    });
                    
                    MenuItem tagItem = new MenuItem("Add Tag");
                    tagItem.setOnAction(e -> showTagDialog(mediaFile));
                    
                    contextMenu.getItems().addAll(favoriteItem, tagItem);
                    contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
                }
            });
        }
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