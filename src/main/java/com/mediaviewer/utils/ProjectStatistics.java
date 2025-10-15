package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectStatistics {
    
    public static class ProjectStats {
        private int totalProjects;
        private Map<String, Integer> projectTypeCount;
        private long totalProjectSize;
        private Map<String, Long> projectTypeSize;
        
        public ProjectStats() {
            this.projectTypeCount = new HashMap<>();
            this.projectTypeSize = new HashMap<>();
        }
        
        // Getters and setters
        public int getTotalProjects() {
            return totalProjects;
        }
        
        public void setTotalProjects(int totalProjects) {
            this.totalProjects = totalProjects;
        }
        
        public Map<String, Integer> getProjectTypeCount() {
            return projectTypeCount;
        }
        
        public void setProjectTypeCount(Map<String, Integer> projectTypeCount) {
            this.projectTypeCount = projectTypeCount;
        }
        
        public long getTotalProjectSize() {
            return totalProjectSize;
        }
        
        public void setTotalProjectSize(long totalProjectSize) {
            this.totalProjectSize = totalProjectSize;
        }
        
        public Map<String, Long> getProjectTypeSize() {
            return projectTypeSize;
        }
        
        public void setProjectTypeSize(Map<String, Long> projectTypeSize) {
            this.projectTypeSize = projectTypeSize;
        }
    }
    
    public static ProjectStats calculateProjectStatistics(List<MediaFile> projectFiles) {
        ProjectStats stats = new ProjectStats();
        
        stats.setTotalProjects(projectFiles.size());
        
        long totalSize = 0;
        
        for (MediaFile project : projectFiles) {
            // Count project types
            String fileType = project.getFileType();
            stats.getProjectTypeCount().merge(fileType, 1, Integer::sum);
            
            // Add to total size
            totalSize += project.getFileSize();
            
            // Add to project type size
            stats.getProjectTypeSize().merge(fileType, project.getFileSize(), Long::sum);
        }
        
        stats.setTotalProjectSize(totalSize);
        
        return stats;
    }
}