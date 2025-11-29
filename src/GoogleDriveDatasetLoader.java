import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads dataset files from Google Drive.
 * Handles direct download links and provides progress feedback.
 */
public class GoogleDriveDatasetLoader {
    
    /**
     * Download dataset from Google Drive with progress dialog
     */
    public static boolean downloadDataset(JFrame parentFrame) {
        String nodesFileId = GoogleDriveConfigHelper.getNodesFileId();
        String edgesFileId = GoogleDriveConfigHelper.getEdgesFileId();
        String clustersFileId = GoogleDriveConfigHelper.getClustersFileId();
        String widthDistFileId = GoogleDriveConfigHelper.getWidthDistFileId();
        String datasetDir = GoogleDriveConfigHelper.getDatasetDirectory();
        
        if (nodesFileId.isEmpty() || edgesFileId.isEmpty() || 
            clustersFileId.isEmpty() || widthDistFileId.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(parentFrame,
                    "Google Drive file IDs are not configured.\n" +
                    "Would you like to configure them now?",
                    "Configure Google Drive",
                    JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                return showConfigDialog(parentFrame);
            }
            return false;
        }
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Preparing download...");
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Downloading dataset files from Google Drive..."), BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        JDialog progressDialog = new JDialog(parentFrame, "Downloading Dataset", true);
        progressDialog.setContentPane(panel);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(parentFrame);
        
        // Download in background thread
        SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Create dataset directory if it doesn't exist
                    File dir = new File(datasetDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    // Download nodes file
                    publish(5);
                    progressBar.setString("Downloading nodes file...");
                    boolean nodesDownloaded = downloadFile(nodesFileId, 
                            new File(dir, "nodes_264346.txt"), 
                            progress -> publish(5 + progress / 4));
                    
                    if (!nodesDownloaded) {
                        return false;
                    }
                    
                    // Download edges file
                    publish(30);
                    progressBar.setString("Downloading edges file...");
                    boolean edgesDownloaded = downloadFile(edgesFileId, 
                            new File(dir, "edges_264346.txt"),
                            progress -> publish(30 + progress / 4));
                    
                    if (!edgesDownloaded) {
                        return false;
                    }
                    
                    // Download clusters file
                    publish(55);
                    progressBar.setString("Downloading clusters file...");
                    boolean clustersDownloaded = downloadFile(clustersFileId, 
                            new File(dir, "clusters_264346.txt"),
                            progress -> publish(55 + progress / 4));
                    
                    if (!clustersDownloaded) {
                        return false;
                    }
                    
                    // Download width-distance file
                    publish(80);
                    progressBar.setString("Downloading width-distance file...");
                    boolean widthDistDownloaded = downloadFile(widthDistFileId, 
                            new File(dir, "width_dist_264346.txt"),
                            progress -> publish(80 + progress / 4));
                    
                    return widthDistDownloaded;
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
        
        try {
            boolean success = worker.get();
            if (success) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Dataset downloaded successfully!",
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "Failed to download dataset files.\n" +
                        "Please check your internet connection and file IDs.",
                        "Download Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
            return success;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Error during download: " + e.getMessage(),
                    "Download Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Download a single file from Google Drive
     */
    private static boolean downloadFile(String fileId, File destFile, ProgressCallback callback) {
        try {
            // Use Google Drive download URL with confirmation to bypass virus scan warning
            String downloadUrl = "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&confirm=t";
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            
            int responseCode = conn.getResponseCode();
            
            // Handle redirect for large files
            if (responseCode == HttpURLConnection.HTTP_OK) {
                long fileSize = conn.getContentLengthLong();
                
                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytesRead = 0;
                    
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        
                        if (fileSize > 0 && callback != null) {
                            int progress = (int) ((totalBytesRead * 100) / fileSize);
                            callback.onProgress(progress);
                        }
                    }
                    
                    System.out.println("[GoogleDrive] Downloaded: " + destFile.getName() + 
                                     " (" + totalBytesRead + " bytes)");
                    return true;
                }
            } else {
                System.err.println("[GoogleDrive] Failed to download " + fileId + 
                                 ". Response code: " + responseCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("[GoogleDrive] Error downloading " + fileId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show configuration dialog for Google Drive file IDs
     */
    private static boolean showConfigDialog(JFrame parentFrame) {
        JTextField nodesField = new JTextField(30);
        JTextField edgesField = new JTextField(30);
        JTextField clustersField = new JTextField(30);
        JTextField widthDistField = new JTextField(30);
        JTextField dirField = new JTextField(GoogleDriveConfigHelper.getDatasetDirectory(), 30);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Nodes File ID:"));
        panel.add(nodesField);
        panel.add(new JLabel("Edges File ID:"));
        panel.add(edgesField);
        panel.add(new JLabel("Clusters File ID:"));
        panel.add(clustersField);
        panel.add(new JLabel("Width-Distance File ID:"));
        panel.add(widthDistField);
        panel.add(new JLabel("Dataset Directory:"));
        panel.add(dirField);
        
        int result = JOptionPane.showConfirmDialog(parentFrame, panel,
                "Configure Google Drive Dataset", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String nodesId = nodesField.getText().trim();
            String edgesId = edgesField.getText().trim();
            String clustersId = clustersField.getText().trim();
            String widthDistId = widthDistField.getText().trim();
            String dir = dirField.getText().trim();
            
            if (!nodesId.isEmpty() && !edgesId.isEmpty() && 
                !clustersId.isEmpty() && !widthDistId.isEmpty()) {
                GoogleDriveConfigHelper.saveConfig(nodesId, edgesId, clustersId, widthDistId, dir);
                return downloadDataset(parentFrame);
            }
        }
        
        return false;
    }
    
    /**
     * Callback interface for download progress
     */
    @FunctionalInterface
    private interface ProgressCallback {
        void onProgress(int percentage);
    }
}
