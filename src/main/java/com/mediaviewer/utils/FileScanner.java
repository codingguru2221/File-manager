package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FileScanner {
    private List<MediaFile> imageFiles;
    private List<MediaFile> videoFiles;
    private List<MediaFile> documentFiles;
    private List<MediaFile> projectFiles;
    private Set<String> projectPaths; // To track already detected project paths
    private AtomicInteger scannedFilesCount;
    
    public FileScanner() {
        this.imageFiles = new ArrayList<>();
        this.videoFiles = new ArrayList<>();
        this.documentFiles = new ArrayList<>();
        this.projectFiles = new ArrayList<>();
        this.projectPaths = new HashSet<>();
        this.scannedFilesCount = new AtomicInteger(0);
    }
    
    public void scanDirectory(String directoryPath, Consumer<Integer> progressCallback) {
        imageFiles.clear();
        videoFiles.clear();
        documentFiles.clear();
        projectFiles.clear();
        projectPaths.clear();
        scannedFilesCount.set(0);
        
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }
        
        scanDirectoryRecursive(directory, progressCallback);
    }
    
    private void scanDirectoryRecursive(File directory, Consumer<Integer> progressCallback) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        // Check if this directory is a project
        MediaFile mediaFile = new MediaFile(directory);
        if (mediaFile.getFileType().endsWith("-project")) {
            projectFiles.add(mediaFile);
            projectPaths.add(directory.getAbsolutePath());
            // We don't scan inside project folders for more projects
            return;
        }
        
        // Continue scanning files and directories
        for (File file : files) {
            if (file.isDirectory()) {
                // Continue scanning subdirectories recursively
                scanDirectoryRecursive(file, progressCallback);
            } else {
                // Categorize regular files
                MediaFile fileMedia = new MediaFile(file);
                categorizeFile(fileMedia);
                
                int count = scannedFilesCount.incrementAndGet();
                if (progressCallback != null && count % 10 == 0) {
                    progressCallback.accept(count);
                }
            }
        }
    }
    
    private void categorizeFile(MediaFile mediaFile) {
        String fileType = mediaFile.getFileType();
        
        // Check if it's a project type - if so, don't categorize it as a document
        // This is a safeguard, but project directories should already be handled in scanDirectoryRecursive
        if (fileType.endsWith("-project")) {
            // Projects should not reach here as directories are handled separately
            // But if they do, ensure they're not added to documents
            if (!projectFiles.contains(mediaFile)) {
                projectFiles.add(mediaFile);
            }
            return;
        }
        
        switch (fileType) {
            case "image":
                imageFiles.add(mediaFile);
                break;
            case "video":
                videoFiles.add(mediaFile);
                break;
            case "document":
                documentFiles.add(mediaFile);
                break;
        }
    }
    
    public List<MediaFile> getImageFiles() {
        return new ArrayList<>(imageFiles);
    }
    
    public List<MediaFile> getVideoFiles() {
        return new ArrayList<>(videoFiles);
    }
    
    public List<MediaFile> getDocumentFiles() {
        return new ArrayList<>(documentFiles);
    }
    
    public List<MediaFile> getProjectFiles() {
        return new ArrayList<>(projectFiles);
    }
    
    public int getTotalFilesCount() {
        return imageFiles.size() + videoFiles.size() + documentFiles.size() + projectFiles.size();
    }
    
    public int getImageFilesCount() {
        return imageFiles.size();
    }
    
    public int getVideoFilesCount() {
        return videoFiles.size();
    }
    
    public int getDocumentFilesCount() {
        return documentFiles.size();
    }
    
    public int getProjectFilesCount() {
        return projectFiles.size();
    }
    
    // New methods for size calculations
    public long getTotalFilesSize() {
        return getImageFilesSize() + getVideoFilesSize() + getDocumentFilesSize() + getProjectFilesSize();
    }
    
    public long getImageFilesSize() {
        return imageFiles.stream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getVideoFilesSize() {
        return videoFiles.stream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getDocumentFilesSize() {
        return documentFiles.stream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getProjectFilesSize() {
        return projectFiles.stream().mapToLong(MediaFile::getFileSize).sum();
    }
}