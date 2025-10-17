package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class FileScanner {
    private List<MediaFile> imageFiles;
    private List<MediaFile> videoFiles;
    private List<MediaFile> documentFiles;
    private List<MediaFile> projectFiles;
    private List<MediaFile> normalFolders; // New field for normal folders
    private Set<String> projectPaths; // To track already detected project paths
    private String rootDirectoryPath; // To track the root directory
    private AtomicInteger scannedFilesCount;
    
    public FileScanner() {
        this.imageFiles = new ArrayList<>();
        this.videoFiles = new ArrayList<>();
        this.documentFiles = new ArrayList<>();
        this.projectFiles = new ArrayList<>();
        this.normalFolders = new ArrayList<>(); // Initialize normal folders list
        this.projectPaths = new HashSet<>();
        this.scannedFilesCount = new AtomicInteger(0);
    }
    
    public void scanDirectory(String directoryPath, Consumer<Integer> progressCallback) {
        imageFiles.clear();
        videoFiles.clear();
        documentFiles.clear();
        projectFiles.clear();
        normalFolders.clear(); // Clear normal folders list
        projectPaths.clear();
        this.rootDirectoryPath = directoryPath; // Store the root directory path
        scannedFilesCount.set(0);
        
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }
        
        // Use parallel processing for better performance on large directories
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            forkJoinPool.invoke(new ScanDirectoryAction(directory, progressCallback));
        } finally {
            forkJoinPool.shutdown();
        }
    }
    
    private class ScanDirectoryAction extends RecursiveAction {
        private final File directory;
        private final Consumer<Integer> progressCallback;
        
        public ScanDirectoryAction(File directory, Consumer<Integer> progressCallback) {
            this.directory = directory;
            this.progressCallback = progressCallback;
        }
        
        @Override
        protected void compute() {
            File[] files = directory.listFiles();
            if (files == null) return;
            
            // Check if this directory is a project
            MediaFile mediaFile = new MediaFile(directory);
            if (mediaFile.getFileType().endsWith("-project")) {
                synchronized (projectFiles) {
                    projectFiles.add(mediaFile);
                    projectPaths.add(directory.getAbsolutePath());
                }
                // We don't scan inside project folders for more projects
                return;
            } else {
                // If it's a directory but not a project, and not the root directory, count it as a normal folder
                if (!directory.getAbsolutePath().equals(rootDirectoryPath)) {
                    synchronized (normalFolders) {
                        normalFolders.add(mediaFile);
                    }
                }
            }
            
            List<ScanDirectoryAction> subTasks = new ArrayList<>();
            
            // Continue scanning files and directories
            for (File file : files) {
                if (file.isDirectory()) {
                    // Create subtask for subdirectories
                    ScanDirectoryAction subTask = new ScanDirectoryAction(file, progressCallback);
                    subTasks.add(subTask);
                    subTask.fork();
                } else {
                    // Categorize regular files
                    MediaFile fileMedia = new MediaFile(file);
                    categorizeFile(fileMedia);
                    
                    int count = scannedFilesCount.incrementAndGet();
                    if (progressCallback != null && count % 50 == 0) { // Update less frequently to reduce UI updates
                        progressCallback.accept(count);
                    }
                }
            }
            
            // Wait for all subtasks to complete
            for (ScanDirectoryAction subTask : subTasks) {
                subTask.join();
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
            synchronized (projectFiles) {
                if (!projectFiles.contains(mediaFile)) {
                    projectFiles.add(mediaFile);
                }
            }
            return;
        }
        
        switch (fileType) {
            case "image":
                synchronized (imageFiles) {
                    imageFiles.add(mediaFile);
                }
                break;
            case "video":
                synchronized (videoFiles) {
                    videoFiles.add(mediaFile);
                }
                break;
            case "document":
                synchronized (documentFiles) {
                    documentFiles.add(mediaFile);
                }
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
    
    public List<MediaFile> getNormalFolders() {
        return new ArrayList<>(normalFolders);
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
    
    public int getNormalFoldersCount() {
        return normalFolders.size();
    }
    
    // New methods for size calculations
    public long getTotalFilesSize() {
        return getImageFilesSize() + getVideoFilesSize() + getDocumentFilesSize() + getProjectFilesSize();
    }
    
    public long getImageFilesSize() {
        return imageFiles.parallelStream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getVideoFilesSize() {
        return videoFiles.parallelStream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getDocumentFilesSize() {
        return documentFiles.parallelStream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getProjectFilesSize() {
        return projectFiles.parallelStream().mapToLong(MediaFile::getFileSize).sum();
    }
    
    public long getNormalFoldersSize() {
        return normalFolders.parallelStream().mapToLong(MediaFile::getFileSize).sum();
    }
}