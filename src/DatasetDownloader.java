import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Automatic dataset downloader for FlexiRoute
 * Downloads graph data from Google Drive if not present locally
 */
public class DatasetDownloader {
    
    // Google Drive folder ID from the shared link
    private static final String DRIVE_FOLDER_ID = "1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP";
    
    // Dataset directory relative to project root
    private static final String DATASET_DIR = "dataset";
    
    // Supported dataset subdirectories
    private static final String[] DATASET_SUBDIRS = {
        "London",
        "California"
    };
    
    // Known dataset file patterns (without directory path)
    private static final String[][] DATASET_FILES = {
        {"nodes_288016.txt", "edges_288016.txt"},  // London
        {"nodes_21048.txt", "edges_21048.txt"}     // California
    };
    
    /**
     * Check if dataset exists in subdirectories
     * @return Path to default dataset directory (London) if found, otherwise base dataset dir
     */
    public static String ensureDatasetExists() {
        try {
            // Get project root directory
            String projectRoot = System.getProperty("user.dir");
            Path datasetPath = Paths.get(projectRoot, DATASET_DIR);
            
            // Create dataset directory if it doesn't exist
            if (!Files.exists(datasetPath)) {
                Files.createDirectories(datasetPath);
                System.out.println("[Dataset] Created dataset directory: " + datasetPath);
            }
            
            // Check for datasets in subdirectories
            String foundDataset = checkDatasetSubdirectories(datasetPath);
            
            if (foundDataset == null) {
                System.out.println("[Dataset] No dataset files found.");
                System.out.println();
                
                // Show manual setup instructions
                showDatasetSetupInstructions();
                
                // Show status of expected datasets
                System.out.println("Dataset status:");
                for (int i = 0; i < DATASET_SUBDIRS.length; i++) {
                    String subdir = DATASET_SUBDIRS[i];
                    Path subdirPath = datasetPath.resolve(subdir);
                    if (Files.isDirectory(subdirPath)) {
                        boolean hasFiles = checkDatasetFiles(subdirPath, DATASET_FILES[i]);
                        String status = hasFiles ? "✓ Complete" : "✗ Incomplete";
                        System.out.println("  " + status + " - " + subdir + "/");
                    } else {
                        System.out.println("  ✗ Missing - " + subdir + "/");
                    }
                }
                System.out.println();
                
                return datasetPath.toAbsolutePath().toString();
            } else {
                System.out.println("[Dataset] ✓ Found dataset: " + foundDataset);
                return Paths.get(projectRoot, DATASET_DIR, foundDataset).toAbsolutePath().toString();
            }
            
        } catch (IOException e) {
            System.err.println("[Dataset] Error: " + e.getMessage());
            return DATASET_DIR;
        }
    }
    
    /**
     * Check for dataset files in subdirectories
     * @return Name of first valid dataset subdirectory found, or null if none
     */
    private static String checkDatasetSubdirectories(Path datasetPath) {
        for (int i = 0; i < DATASET_SUBDIRS.length; i++) {
            String subdir = DATASET_SUBDIRS[i];
            Path subdirPath = datasetPath.resolve(subdir);
            
            if (Files.isDirectory(subdirPath) && checkDatasetFiles(subdirPath, DATASET_FILES[i])) {
                return subdir;
            }
        }
        return null;
    }
    
    /**
     * Check if specific dataset files exist in a directory
     */
    private static boolean checkDatasetFiles(Path directory, String[] files) {
        for (String file : files) {
            if (!Files.exists(directory.resolve(file))) {
                return false;
            }
        }
        return true;
    }

    
    /**
     * Show dataset setup instructions
     */
    private static void showDatasetSetupInstructions() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           DATASET SETUP INSTRUCTIONS                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("FlexiRoute requires dataset files organized in subdirectories:");
        System.out.println();
        System.out.println("Expected structure:");
        System.out.println("  dataset/");
        System.out.println("  ├── London/           (Default - 288K nodes)");
        System.out.println("  │   ├── nodes_288016.txt");
        System.out.println("  │   └── edges_288016.txt");
        System.out.println("  └── California/       (21K nodes)");
        System.out.println("      ├── nodes_21048.txt");
        System.out.println("      └── edges_21048.txt");
        System.out.println();
        System.out.println("To add datasets:");
        System.out.println("1. Create subdirectories in: " + Paths.get(System.getProperty("user.dir"), DATASET_DIR).toAbsolutePath());
        System.out.println("2. Place nodes_*.txt and edges_*.txt files in each subdirectory");
        System.out.println("3. Restart the application");
        System.out.println();
        System.out.println("For converting London datasets, see: scripts/README_LONDON.md");
        System.out.println();
    }
    
    /**
     * Get the absolute path to a dataset file
     */
    public static String getDatasetFilePath(String filename) {
        String projectRoot = System.getProperty("user.dir");
        return Paths.get(projectRoot, DATASET_DIR, filename).toAbsolutePath().toString();
    }
    
    /**
     * List all files in dataset subdirectories
     */
    public static void listDatasetFiles() {
        try {
            String projectRoot = System.getProperty("user.dir");
            Path datasetPath = Paths.get(projectRoot, DATASET_DIR);
            
            if (!Files.exists(datasetPath)) {
                System.out.println("[Dataset] Dataset directory does not exist.");
                return;
            }
            
            System.out.println("[Dataset] Available datasets:");
            for (String subdir : DATASET_SUBDIRS) {
                Path subdirPath = datasetPath.resolve(subdir);
                if (Files.isDirectory(subdirPath)) {
                    System.out.println("[Dataset]   " + subdir + "/");
                    Files.list(subdirPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                long size = Files.size(file);
                                String sizeStr = formatFileSize(size);
                                System.out.println("[Dataset]     - " + file.getFileName() + " (" + sizeStr + ")");
                            } catch (IOException e) {
                                System.out.println("[Dataset]     - " + file.getFileName());
                            }
                        });
                }
            }
                
        } catch (IOException e) {
            System.err.println("[Dataset] Error listing files: " + e.getMessage());
        }
    }
    
    /**
     * Format file size for display
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("=== FlexiRoute Dataset Manager ===");
        System.out.println();
        
        String datasetDir = ensureDatasetExists();
        System.out.println();
        
        listDatasetFiles();
        System.out.println();
        
        System.out.println("Dataset directory: " + datasetDir);
    }
}
