package com.mediaviewer.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;

public class ThumbnailGenerator {
    
    public static Image generateThumbnail(File imageFile, int width, int height) {
        try {
            // For now, we'll create a placeholder thumbnail
            // In a full implementation, we would use a library like Thumbnailator
            String imagePath = imageFile.toURI().toString();
            Image image = new Image(imagePath, width, height, true, true, true);
            
            // If the image failed to load, return null to indicate failure
            if (image.isError()) {
                return null;
            }
            
            return image;
        } catch (Exception e) {
            // Return null if thumbnail generation fails
            return null;
        }
    }
    
    public static Image generateVideoThumbnail(int width, int height) {
        // Return a default video icon
        return createDefaultIcon(width, height, Color.RED);
    }
    
    public static Image generateDocumentThumbnail(int width, int height) {
        // Return a default document icon
        return createDefaultIcon(width, height, Color.ORANGE);
    }
    
    private static Image createDefaultIcon(int width, int height, Color color) {
        // Create a simple colored rectangle as a placeholder
        // In a real implementation, you would load actual icon images
        try {
            // Create a 1x1 pixel image as a placeholder
            String imageData = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
            return new Image("data:image/png;base64," + imageData);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void setDefaultImage(ImageView imageView, String fileType) {
        // Set a default icon based on file type
        Color color;
        switch (fileType.toLowerCase()) {
            case "image":
                color = Color.BLUE;
                break;
            case "video":
                color = Color.RED;
                break;
            case "document":
                color = Color.ORANGE;
                break;
            default:
                color = Color.GRAY;
                break;
        }
        
        // Create a colored rectangle as a placeholder
        Rectangle rect = new Rectangle(150, 150, color);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(1);
        
        // Since we can't directly set a Shape to an ImageView, 
        // we'll just style the ImageView itself
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setStyle("-fx-background-color: " + toHex(color) + ";");
    }
    
    private static String toHex(Color color) {
        return String.format("#%02x%02x%02x", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
    }
}