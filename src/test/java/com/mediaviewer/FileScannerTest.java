package com.mediaviewer;

import com.mediaviewer.utils.FileScanner;
import com.mediaviewer.model.MediaFile;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileScannerTest {

    @Test
    public void testFileScannerWithSampleFiles() {
        // Create a temporary directory for testing
        try {
            Path tempDir = Files.createTempDirectory("mediaViewerTest");
            
            // Create sample files
            createSampleFile(tempDir, "image1.jpg");
            createSampleFile(tempDir, "image2.png");
            createSampleFile(tempDir, "video1.mp4");
            createSampleFile(tempDir, "document1.pdf");
            createSampleFile(tempDir, "document2.docx");
            
            // Create a subdirectory with more files
            Path subDir = tempDir.resolve("subfolder");
            Files.createDirectory(subDir);
            createSampleFile(subDir, "image3.gif");
            createSampleFile(subDir, "video2.avi");
            
            // Test the FileScanner
            FileScanner scanner = new FileScanner();
            scanner.scanDirectory(tempDir.toString(), null);
            
            // Verify the results
            assertEquals(3, scanner.getImageFilesCount());
            assertEquals(2, scanner.getVideoFilesCount());
            assertEquals(2, scanner.getDocumentFilesCount());
            assertEquals(7, scanner.getTotalFilesCount());
            
            // Verify specific file types
            List<MediaFile> imageFiles = scanner.getImageFiles();
            List<MediaFile> videoFiles = scanner.getVideoFiles();
            List<MediaFile> documentFiles = scanner.getDocumentFiles();
            
            assertTrue(imageFiles.stream().anyMatch(f -> f.getFileName().equals("image1.jpg")));
            assertTrue(videoFiles.stream().anyMatch(f -> f.getFileName().equals("video1.mp4")));
            assertTrue(documentFiles.stream().anyMatch(f -> f.getFileName().equals("document1.pdf")));
            
            // Clean up
            deleteDirectory(tempDir.toFile());
            
        } catch (IOException e) {
            fail("Test failed due to IOException: " + e.getMessage());
        }
    }
    
    private void createSampleFile(Path directory, String fileName) throws IOException {
        Path filePath = directory.resolve(fileName);
        Files.createFile(filePath);
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}