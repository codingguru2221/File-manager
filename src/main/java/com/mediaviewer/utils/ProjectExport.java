package com.mediaviewer.utils;

import com.mediaviewer.model.MediaFile;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProjectExport {
    
    public static void exportProjectsToCSV(List<MediaFile> projects, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write CSV header
            writer.println("Name,Type,Size,Modified,Favorite,Tags");
            
            // Write project data
            for (MediaFile project : projects) {
                String tags = String.join(";", project.getTags());
                writer.printf("\"%s\",%s,%d,\"%s\",%s,\"%s\"%n", 
                    project.getFileName().replace("\"", "\"\""), 
                    project.getFileType(), 
                    project.getFileSize(), 
                    project.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    project.isFavorite() ? "Yes" : "No",
                    tags.replace("\"", "\"\""));
            }
        }
    }
    
    public static void exportProjectsToJSON(List<MediaFile> projects, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("[");
            
            for (int i = 0; i < projects.size(); i++) {
                MediaFile project = projects.get(i);
                writer.println("  {");
                writer.printf("    \"name\": \"%s\",%n", escapeJsonString(project.getFileName()));
                writer.printf("    \"type\": \"%s\",%n", project.getFileType());
                writer.printf("    \"size\": %d,%n", project.getFileSize());
                writer.printf("    \"modified\": \"%s\",%n", 
                    project.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.printf("    \"favorite\": %s,%n", project.isFavorite());
                writer.printf("    \"tags\": [\"%s\"]%n", 
                    String.join("\",\"", project.getTags().stream().map(ProjectExport::escapeJsonString).toArray(String[]::new)));
                writer.print("  }");
                
                if (i < projects.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            
            writer.println("]");
        }
    }
    
    private static String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}