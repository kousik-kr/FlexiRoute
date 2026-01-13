"""Convert the London network dataset to Wide-Path format.

The solver expects merged files in the output directory:
- nodes_<N>.txt : space-separated `id latitude longitude clusterId`
- edges_<N>.txt : first line arrival time points, second line width time points,
                  remaining lines `src dst travel_costs baseWidth rushWidth distance`

This script uses the London_Edgelist.csv file which contains:
- XCoord, YCoord, START_NODE, END_NODE, EDGE, LENGTH (in meters)

The coordinates appear to be in British National Grid (EPSG:27700).
We convert them to WGS84 (lat/lon) for compatibility with the mapping system.

Travel time generation:
- Time-dependent costs with rush hour variations (7:30-9:30am, 4:00-6:30pm)
- Base travel time calculated from distance / 100 (100 meter/min default speed)
- Rush hour increases: 10-40% based on time within rush period
- All nodes assigned cluster 1

Usage (from project root):
    python scripts/convert_london.py
"""

from __future__ import annotations

import argparse
import csv
import pathlib
import random
from dataclasses import dataclass
from typing import Dict, List, Tuple

RAW_EDGE_FILE = "London_Edgelist.csv"

DEFAULT_BASE_WIDTH = 3.5  # meters (typical lane width)
DEFAULT_RUSH_WIDTH = 2.5  # meters under congestion
CLEARWAY_RUSH_WIDTH = 4.5  # meters during rush hour (no parking allowed - UK clearway)
CLEARWAY_PERCENTAGE = 5  # percentage of roads with urban clearway condition
DEFAULT_SPEED = 100.0  # meters per minute (6 km/h walking speed baseline)
DENSITY = 20  # percentage of edges with positive scores

# Rush hour definitions (minutes from midnight)
RUSH_HOURS = [
    (7 * 60 + 30, 9 * 60 + 30),   # 7:30am - 9:30am
    (16 * 60, 18 * 60 + 30)        # 4:00pm - 6:30pm
]


def generate_time_series() -> List[int]:
    """Generate time series with rush hour intervals."""
    series = [0]
    for start, end in RUSH_HOURS:
        time = start
        while time <= end:
            series.append(time)
            time += 30
    return series


ARRIVAL_SERIES = generate_time_series()
WIDTH_SERIES = [0]  # placeholder; currently unused by solver


@dataclass
class NodeRecord:
    node_id: int
    x: float  # British National Grid Easting
    y: float  # British National Grid Northing
    lat: float  # WGS84 latitude
    lon: float  # WGS84 longitude


@dataclass
class EdgeRecord:
    src: int
    dst: int
    distance: float  # in meters


def bng_to_wgs84(easting: float, northing: float) -> Tuple[float, float]:
    """Convert British National Grid coordinates to WGS84 lat/lon.
    
    This is a simplified conversion using the approximate transformation.
    For more accuracy, use a proper projection library like pyproj.
    
    Args:
        easting: BNG Easting coordinate (meters)
        northing: BNG Northing coordinate (meters)
        
    Returns:
        Tuple of (latitude, longitude) in WGS84
    """
    try:
        from pyproj import Transformer
        # Create transformer from EPSG:27700 (BNG) to EPSG:4326 (WGS84)
        transformer = Transformer.from_crs("EPSG:27700", "EPSG:4326", always_xy=True)
        lon, lat = transformer.transform(easting, northing)
        return lat, lon
    except ImportError:
        # Fallback to approximate conversion if pyproj is not available
        # This is based on the Ordnance Survey approximate formula
        # Reference point: False Origin
        E0 = 400000.0
        N0 = -100000.0
        lat0 = 49.0
        lon0 = -2.0
        
        # Scale factors (approximate)
        lat_per_meter = 1.0 / 111320.0
        lon_per_meter = 1.0 / (111320.0 * 0.68)  # Approximate for UK latitude
        
        lat = lat0 + (northing - N0) * lat_per_meter
        lon = lon0 + (easting - E0) * lon_per_meter
        
        return lat, lon


def load_london_edges(path: pathlib.Path) -> Tuple[Dict[int, NodeRecord], List[EdgeRecord]]:
    """Load nodes and edges from London_Edgelist.csv.
    
    The CSV format is:
    XCoord,YCoord,START_NODE,END_NODE,EDGE,LENGTH
    
    Returns:
        Tuple of (nodes_dict, edges_list)
    """
    nodes: Dict[int, NodeRecord] = {}
    edges: List[EdgeRecord] = []
    
    with path.open("r", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            x = float(row["XCoord"])
            y = float(row["YCoord"])
            src = int(row["START_NODE"])
            dst = int(row["END_NODE"])
            distance = float(row["LENGTH"])  # already in meters
            
            # Convert coordinates to WGS84
            lat, lon = bng_to_wgs84(x, y)
            
            # Store node info (using src node coords for both endpoints)
            # Note: In the original data, all edges from the same source have same coords
            if src not in nodes:
                nodes[src] = NodeRecord(node_id=src, x=x, y=y, lat=lat, lon=lon)
            if dst not in nodes:
                # We'll need to get dst coordinates from edges where dst is src
                # For now, use the src coordinates as placeholder
                nodes[dst] = NodeRecord(node_id=dst, x=x, y=y, lat=lat, lon=lon)
            
            edges.append(EdgeRecord(src=src, dst=dst, distance=distance))
    
    # Second pass: update destination node coordinates from edges where they are sources
    with path.open("r", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            x = float(row["XCoord"])
            y = float(row["YCoord"])
            src = int(row["START_NODE"])
            lat, lon = bng_to_wgs84(x, y)
            
            if src in nodes:
                nodes[src] = NodeRecord(node_id=src, x=x, y=y, lat=lat, lon=lon)
    
    return nodes, edges


def write_nodes(out_path: pathlib.Path, nodes: Dict[int, NodeRecord]) -> None:
    """Write nodes file in format: id lat lon clusterId"""
    with out_path.open("w", encoding="utf-8", newline="\n") as fh:
        for node_id in sorted(nodes.keys()):
            n = nodes[node_id]
            # id lat lon clusterId
            fh.write(f"{n.node_id} {n.lat} {n.lon} 1\n")


def write_edges(
    out_path: pathlib.Path,
    edges: List[EdgeRecord],
    base_width: float,
    rush_width: float,
    clearway_width: float,
    density: int,
    clearway_pct: int,
    speed: float
) -> None:
    """Write edges file with time-dependent travel costs and merged width/distance data.
    
    Implements UK urban clearway condition:
    - clearway_pct% of roads have increased width during rush hour (no parking)
    - Remaining roads maintain constant width throughout the day
    """
    with out_path.open("w", encoding="utf-8", newline="\n") as fh:
        # Line 1: Arrival time series
        fh.write(" ".join(str(t) for t in ARRIVAL_SERIES) + "\n")
        # Line 2: Width time series
        fh.write(" ".join(str(t) for t in WIDTH_SERIES) + "\n")
        
        # Generate score assignments
        random.seed(42)  # Reproducible scores
        scores = [1 if i < len(edges) * density // 100 else 0 for i in range(len(edges))]
        random.shuffle(scores)
        
        # Generate clearway assignments (5% of roads with urban clearway condition)
        random.seed(123)  # Different seed for clearway assignment
        is_clearway = [1 if i < len(edges) * clearway_pct // 100 else 0 for i in range(len(edges))]
        random.shuffle(is_clearway)
        
        for idx, e in enumerate(edges):
            # Distance is in meters
            # Choose a random speed variation for this edge (80-120% of base speed)
            speed_multiplier = random.uniform(0.8, 1.2)
            edge_speed = speed * speed_multiplier
            
            # Calculate base travel time (in minutes) for non-rush hour
            base_cost = e.distance / edge_speed
            
            # Generate time-dependent costs for each time point
            travel_costs = []
            rush_idx = 0
            inside_rush = False
            
            for time in ARRIVAL_SERIES:
                # Check rush hour transitions
                if rush_idx < len(RUSH_HOURS):
                    start, end = RUSH_HOURS[rush_idx]
                    if time == start:
                        inside_rush = True
                    elif time >= end:
                        if inside_rush:  # Only advance if we were inside
                            inside_rush = False
                            rush_idx += 1
                
                temp_cost = base_cost
                
                if inside_rush:
                    # Calculate position within rush hour
                    start, _ = RUSH_HOURS[rush_idx]
                    difference = time - start
                    position = difference // 30
                    
                    # Apply rush hour multiplier based on position
                    if position == 0 or position == 4:
                        multiplier = random.uniform(0.10, 0.15)
                    elif position == 1 or position == 3:
                        multiplier = random.uniform(0.20, 0.25)
                    elif position == 2:
                        multiplier = random.uniform(0.30, 0.40)
                    else:
                        multiplier = 0
                    
                    temp_cost += base_cost * multiplier
                
                travel_costs.append(temp_cost)
            
            # Determine widths based on clearway status
            # UK urban clearway: width increases during rush hour (no parking allowed)
            # Non-clearway roads: constant width throughout the day
            if is_clearway[idx]:
                # Clearway road: increased width during rush hour
                edge_base_width = base_width
                edge_rush_width = clearway_width  # Wider during rush hour (no parking)
            else:
                # Regular road: constant width all day
                edge_base_width = base_width
                edge_rush_width = base_width  # Same as base (no change)
            
            # Format: src dst travel_costs baseWidth rushWidth distance
            costs_str = ",".join(f"{c:.6f}" for c in travel_costs)
            fh.write(
                f"{e.src} {e.dst} {costs_str} {edge_base_width} {edge_rush_width} {e.distance}\n"
            )


def dedupe_edges(edges: List[EdgeRecord]) -> List[EdgeRecord]:
    """Remove duplicate edges, keeping the one with shortest distance."""
    dedup: Dict[Tuple[int, int], EdgeRecord] = {}
    for e in edges:
        key = (e.src, e.dst)
        if key not in dedup or e.distance < dedup[key].distance:
            dedup[key] = e
    return [dedup[k] for k in sorted(dedup.keys())]


def renumber_nodes_contiguous(
    nodes: Dict[int, NodeRecord],
    edges: List[EdgeRecord]
) -> Tuple[Dict[int, NodeRecord], List[EdgeRecord]]:
    """Renumber nodes to be contiguous from 0 to N-1."""
    # Get all unique node IDs that are actually used
    used_ids = {e.src for e in edges} | {e.dst for e in edges}
    
    # Create mapping from old IDs to new contiguous IDs
    old_to_new = {old_id: new_id for new_id, old_id in enumerate(sorted(used_ids))}
    
    # Renumber nodes
    new_nodes = {}
    for old_id, node in nodes.items():
        if old_id in used_ids:
            new_id = old_to_new[old_id]
            new_nodes[new_id] = NodeRecord(
                node_id=new_id,
                x=node.x,
                y=node.y,
                lat=node.lat,
                lon=node.lon
            )
    
    # Renumber edges
    new_edges = []
    for e in edges:
        new_edges.append(EdgeRecord(
            src=old_to_new[e.src],
            dst=old_to_new[e.dst],
            distance=e.distance
        ))
    
    return new_nodes, new_edges


def main() -> None:
    parser = argparse.ArgumentParser(description="Convert London dataset to Wide-Path format")
    parser.add_argument("--input", type=pathlib.Path, default=pathlib.Path("scripts"),
                        help="Input directory containing London_Edgelist.csv")
    parser.add_argument("--output", type=pathlib.Path, default=pathlib.Path("scripts"),
                        help="Output directory for Wide-Path formatted files")
    parser.add_argument("--base-width", type=float, default=DEFAULT_BASE_WIDTH,
                        help="Base lane width (m)")
    parser.add_argument("--rush-width", type=float, default=DEFAULT_RUSH_WIDTH,
                        help="Rush-hour width (m) for non-clearway roads")
    parser.add_argument("--clearway-width", type=float, default=CLEARWAY_RUSH_WIDTH,
                        help="Clearway width during rush hour (m)")
    parser.add_argument("--clearway-pct", type=int, default=CLEARWAY_PERCENTAGE,
                        help="Percentage of roads with clearway condition")
    parser.add_argument("--density", type=int, default=DENSITY,
                        help="Percentage of edges with scores")
    parser.add_argument("--speed", type=float, default=DEFAULT_SPEED,
                        help="Base speed in meters per minute")
    args = parser.parse_args()
    
    input_dir = pathlib.Path(args.input)
    output_dir = pathlib.Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    edge_path = input_dir / RAW_EDGE_FILE
    
    if not edge_path.exists():
        raise FileNotFoundError(f"Missing edge file: {edge_path}")
    
    print("Loading London dataset...")
    nodes, edges = load_london_edges(edge_path)
    print(f"Loaded {len(nodes)} nodes, {len(edges)} edges")
    
    print("Deduplicating edges...")
    edges = dedupe_edges(edges)
    print(f"After deduplication: {len(edges)} edges")
    
    print("Renumbering nodes to be contiguous...")
    nodes, edges = renumber_nodes_contiguous(nodes, edges)
    
    vertex_count = len(nodes)
    edges_count = len(edges)
    print(f"Final: {vertex_count} nodes, {edges_count} edges")
    
    nodes_out = output_dir / f"nodes_{vertex_count}.txt"
    edges_out = output_dir / f"edges_{vertex_count}.txt"
    
    print("Writing output files...")
    write_nodes(nodes_out, nodes)
    write_edges(
        edges_out, edges,
        args.base_width, args.rush_width, args.clearway_width,
        args.density, args.clearway_pct, args.speed
    )
    
    print("\n" + "="*70)
    print("Conversion complete - MERGED FORMAT")
    print("="*70)
    print(f"  Nodes   -> {nodes_out}")
    print(f"  Edges   -> {edges_out}")
    print(f"  Time series: {len(ARRIVAL_SERIES)} points (rush hours: 7:30-9:30am, 4:00-6:30pm)")
    print(f"  Base speed: {args.speed} m/min")
    print(f"  Density: {args.density}% edges with scores")
    print(f"  UK Urban Clearway: {args.clearway_pct}% roads with increased width during rush hour ({args.clearway_width}m)")
    print(f"  Remaining roads: constant width ({args.base_width}m) throughout the day")
    print("="*70)


if __name__ == "__main__":
    main()
