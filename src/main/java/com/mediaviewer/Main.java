package com.mediaviewer;

import com.mediaviewer.controller.DashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the dashboard FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();
        
        // Get the dashboard controller
        DashboardController dashboardController = loader.getController();
        dashboardController.setPrimaryStage(primaryStage);
        
        // Set up the scene and stage
        Scene scene = new Scene(root, 1000, 700);
        
        // Add CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        primaryStage.setTitle("Smart Media Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}