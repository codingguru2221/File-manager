package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;

public class FileOrganizer {
    
    public void organizeByType(File directory, MediaFile mediaFile) {
        try {
            String subDirName = mediaFile.getFileType(); // image, video, document
            Path subDir = directory.toPath().resolve(subDirName);
            
            // Create subdirectory if it doesn't exist
            if (!Files.exists(subDir)) {
                Files.createDirectories(subDir);
            }
            
            // Move file to the appropriate subdirectory
            Path sourcePath = mediaFile.getFilePath();
            Path targetPath = subDir.resolve(sourcePath.getFileName());
            
            // If file already exists, add a number to the filename
            targetPath = getUniquePath(targetPath);
            
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception appropriately in a real application
        }
    }
    
    public void organizeByDate(File directory, MediaFile mediaFile) {
        try {
            // Create subdirectory based on year/month
            String year = String.valueOf(mediaFile.getLastModified().getYear());
            String month = mediaFile.getLastModified().format(DateTimeFormatter.ofPattern("MM"));
            String subDirName = year + "/" + month;
            
            Path subDir = directory.toPath().resolve(subDirName);
            
            // Create subdirectory if it doesn't exist
            if (!Files.exists(subDir)) {
                Files.createDirectories(subDir);
            }
            
            // Move file to the appropriate subdirectory
            Path sourcePath = mediaFile.getFilePath();
            Path targetPath = subDir.resolve(sourcePath.getFileName());
            
            // If file already exists, add a number to the filename
            targetPath = getUniquePath(targetPath);
            
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception appropriately in a real application
        }
    }
    
    public void organizeBySize(File directory, MediaFile mediaFile) {
        try {
            String subDirName;
            long size = mediaFile.getFileSize();
            
            // Categorize by size
            if (size < 1024 * 1024) { // Less than 1MB
                subDirName = "Small";
            } else if (size < 10 * 1024 * 1024) { // Less than 10MB
                subDirName = "Medium";
            } else { // 10MB or larger
                subDirName = "Large";
            }
            
            Path subDir = directory.toPath().resolve(subDirName);
            
            // Create subdirectory if it doesn't exist
            if (!Files.exists(subDir)) {
                Files.createDirectories(subDir);
            }
            
            // Move file to the appropriate subdirectory
            Path sourcePath = mediaFile.getFilePath();
            Path targetPath = subDir.resolve(sourcePath.getFileName());
            
            // If file already exists, add a number to the filename
            targetPath = getUniquePath(targetPath);
            
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception appropriately in a real application
        }
    }
    
    private Path getUniquePath(Path targetPath) {
        Path uniquePath = targetPath;
        int counter = 1;
        
        while (Files.exists(uniquePath)) {
            String fileName = targetPath.getFileName().toString();
            String extension = "";
            String nameWithoutExtension = fileName;
            
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
                nameWithoutExtension = fileName.substring(0, dotIndex);
            }
            
            String newName = nameWithoutExtension + "_" + counter + extension;
            uniquePath = targetPath.getParent().resolve(newName);
            counter++;
        }
        
        return uniquePath;
    }
}