package com.mediaviewer.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.coobird.thumbnailator.Thumbnails;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThumbnailGenerator {
    
    // Thread pool for thumbnail generation to prevent blocking UI
    private static final ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );
    
    public static CompletableFuture<Image> generateThumbnailAsync(File imageFile, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For very large files, we need to be more careful about memory usage
                long fileSize = imageFile.length();
                
                // If file is larger than 100MB, use more aggressive compression
                double quality = fileSize > 100 * 1024 * 1024 ? 0.5 : 0.7;
                
                // Use Thumbnailator library for better thumbnail generation
                BufferedImage thumbnail = Thumbnails.of(imageFile)
                    .size(width, height)
                    .outputQuality(quality) // Adjust quality based on file size
                    .useExifOrientation(true) // Handle image orientation
                    .asBufferedImage();
                
                return SwingFXUtils.toFXImage(thumbnail, null);
            } catch (IOException e) {
                // Return null if thumbnail generation fails
                return null;
            } catch (OutOfMemoryError e) {
                // If we run out of memory, try with even lower quality
                try {
                    BufferedImage thumbnail = Thumbnails.of(imageFile)
                        .size(width, height)
                        .outputQuality(0.3) // Very low quality to prevent OOM
                        .useExifOrientation(true)
                        .asBufferedImage();
                    
                    return SwingFXUtils.toFXImage(thumbnail, null);
                } catch (Exception ex) {
                    return null;
                }
            }
        }, thumbnailExecutor);
    }
    
    public static Image generateThumbnail(File imageFile, int width, int height) {
        try {
            // For very large files, we need to be more careful about memory usage
            long fileSize = imageFile.length();
            
            // If file is larger than 100MB, use more aggressive compression
            double quality = fileSize > 100 * 1024 * 1024 ? 0.5 : 0.7;
            
            // Use Thumbnailator library for better thumbnail generation
            BufferedImage thumbnail = Thumbnails.of(imageFile)
                .size(width, height)
                .outputQuality(quality) // Adjust quality based on file size
                .useExifOrientation(true) // Handle image orientation
                .asBufferedImage();
            
            return SwingFXUtils.toFXImage(thumbnail, null);
        } catch (IOException e) {
            // Return null if thumbnail generation fails
            return null;
        } catch (OutOfMemoryError e) {
            // If we run out of memory, try with even lower quality
            try {
                BufferedImage thumbnail = Thumbnails.of(imageFile)
                    .size(width, height)
                    .outputQuality(0.3) // Very low quality to prevent OOM
                    .useExifOrientation(true)
                    .asBufferedImage();
                
                return SwingFXUtils.toFXImage(thumbnail, null);
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    // Method to load full-size image with proper scaling for display
    public static Image loadScaledImage(File imageFile, int maxWidth, int maxHeight) {
        try {
            // Load image with background loading and scaling
            return new Image(imageFile.toURI().toString(), 
                           maxWidth, maxHeight, 
                           true,  // preserveRatio
                           true,  // smooth
                           true); // backgroundLoading
        } catch (Exception e) {
            return null;
        }
    }
    
    public static CompletableFuture<Image> generateVideoThumbnailAsync(int width, int height) {
        return CompletableFuture.supplyAsync(() -> createDefaultIcon(width, height, Color.RED), 
                                           thumbnailExecutor);
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
    
    // Shutdown method for cleanup
    public static void shutdown() {
        thumbnailExecutor.shutdown();
    }
}