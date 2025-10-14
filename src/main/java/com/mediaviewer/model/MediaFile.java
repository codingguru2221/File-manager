package com.mediaviewer.model;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class MediaFile {
    private Path filePath;
    private String fileName;
    private String fileType; // image, video, document
    private String extension;
    private long fileSize;
    private LocalDateTime lastModified;
    private boolean isFavorite;
    
    public MediaFile(File file) {
        this.filePath = file.toPath();
        this.fileName = file.getName();
        this.fileSize = file.length();
        this.lastModified = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(file.lastModified()), 
            java.time.ZoneId.systemDefault()
        );
        this.extension = getFileExtension(file);
        this.fileType = categorizeFileType(this.extension);
        this.isFavorite = false;
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return name.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    private String categorizeFileType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "image";
            case "mp4":
            case "mkv":
            case "avi":
            case "mov":
                return "video";
            case "pdf":
            case "docx":
            case "txt":
            case "pptx":
            case "xlsx":
                return "document";
            default:
                return "unknown";
        }
    }
    
    // Getters and setters
    public Path getFilePath() {
        return filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    @Override
    public String toString() {
        return "MediaFile{" +
                "fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", extension='" + extension + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}