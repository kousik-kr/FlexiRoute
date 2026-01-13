/**
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import models.RoutingMode;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 
 */
public class BidirectionalAstar {

	/**
	 * @param args
	 */
	private static final String currentDirectory = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "London" + File.separator;	//current directory of the code
	private static final int defaultVertexCount = 288016; // Default vertex count for London dataset
    private static String dataDirectory = currentDirectory;
    private static String configuredGraphDataDir = currentDirectory;
    private static int configuredGraphVertexCount = defaultVertexCount;
	//public static int MAX_SPEED = 2400;
	private static Queue<Query> queries = new LinkedList<Query>();
	//public static double departure_time = 0;
	@SuppressWarnings("unused")
	public static ForkJoinPool pool;// = new ForkJoinPool(16);
	public static long start;
	public static Runtime runtime;
	private static long memory_after;
	private static boolean updated_memory;
        public static double TIME_LIMIT;
        private static double overhead;
        private static int density;
        private static int no_of_core;
        public static boolean optimization = true;
        // Flags to control whether queries should use clustered nodes for sources/destinations
        private static boolean sourceInCluster = true;
        private static boolean destinationInCluster = true;
        public static double interval_duration;
        public static double THRESHOLD;
        public static boolean forceStop = false;
	public static boolean Optimization;
	public static int SHARP_THRESHOLD = 60;
	public static double WIDENESS_THRESHOLD = 12.8;
		
//	private static HashMap<Integer, Integer> subgraphNodes = new HashMap<Integer, Integer>(); 
//	private static HashMap<Integer, Integer> subgraphIndexes = new HashMap<Integer, Integer>(); 
//	public static int subgraphSize = 0;
	
	public static void driver () throws IOException, InterruptedException, ExecutionException{
		//currentDirectory = args[0];
		//String s = "6105";//args[0];
		int n = 21048;//Integer.parseInt(args[1]);
		density = 20;//Integer.parseInt(args[2]);
		overhead = 30;//Double.parseDouble(args[3]);
		no_of_core = 16;//Integer.parseInt(args[4]);
		// TIME_LIMIT removed - no query execution time limit
		interval_duration = 360;//Integer.parseInt(args[6]);
		THRESHOLD = 10;//Integer.parseInt(args[7]);
		Optimization = true;
		pool = new ForkJoinPool(no_of_core);
		Graph.set_vertex_count(n);
		extract_nodes();
		extract_edges();
		//if(n==23947347)
			//create_query_file();
		//create_query_bucket();
		//query_processing();
	}
	
	private static void create_query_file() {
		String output_file = "Src-dest_" + Graph.get_vertex_count() + ".txt";
		FileWriter fout = null;
		try {
			fout = new FileWriter(output_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter writer = new BufferedWriter(fout);
		Random rand = new Random();
		int i=0;
		while(i<1000) {
			int source = rand.nextInt(Graph.get_vertex_count());
			int departure_time = rand.nextInt(7*60+30, 9*60);
			double budget = rand.nextDouble(1, 59);
			List<Double> dest_budg = dijkstra(source, departure_time, budget);
			int destination = (int) Math.round(dest_budg.get(0));
			try {
				writer.write(source + "\t" + destination + "\t" + dest_budg.get(1) + "\t" + budget + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			i++;
		}
		try {
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
}

	private static List<Double> dijkstra(int source, int departure_time, double budget) {
		Map<Integer, Double> gScore = new HashMap<Integer, Double>();
		
		PriorityQueue<Integer> pQueue = new PriorityQueue<Integer>(Graph.get_vertex_count(), new Comparator<Integer>(){
			@Override
        	public int compare(Integer i, Integer j){
				
                if(gScore.get(i) > gScore.get(j)){
                    return 1;
                }
                else if (gScore.get(i) < gScore.get(j)){
                    return -1;
                }
                return 0;
            }
		});

		
		pQueue.add(source);
		gScore.put(source, (double)departure_time);
		
		while(!pQueue.isEmpty()) {

			int current_vertex = pQueue.poll();
			
			Node node = Graph.get_node(current_vertex);
			double current_cost = gScore.get(current_vertex);
			if(current_cost>=budget + departure_time) {
				budget = current_cost-departure_time;
				List<Double> list = new ArrayList<Double>();
				list.add((double)current_vertex);
				list.add(budget);
				return list;
			}
			Map<Integer, Edge> temp_outgoing_edge = node.get_outgoing_edges();
			
 			for(Entry<Integer, Edge> entry : temp_outgoing_edge.entrySet()) {
				
				Edge edge = entry.getValue();
				int j = edge.get_destination();
				double cost_j = edge.get_arrival_time(current_cost);	
				if(!gScore.containsKey(j)) {
					gScore.put(j, cost_j); 
					pQueue.add(j);
				}
				
				else if(gScore.get(j)>cost_j) {
					gScore.replace(j, cost_j);
					
				}
			}
			
		}
		return null;
	}

	private static void create_query_bucket() throws IOException{
		String query_file = dataDirectory + "/Src-dest_" + Graph.get_vertex_count() + ".txt";
		File fin = new File(query_file);
		BufferedReader br = new BufferedReader(new FileReader(fin));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] entries = line.split("\t");
			int source = Integer.parseInt(entries[0]);
			int destination = Integer.parseInt(entries[1]);

			// Check if source and destination meet the cluster criteria
			Node sourceNode = Graph.get_node(source);
			Node destinationNode = Graph.get_node(destination);

                        if (sourceNode != null && destinationNode != null) {
                                boolean validSource = sourceInCluster ? sourceNode.getClusterId() != -1 : sourceNode.getClusterId() == -1;
                                boolean validDestination = destinationInCluster ? destinationNode.getClusterId() != -1 : destinationNode.getClusterId() == -1;

                                if (validSource && validDestination) {
                                        Query query = new Query(source, destination, Double.parseDouble(entries[2]),
                                                        Double.parseDouble(entries[2]) + interval_duration, Double.parseDouble(entries[3]));
                                        queries.add(query);
                                }
                        }
		}
		br.close();
	}

	private static void extract_nodes() throws NumberFormatException, IOException{
		String node_file = dataDirectory + "/" + "nodes_" + Graph.get_vertex_count() +".txt";
		File fin = new File(node_file);
		BufferedReader br = new BufferedReader(new FileReader(fin));
		String line = null;
		while((line = br.readLine()) != null){
			// Supported formats:
			// id lat lon
			// id lat lon clusterId
			String[] entries = line.trim().split(" ");
			if (entries.length < 3) {
				System.err.println("Skipping malformed node line: " + line);
				continue;
			}

			int nodeId = Integer.parseInt(entries[0]);
			double latitude = Double.parseDouble(entries[1]);
			double longitude = Double.parseDouble(entries[2]);
			Node node = new Node(latitude, longitude);

			// Parse cluster ID (always present in merged format as 4th column)
			if (entries.length >= 4) {
				try {
					int clusterId = Integer.parseInt(entries[3]);
					node.setClusterId(clusterId);
					// Add node to cluster
					Cluster cluster = Graph.getCluster(clusterId);
					if (cluster == null) {
						cluster = new Cluster(clusterId);
						Graph.addCluster(cluster);
					}
					cluster.addNode(node);
				} catch (NumberFormatException nfe) {
					System.err.println("Cluster parse error for node " + nodeId + ": " + entries[3]);
				}
			}

			Graph.add_node(nodeId, node);
		}
		br.close();
	}

	private static void extract_edges() throws NumberFormatException, IOException{
		String edge_file = dataDirectory + "/" + "edges_" + Graph.get_vertex_count()+ ".txt";
		File fin = new File(edge_file);
		BufferedReader br = new BufferedReader(new FileReader(fin));
		String line;
		String[] arrival_time_series = null;
		String[] width_time_series = null;

		if((line = br.readLine()) != null){
			arrival_time_series = line.trim().split(" ");
		}
        
		if((line = br.readLine()) != null){
			width_time_series = line.trim().split(" ");
		}
        
		Graph.updateArrivalTimeSeries(arrival_time_series);
		Graph.updateWidthTimeSeries(width_time_series);

		while((line = br.readLine()) != null){
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}

			// Supported formats:
			// src dst travelCosts
			// src dst travelCosts baseWidth rushWidth distance
			String[] entries = line.split(" ");

			int source = Integer.parseInt(entries[0]);
			int destination = Integer.parseInt(entries[1]);
			String travel_cost = entries[2];

			double baseWidth = 2.5;
			double rushWidth = 3.5;
			double distance = -1;
			if (entries.length >= 6) {
				baseWidth = Double.parseDouble(entries[3]);
				rushWidth = Double.parseDouble(entries[4]);
				distance = Double.parseDouble(entries[5]);
			}

			Edge edge = new Edge(source, destination);
			edge.setBaseWidth(baseWidth);
			edge.setRushWidth(rushWidth);
			edge.setWidth(baseWidth);
			if (distance >= 0) {
				edge.setDistance(distance);
			}

			String[] travel_costs = travel_cost.split(",");

			// Add time-dependent travel costs
			for(int i=0;i<travel_costs.length;i++){
				Properties properties = new Properties(Double.parseDouble(travel_costs[i]));
				edge.add_time_property(Integer.parseInt(arrival_time_series[i]), properties);
			}

			// Insert edge into graph
			Graph.get_node(source).insert_outgoing_edge(edge);
			Graph.get_node(destination).insert_incoming_edge(edge);
		}
		br.close();
	}

	private static void query_processing() throws IOException, InterruptedException, ExecutionException{
		String output_file = "Output_BiTDCPO_" + Graph.get_vertex_count() + ".txt";
		FileWriter fout = new FileWriter(output_file);
		BufferedWriter writer = new BufferedWriter(fout);
		
		
		runtime = Runtime.getRuntime();
		//int index=0;
		while(!queries.isEmpty()){
			forceStop=false;
			double start_departure_time = queries.peek().get_start_departure_time();
			
			runtime.gc();
			long memory_before = runtime.totalMemory() - runtime.freeMemory();
			double budget = queries.peek().get_budget()*(1+overhead/100);
			
			start = System.currentTimeMillis();
			BidirectionalDriver driver = new BidirectionalDriver(queries.peek(), budget);
			Result output = driver.driver();
			
			long end = System.currentTimeMillis();
			long memory_used = memory_after - memory_before;
			
			if(output != null) {
//					if (output.get_departureTime()==-1 && optimization) {
//						optimization = false;
//						Graph.reset_blabeling();
//						continue;
//					}
				writer.write(queries.peek().get_source() + "\t" + queries.peek().get_destination() + "\t" + start_departure_time
						+ "\t" + queries.peek().get_budget() + "\t" + output.get_departureTime() + "\t" + output.get_score() + "\t" + (end - start) / 1000F +
						"\t" + (memory_used/(1024*1024)) +  " " + forceStop + "\n");
				writer.flush();
					System.out.println(queries.peek().get_source() + "\t" + queries.peek().get_destination() + "\t" + start_departure_time
							+ "\t" + queries.peek().get_budget() + "\t" + output.get_departureTime() + "\t" + output.get_score() + "\t" + (end - start) / 1000F +
							"\t" + (memory_used/(1024*1024)) +  "\t" + forceStop);
				
			}
			else {
				System.out.println(queries.peek().get_source() + "\t" + queries.peek().get_destination() + "\t" + queries.peek().get_start_departure_time()
						+ "\t" + Graph.get_node(queries.peek().get_destination()).get_forward_hTime() + "\t" + 0 + "\t" + 0 + "\t" + (end - start) / 1000F +
						"\t" + (memory_used/(1024*1024)) +  "\t" + forceStop);
			}
//				writer2.close();
//				fanalysis.close();
//				writer3.close();
//				fpath.close();
			Graph.reset();
			//clearSubgraph();
			
			queries.poll();
		}
//			if(!optimization)
//				optimization = true;
//			
		
			
		writer.close();
		fout.close();
		System.out.println("All query processing is done.");
	}

    /**
     * Configure solver defaults so repeated runs have consistent thresholds.
     */
    public static void configureDefaults() {
        THRESHOLD = 10;
        SHARP_THRESHOLD = 60;
        WIDENESS_THRESHOLD = 12.8;
		TIME_LIMIT = TIME_LIMIT > 0 ? TIME_LIMIT : 5;
        interval_duration = interval_duration > 0 ? interval_duration : 360;
        pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        System.out.println("[Init] Defaults configured. Thresholds set and pool size=" + pool.getParallelism());
        
        // Use the pre-configured default directory (dataset/London/)
        // configuredGraphDataDir and dataDirectory are already initialized to currentDirectory
        System.out.println("[Init] Default data directory: " + configuredGraphDataDir);
    }

    /**
     * Load the graph from disk using the parsing routines in this class. The
     * directory and vertex count may be overridden; otherwise the configured
     * static defaults are used, falling back to the built-in default path.
     */
    public static boolean loadGraphFromDisk(String directoryOverride, Integer vertexCountOverride) {
        try {
            String dir = resolveDataDirectory(directoryOverride);
            if (dir == null) {
                System.err.println("No graph data directory found. Set configuredGraphDataDir or provide an override.");
                return false;
            }
            dataDirectory = dir.endsWith(File.separator) ? dir : dir + File.separator;
            System.out.println("[Load] Using data directory: " + dataDirectory);

            int vertexCount = resolveVertexCount(dataDirectory, vertexCountOverride);
            if (vertexCount <= 0) {
                System.err.println("Unable to determine vertex count in " + dataDirectory);
                return false;
            }
            // Ensure vertex count is propagated to Graph before parsing files
            Graph.set_vertex_count(vertexCount);
            System.out.println("[Load] Resolved vertex count: " + vertexCount);

            extract_nodes();
            System.out.println("[Load] Nodes extracted: " + Graph.get_nodes().size());
            extract_edges();
            System.out.println("[Load] Edges extracted.");
            System.out.println("[Load] Using merged format - clusters and widths included in nodes/edges files");

            System.out.println("Loaded graph from " + dataDirectory + " with " + Graph.get_nodes().size() + " nodes.");
            return true;
        } catch (Exception e) {
            System.err.println("Error loading graph from files: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String resolveDataDirectory(String override) {
        if (override != null && !override.isBlank()) {
            Path p = Path.of(override);
            if (Files.isDirectory(p)) {
                return p.toString();
            }
        }

        if (configuredGraphDataDir != null && !configuredGraphDataDir.isBlank()) {
            Path p = Path.of(configuredGraphDataDir);
            if (Files.isDirectory(p)) {
                return p.toString();
            }
        }

        if (Files.isDirectory(Path.of(currentDirectory))) {
            return currentDirectory;
        }
        return null;
    }

    private static int resolveVertexCount(String dir, Integer override) throws IOException {
        if (override != null && override > 0) {
            return override;
        }

        Pattern pattern = Pattern.compile("nodes_(\\d+)\\.txt");
        try (Stream<Path> files = Files.list(Path.of(dir))) {
            int detected = files
                    .map(path -> path.getFileName().toString())
                    .map(name -> {
                        Matcher m = pattern.matcher(name);
                        if (m.matches()) {
                            return Integer.parseInt(m.group(1));
                        }
                        return null;
                    })
                    .filter(v -> v != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            if (detected > 0) {
                return detected;
            }
        }

        return configuredGraphVertexCount > 0 ? configuredGraphVertexCount : defaultVertexCount;
    }

    public static void setConfiguredGraphDataDir(String path) {
        configuredGraphDataDir = path;
    }

    public static String getConfiguredGraphDataDir() {
        return configuredGraphDataDir;
    }

    public static void setConfiguredGraphVertexCount(int vertexCount) {
        configuredGraphVertexCount = vertexCount;
    }

    public static int getConfiguredGraphVertexCount() {
        return configuredGraphVertexCount;
    }

   
    /**
     * Run a single query with an explicit interval duration for the end time window.
     * Uses default WIDENESS_ONLY routing mode.
     */
    public static Result runSingleQuery(int source, int destination, double departureMinutes, double intervalMinutes, double budgetMinutes)
            throws InterruptedException, ExecutionException {
        return runSingleQuery(source, destination, departureMinutes, intervalMinutes, budgetMinutes, RoutingMode.WIDENESS_ONLY);
    }

    /**
     * Run a single query with explicit routing mode selection.
     * 
     * @param source Source node ID
     * @param destination Destination node ID
     * @param departureMinutes Departure time in minutes from midnight
     * @param intervalMinutes Time interval for flexible departure
     * @param budgetMinutes Travel time budget constraint
     * @param routingMode User-selected optimization mode
     * @return Result containing optimal path(s) based on routing mode
     */
    public static Result runSingleQuery(int source, int destination, double departureMinutes, 
            double intervalMinutes, double budgetMinutes, RoutingMode routingMode)
            throws InterruptedException, ExecutionException {
        start = System.currentTimeMillis();  // Initialize timer for query execution
        double interval = intervalMinutes > 0 ? intervalMinutes : budgetMinutes;
        interval_duration = interval;
        Query query = new Query(source, destination, departureMinutes, departureMinutes + interval, budgetMinutes, routingMode);
        System.out.println("[Query] Running with mode: " + routingMode.getDisplayName());
        BidirectionalDriver driver = new BidirectionalDriver(query, budgetMinutes);
        try {
            return driver.driver();
        } finally {
            Graph.reset();
        }
    }

    public static void setIntervalDuration(double intervalMinutes) {
        interval_duration = intervalMinutes;
    }

    public static double getIntervalDuration() {
        return interval_duration;
    }

	public static void updateMemory() {
		//runtime.gc();
		memory_after = runtime.totalMemory() - runtime.freeMemory();
		updated_memory = true;
	}

	public static boolean isMemoryUpdated() {
		return updated_memory;
	}
//	public static void updateSubgraph(int n) {
//		subgraphNodes.put(n, subgraphSize);
//		subgraphSize++;
//	}
//	
//	public static void clearSubgraph() {
//		subgraphNodes.clear();
//		subgraphSize = 0;
//	}
//
//	public static int getIndex(int n) {
//		return subgraphNodes.get(n);
//	}
//	
//	public static int getSubgraphNode(int index) {
//		return subgraphIndexes.get(index);
//	}


}
