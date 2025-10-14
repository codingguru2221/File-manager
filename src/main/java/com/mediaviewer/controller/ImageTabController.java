package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageTabController {
    
    private static final Logger LOGGER = Logger.getLogger(ImageTabController.class.getName());
    
    @FXML
    private FlowPane imageFlowPane;
    
    private DashboardController dashboardController;
    
    @FXML
    public void initialize() {
        // Ensure the FlowPane grows to fill available space
        if (imageFlowPane != null) {
            imageFlowPane.setPrefWrapLength(200); // Wrap at 200 pixels
        }
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateImages(List<MediaFile> imageFiles) {
        if (imageFlowPane != null) {
            imageFlowPane.getChildren().clear();
            
            for (MediaFile mediaFile : imageFiles) {
                VBox imageCard = createImageCard(mediaFile);
                imageFlowPane.getChildren().add(imageCard);
            }
        }
    }
    
    private VBox createImageCard(MediaFile mediaFile) {
        VBox card = new VBox(5);
        card.getStyleClass().add("media-card");
        
        // Ensure the card can grow and shrink as needed
        VBox.setVgrow(card, Priority.NEVER);
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Try to load the image
        File imageFile = mediaFile.getFilePath().toFile();
        loadImage(imageView, imageFile);
        
        // Make the image clickable to open the file
        imageView.setOnMouseClicked(event -> openFile(mediaFile));
        
        Label nameLabel = new Label(mediaFile.getFileName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        
        card.getChildren().addAll(imageView, nameLabel);
        return card;
    }
    
    private void loadImage(ImageView imageView, File imageFile) {
        try {
            // Create an image from the file URL
            String imageUrl = imageFile.toURI().toURL().toString();
            
            // Create image with background loading and error handling
            Image image = new Image(imageUrl, 150, 150, true, true, true);
            
            // Set the image immediately
            imageView.setImage(image);
            
            // Check if there was an error loading the image
            if (image.isError()) {
                LOGGER.log(Level.WARNING, "Failed to load image: " + imageFile.getName());
                setDefaultPlaceholder(imageView);
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Invalid URL for image file: " + imageFile.getName(), e);
            setDefaultPlaceholder(imageView);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading image: " + imageFile.getName(), e);
            setDefaultPlaceholder(imageView);
        }
    }
    
    private void setDefaultPlaceholder(ImageView imageView) {
        // Set a colored background as a placeholder with a border
        imageView.setStyle("-fx-background-color: #3498db; -fx-border-color: #2980b9; -fx-border-width: 2px;");
    }
    
    private void openFile(MediaFile mediaFile) {
        try {
            Desktop.getDesktop().open(mediaFile.getFilePath().toFile());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening file: " + mediaFile.getFileName(), e);
            // In a real application, you might want to show an error dialog
        }
    }
}