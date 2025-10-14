# Smart Media Viewer

A feature-rich media viewing application built with JavaFX that allows users to organize, view, and manage their media files.

## Features

### Media Viewing
- View images, videos, and documents in a user-friendly interface
- High-quality thumbnails for all media types
- Detailed file information display

### Advanced Organization
- **Favorites System**: Mark important files as favorites
- **Tagging**: Add custom tags to files for better organization
- **Automatic Sorting**: Organize files by type, date, or size
- **Filtering**: Filter files by type, date range, or tags

### Search and Export
- **Advanced Search**: Search across filenames, extensions, and file sizes
- **CSV Export**: Export file information to CSV format

### User Interface
- Modern, responsive design
- Context menus for quick actions
- Visual feedback for favorite files
- Intuitive tab-based navigation

## Technologies Used

- **Java 17**
- **JavaFX** for the graphical user interface
- **Apache Tika** for file type detection and metadata extraction
- **Thumbnailator** for high-quality thumbnail generation
- **Maven** for dependency management

## Dependencies

```xml
<dependencies>
    <!-- JavaFX Dependencies -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-swing</artifactId>
        <version>17.0.2</version>
    </dependency>
    
    <!-- Apache Tika for file type detection -->
    <dependency>
        <groupId>org.apache.tika</groupId>
        <artifactId>tika-core</artifactId>
        <version>2.4.1</version>
    </dependency>
    
    <!-- Thumbnailator for image thumbnail generation -->
    <dependency>
        <groupId>net.coobird</groupId>
        <artifactId>thumbnailator</artifactId>
        <version>0.4.17</version>
    </dependency>
    
    <!-- JUnit for testing -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## How to Build and Run

### Prerequisites
- Java 17 or higher
- Apache Maven 3.6 or higher

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn javafx:run
```

## Project Structure

```
src/
├── main/
│   ├── java/com/mediaviewer/
│   │   ├── controller/     # UI controllers
│   │   ├── model/          # Data models
│   │   ├── utils/          # Utility classes
│   │   └── Main.java       # Application entry point
│   └── resources/
│       ├── css/            # Stylesheets
│       └── fxml/           # UI layout files
└── test/
    └── java/com/mediaviewer/
        └── FileScannerTest.java  # Unit tests
```

## Key Classes

### Model
- **MediaFile**: Represents a media file with metadata, favorites, and tagging capabilities

### Controllers
- **DashboardController**: Main application controller with search and filtering
- **ImageTabController**: Manages the image viewing tab
- **VideoTabController**: Manages the video viewing tab
- **DocumentTabController**: Manages the document viewing tab

### Utilities
- **FileOrganizer**: Organizes files by type, date, or size
- **FileScanner**: Scans directories for media files
- **ThumbnailGenerator**: Generates high-quality thumbnails

## Implementation Summary

For a detailed summary of all implemented features, see [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md).

## License

This project is licensed under the MIT License - see the LICENSE file for details.