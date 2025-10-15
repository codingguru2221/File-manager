package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class RecentProjectsManager {
    
    private static final int MAX_RECENT_PROJECTS = 10;
    private List<MediaFile> recentProjects;
    private Preferences prefs;
    
    public RecentProjectsManager() {
        recentProjects = new ArrayList<>();
        prefs = Preferences.userNodeForPackage(RecentProjectsManager.class);
        loadRecentProjects();
    }
    
    public void addProject(MediaFile project) {
        // Remove if already exists
        recentProjects.removeIf(p -> p.getFilePath().equals(project.getFilePath()));
        
        // Add to the beginning of the list
        recentProjects.add(0, project);
        
        // Keep only the most recent projects
        if (recentProjects.size() > MAX_RECENT_PROJECTS) {
            recentProjects = recentProjects.subList(0, MAX_RECENT_PROJECTS);
        }
        
        saveRecentProjects();
    }
    
    public List<MediaFile> getRecentProjects() {
        return new ArrayList<>(recentProjects);
    }
    
    public void clearRecentProjects() {
        recentProjects.clear();
        saveRecentProjects();
    }
    
    private void saveRecentProjects() {
        // In a real implementation, you would serialize the project paths
        // For simplicity, we'll just clear the preferences
        try {
            prefs.clear();
        } catch (Exception e) {
            // Handle exception
        }
    }
    
    private void loadRecentProjects() {
        // In a real implementation, you would deserialize the project paths
        // For simplicity, we'll just start with an empty list
        recentProjects.clear();
    }
}