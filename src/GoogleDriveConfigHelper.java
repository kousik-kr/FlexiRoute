import java.io.*;
import java.util.Properties;

/**
 * Helper class for managing Google Drive configuration for dataset downloads.
 * Stores and retrieves Google Drive file IDs for nodes and edges datasets.
 */
public class GoogleDriveConfigHelper {
    private static final String CONFIG_FILE = "drive_config.properties";
    private static final String NODES_FILE_ID_KEY = "drive.nodes.fileId";
    private static final String EDGES_FILE_ID_KEY = "drive.edges.fileId";
    private static final String CLUSTERS_FILE_ID_KEY = "drive.clusters.fileId";
    private static final String WIDTH_DIST_FILE_ID_KEY = "drive.widthDist.fileId";
    private static final String DATASET_DIR_KEY = "dataset.directory";
    
    /**
     * Check if dataset files exist in the configured directory
     */
    public static boolean checkDatasetExists() {
        Properties props = loadConfig();
        String datasetDir = props.getProperty(DATASET_DIR_KEY, "dataset");
        
        // Check for files with the naming convention used by BidirectionalAstar
        File nodesFile = new File(datasetDir, "nodes_264346.txt");
        File edgesFile = new File(datasetDir, "edges_264346.txt");
        File clustersFile = new File(datasetDir, "clusters_264346.txt");
        File widthDistFile = new File(datasetDir, "width_dist_264346.txt");
        
        return nodesFile.exists() && edgesFile.exists() && 
               clustersFile.exists() && widthDistFile.exists();
    }
    
    /**
     * Get the configured dataset directory
     */
    public static String getDatasetDirectory() {
        Properties props = loadConfig();
        return props.getProperty(DATASET_DIR_KEY, "dataset");
    }
    
    /**
     * Get the Google Drive file ID for nodes dataset
     */
    public static String getNodesFileId() {
        Properties props = loadConfig();
        return props.getProperty(NODES_FILE_ID_KEY, "");
    }
    
    /**
     * Get the Google Drive file ID for edges dataset
     */
    public static String getEdgesFileId() {
        Properties props = loadConfig();
        return props.getProperty(EDGES_FILE_ID_KEY, "");
    }
    
    /**
     * Get the Google Drive file ID for clusters dataset
     */
    public static String getClustersFileId() {
        Properties props = loadConfig();
        return props.getProperty(CLUSTERS_FILE_ID_KEY, "");
    }
    
    /**
     * Get the Google Drive file ID for width-distance dataset
     */
    public static String getWidthDistFileId() {
        Properties props = loadConfig();
        return props.getProperty(WIDTH_DIST_FILE_ID_KEY, "");
    }
    
    /**
     * Save configuration to file
     */
    public static void saveConfig(String nodesFileId, String edgesFileId, 
                                   String clustersFileId, String widthDistFileId, 
                                   String datasetDir) {
        Properties props = new Properties();
        props.setProperty(NODES_FILE_ID_KEY, nodesFileId);
        props.setProperty(EDGES_FILE_ID_KEY, edgesFileId);
        props.setProperty(CLUSTERS_FILE_ID_KEY, clustersFileId);
        props.setProperty(WIDTH_DIST_FILE_ID_KEY, widthDistFileId);
        props.setProperty(DATASET_DIR_KEY, datasetDir);
        
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Google Drive Dataset Configuration");
            System.out.println("[GoogleDriveConfig] Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("[GoogleDriveConfig] Failed to save configuration: " + e.getMessage());
        }
    }
    
    /**
     * Load configuration from file
     */
    private static Properties loadConfig() {
        Properties props = new Properties();
        
        // Set default values
        props.setProperty(DATASET_DIR_KEY, "dataset");
        
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("[GoogleDriveConfig] Failed to load configuration: " + e.getMessage());
            }
        }
        
        return props;
    }
    
    /**
     * Check if Google Drive is configured
     */
    public static boolean isConfigured() {
        String nodesId = getNodesFileId();
        String edgesId = getEdgesFileId();
        String clustersId = getClustersFileId();
        String widthDistId = getWidthDistFileId();
        return !nodesId.isEmpty() && !edgesId.isEmpty() && 
               !clustersId.isEmpty() && !widthDistId.isEmpty();
    }
}
