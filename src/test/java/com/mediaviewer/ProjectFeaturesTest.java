package com.mediaviewer;

import com.mediaviewer.utils.ProjectStatistics;
import com.mediaviewer.utils.ProjectGrouping;
import com.mediaviewer.utils.ProjectExport;
import com.mediaviewer.utils.ProjectTemplateManager;
import com.mediaviewer.model.MediaFile;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ProjectFeaturesTest {

    @Test
    public void testProjectStatistics() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-projects");
        
        try {
            // Create test projects
            Path javaProject = tempDir.resolve("java-project");
            Files.createDirectories(javaProject);
            File pomFile = javaProject.resolve("pom.xml").toFile();
            pomFile.createNewFile();
            File srcDir = javaProject.resolve("src").toFile();
            srcDir.mkdir();
            
            Path pythonProject = tempDir.resolve("python-project");
            Files.createDirectories(pythonProject);
            File requirementsFile = pythonProject.resolve("requirements.txt").toFile();
            requirementsFile.createNewFile();
            
            // Create MediaFile objects for testing
            MediaFile javaMediaFile = new MediaFile(javaProject.toFile());
            MediaFile pythonMediaFile = new MediaFile(pythonProject.toFile());
            
            // Test statistics calculation
            ProjectStatistics.ProjectStats stats = ProjectStatistics.calculateProjectStatistics(
                List.of(javaMediaFile, pythonMediaFile));
            
            assertEquals(2, stats.getTotalProjects());
            assertTrue(stats.getProjectTypeCount().containsKey("java-project"));
            assertTrue(stats.getProjectTypeCount().containsKey("python-project"));
            assertEquals(1, (int) stats.getProjectTypeCount().get("java-project"));
            assertEquals(1, (int) stats.getProjectTypeCount().get("python-project"));
        } finally {
            // Clean up
            deleteRecursively(tempDir.toFile());
        }
    }
    
    @Test
    public void testProjectGrouping() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-projects-grouping");
        
        try {
            // Create test projects
            Path javaProject = tempDir.resolve("java-project");
            Files.createDirectories(javaProject);
            File pomFile = javaProject.resolve("pom.xml").toFile();
            pomFile.createNewFile();
            
            Path pythonProject = tempDir.resolve("python-project");
            Files.createDirectories(pythonProject);
            File requirementsFile = pythonProject.resolve("requirements.txt").toFile();
            requirementsFile.createNewFile();
            
            // Create MediaFile objects for testing
            MediaFile javaMediaFile = new MediaFile(javaProject.toFile());
            MediaFile pythonMediaFile = new MediaFile(pythonProject.toFile());
            
            // Test grouping by type
            Map<String, List<MediaFile>> groupedByType = ProjectGrouping.groupByType(
                List.of(javaMediaFile, pythonMediaFile));
            
            assertTrue(groupedByType.containsKey("java-project"));
            assertTrue(groupedByType.containsKey("python-project"));
            assertEquals(1, groupedByType.get("java-project").size());
            assertEquals(1, groupedByType.get("python-project").size());
            
            // Test grouping by language
            Map<String, List<MediaFile>> groupedByLanguage = ProjectGrouping.groupByLanguage(
                List.of(javaMediaFile, pythonMediaFile));
            
            assertTrue(groupedByLanguage.containsKey("Java"));
            assertTrue(groupedByLanguage.containsKey("Python"));
            assertEquals(1, groupedByLanguage.get("Java").size());
            assertEquals(1, groupedByLanguage.get("Python").size());
        } finally {
            // Clean up
            deleteRecursively(tempDir.toFile());
        }
    }
    
    @Test
    public void testProjectTemplateManager() {
        ProjectTemplateManager templateManager = new ProjectTemplateManager();
        
        // Test available templates
        String[] templates = templateManager.getAvailableTemplates();
        assertTrue(templates.length > 0);
        
        // Test getting a specific template
        String javaTemplate = templateManager.getTemplate("java-maven");
        assertNotNull(javaTemplate);
        assertTrue(javaTemplate.contains("pom.xml"));
        assertTrue(javaTemplate.contains("src/"));
    }
    
    @Test
    public void testProjectExport() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-projects-export");
        
        try {
            // Create test projects
            Path javaProject = tempDir.resolve("java-project");
            Files.createDirectories(javaProject);
            File pomFile = javaProject.resolve("pom.xml").toFile();
            pomFile.createNewFile();
            
            // Create MediaFile object for testing
            MediaFile javaMediaFile = new MediaFile(javaProject.toFile());
            
            // Test CSV export
            Path csvFile = tempDir.resolve("projects.csv");
            ProjectExport.exportProjectsToCSV(List.of(javaMediaFile), csvFile.toString());
            assertTrue(Files.exists(csvFile));
            
            // Test JSON export
            Path jsonFile = tempDir.resolve("projects.json");
            ProjectExport.exportProjectsToJSON(List.of(javaMediaFile), jsonFile.toString());
            assertTrue(Files.exists(jsonFile));
        } finally {
            // Clean up
            deleteRecursively(tempDir.toFile());
        }
    }
    
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}