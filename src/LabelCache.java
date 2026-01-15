import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import models.RoutingMode;

/**
 * LabelCache - Caches bidirectional labeling results for fast re-computation
 * 
 * When only the routing mode changes (not source, destination, budget, or departure time),
 * we can reuse the cached labels to instantly compute new paths without re-running
 * the expensive labeling algorithm.
 */
public class LabelCache {
    
    // Query parameters that invalidate the cache if changed
    private int cachedSource = -1;
    private int cachedDestination = -1;
    private double cachedBudget = -1;
    private double cachedDepartureTime = -1;
    
    // Cached labeling results
    private Set<Integer> cachedIntersectionNodes;
    private ConcurrentHashMap<Integer, PriorityBlockingQueue<Label>> cachedForwardVisited;
    private ConcurrentHashMap<Integer, PriorityBlockingQueue<Label>> cachedBackwardVisited;
    
    // Last computed result per routing mode (optional optimization)
    private Result lastWidenessResult;
    private Result lastTurnsResult;
    private Result lastParetoResult;
    
    // Singleton instance
    private static LabelCache instance;
    
    private LabelCache() {}
    
    public static synchronized LabelCache getInstance() {
        if (instance == null) {
            instance = new LabelCache();
        }
        return instance;
    }
    
    /**
     * Check if the cache is valid for the given query parameters
     */
    public boolean isValid(int source, int destination, double budget, double departureTime) {
        return cachedSource == source 
            && cachedDestination == destination 
            && Math.abs(cachedBudget - budget) < 0.001
            && Math.abs(cachedDepartureTime - departureTime) < 0.001
            && cachedIntersectionNodes != null
            && cachedForwardVisited != null
            && cachedBackwardVisited != null
            && !cachedIntersectionNodes.isEmpty();
    }
    
    /**
     * Store labeling results in the cache
     */
    public void store(int source, int destination, double budget, double departureTime,
                      Set<Integer> intersectionNodes,
                      ConcurrentHashMap<Integer, PriorityBlockingQueue<Label>> forwardVisited,
                      ConcurrentHashMap<Integer, PriorityBlockingQueue<Label>> backwardVisited) {
        this.cachedSource = source;
        this.cachedDestination = destination;
        this.cachedBudget = budget;
        this.cachedDepartureTime = departureTime;
        this.cachedIntersectionNodes = intersectionNodes;
        this.cachedForwardVisited = forwardVisited;
        this.cachedBackwardVisited = backwardVisited;
        
        // Clear cached results since we have new labels
        this.lastWidenessResult = null;
        this.lastTurnsResult = null;
        this.lastParetoResult = null;
        
        System.out.println("[LabelCache] Stored labels for " + source + " -> " + destination + 
            " (budget=" + budget + ", departure=" + departureTime + ")");
        System.out.println("[LabelCache] Intersections: " + intersectionNodes.size() + 
            ", Forward nodes: " + forwardVisited.size() + 
            ", Backward nodes: " + backwardVisited.size());
    }
    
    /**
     * Get cached labels for recomputation
     */
    public Set<Integer> getIntersectionNodes() {
        return cachedIntersectionNodes;
    }
    
    public ConcurrentHashMap<Integer, PriorityBlockingQueue<Label>> getForwardVisited() {
        return cachedForwardVisited;
    }
    
    public ConcurrentHashMap<Integer, PriorityBlockingQueue<Label>> getBackwardVisited() {
        return cachedBackwardVisited;
    }
    
    public int getCachedSource() {
        return cachedSource;
    }
    
    public int getCachedDestination() {
        return cachedDestination;
    }
    
    public double getCachedBudget() {
        return cachedBudget;
    }
    
    public double getCachedDepartureTime() {
        return cachedDepartureTime;
    }
    
    /**
     * Clear the cache
     */
    public void clear() {
        cachedSource = -1;
        cachedDestination = -1;
        cachedBudget = -1;
        cachedDepartureTime = -1;
        cachedIntersectionNodes = null;
        cachedForwardVisited = null;
        cachedBackwardVisited = null;
        lastWidenessResult = null;
        lastTurnsResult = null;
        lastParetoResult = null;
        System.out.println("[LabelCache] Cache cleared");
    }
    
    /**
     * Get a short status string
     */
    public String getStatus() {
        if (!isValid(cachedSource, cachedDestination, cachedBudget, cachedDepartureTime)) {
            return "empty";
        }
        return cachedSource + " -> " + cachedDestination + " (budget=" + cachedBudget + ")";
    }
}
