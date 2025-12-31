"""
Test script to run pathfinding with hardcoded input and capture output.
"""
import heapq
import os


def load_graph(nodes_path, edges_path):
    """Load graph from files."""
    adj = {}
    num_nodes = 0

    # Load nodes
    with open(nodes_path) as f:
        for line in f:
            parts = line.strip().split()
            if not parts:
                continue
            num_nodes += 1
    
    # Load edges
    with open(edges_path) as f:
        _arrival_line = f.readline()
        _width_line = f.readline()
        for line in f:
            parts = line.strip().split()
            if not parts:
                continue
            try:
                src = int(parts[0])
                dst = int(parts[1])
                cost_series_str = parts[2]
            except (ValueError, IndexError):
                continue
            cost_values = [float(x) for x in cost_series_str.split(',') if x]
            if not cost_values:
                continue
            weight = sum(cost_values) / len(cost_values)
            adj.setdefault(src, []).append((dst, weight))
    return adj, num_nodes


def dijkstra(adj, source, dest):
    """Run Dijkstra's algorithm."""
    if source not in adj:
        return [], float('inf')
    dist = {source: 0.0}
    prev = {}
    pq = [(0.0, source)]
    visited = set()
    while pq:
        cost, u = heapq.heappop(pq)
        if u in visited:
            continue
        visited.add(u)
        if u == dest:
            break
        for v, w in adj.get(u, []):
            new_cost = cost + w
            if new_cost < dist.get(v, float('inf')):
                dist[v] = new_cost
                prev[v] = u
                heapq.heappush(pq, (new_cost, v))
    if dest not in dist:
        return [], float('inf')
    # Reconstruct path
    path = [dest]
    while path[-1] != source:
        path.append(prev[path[-1]])
    path.reverse()
    return path, dist[dest]


def main():
    # Hardcoded test input
    source = 72594
    dest = 72541
    departure_time = 450
    interval = 360
    budget = 5
    
    print(f"Python FlexRoute Test")
    print(f"=" * 60)
    print(f"Input:")
    print(f"  Source: {source}")
    print(f"  Destination: {dest}")
    print(f"  Departure time: {departure_time}")
    print(f"  Interval: {interval}")
    print(f"  Budget: {budget}")
    print(f"=" * 60)
    
    # Load graph
    nodes_file = r'C:\Users\kousi\eclipse-workspace\FlexRoute\nodes_264346.txt'
    edges_file = r'C:\Users\kousi\eclipse-workspace\FlexRoute\edges_264346.txt'
    
    if not (os.path.exists(nodes_file) and os.path.exists(edges_file)):
        print("Error: Dataset files not found!")
        return
    
    print("Loading graph...")
    adj, num_nodes = load_graph(nodes_file, edges_file)
    print(f"Graph loaded: {num_nodes} nodes, {sum(len(v) for v in adj.values())} edges")
    
    # Run pathfinding
    print(f"\nComputing path from {source} to {dest}...")
    path, cost = dijkstra(adj, source, dest)
    
    # Output results
    print(f"\n{'='*60}")
    print(f"RESULTS:")
    print(f"{'='*60}")
    if not path:
        print("No path found.")
    else:
        print(f"Path length (cost): {cost:.3f}")
        print(f"Number of nodes in path: {len(path)}")
        print(f"\nPath nodes:")
        for i in range(0, len(path), 10):
            segment = path[i:i+10]
            print(' '.join(str(n) for n in segment))
    print(f"{'='*60}")


if __name__ == '__main__':
    main()
