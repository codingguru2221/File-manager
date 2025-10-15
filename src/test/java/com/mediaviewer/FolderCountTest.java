package com.mediaviewer;

import com.mediaviewer.utils.FileScanner;
import com.mediaviewer.model.MediaFile;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FolderCountTest {

    @Test
    public void testNormalFolderCounting() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-folders");
        
        try {
            // Create a project folder
            Path projectFolder = tempDir.resolve("java-project");
            Files.createDirectories(projectFolder);
            File pomFile = projectFolder.resolve("pom.xml").toFile();
            pomFile.createNewFile();
            
            // Create normal folders
            Path normalFolder1 = tempDir.resolve("normal-folder-1");
            Files.createDirectories(normalFolder1);
            
            Path normalFolder2 = tempDir.resolve("normal-folder-2");
            Files.createDirectories(normalFolder2);
            
            // Create a subfolder to test recursive counting
            Path subFolder = normalFolder1.resolve("sub-folder");
            Files.createDirectories(subFolder);
            
            // Create some files
            File imageFile = tempDir.resolve("image.jpg").toFile();
            imageFile.createNewFile();
            
            File documentFile = tempDir.resolve("document.txt").toFile();
            documentFile.createNewFile();
            
            // Scan the directory
            FileScanner scanner = new FileScanner();
            scanner.scanDirectory(tempDir.toString(), null);
            
            // Verify results
            assertEquals("Should detect 1 project", 1, scanner.getProjectFilesCount());
            assertEquals("Should detect 3 normal folders (normal-folder-1, normal-folder-2, sub-folder)", 3, scanner.getNormalFoldersCount());
            assertEquals("Should detect 1 image", 1, scanner.getImageFilesCount());
            assertEquals("Should detect 1 document", 1, scanner.getDocumentFilesCount());
            
            // Verify that normal folders are not counted as projects
            for (MediaFile folder : scanner.getNormalFolders()) {
                assertFalse("Normal folders should not be categorized as projects", 
                           folder.getFileType().endsWith("-project"));
            }
            
            // Verify that project folders are not counted as normal folders
            for (MediaFile project : scanner.getProjectFiles()) {
                assertTrue("Project folders should be categorized as projects", 
                          project.getFileType().endsWith("-project"));
            }
            
            // Verify that the root directory is not counted as a normal folder
            boolean rootDirectoryCounted = false;
            for (MediaFile folder : scanner.getNormalFolders()) {
                if (folder.getFilePath().toString().equals(tempDir.toString())) {
                    rootDirectoryCounted = true;
                    break;
                }
            }
            assertFalse("Root directory should not be counted as a normal folder", rootDirectoryCounted);
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