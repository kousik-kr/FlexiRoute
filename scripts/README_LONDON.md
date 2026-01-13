# London Dataset Conversion

This directory contains the conversion script for the London road network dataset.

## Source Data

- **File**: `London_Edgelist.csv` (52 MB, 753,959 edges)
- **Format**: CSV with columns: XCoord, YCoord, START_NODE, END_NODE, EDGE, LENGTH
- **Coordinate System**: British National Grid (EPSG:27700)
- **Distance Unit**: Meters

## Conversion Process

The `convert_london.py` script performs the following transformations:

1. **Coordinate Conversion**: Converts British National Grid coordinates to WGS84 (lat/lon) for map compatibility
2. **Node Extraction**: Extracts 288,016 unique nodes from edge endpoints
3. **Edge Deduplication**: Removes duplicate edges (753,958 → 744,610 edges)
4. **Node Renumbering**: Renumbers nodes to be contiguous (0 to N-1)
5. **Time-Dependent Costs**: Generates rush hour travel time variations
6. **Width Assignment**: Implements UK urban clearway conditions (5% of roads wider during rush hours)

## Output Format

### nodes_288016.txt
Format: `id latitude longitude clusterId`
- 288,016 nodes
- WGS84 coordinates
- All nodes in cluster 1

### edges_288016.txt
- Line 1: Arrival time series (12 time points with rush hours: 7:30-9:30am, 4:00-6:30pm)
- Line 2: Width time series (placeholder)
- Remaining lines: `src dst travel_costs baseWidth rushWidth distance`
  - travel_costs: Comma-separated time-dependent costs for each time point
  - baseWidth: Lane width in meters (3.5m default)
  - rushWidth: Width during rush hour (4.5m for clearway roads, 3.5m for regular roads)
  - distance: Edge length in meters

## Usage

```bash
# Run from project root
python3 scripts/convert_london.py

# Custom parameters
python3 scripts/convert_london.py \
    --base-width 3.5 \
    --rush-width 2.5 \
    --clearway-width 4.5 \
    --clearway-pct 5 \
    --speed 100 \
    --density 20
```

## Parameters

- `--base-width`: Base lane width in meters (default: 3.5m)
- `--rush-width`: Rush hour width for non-clearway roads (default: 2.5m)
- `--clearway-width`: Rush hour width for clearway roads (default: 4.5m)
- `--clearway-pct`: Percentage of roads with clearway condition (default: 5%)
- `--speed`: Base travel speed in meters/minute (default: 100 m/min = 6 km/h)
- `--density`: Percentage of edges with positive scores (default: 20%)

## Features

### Time-Dependent Travel Costs
- Base travel time: `distance / speed`
- Rush hour multipliers (based on position within rush period):
  - Start/End (positions 0, 4): 10-15% increase
  - Early/Late middle (positions 1, 3): 20-25% increase
  - Peak middle (position 2): 30-40% increase

### UK Urban Clearway Condition
- 5% of roads designated as clearway (no parking during certain hours)
- Clearway roads: Width increases from 3.5m to 4.5m during rush hours
- Regular roads: Constant width (3.5m) throughout the day

### Coordinate System
The conversion uses the `pyproj` library (if available) for accurate coordinate transformation from British National Grid (EPSG:27700) to WGS84 (EPSG:4326). If `pyproj` is not installed, it falls back to an approximate conversion formula.

To install pyproj:
```bash
pip install pyproj
```

## Statistics

- **Original nodes**: 288,016
- **Original edges**: 753,958
- **After deduplication**: 744,610 edges
- **File sizes**:
  - nodes_288016.txt: ~13 MB
  - edges_288016.txt: ~104 MB

## Integration

The converted files are compatible with the FlexiRoute routing engine and can be loaded through the GUI or command-line interface. The application will automatically detect and use these files if placed in the `dataset/` directory.
