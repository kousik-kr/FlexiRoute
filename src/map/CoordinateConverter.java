package map;

import java.awt.geom.Point2D;

/**
 * Coordinate Converter - Handles conversions between geographic coordinates and screen pixels.
 * 
 * Architecture:
 * +--------------------------+
 * | CoordinateConverter      |
 * |  - Lat/Lon → Pixel       |
 * |  - Pixel → Lat/Lon       |
 * |  - Mercator projection   |
 * |  - Zoom level handling   |
 * +--------------------------+
 * 
 * Uses Web Mercator projection (EPSG:3857) for compatibility with OSM tiles.
 */
public class CoordinateConverter {
    
    // Tile size in pixels
    private static final int TILE_SIZE = 256;
    
    // Earth's radius in meters (for distance calculations)
    private static final double EARTH_RADIUS = 6378137.0;
    
    // Maximum latitude for Web Mercator
    private static final double MAX_LAT = 85.0511287798;
    
    // View parameters
    private double centerLat;
    private double centerLon;
    private int zoomLevel;
    private int viewWidth;
    private int viewHeight;
    
    // Pan offset in pixels
    private double panOffsetX = 0;
    private double panOffsetY = 0;
    
    public CoordinateConverter() {
        this(42.0, -122.0, 12, 800, 600); // Default: Northern California (dataset location)
    }
    
    public CoordinateConverter(double centerLat, double centerLon, int zoomLevel, int viewWidth, int viewHeight) {
        this.centerLat = Math.max(-MAX_LAT, Math.min(MAX_LAT, centerLat));
        this.centerLon = centerLon;
        this.zoomLevel = zoomLevel;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }
    
    /**
     * Convert latitude/longitude to screen pixel coordinates
     */
    public Point2D.Double latLonToPixel(double lat, double lon) {
        // Clamp latitude to valid Mercator range
        lat = Math.max(-MAX_LAT, Math.min(MAX_LAT, lat));
        
        // Calculate world pixel coordinates
        double worldX = lonToWorldPixelX(lon, zoomLevel);
        double worldY = latToWorldPixelY(lat, zoomLevel);
        
        // Calculate center world pixel coordinates
        double centerWorldX = lonToWorldPixelX(centerLon, zoomLevel);
        double centerWorldY = latToWorldPixelY(centerLat, zoomLevel);
        
        // Convert to screen coordinates
        double screenX = worldX - centerWorldX + viewWidth / 2.0 + panOffsetX;
        double screenY = worldY - centerWorldY + viewHeight / 2.0 + panOffsetY;
        
        return new Point2D.Double(screenX, screenY);
    }
    
    /**
     * Convert screen pixel coordinates to latitude/longitude
     */
    public double[] pixelToLatLon(double screenX, double screenY) {
        // Convert screen to world pixel coordinates
        double centerWorldX = lonToWorldPixelX(centerLon, zoomLevel);
        double centerWorldY = latToWorldPixelY(centerLat, zoomLevel);
        
        double worldX = screenX - viewWidth / 2.0 - panOffsetX + centerWorldX;
        double worldY = screenY - viewHeight / 2.0 - panOffsetY + centerWorldY;
        
        // Convert world pixel to lat/lon
        double lon = worldPixelXToLon(worldX, zoomLevel);
        double lat = worldPixelYToLat(worldY, zoomLevel);
        
        return new double[] { lat, lon };
    }
    
    /**
     * Convert longitude to world pixel X (at given zoom level)
     */
    public static double lonToWorldPixelX(double lon, int zoom) {
        double n = Math.pow(2, zoom);
        return ((lon + 180.0) / 360.0) * n * TILE_SIZE;
    }
    
    /**
     * Convert latitude to world pixel Y (at given zoom level)
     */
    public static double latToWorldPixelY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        double n = Math.pow(2, zoom);
        double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0;
        return y * n * TILE_SIZE;
    }
    
    /**
     * Convert world pixel X to longitude
     */
    public static double worldPixelXToLon(double worldX, int zoom) {
        double n = Math.pow(2, zoom);
        return worldX / (n * TILE_SIZE) * 360.0 - 180.0;
    }
    
    /**
     * Convert world pixel Y to latitude
     */
    public static double worldPixelYToLat(double worldY, int zoom) {
        double n = Math.pow(2, zoom);
        double y = worldY / (n * TILE_SIZE);
        double latRad = Math.atan(Math.sinh(Math.PI * (1.0 - 2.0 * y)));
        return Math.toDegrees(latRad);
    }
    
    /**
     * Get the tile coordinates for a given lat/lon
     */
    public int[] getTileForLatLon(double lat, double lon) {
        int tileX = (int) Math.floor(lonToWorldPixelX(lon, zoomLevel) / TILE_SIZE);
        int tileY = (int) Math.floor(latToWorldPixelY(lat, zoomLevel) / TILE_SIZE);
        return new int[] { tileX, tileY };
    }
    
    /**
     * Get tile coordinates and pixel offset within tile for a lat/lon
     */
    public TilePosition getTilePosition(double lat, double lon) {
        double worldX = lonToWorldPixelX(lon, zoomLevel);
        double worldY = latToWorldPixelY(lat, zoomLevel);
        
        int tileX = (int) Math.floor(worldX / TILE_SIZE);
        int tileY = (int) Math.floor(worldY / TILE_SIZE);
        
        double offsetX = worldX - tileX * TILE_SIZE;
        double offsetY = worldY - tileY * TILE_SIZE;
        
        return new TilePosition(tileX, tileY, offsetX, offsetY);
    }
    
    /**
     * Get visible tile range
     */
    public int[] getVisibleTileRange() {
        // Get corner coordinates
        double[] topLeft = pixelToLatLon(0, 0);
        double[] bottomRight = pixelToLatLon(viewWidth, viewHeight);
        
        // Get tile coordinates
        int[] topLeftTile = getTileForLatLon(topLeft[0], topLeft[1]);
        int[] bottomRightTile = getTileForLatLon(bottomRight[0], bottomRight[1]);
        
        // Add buffer of 1 tile on each side
        return new int[] {
            topLeftTile[0] - 1,      // minTileX
            bottomRightTile[0] + 1,  // maxTileX
            topLeftTile[1] - 1,      // minTileY
            bottomRightTile[1] + 1   // maxTileY
        };
    }
    
    /**
     * Calculate Haversine distance between two points in meters
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Calculate bearing between two points in degrees
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double x = Math.sin(dLon) * Math.cos(lat2Rad);
        double y = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);
        
        return (Math.toDegrees(Math.atan2(x, y)) + 360) % 360;
    }
    
    /**
     * Calculate zoom level to fit bounds
     */
    public static int calculateZoomToFit(double minLat, double maxLat, double minLon, double maxLon, 
                                          int viewWidth, int viewHeight) {
        // Add padding
        double latPadding = (maxLat - minLat) * 0.1;
        double lonPadding = (maxLon - minLon) * 0.1;
        minLat -= latPadding;
        maxLat += latPadding;
        minLon -= lonPadding;
        maxLon += lonPadding;
        
        // Try zoom levels from high to low
        for (int zoom = 18; zoom >= 1; zoom--) {
            double minX = lonToWorldPixelX(minLon, zoom);
            double maxX = lonToWorldPixelX(maxLon, zoom);
            double minY = latToWorldPixelY(maxLat, zoom); // Note: Y is inverted
            double maxY = latToWorldPixelY(minLat, zoom);
            
            double boundsWidth = maxX - minX;
            double boundsHeight = maxY - minY;
            
            if (boundsWidth <= viewWidth && boundsHeight <= viewHeight) {
                return zoom;
            }
        }
        
        return 1;
    }
    
    // Getters and setters
    
    public void setCenter(double lat, double lon) {
        this.centerLat = Math.max(-MAX_LAT, Math.min(MAX_LAT, lat));
        this.centerLon = lon;
    }
    
    public double getCenterLat() {
        return centerLat;
    }
    
    public double getCenterLon() {
        return centerLon;
    }
    
    public void setZoomLevel(int zoom) {
        this.zoomLevel = Math.max(1, Math.min(19, zoom));
    }
    
    public int getZoomLevel() {
        return zoomLevel;
    }
    
    public void setViewSize(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
    }
    
    public int getViewWidth() {
        return viewWidth;
    }
    
    public int getViewHeight() {
        return viewHeight;
    }
    
    public void setPanOffset(double x, double y) {
        this.panOffsetX = x;
        this.panOffsetY = y;
    }
    
    public void addPanOffset(double dx, double dy) {
        this.panOffsetX += dx;
        this.panOffsetY += dy;
    }
    
    public double getPanOffsetX() {
        return panOffsetX;
    }
    
    public double getPanOffsetY() {
        return panOffsetY;
    }
    
    public void resetPan() {
        this.panOffsetX = 0;
        this.panOffsetY = 0;
    }
    
    /**
     * Helper class for tile position with offset
     */
    public static class TilePosition {
        public final int tileX;
        public final int tileY;
        public final double offsetX;
        public final double offsetY;
        
        public TilePosition(int tileX, int tileY, double offsetX, double offsetY) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }
}
