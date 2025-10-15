package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectGrouping {
    
    public static Map<String, List<MediaFile>> groupByType(List<MediaFile> projects) {
        return projects.stream()
            .collect(Collectors.groupingBy(MediaFile::getFileType));
    }
    
    public static Map<String, List<MediaFile>> groupByLanguage(List<MediaFile> projects) {
        Map<String, List<MediaFile>> grouped = new HashMap<>();
        
        for (MediaFile project : projects) {
            String language = getLanguageFromProjectType(project.getFileType());
            grouped.computeIfAbsent(language, k -> new ArrayList<>()).add(project);
        }
        
        return grouped;
    }
    
    private static String getLanguageFromProjectType(String projectType) {
        switch (projectType) {
            case "java-project":
            case "gradle-project":
            case "intellij-project":
                return "Java";
            case "python-project":
                return "Python";
            case "javascript-project":
            case "vscode-project":
                return "JavaScript";
            case "generic-project":
                return "Generic";
            default:
                return "Other";
        }
    }
    
    public static Map<String, List<MediaFile>> groupBySize(List<MediaFile> projects) {
        Map<String, List<MediaFile>> grouped = new HashMap<>();
        
        for (MediaFile project : projects) {
            String sizeCategory = getSizeCategory(project.getFileSize());
            grouped.computeIfAbsent(sizeCategory, k -> new ArrayList<>()).add(project);
        }
        
        return grouped;
    }
    
    private static String getSizeCategory(long size) {
        if (size < 1024 * 1024) { // Less than 1MB
            return "Small (< 1MB)";
        } else if (size < 10 * 1024 * 1024) { // Less than 10MB
            return "Medium (1MB - 10MB)";
        } else if (size < 100 * 1024 * 1024) { // Less than 100MB
            return "Large (10MB - 100MB)";
        } else {
            return "Very Large (> 100MB)";
        }
    }
}