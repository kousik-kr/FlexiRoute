package map;

/**
 * MapViewMode - Controls the map rendering approach.
 * 
 * The application supports two rendering modes:
 * 1. OSM_TILES - Real OpenStreetMap tiles with route overlay
 * 2. COORDINATE_BASED - Original coordinate-based rendering (legacy)
 * 
 * This enum allows switching between the two approaches.
 */
public enum MapViewMode {
    OSM_TILES("🗺️ OSM Map", "Real OpenStreetMap tiles with route overlay"),
    COORDINATE_BASED("📐 Graph View", "Coordinate-based visualization (legacy)");
    
    private final String displayName;
    private final String description;
    
    MapViewMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
