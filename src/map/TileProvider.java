package map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

/**
 * OSM Tile Provider - Downloads and caches map tiles from OpenStreetMap servers.
 * 
 * Architecture:
 * +--------------------------+
 * | TileProvider             |
 * |  - Tile URL generation   |
 * |  - HTTP tile fetching    |
 * |  - Memory/disk caching   |
 * |  - Async tile loading    |
 * +--------------------------+
 * 
 * Supports multiple tile servers for reliability and different map styles.
 */
public class TileProvider {
    
    // Standard OSM tile size
    public static final int TILE_SIZE = 256;
    
    // Tile server URLs - using OpenStreetMap and alternatives
    public enum TileServer {
        OSM_STANDARD("https://tile.openstreetmap.org/{z}/{x}/{y}.png", "OpenStreetMap"),
        OSM_HUMANITARIAN("https://a.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png", "Humanitarian"),
        CARTO_LIGHT("https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png", "Carto Light"),
        CARTO_DARK("https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png", "Carto Dark"),
        STAMEN_TERRAIN("https://stamen-tiles.a.ssl.fastly.net/terrain/{z}/{x}/{y}.png", "Terrain");
        
        private final String urlTemplate;
        private final String displayName;
        
        TileServer(String urlTemplate, String displayName) {
            this.urlTemplate = urlTemplate;
            this.displayName = displayName;
        }
        
        public String getUrl(int x, int y, int z) {
            return urlTemplate
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // In-memory tile cache
    private final ConcurrentHashMap<String, BufferedImage> tileCache = new ConcurrentHashMap<>();
    
    // Track pending tile loads to avoid duplicate requests
    private final ConcurrentHashMap<String, Boolean> pendingTiles = new ConcurrentHashMap<>();
    
    // Disk cache directory
    private final File cacheDir;
    
    // Background loader - increased pool size for faster loading
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    
    // Current tile server
    private TileServer currentServer = TileServer.OSM_STANDARD;
    
    // Tile load listeners
    private TileLoadListener listener;
    
    // Maximum cache size (tiles) - increased for better performance
    private static final int MAX_CACHE_SIZE = 1000;
    
    public interface TileLoadListener {
        void onTileLoaded(int x, int y, int z, BufferedImage tile);
        void onTileError(int x, int y, int z, Exception e);
    }
    
    public TileProvider() {
        // Create cache directory
        String userHome = System.getProperty("user.home");
        cacheDir = new File(userHome, ".flexiroute/tiles");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }
    
    /**
     * Convert latitude to tile Y coordinate
     */
    public static int latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        int n = 1 << zoom;
        // asinh(x) = log(x + sqrt(x^2 + 1)) - compatible with Java 8
        double tanLat = Math.tan(latRad);
        double asinh = Math.log(tanLat + Math.sqrt(tanLat * tanLat + 1));
        return (int) Math.floor((1.0 - asinh / Math.PI) / 2.0 * n);
    }
    
    /**
     * Convert longitude to tile X coordinate
     */
    public static int lonToTileX(double lon, int zoom) {
        int n = 1 << zoom;
        return (int) Math.floor((lon + 180.0) / 360.0 * n);
    }
    
    /**
     * Convert tile X to longitude
     */
    public static double tileXToLon(int x, int zoom) {
        int n = 1 << zoom;
        return x * 360.0 / n - 180.0;
    }
    
    /**
     * Convert tile Y to latitude
     */
    public static double tileYToLat(int y, int zoom) {
        int n = 1 << zoom;
        double latRad = Math.atan(Math.sinh(Math.PI * (1 - 2.0 * y / n)));
        return Math.toDegrees(latRad);
    }
    
    /**
     * Get tile from cache or load asynchronously
     */
    public BufferedImage getTile(int x, int y, int z) {
        String key = getCacheKey(x, y, z);
        
        // Check memory cache
        BufferedImage cached = tileCache.get(key);
        if (cached != null) {
            return cached;
        }
        
        // Check disk cache
        File diskFile = getDiskCacheFile(x, y, z);
        if (diskFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(diskFile);
                if (img != null) {
                    addToCache(key, img);
                    return img;
                }
            } catch (IOException e) {
                // Ignore, will fetch from network
            }
        }
        
        // Load asynchronously
        loadTileAsync(x, y, z);
        
        return null; // Return null, tile will be loaded in background
    }
    
    /**
     * Load tile synchronously (blocking)
     */
    public BufferedImage getTileSync(int x, int y, int z) {
        String key = getCacheKey(x, y, z);
        
        // Check memory cache first
        BufferedImage cached = tileCache.get(key);
        if (cached != null) {
            return cached;
        }
        
        // Try disk cache
        File diskFile = getDiskCacheFile(x, y, z);
        if (diskFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(diskFile);
                if (img != null) {
                    addToCache(key, img);
                    return img;
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        
        // Fetch from network
        try {
            return downloadTile(x, y, z);
        } catch (IOException e) {
            System.err.println("Failed to download tile: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Load tile in background thread
     */
    private void loadTileAsync(int x, int y, int z) {
        String key = getCacheKey(x, y, z);
        
        // Skip if already pending
        if (pendingTiles.putIfAbsent(key, true) != null) {
            return; // Already loading
        }
        
        executor.submit(() -> {
            try {
                BufferedImage tile = downloadTile(x, y, z);
                if (tile != null && listener != null) {
                    listener.onTileLoaded(x, y, z, tile);
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onTileError(x, y, z, e);
                }
            } finally {
                pendingTiles.remove(key);
            }
        });
    }
    
    /**
     * Download tile from server
     */
    private BufferedImage downloadTile(int x, int y, int z) throws IOException {
        String urlStr = currentServer.getUrl(x, y, z);
        
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "FlexiRoute/1.0 (https://github.com/flexiroute)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream()) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null) {
                        // Save to disk cache
                        saveToDiskCache(img, x, y, z);
                        // Add to memory cache
                        addToCache(getCacheKey(x, y, z), img);
                    }
                    return img;
                }
            } else {
                System.err.println("Failed to download tile " + z + "/" + x + "/" + y + ": HTTP " + responseCode);
            }
        } catch (Exception e) {
            throw new IOException("Tile download failed", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        return null;
    }
    
    private void saveToDiskCache(BufferedImage img, int x, int y, int z) {
        File file = getDiskCacheFile(x, y, z);
        file.getParentFile().mkdirs();
        try {
            ImageIO.write(img, "PNG", file);
        } catch (IOException e) {
            // Ignore cache write failures
        }
    }
    
    private File getDiskCacheFile(int x, int y, int z) {
        return new File(cacheDir, currentServer.name() + "/" + z + "/" + x + "/" + y + ".png");
    }
    
    private String getCacheKey(int x, int y, int z) {
        return currentServer.name() + "_" + z + "_" + x + "_" + y;
    }
    
    private void addToCache(String key, BufferedImage img) {
        // Simple cache eviction if too large
        if (tileCache.size() > MAX_CACHE_SIZE) {
            // Remove first 100 entries
            int count = 0;
            for (String k : tileCache.keySet()) {
                if (count++ < 100) {
                    tileCache.remove(k);
                } else {
                    break;
                }
            }
        }
        tileCache.put(key, img);
    }
    
    public void setTileServer(TileServer server) {
        this.currentServer = server;
    }
    
    public TileServer getTileServer() {
        return currentServer;
    }
    
    public void setTileLoadListener(TileLoadListener listener) {
        this.listener = listener;
    }
    
    public void clearCache() {
        tileCache.clear();
        pendingTiles.clear();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
