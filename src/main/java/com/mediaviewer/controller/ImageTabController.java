package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.ThumbnailGenerator;
import com.mediaviewer.utils.LargeImageLoader;
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
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Optional;

public class ImageTabController {
    
    private static final Logger LOGGER = Logger.getLogger(ImageTabController.class.getName());
    
    @FXML
    private FlowPane imageFlowPane;
    
    private DashboardController dashboardController;
    private ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2) // Use fewer threads for large images
    );
    
    // Cache for thumbnail futures to avoid duplicate work
    private Map<String, Future<?>> thumbnailCache = new HashMap<>();
    
    // Weak cache for loaded images to allow garbage collection
    private Map<String, Image> imageCache = new WeakHashMap<>();
    
    @FXML
    public void initialize() {
        // Ensure the FlowPane grows to fill available space
        if (imageFlowPane != null) {
            imageFlowPane.setPrefWrapLength(200); // Wrap at 200 pixels
            imageFlowPane.setHgap(10);
            imageFlowPane.setVgap(10);
            // Enable virtualized scrolling for better performance with large datasets
            imageFlowPane.setPrefHeight(600);
        }
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateImages(List<MediaFile> imageFiles) {
        if (imageFlowPane != null) {
            imageFlowPane.getChildren().clear();
            // Clear thumbnail cache for new update
            thumbnailCache.clear();
            
            for (MediaFile mediaFile : imageFiles) {
                // Add placeholder immediately
                VBox card = createPlaceholderCard(mediaFile);
                imageFlowPane.getChildren().add(card);
                
                // Check if we already have this image in cache
                String filePath = mediaFile.getFilePath().toString();
                if (imageCache.containsKey(filePath)) {
                    Image cachedImage = imageCache.get(filePath);
                    Platform.runLater(() -> updateCardWithThumbnail(card, cachedImage, mediaFile));
                } else {
                    // For very large files, use a more conservative approach
                    long fileSize = mediaFile.getFileSize();
                    if (fileSize > 500 * 1024 * 1024) { // Files larger than 500MB
                        // Show a simple placeholder immediately and load thumbnail in background
                        Platform.runLater(() -> updateCardWithThumbnail(card, null, mediaFile));
                    } else {
                        // Load thumbnail in background
                        Future<?> future = thumbnailExecutor.submit(() -> {
                            Image thumbnail = ThumbnailGenerator.generateThumbnail(
                                mediaFile.getFilePath().toFile(), 150, 150);
                            
                            // Cache the image
                            if (thumbnail != null) {
                                imageCache.put(filePath, thumbnail);
                            }
                            
                            // Update UI on JavaFX thread
                            Platform.runLater(() -> updateCardWithThumbnail(card, thumbnail, mediaFile));
                        });
                        
                        // Store future in cache
                        thumbnailCache.put(filePath, future);
                    }
                }
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
        
        // Set a placeholder with better styling
        setPlaceholderImage(imageView);
        
        Label nameLabel = new Label(mediaFile.getFileName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        nameLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Add file size info for large files
        long fileSize = mediaFile.getFileSize();
        if (fileSize > 100 * 1024 * 1024) { // Files larger than 100MB
            Label sizeLabel = new Label(formatFileSize(fileSize));
            sizeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");
            card.getChildren().addAll(imageView, nameLabel, sizeLabel);
        } else {
            card.getChildren().addAll(imageView, nameLabel);
        }
        
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
        // Set a colored background as a placeholder with better styling
        imageView.setStyle("-fx-background-color: linear-gradient(to bottom, #bdc3c7, #95a5a6); -fx-border-color: #7f8c8d; -fx-border-width: 1px; -fx-border-radius: 4px;");
    }
    
    private void updateCardWithThumbnail(VBox card, Image thumbnail, MediaFile mediaFile) {
        if (card.getChildren().size() > 0 && card.getChildren().get(0) instanceof ImageView) {
            ImageView imageView = (ImageView) card.getChildren().get(0);
            
            if (thumbnail != null) {
                imageView.setImage(thumbnail);
                imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 1, 1);"); // Add subtle shadow
            } else {
                // For large files or when thumbnail generation failed, load directly with background loading
                long fileSize = mediaFile.getFileSize();
                if (fileSize > 500 * 1024 * 1024) { // Very large files
                    // Show loading indicator
                    imageView.setStyle("-fx-background-color: linear-gradient(to bottom, #bdc3c7, #95a5a6); -fx-border-color: #7f8c8d; -fx-border-width: 1px; -fx-border-radius: 4px;");
                    
                    // Load the image when clicked, not during thumbnail generation
                    // The click handler will handle loading the full image
                } else {
                    // Try loading directly with background loading
                    try {
                        // Use the new method for scaled image loading
                        Image directImage = ThumbnailGenerator.loadScaledImage(
                            mediaFile.getFilePath().toFile(), 150, 150);
                        
                        if (directImage != null && !directImage.isError()) {
                            imageView.setImage(directImage);
                            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 1, 1);");
                            // Cache the image
                            imageCache.put(mediaFile.getFilePath().toString(), directImage);
                        } else {
                            // Keep placeholder if loading failed
                            LOGGER.log(Level.WARNING, "Failed to load image: " + mediaFile.getFileName());
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to load image: " + mediaFile.getFileName(), e);
                    }
                }
            }
            
            // Make the image clickable to open the file
            imageView.setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    openFile(mediaFile, imageView);
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
    
    private void openFile(MediaFile mediaFile, ImageView imageView) {
        try {
            // For large images, use our custom viewer
            long fileSize = mediaFile.getFileSize();
            if (fileSize > 100 * 1024 * 1024) { // Files larger than 100MB
                // Show in custom large image viewer
                showInLargeImageViewer(mediaFile);
            } else {
                // For smaller files, open directly
                Desktop.getDesktop().open(mediaFile.getFilePath().toFile());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening file: " + mediaFile.getFileName(), e);
            // In a real application, you might want to show an error dialog
        }
    }
    
    private void showInLargeImageViewer(MediaFile mediaFile) {
        try {
            // Load the FXML for the large image viewer
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/largeImageViewer.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get the controller and set up the viewer
            LargeImageViewerController controller = loader.getController();
            Stage viewerStage = new Stage();
            controller.setStage(viewerStage);
            controller.setImageFile(mediaFile.getFilePath().toFile());
            
            // Set up the stage
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
            viewerStage.setTitle("Large Image Viewer - " + mediaFile.getFileName());
            viewerStage.setScene(scene);
            viewerStage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening large image viewer: " + mediaFile.getFileName(), e);
            // Fallback to system viewer
            try {
                Desktop.getDesktop().open(mediaFile.getFilePath().toFile());
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Error opening file in system viewer: " + mediaFile.getFileName(), ioException);
            }
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
    
    // Cleanup method
    public void cleanup() {
        thumbnailExecutor.shutdown();
        thumbnailCache.clear();
        imageCache.clear();
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}