package com.mediaviewer;

import com.mediaviewer.utils.FileScanner;
import com.mediaviewer.model.MediaFile;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectDetectionTest {

    @Test
    public void testSimpleDocumentDetection() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-documents");
        
        try {
            // Create a regular file that should be categorized as document
            File docFile = tempDir.resolve("readme.txt").toFile();
            docFile.createNewFile();
            
            // Scan the directory
            FileScanner scanner = new FileScanner();
            scanner.scanDirectory(tempDir.toString(), null);
            
            // Verify results
            java.util.List<MediaFile> documentFiles = scanner.getDocumentFiles();
            
            // Should find 1 document (readme.txt)
            assertEquals("Should detect 1 document", 1, documentFiles.size());
        } finally {
            // Clean up temporary files
            deleteRecursively(tempDir.toFile());
        }
    }
    
    @Test
    public void testProjectDetectionWithoutSubprojects() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-projects-simple");
        
        try {
            // Create a Java project
            Path javaProject = tempDir.resolve("java-project");
            Files.createDirectories(javaProject);
            
            // Create project files
            File pomFile = javaProject.resolve("pom.xml").toFile();
            pomFile.createNewFile();
            
            File srcDir = javaProject.resolve("src").toFile();
            srcDir.mkdir();
            
            // Create a regular file that should be categorized as document
            File docFile = tempDir.resolve("readme.txt").toFile();
            docFile.createNewFile();
            
            // Scan the directory
            FileScanner scanner = new FileScanner();
            scanner.scanDirectory(tempDir.toString(), null);
            
            // Verify results
            java.util.List<MediaFile> projectFiles = scanner.getProjectFiles();
            java.util.List<MediaFile> documentFiles = scanner.getDocumentFiles();
            
            // Should find 1 project
            assertEquals("Should detect 1 project", 1, projectFiles.size());
            
            // Should find 1 document (readme.txt)
            assertEquals("Should detect 1 document", 1, documentFiles.size());
            
            // Verify project type
            MediaFile project = projectFiles.get(0);
            assertEquals("Should detect Java project", "java-project", project.getFileType());
            
            // Verify the document is not a project
            MediaFile document = documentFiles.get(0);
            assertFalse("Document should not be categorized as project", 
                       document.getFileType().endsWith("-project"));
        } finally {
            // Clean up temporary files
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