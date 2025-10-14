package com.mediaviewer.controller;

import com.mediaviewer.model.MediaFile;
import com.mediaviewer.utils.ThumbnailGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VideoTabController {
    
    @FXML
    private FlowPane videoFlowPane;
    
    private DashboardController dashboardController;
    
    @FXML
    public void initialize() {
        // Initialization code
    }
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void updateVideos(List<MediaFile> videoFiles) {
        videoFlowPane.getChildren().clear();
        
        for (MediaFile mediaFile : videoFiles) {
            VBox videoCard = createVideoCard(mediaFile);
            videoFlowPane.getChildren().add(videoCard);
        }
    }
    
    private VBox createVideoCard(MediaFile mediaFile) {
        VBox card = new VBox(5);
        card.getStyleClass().add("media-card");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Use a default video thumbnail
        javafx.scene.image.Image thumbnail = ThumbnailGenerator.generateVideoThumbnail(150, 150);
        imageView.setImage(thumbnail);
        
        // Make the image clickable to open the file
        imageView.setOnMouseClicked(event -> openFile(mediaFile));
        
        Label nameLabel = new Label(mediaFile.getFileName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        
        card.getChildren().addAll(imageView, nameLabel);
        return card;
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