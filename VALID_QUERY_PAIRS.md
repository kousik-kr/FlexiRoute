# Valid Query Pairs for California Dataset Testing

## Dataset Summary
- **Total Nodes**: 21,048
- **Total Edges**: 21,693
- **Nodes with outgoing edges**: 19,596 (93%)
- **Nodes with incoming edges**: 20,238 (96%)
- **Isolated nodes**: 0 (all nodes are connected to the graph)

## Sample Valid Query Pairs

These pairs have been verified to be connected via BFS traversal:

### Query 1: Adjacent Nodes
- **Source**: 0 (lat=41.974556, lon=-121.904167)
- **Destination**: 1 (lat=41.974766, lon=-121.902153)
- **Distance**: ~0.17 km
- **Test**: `0 → 1, departure=0:00, budget=60 min`

### Query 2: Short Distance
- **Source**: 0 (lat=41.974556, lon=-121.904167)  
- **Destination**: 6 (lat=41.973942, lon=-121.910088)
- **Distance**: ~0.49 km
- **Test**: `0 → 6, departure=0:00, budget=60 min`

### Query 3: Short-Medium Distance
- **Source**: 2 (lat=41.988075, lon=-121.896790)
- **Destination**: 3 (lat=41.998032, lon=-121.889603)
- **Distance**: ~1.26 km
- **Test**: `2 → 3, departure=0:00, budget=60 min`

### Query 4: Medium Distance
- **Source**: 1 (lat=41.974766, lon=-121.902153)
- **Destination**: 2 (lat=41.988075, lon=-121.896790)
- **Distance**: ~1.54 km
- **Test**: `1 → 2, departure=0:00, budget=60 min`

### Query 5: Medium Distance  
- **Source**: 0 (lat=41.974556, lon=-121.904167)
- **Destination**: 2 (lat=41.988075, lon=-121.896790)
- **Distance**: ~1.62 km
- **Test**: `0 → 2, departure=0:00, budget=60 min`

### Query 6-10: Additional Connected Pairs
All verified as reachable via BFS:
- `0 → 3` (2.87 km)
- `0 → 4` (4.07 km)
- `1 → 3` (2.79 km)
- `1 → 4` (3.99 km)
- `2 → 4` (2.44 km)

## Testing Notes

### Dataset Status: ✅ COMPLETE
- California dataset successfully converted and loaded
- Merged format working correctly
- All 21,048 nodes loaded with cluster information
- All 21,693 edges loaded with time-dependent costs (12 values per edge)
- Edge properties (baseWidth, rushWidth, distance) properly stored

### Pathfinding Algorithm Status: ⚠️ HAS BUGS
**Current Issue**: `NullPointerException` in `Graph.java:278`
- **Error**: "Cannot invoke 'java.lang.Double.doubleValue()' because the return value of 'java.util.Map.get(Object)' is null"
- **Location**: Backward A* algorithm
- **Cause**: Algorithm trying to access time property with a key that doesn't exist in the time property map
- **Impact**: Prevents ALL queries from finding paths, even for connected nodes

### Recommended Test Commands (once pathfinding bug is fixed):

```bash
# Test in GUI - enter these values:
Source: 0, Destination: 1, Departure: 0:00, Budget: 60 min
Source: 0, Destination: 6, Departure: 0:00, Budget: 60 min  
Source: 2, Destination: 3, Departure: 8:30, Budget: 60 min  # rush hour
Source: 0, Destination: 2, Departure: 17:00, Budget: 60 min # evening rush
```

## Conclusion

**Dataset Conversion**: ✅ SUCCESS
- California road network (21,048 nodes, 21,693 edges) successfully converted
- Merged format fully operational
- Time-dependent costs generated with rush hour variations
- All data loading correctly in both GUI and command-line

**Pathfinding Algorithm**: ⚠️ NEEDS FIX  
- Bug in backward A* prevents path finding
- Issue is in the core algorithm, NOT related to dataset format
- Once fixed, the valid query pairs listed above should return paths successfully
