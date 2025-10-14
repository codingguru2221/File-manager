package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoTabController {
    
    private static final Logger LOGGER = Logger.getLogger(VideoTabController.class.getName());
    
    @FXML
    private FlowPane videoFlowPane;
    
    private DashboardController dashboardController;
    
    @FXML
    public void initialize() {
        // Ensure the FlowPane grows to fill available space
        if (videoFlowPane != null) {
            videoFlowPane.setPrefWrapLength(200); // Wrap at 200 pixels
        }
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateVideos(List<MediaFile> videoFiles) {
        if (videoFlowPane != null) {
            videoFlowPane.getChildren().clear();
            
            for (MediaFile mediaFile : videoFiles) {
                VBox videoCard = createVideoCard(mediaFile);
                videoFlowPane.getChildren().add(videoCard);
            }
        }
    }
    
    private VBox createVideoCard(MediaFile mediaFile) {
        VBox card = new VBox(5);
        card.getStyleClass().add("media-card");
        
        // Ensure the card can grow and shrink as needed
        VBox.setVgrow(card, Priority.NEVER);
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Set a default video placeholder
        setVideoPlaceholder(imageView);
        
        // Make the image clickable to open the file
        imageView.setOnMouseClicked(event -> openFile(mediaFile));
        
        Label nameLabel = new Label(mediaFile.getFileName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        
        card.getChildren().addAll(imageView, nameLabel);
        return card;
    }
    
    private void setVideoPlaceholder(ImageView imageView) {
        // Set a colored background as a video placeholder
        imageView.setStyle("-fx-background-color: #e74c3c; -fx-border-color: #c0392b; -fx-border-width: 2px;");
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