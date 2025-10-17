package com.mediaviewer.controller;

import com.mediaviewer.utils.LargeImageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Point2D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LargeImageViewerController {
    private static final Logger LOGGER = Logger.getLogger(LargeImageViewerController.class.getName());
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private StackPane imageContainer;
    
    @FXML
    private ProgressBar loadingProgressBar;
    
    @FXML
    private Label statusLabel;
    
    private Stage stage;
    private File imageFile;
    private Image loadedImage;
    
    // Zoom properties
    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
    private double mouseX;
    private double mouseY;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
        loadAndDisplayImage();
    }
    
    private void loadAndDisplayImage() {
        if (imageFile == null) return;
        
        // Show loading indicator
        loadingProgressBar.setVisible(true);
        statusLabel.setText("Loading " + imageFile.getName() + "...");
        
        // Load image asynchronously
        CompletableFuture<Image> imageFuture = LargeImageLoader.loadLargeImageAsync(
            imageFile, 
            1920, // Max width
            1080  // Max height
        );
        
        imageFuture.thenAccept(image -> {
            javafx.application.Platform.runLater(() -> {
                loadingProgressBar.setVisible(false);
                
                if (image != null) {
                    this.loadedImage = image;
                    imageView.setImage(image);
                    statusLabel.setText(imageFile.getName() + " (" + 
                                      (int)image.getWidth() + "x" + (int)image.getHeight() + ")");
                    
                    // Set up zoom functionality
                    setupZoomFunctionality();
                } else {
                    statusLabel.setText("Failed to load image: " + imageFile.getName());
                }
            });
        }).exceptionally(throwable -> {
            javafx.application.Platform.runLater(() -> {
                loadingProgressBar.setVisible(false);
                statusLabel.setText("Error loading image: " + imageFile.getName());
                LOGGER.log(Level.SEVERE, "Error loading image", throwable);
            });
            return null;
        });
    }
    
    private void setupZoomFunctionality() {
        // Handle scroll for zoom
        imageView.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
        
        // Handle drag for panning
        imageView.setOnMousePressed(this::handleMousePressed);
        imageView.setOnMouseDragged(this::handleMouseDragged);
        
        // Reset zoom on double click
        imageView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                resetZoom();
            }
        });
    }
    
    private void handleScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        double scale = delta > 0 ? 1.1 : 0.9;
        
        // Calculate new zoom factor
        double newZoomFactor = zoomFactor.get() * scale;
        
        // Limit zoom range
        if (newZoomFactor < 0.1) newZoomFactor = 0.1;
        if (newZoomFactor > 10.0) newZoomFactor = 10.0;
        
        zoomFactor.set(newZoomFactor);
        
        // Apply zoom
        imageView.setScaleX(zoomFactor.get());
        imageView.setScaleY(zoomFactor.get());
        
        event.consume();
    }
    
    private void handleMousePressed(MouseEvent event) {
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();
    }
    
    private void handleMouseDragged(MouseEvent event) {
        if (zoomFactor.get() > 1.0) {
            // Only pan when zoomed in
            double deltaX = event.getSceneX() - mouseX;
            double deltaY = event.getSceneY() - mouseY;
            
            // Apply panning
            imageView.setTranslateX(imageView.getTranslateX() + deltaX);
            imageView.setTranslateY(imageView.getTranslateY() + deltaY);
            
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        }
    }
    
    private void resetZoom() {
        zoomFactor.set(1.0);
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
    }
    
    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
    
    @FXML
    private void handleZoomIn() {
        double newZoomFactor = zoomFactor.get() * 1.5;
        if (newZoomFactor > 10.0) newZoomFactor = 10.0;
        zoomFactor.set(newZoomFactor);
        imageView.setScaleX(zoomFactor.get());
        imageView.setScaleY(zoomFactor.get());
    }
    
    @FXML
    private void handleZoomOut() {
        double newZoomFactor = zoomFactor.get() * 0.75;
        if (newZoomFactor < 0.1) newZoomFactor = 0.1;
        zoomFactor.set(newZoomFactor);
        imageView.setScaleX(zoomFactor.get());
        imageView.setScaleY(zoomFactor.get());
    }
    
    @FXML
    private void handleResetView() {
        resetZoom();
    }
}