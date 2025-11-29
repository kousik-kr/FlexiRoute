import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Automatic dataset downloader for Wide-Path
 * Downloads graph data from Google Drive if not present locally
 */
public class DatasetDownloader {
    
    // Google Drive folder ID from the shared link
    private static final String DRIVE_FOLDER_ID = "1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP";
    
    // Dataset directory relative to project root
    private static final String DATASET_DIR = "dataset";
    
    // Expected dataset files
    private static final String[] REQUIRED_FILES = {
        "edges_264346.txt",
        "edges_264346_old.txt",
        "graph_264346.txt",
        "graph_264346_old.txt",
        "nodes_264346.txt",
        "nodes_264346_old.txt"
    };
    
    /**
     * Check if dataset exists, download if missing
     * @return Path to dataset directory
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
            
            // Check if required files exist
            boolean allFilesExist = checkRequiredFiles(datasetPath);
            
            if (!allFilesExist) {
                System.out.println("[Dataset] Dataset files not found.");
                System.out.println();
                
                // Show manual download instructions immediately
                showManualInstructions();
                
                // Check which files are missing
                System.out.println("File status:");
                for (String file : REQUIRED_FILES) {
                    Path filePath = datasetPath.resolve(file);
                    String status = Files.exists(filePath) ? "✓ Found" : "✗ Missing";
                    System.out.println("  " + status + " - " + file);
                }
                System.out.println();
                
                // Try automatic download if gdown available
                if (isGdownAvailable()) {
                    System.out.println("[Dataset] gdown detected - attempting automatic download...");
                    downloadUsingGdown(datasetPath);
                } else {
                    System.out.println("[Dataset] For automatic download, install gdown:");
                    System.out.println("[Dataset]   pip3 install gdown --break-system-packages");
                    System.out.println();
                }
            } else {
                System.out.println("[Dataset] ✓ Dataset files found!");
            }
            
            return datasetPath.toAbsolutePath().toString();
            
        } catch (IOException e) {
            System.err.println("[Dataset] Error: " + e.getMessage());
            return DATASET_DIR;
        }
    }
    
    /**
     * Check if all required dataset files exist
     */
    private static boolean checkRequiredFiles(Path datasetPath) {
        // Check for at least one set of files (old or new)
        boolean hasNewFiles = Files.exists(datasetPath.resolve("nodes_264346.txt")) &&
                              Files.exists(datasetPath.resolve("edges_264346.txt"));
        
        boolean hasOldFiles = Files.exists(datasetPath.resolve("nodes_264346_old.txt")) &&
                             Files.exists(datasetPath.resolve("edges_264346_old.txt"));
        
        return hasNewFiles || hasOldFiles;
    }
    
    /**
     * Check if gdown is available for automatic downloads
     */
    private static boolean isGdownAvailable() {
        try {
            Process process = new ProcessBuilder("gdown", "--version")
                .redirectErrorStream(true)
                .start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Download dataset using gdown (Python tool)
     */
    private static void downloadUsingGdown(Path datasetPath) {
        try {
            System.out.println("[Dataset] Attempting automatic download...");
            System.out.println("[Dataset] This may take several minutes depending on file size.");
            System.out.println();
            
            // Use gdown to download entire folder
            ProcessBuilder pb = new ProcessBuilder(
                "gdown",
                "--folder",
                "https://drive.google.com/drive/folders/" + DRIVE_FOLDER_ID,
                "-O",
                datasetPath.toAbsolutePath().toString(),
                "--remaining-ok"
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Show download progress
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Dataset] " + line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println();
                System.out.println("[Dataset] ✓ Download completed successfully!");
                System.out.println("[Dataset] Restart the application to load the dataset.");
            } else {
                System.out.println();
                System.out.println("[Dataset] ✗ Automatic download failed.");
                showManualInstructions();
            }
            
        } catch (Exception e) {
            System.out.println();
            System.out.println("[Dataset] ✗ Automatic download not available.");
            showManualInstructions();
        }
    }
    
    /**
     * Show detailed manual download instructions
     */
    private static void showManualInstructions() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           MANUAL DOWNLOAD INSTRUCTIONS                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Please follow these steps:");
        System.out.println();
        System.out.println("1. Open this link in your browser:");
        System.out.println("   https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP");
        System.out.println();
        System.out.println("2. Download ALL .txt files from the folder");
        System.out.println();
        System.out.println("3. Place the files in this directory:");
        System.out.println("   " + Paths.get(System.getProperty("user.dir"), DATASET_DIR).toAbsolutePath());
        System.out.println();
        System.out.println("4. Restart the application");
        System.out.println();
        System.out.println("Need help? See: dataset/README.md");
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
     * List all files in the dataset directory
     */
    public static void listDatasetFiles() {
        try {
            String projectRoot = System.getProperty("user.dir");
            Path datasetPath = Paths.get(projectRoot, DATASET_DIR);
            
            if (!Files.exists(datasetPath)) {
                System.out.println("[Dataset] Dataset directory does not exist.");
                return;
            }
            
            System.out.println("[Dataset] Files in dataset directory:");
            Files.list(datasetPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        long size = Files.size(file);
                        String sizeStr = formatFileSize(size);
                        System.out.println("[Dataset]   - " + file.getFileName() + " (" + sizeStr + ")");
                    } catch (IOException e) {
                        System.out.println("[Dataset]   - " + file.getFileName());
                    }
                });
                
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
        System.out.println("=== Wide-Path Dataset Manager ===");
        System.out.println();
        
        String datasetDir = ensureDatasetExists();
        System.out.println();
        
        listDatasetFiles();
        System.out.println();
        
        System.out.println("Dataset directory: " + datasetDir);
    }
}
