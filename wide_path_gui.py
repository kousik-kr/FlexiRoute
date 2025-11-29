"""
Wide Path GUI application implemented in Python.

This application loads the provided dataset (nodes and edges files) from the
`/home/oai/share` directory, constructs a directed graph using the average
travel time for each edge, and provides a simple GUI to query a route
between two nodes using Dijkstra's algorithm.

Due to limitations in the execution environment (no Java compiler), this
is a re‑implementation of the core functionality of the original
"Wide‑Path" project. It does not replicate the full bidirectional A* search
with clustering and width constraints, but it offers a functional path
finding GUI based on average travel times.

Usage:
    python3 wide_path_gui.py

The GUI contains input fields for source node, destination node,
departure time, interval, and budget. Departure time, interval,
and budget are accepted but not currently used in the computation.
When the "Find Path" button is clicked, the application computes the
shortest path between the specified nodes and displays the path and
estimated cost.
"""

import tkinter as tk
from tkinter import messagebox, scrolledtext
import threading
import heapq
import os


def load_graph(nodes_path: str, edges_path: str):
    """
    Load the graph from the provided nodes and edges files.

    Parameters
    ----------
    nodes_path : str
        Path to the nodes file. This file is parsed for node existence
        but the coordinates are not used here. The function returns
        the number of nodes present.
    edges_path : str
        Path to the edges file. The function reads the arrival and width
        breakpoints (ignored) and then parses each edge line into an
        adjacency list using the average travel time of the edge.

    Returns
    -------
    tuple(dict, int)
        A tuple containing the adjacency list (mapping from source node
        to a list of (destination, weight) pairs) and the number of nodes.
    """
    adj = {}
    num_nodes = 0

    # Load nodes to count them and ensure validity
    with open(nodes_path) as f:
        for line in f:
            parts = line.strip().split()
            if not parts:
                continue
            # parts[0] is node id
            num_nodes += 1
    
    # Load edges
    with open(edges_path) as f:
        # First two lines contain arrival and width breakpoints; ignore values
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
                continue  # skip malformed lines
            cost_values = [float(x) for x in cost_series_str.split(',') if x]
            if not cost_values:
                continue
            weight = sum(cost_values) / len(cost_values)
            adj.setdefault(src, []).append((dst, weight))
    return adj, num_nodes


def dijkstra(adj, source, dest):
    """
    Compute the shortest path between `source` and `dest` using Dijkstra's algorithm.

    Parameters
    ----------
    adj : dict
        Adjacency list mapping each node to a list of (neighbor, weight).
    source : int
        Starting node identifier.
    dest : int
        Destination node identifier.

    Returns
    -------
    tuple(list[int], float)
        A tuple containing the list of node IDs forming the shortest path
        (including source and destination) and the total weight of the path.
        If the destination is unreachable, returns ([], float('inf')).
    """
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


class WidePathApp(tk.Tk):
    """
    Main application class for the Wide Path GUI.
    """

    def __init__(self, adj, num_nodes):
        super().__init__()
        self.title("Wide Path - Path Finder")
        self.geometry("600x500")
        self.adj = adj
        self.num_nodes = num_nodes
        self.create_widgets()

    def create_widgets(self):
        # Input frame
        input_frame = tk.Frame(self)
        input_frame.pack(padx=10, pady=10, fill=tk.X)

        # Source input
        tk.Label(input_frame, text="Source node:").grid(row=0, column=0, sticky=tk.W)
        self.source_entry = tk.Entry(input_frame)
        self.source_entry.grid(row=0, column=1, sticky=tk.W)
        self.source_entry.insert(0, "72594")

        # Destination input
        tk.Label(input_frame, text="Destination node:").grid(row=1, column=0, sticky=tk.W)
        self.dest_entry = tk.Entry(input_frame)
        self.dest_entry.grid(row=1, column=1, sticky=tk.W)
        self.dest_entry.insert(0, "72541")

        # Departure time
        tk.Label(input_frame, text="Departure time:").grid(row=2, column=0, sticky=tk.W)
        self.departure_entry = tk.Entry(input_frame)
        self.departure_entry.grid(row=2, column=1, sticky=tk.W)
        self.departure_entry.insert(0, "450")

        # Interval
        tk.Label(input_frame, text="Interval:").grid(row=3, column=0, sticky=tk.W)
        self.interval_entry = tk.Entry(input_frame)
        self.interval_entry.grid(row=3, column=1, sticky=tk.W)
        self.interval_entry.insert(0, "360")

        # Budget
        tk.Label(input_frame, text="Budget:").grid(row=4, column=0, sticky=tk.W)
        self.budget_entry = tk.Entry(input_frame)
        self.budget_entry.grid(row=4, column=1, sticky=tk.W)
        self.budget_entry.insert(0, "5")

        # Find path button
        self.find_button = tk.Button(input_frame, text="Find Path", command=self.find_path_thread)
        self.find_button.grid(row=5, column=0, columnspan=2, pady=10)

        # Output area
        self.output_area = scrolledtext.ScrolledText(self, height=15)
        self.output_area.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        self.output_area.configure(state=tk.DISABLED)

    def find_path_thread(self):
        """
        Spawn a thread to compute the path to avoid blocking the GUI.
        """
        thread = threading.Thread(target=self.find_path)
        thread.daemon = True
        thread.start()

    def find_path(self):
        """
        Parse user input, compute the path, and display the results.
        """
        try:
            source = int(self.source_entry.get().strip())
            dest = int(self.dest_entry.get().strip())
        except ValueError:
            messagebox.showerror("Invalid input", "Source and destination must be integers.")
            return
        # Note: departure time, interval, and budget are currently unused but parsed for future extension
        departure_time = self.departure_entry.get().strip()
        interval = self.interval_entry.get().strip()
        budget = self.budget_entry.get().strip()

        # Compute path
        self.output_area.configure(state=tk.NORMAL)
        self.output_area.delete('1.0', tk.END)
        self.output_area.insert(tk.END, f"Computing path from {source} to {dest}...\n")
        self.output_area.update()

        path, cost = dijkstra(self.adj, source, dest)
        if not path:
            self.output_area.insert(tk.END, "No path found.\n")
        else:
            self.output_area.insert(tk.END, f"Path length: {cost:.3f}\n")
            self.output_area.insert(tk.END, f"Number of nodes: {len(path)}\n")
            self.output_area.insert(tk.END, "Path nodes: \n")
            # Format path in lines of 10 nodes for readability
            for i in range(0, len(path), 10):
                segment = path[i:i+10]
                self.output_area.insert(tk.END, ' '.join(str(n) for n in segment) + '\n')
        self.output_area.configure(state=tk.DISABLED)


def main():
    nodes_file = os.path.join('C:\\Users\\kousi\\eclipse-workspace\\Wide-Path\\', 'nodes_264346.txt')
    edges_file = os.path.join('C:\\Users\\kousi\\eclipse-workspace\\Wide-Path\\', 'edges_264346.txt')
    if not (os.path.exists(nodes_file) and os.path.exists(edges_file)):
        print("Error: Dataset files not found in C:\\Users\\kousi\\eclipse-workspace\\Wide-Path\\.")
        return
    print("Loading graph... this may take a minute...")
    adj, num_nodes = load_graph(nodes_file, edges_file)
    print(f"Graph loaded with {num_nodes} nodes.")
    app = WidePathApp(adj, num_nodes)
    app.mainloop()


if __name__ == '__main__':
    main()