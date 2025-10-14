package com.mediaviewer.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.coobird.thumbnailator.Thumbnails;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ThumbnailGenerator {
    
    public static Image generateThumbnail(File imageFile, int width, int height) {
        try {
            // Use Thumbnailator library for better thumbnail generation
            BufferedImage thumbnail = Thumbnails.of(imageFile)
                .size(width, height)
                .outputQuality(0.8)
                .asBufferedImage();
            
            return SwingFXUtils.toFXImage(thumbnail, null);
        } catch (IOException e) {
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
        // Create a simple colored rectangle as placeholder
        Rectangle rect = new Rectangle(width, height);
        rect.setFill(color);
        return rect.snapshot(null, null);
    }
}