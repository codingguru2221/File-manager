package com.mediaviewer.model;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

public class MediaFile {
    private Path filePath;
    private String fileName;
    private String fileType; // image, video, document
    private String extension;
    private long fileSize;
    private LocalDateTime lastModified;
    private boolean isFavorite;
    private List<String> tags;
    private Map<String, String> metadata;
    
    public MediaFile(File file) {
        this.filePath = file.toPath();
        this.fileName = file.getName();
        this.fileSize = file.length();
        this.lastModified = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(file.lastModified()), 
            java.time.ZoneId.systemDefault()
        );
        this.extension = getFileExtension(file);
        this.fileType = categorizeFileType(this.extension, file);
        this.isFavorite = false;
        this.tags = new ArrayList<>();
        this.metadata = extractMetadata(file);
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return name.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    private String categorizeFileType(String extension, File file) {
        try {
            Tika tika = new Tika();
            String mimeType = tika.detect(file);
            
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.startsWith("video/")) {
                return "video";
            } else if (mimeType.startsWith("application/")) {
                return "document";
            }
        } catch (Exception e) {
            // Fall back to extension-based detection
            return categorizeByExtension(extension);
        }
        return "unknown";
    }
    
    private String categorizeByExtension(String extension) {
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
    
    private Map<String, String> extractMetadata(File file) {
        Map<String, String> metadata = new HashMap<>();
        try {
            Tika tika = new Tika();
            Metadata meta = new Metadata();
            tika.parse(file, meta);
            
            for (String key : meta.names()) {
                metadata.put(key, meta.get(key));
            }
        } catch (Exception e) {
            // Handle exception silently
        }
        return metadata;
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
    
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }
    
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    public void removeTag(String tag) {
        tags.remove(tag);
    }
    
    public Map<String, String> getMetadata() {
        return new HashMap<>(metadata);
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