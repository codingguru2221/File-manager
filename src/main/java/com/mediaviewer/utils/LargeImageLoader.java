package com.mediaviewer.utils;

import javafx.scene.image.Image;
import javafx.concurrent.Task;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LargeImageLoader {
    private static final Logger LOGGER = Logger.getLogger(LargeImageLoader.class.getName());
    
    // Thread pool specifically for loading large images
    private static final ExecutorService imageLoadExecutor = Executors.newFixedThreadPool(2);
    
    /**
     * Load a large image asynchronously with proper scaling
     * @param imageFile The image file to load
     * @param maxWidth Maximum width for the image
     * @param maxHeight Maximum height for the image
     * @return CompletableFuture with the loaded Image
     */
    public static CompletableFuture<Image> loadLargeImageAsync(File imageFile, double maxWidth, double maxHeight) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.info("Loading large image: " + imageFile.getName() + 
                           " (Size: " + formatFileSize(imageFile.length()) + ")");
                
                // Load image with background loading and proper scaling
                // Using a Task to allow for progress monitoring if needed
                Image image = new Image(
                    imageFile.toURI().toString(),
                    maxWidth,     // requested width
                    maxHeight,    // requested height
                    true,         // preserveRatio
                    true,         // smooth
                    true          // backgroundLoading
                );
                
                // Wait for loading to complete (but in background thread)
                // This is necessary because backgroundLoading is asynchronous
                int attempts = 0;
                while (image.getProgress() < 1.0 && attempts < 50) { // Wait up to 5 seconds
                    try {
                        Thread.sleep(100);
                        attempts++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                if (image.isError()) {
                    LOGGER.warning("Error loading image: " + imageFile.getName());
                    return null;
                }
                
                LOGGER.info("Successfully loaded image: " + imageFile.getName() + 
                           " (Dimensions: " + (int)image.getWidth() + "x" + (int)image.getHeight() + ")");
                return image;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load large image: " + imageFile.getName(), e);
                return null;
            }
        }, imageLoadExecutor);
    }
    
    /**
     * Load a large image synchronously with proper scaling
     * @param imageFile The image file to load
     * @param maxWidth Maximum width for the image
     * @param maxHeight Maximum height for the image
     * @return The loaded Image or null if failed
     */
    public static Image loadLargeImage(File imageFile, double maxWidth, double maxHeight) {
        try {
            LOGGER.info("Loading large image synchronously: " + imageFile.getName() + 
                       " (Size: " + formatFileSize(imageFile.length()) + ")");
            
            // Load image with background loading and proper scaling
            Image image = new Image(
                imageFile.toURI().toString(),
                maxWidth,     // requested width
                maxHeight,    // requested height
                true,         // preserveRatio
                true,         // smooth
                true          // backgroundLoading
            );
            
            // Wait for loading to complete
            int attempts = 0;
            while (image.getProgress() < 1.0 && attempts < 50) { // Wait up to 5 seconds
                try {
                    Thread.sleep(100);
                    attempts++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (image.isError()) {
                LOGGER.warning("Error loading image: " + imageFile.getName());
                return null;
            }
            
            LOGGER.info("Successfully loaded image: " + imageFile.getName() + 
                       " (Dimensions: " + (int)image.getWidth() + "x" + (int)image.getHeight() + ")");
            return image;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load large image: " + imageFile.getName(), e);
            return null;
        }
    }
    
    private static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    // Shutdown method for cleanup
    public static void shutdown() {
        imageLoadExecutor.shutdown();
    }
}