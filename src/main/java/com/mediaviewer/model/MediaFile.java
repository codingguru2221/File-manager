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
    private String fileType; // image, video, document, or specific project type
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
        // First check if it's a project directory
        String projectType = detectProjectType(file);
        if (projectType != null) {
            return projectType;
        }
        
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
    
    private String detectProjectType(File file) {
        // Check if it's a directory (project folder)
        if (!file.isDirectory()) {
            return null;
        }
        
        // Check for specific project types based on common files/folders
        File[] files = file.listFiles();
        if (files == null) return null;
        
        boolean hasGit = false;
        boolean hasSrc = false;
        boolean hasPackageJson = false;
        boolean hasPomXml = false;
        boolean hasRequirementsTxt = false;
        boolean hasSetupPy = false;
        boolean hasGradle = false;
        boolean hasMaven = false;
        boolean hasNodeModules = false;
        boolean hasVSCode = false;
        boolean hasIntelliJ = false;
        
        for (File f : files) {
            String name = f.getName();
            if (".git".equals(name)) hasGit = true;
            else if ("src".equals(name)) hasSrc = true;
            else if ("package.json".equals(name)) hasPackageJson = true;
            else if ("pom.xml".equals(name)) hasPomXml = true;
            else if ("requirements.txt".equals(name)) hasRequirementsTxt = true;
            else if ("setup.py".equals(name)) hasSetupPy = true;
            else if ("build.gradle".equals(name) || "gradlew".equals(name)) hasGradle = true;
            else if ("node_modules".equals(name)) hasNodeModules = true;
            else if (".vscode".equals(name)) hasVSCode = true;
            else if (".idea".equals(name)) hasIntelliJ = true;
            else if (name.endsWith(".iml")) hasIntelliJ = true;
        }
        
        // Detect specific project types
        if (hasPomXml || (hasSrc && hasMaven)) {
            return "java-project";
        } else if (hasPackageJson || (hasNodeModules && hasSrc)) {
            return "javascript-project";
        } else if (hasRequirementsTxt || hasSetupPy) {
            return "python-project";
        } else if (hasGradle) {
            // Could be Java/Kotlin/other Gradle project
            return "gradle-project";
        } else if (hasIntelliJ) {
            return "intellij-project";
        } else if (hasVSCode) {
            return "vscode-project";
        } else if (hasGit && hasSrc) {
            return "generic-project";
        }
        
        return null;
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