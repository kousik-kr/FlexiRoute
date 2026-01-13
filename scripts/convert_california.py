"""Convert the Utah SpatialDataset California network to Wide-Path format.

The solver expects merged files in the output directory:
- nodes_<N>.txt : space-separated `id latitude longitude clusterId`
- edges_<N>.txt : first line arrival time points, second line width time points,
                  remaining lines `src dst travel_costs baseWidth rushWidth distance`

This script uses the raw California files shipped in `datasets/California`:
- "node coordinates.txt"  (id lon lat)
- "edge distance.txt"     (edge_id src dst distance)

Travel time generation:
- Time-dependent costs with rush hour variations (7:30-9:30am, 4:00-6:30pm)
- Base travel time calculated from distance / 6000 (100 meter/min default speed)
- Rush hour increases: 10-40% based on time within rush period
- All nodes assigned cluster 1

Usage (from project root):
    python datasets/California/convert_california.py \
        --input datasets/California \
        --output dataset
"""

from __future__ import annotations

import argparse
import pathlib
import random
from dataclasses import dataclass
from typing import Dict, List, Tuple

RAW_NODE_FILE = "node coordinates.txt"
RAW_EDGE_FILE = "edge distance.txt"

DEFAULT_BASE_WIDTH = 3.5  # meters (typical lane width)
DEFAULT_RUSH_WIDTH = 2.5  # meters under congestion
CLEARWAY_RUSH_WIDTH = 4.5  # meters during rush hour (no parking allowed - UK clearway)
CLEARWAY_PERCENTAGE = 5  # percentage of roads with urban clearway condition
TIME_UNIT_CONVERTOR = 1.0  # meters/min for base travel time
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
    lat: float
    lon: float


@dataclass
class EdgeRecord:
    src: int
    dst: int
    distance: float


def load_nodes(path: pathlib.Path) -> List[NodeRecord]:
    nodes: List[NodeRecord] = []
    with path.open("r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            parts = line.split()
            if len(parts) < 3:
                raise ValueError(f"Malformed node line: '{line}'")
            node_id = int(parts[0])
            lon = float(parts[1])
            lat = float(parts[2])
            nodes.append(NodeRecord(node_id=node_id, lat=lat, lon=lon))
    return nodes


def load_edges(path: pathlib.Path) -> List[EdgeRecord]:
    edges: List[EdgeRecord] = []
    with path.open("r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            parts = line.split()
            if len(parts) < 4:
                raise ValueError(f"Malformed edge line: '{line}'")
            # parts: edge_id, src, dst, distance
            src = int(parts[1])
            dst = int(parts[2])
            distance = float(parts[3])
            edges.append(EdgeRecord(src=src, dst=dst, distance=distance))
    return edges


def write_nodes(out_path: pathlib.Path, nodes: List[NodeRecord]) -> None:
    with out_path.open("w", encoding="utf-8", newline="\n") as fh:
        for n in sorted(nodes, key=lambda x: x.node_id):
            # id lat lon clusterId
            fh.write(f"{n.node_id} {n.lat} {n.lon} 1\n")


def write_edges(out_path: pathlib.Path, edges: List[EdgeRecord], base_width: float, rush_width: float, clearway_width: float, density: int, clearway_pct: int) -> None:
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
            # Distance is in kilometers
            # Choose a random speed between 20 and 25 mph for this edge
            speed_mph = random.uniform(20, 25)
            speed_km_per_hr = speed_mph * 1.60934
            speed_km_per_min = speed_km_per_hr / 60.0
            # Calculate base travel time (in minutes) for non-rush hour
            base_cost = e.distance / speed_km_per_min

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
                    elif time == end:
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


def write_clusters(out_path: pathlib.Path, nodes: List[NodeRecord], cluster_id: int = 1) -> None:
    """DEPRECATED: Cluster info now merged into nodes file."""
    pass  # No longer needed - clusters are in nodes_ file


def write_edge_widths(out_path: pathlib.Path, edges: List[EdgeRecord], base_width: float, rush_width: float) -> None:
    """DEPRECATED: Edge widths now merged into edges file."""
    pass  # No longer needed - widths are in edges_ file


def ensure_ids_contiguous(nodes: List[NodeRecord], edges: List[EdgeRecord]) -> None:
    node_ids = {n.node_id for n in nodes}
    missing: List[int] = []
    for i in range(0, max(node_ids) + 1):
        if i not in node_ids:
            missing.append(i)
    if missing:
        raise ValueError(
            "Node ids are not contiguous from 0..N-1. Missing: " + ",".join(map(str, missing[:20]))
        )
    edge_ids = {e.src for e in edges} | {e.dst for e in edges}
    unknown = edge_ids - node_ids
    if unknown:
        raise ValueError("Edges reference unknown nodes: " + ",".join(map(str, sorted(list(unknown))[:20])))


def dedupe_edges(edges: List[EdgeRecord]) -> List[EdgeRecord]:
    dedup: Dict[Tuple[int, int], EdgeRecord] = {}
    for e in edges:
        key = (e.src, e.dst)
        if key not in dedup or e.distance < dedup[key].distance:
            dedup[key] = e
    return [dedup[k] for k in sorted(dedup.keys())]


def main() -> None:
    parser = argparse.ArgumentParser(description="Convert California dataset to Wide-Path format")
    parser.add_argument("--input", type=pathlib.Path, default=pathlib.Path("datasets/California"),
                        help="Input directory containing raw California files")
    parser.add_argument("--output", type=pathlib.Path, default=pathlib.Path("dataset"),
                        help="Output directory for Wide-Path formatted files")
    parser.add_argument("--base-width", type=float, default=DEFAULT_BASE_WIDTH, help="Base lane width (m)")
    parser.add_argument("--rush-width", type=float, default=DEFAULT_RUSH_WIDTH, help="Rush-hour width (m) for non-clearway roads")
    parser.add_argument("--clearway-width", type=float, default=CLEARWAY_RUSH_WIDTH, help="Clearway width during rush hour (m)")
    parser.add_argument("--clearway-pct", type=int, default=CLEARWAY_PERCENTAGE, help="Percentage of roads with clearway condition")
    parser.add_argument("--density", type=int, default=DENSITY, help="Percentage of edges with scores")
    args = parser.parse_args()

    input_dir = pathlib.Path(args.input)
    output_dir = pathlib.Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    node_path = input_dir / RAW_NODE_FILE
    edge_path = input_dir / RAW_EDGE_FILE

    if not node_path.exists():
        raise FileNotFoundError(f"Missing node file: {node_path}")
    if not edge_path.exists():
        raise FileNotFoundError(f"Missing edge file: {edge_path}")

    nodes = load_nodes(node_path)
    edges = load_edges(edge_path)
    ensure_ids_contiguous(nodes, edges)
    edges = dedupe_edges(edges)

    vertex_count = len(nodes)
    edges_count = len(edges)
    print(f"Loaded {vertex_count} nodes, {edges_count} edges")

    script_dir = pathlib.Path(__file__).parent.resolve()
    nodes_out = script_dir / f"nodes_{vertex_count}.txt"
    edges_out = script_dir / f"edges_{vertex_count}.txt"

    write_nodes(nodes_out, nodes)
    write_edges(edges_out, edges, args.base_width, args.rush_width, args.clearway_width, args.density, args.clearway_pct)

    print("Conversion complete - MERGED FORMAT")
    print(f"  nodes   -> {nodes_out}")
    print(f"  edges   -> {edges_out}")
    print(f"  Time series: {len(ARRIVAL_SERIES)} points (rush hours: 7:30-9:30am, 4:00-6:30pm)")
    print(f"  Density: {args.density}% edges with scores")
    print(f"  UK Urban Clearway: {args.clearway_pct}% roads with increased width during rush hour ({args.clearway_width}m)")
    print(f"  Remaining roads: constant width ({args.base_width}m) throughout the day")
    
    # Clean up old separate files if they exist
    old_files = [
        output_dir / f"node_{vertex_count}.txt",
        output_dir / f"edge_{vertex_count}.txt"
    ]
    removed = []
    for old_file in old_files:
        if old_file.exists():
            old_file.unlink()
            removed.append(old_file.name)
    
    if removed:
        print(f"\nRemoved old separate files: {', '.join(removed)}")


if __name__ == "__main__":
    main()
