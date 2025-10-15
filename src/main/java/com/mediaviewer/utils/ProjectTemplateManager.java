package com.mediaviewer.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ProjectTemplateManager {
    
    private Map<String, String> templates;
    
    public ProjectTemplateManager() {
        templates = new HashMap<>();
        initializeDefaultTemplates();
    }
    
    private void initializeDefaultTemplates() {
        // Java Maven Project Template
        templates.put("java-maven", 
            "pom.xml\n" +
            "src/\n" +
            "  main/\n" +
            "    java/\n" +
            "    resources/\n" +
            "  test/\n" +
            "    java/\n" +
            "    resources/\n" +
            "README.md");
        
        // Python Project Template
        templates.put("python", 
            "requirements.txt\n" +
            "setup.py\n" +
            "src/\n" +
            "tests/\n" +
            "README.md");
        
        // JavaScript Node.js Project Template
        templates.put("javascript-node", 
            "package.json\n" +
            "src/\n" +
            "tests/\n" +
            "README.md");
        
        // Generic Project Template
        templates.put("generic", 
            ".git/\n" +
            "src/\n" +
            "docs/\n" +
            "README.md");
    }
    
    public String getTemplate(String templateName) {
        return templates.get(templateName);
    }
    
    public void createProjectFromTemplate(String templateName, String projectName, String basePath) throws IOException {
        String template = getTemplate(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        Path projectPath = Paths.get(basePath, projectName);
        Files.createDirectories(projectPath);
        
        // Parse and create the template structure
        String[] lines = template.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            Path itemPath = projectPath.resolve(line);
            if (line.endsWith("/")) {
                // It's a directory
                Files.createDirectories(itemPath);
            } else {
                // It's a file
                Files.createFile(itemPath);
            }
        }
    }
    
    public String[] getAvailableTemplates() {
        return templates.keySet().toArray(new String[0]);
    }
}