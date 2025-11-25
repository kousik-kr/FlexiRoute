import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lightweight HTTP facade that exposes node search, metadata, query execution,
 * and live metrics to the Vite frontend without pulling in a heavier servlet
 * container. The server relies on {@link Graph}'s static lifecycle and keeps a
 * minimal demo network in-memory so developers can hit the endpoints immediately.
 */
public class ApiServer {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);

        initializeGraph();

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (BindException ex) {
            System.err.println("Port " + port + " is in use; falling back to an available port.");
            server = HttpServer.create(new InetSocketAddress(0), 0);
            port = server.getAddress().getPort();
        }
        server.createContext("/api/nodes", ApiServer::handleNodes);
        server.createContext("/api/network/meta", ApiServer::handleNetworkMeta);
        server.createContext("/api/network/graph", ApiServer::handleGraphPreview);
        server.createContext("/api/queries/run", ApiServer::handleRunQuery);
        server.createContext("/api/metrics/live", ApiServer::handleMetrics);
        server.setExecutor(ForkJoinPool.commonPool());
        System.out.println("API server started on port " + port);
        server.start();
    }

    private static int resolvePort(String[] args) {
        int port = DEFAULT_PORT;
        String envPort = System.getenv("BACKEND_PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) { }
        }
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) { }
        }
        return port;
    }

    /**
     * Seed the static {@link Graph} with the smallest possible network and set
     * conservative defaults for the A* parameters. This keeps repeated runs
     * deterministic and prevents null-pointer crashes while the real dataset is
     * loading.
     */
    private static void bootstrapGraph() {
        // If graph already populated skip
        if (!Graph.get_nodes().isEmpty()) {
            return;
        }

        // Provide a tiny in-memory network so the API is immediately usable even
        // without the full dataset on disk.
        Graph.set_vertex_count(4);
        Graph.updateArrivalTimeSeries(new String[]{"0", "720", "1440"});
        Graph.updateWidthTimeSeries(new String[]{"0", "720", "1440"});

        Graph.add_node(0, new Node(40.7128, -74.0060));
        Graph.add_node(1, new Node(40.7138, -74.0010));
        Graph.add_node(2, new Node(40.7160, -74.0040));
        Graph.add_node(3, new Node(40.7140, -74.0100));

        addDemoEdge(0, 1, 0.5, false, 8.0, 8.0, 6.0);
        addDemoEdge(1, 2, 0.6, false, 10.0, 10.0, 5.5);
        addDemoEdge(2, 3, 0.7, false, 9.0, 9.0, 6.5);
        addDemoEdge(0, 3, 0.9, false, 12.0, 12.0, 8.0);
    }

    /**
     * Configure solver defaults then attempt to load the real road network from
     * disk. Falls back to the built-in demo network if files are missing.
     */
    private static void initializeGraph() {
        configureSolverDefaults();

        if (!Graph.get_nodes().isEmpty()) {
            return;
        }

        boolean loaded = loadGraphFromFiles();
        if (!loaded) {
            System.err.println("Unable to load road network from disk; starting demo graph instead.");
            bootstrapGraph();
        }
    }

    private static void configureSolverDefaults() {
        BidirectionalAstar.THRESHOLD = 10;
        BidirectionalAstar.SHARP_THRESHOLD = 60;
        BidirectionalAstar.WIDENESS_THRESHOLD = 12.8;
        BidirectionalAstar.TIME_LIMIT = 5;
        BidirectionalAstar.pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    private static boolean loadGraphFromFiles() {
        try {
            Path dataRoot = resolveDataDirectory();
            if (dataRoot == null) {
                System.err.println("No graph data directory found. Set BidirectionalAstar configuredGraphDataDir or ensure BidirectionalAstar.currentDirectory exists.");
                return false;
            }

            int vertexCount = resolveVertexCount(dataRoot);
            if (vertexCount <= 0) {
                System.err.println("Unable to determine vertex count in " + dataRoot);
                return false;
            }

            // Path nodesPath = dataRoot.resolve("nodes_" + vertexCount + ".txt");
            // Path edgesPath = dataRoot.resolve("edges_" + vertexCount + ".txt");
            // Path clusterPath = dataRoot.resolve("node_" + vertexCount + ".txt");
            // Path edgeWidthPath = dataRoot.resolve("edge_" + vertexCount + ".txt");

            // if (!Files.exists(nodesPath) || !Files.exists(edgesPath)) {
            //     System.err.println("Missing required graph files: " + nodesPath + " or " + edgesPath);
            //     return false;
            // }

            BidirectionalAstar.driver();

            System.out.println("Loaded road network from " + dataRoot + " with " + Graph.get_nodes().size() + " nodes.");
            return true;
        } catch (Exception e) {
            System.err.println("Error loading graph from files: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static Path resolveDataDirectory() {
        String configured = BidirectionalAstar.getConfiguredGraphDataDir();
        if (configured != null && !configured.isBlank()) {
            Path dir = Paths.get(configured);
            if (Files.isDirectory(dir)) {
                return dir;
            }
        }

        try {
            java.lang.reflect.Field field = BidirectionalAstar.class.getDeclaredField("currentDirectory");
            field.setAccessible(true);
            String path = (String) field.get(null);
            if (path != null && !path.isBlank()) {
                Path dir = Paths.get(path);
                if (Files.isDirectory(dir)) {
                    return dir;
                }
            }
        } catch (Exception ignored) { }

        return null;
    }

    private static int resolveVertexCount(Path dataRoot) throws IOException {
        Pattern pattern = Pattern.compile("nodes_(\\d+)\\.txt");
        try (Stream<Path> files = Files.list(dataRoot)) {
            Optional<Integer> count = files
                    .map(path -> path.getFileName().toString())
                    .map(name -> {
                        Matcher m = pattern.matcher(name);
                        if (m.matches()) {
                            return Integer.parseInt(m.group(1));
                        }
                        return null;
                    })
                    .filter(val -> val != null)
                    .max(Integer::compareTo);
            if (count.isPresent()) {
                return count.get();
            }
        }

        int configured = BidirectionalAstar.getConfiguredGraphVertexCount();
        return Math.max(configured, 0);
    }

    private static void loadNodes(Path nodesPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(nodesPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] entries = line.split(" ");
                if (entries.length < 3) continue;
                Node node = new Node(Double.parseDouble(entries[1]), Double.parseDouble(entries[2]));
                Graph.add_node(Integer.parseInt(entries[0]), node);
            }
        }
    }

    private static void loadEdges(Path edgesPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(edgesPath, StandardCharsets.UTF_8)) {
            String line = br.readLine();
            String[] arrivalTimeSeries = line != null ? line.split(" ") : new String[0];
            line = br.readLine();
            String[] widthTimeSeries = line != null ? line.split(" ") : new String[0];
            Graph.updateArrivalTimeSeries(arrivalTimeSeries);
            Graph.updateWidthTimeSeries(widthTimeSeries);

            while ((line = br.readLine()) != null) {
                String[] entries = line.split(" ");
                if (entries.length < 5) continue;

                int source = Integer.parseInt(entries[0]);
                int destination = Integer.parseInt(entries[1]);
                double distance = Double.parseDouble(entries[2]);
                boolean clearway = Boolean.parseBoolean(entries[3]);
                String travelCost = entries[4];
                String width = entries[3];

                Edge edge = new Edge(source, destination);
                String[] travelCosts = travelCost.split(",");
                for (int i = 0; i < travelCosts.length; i++) {
                    Properties properties = new Properties(Double.parseDouble(travelCosts[i]));
                    edge.add_time_property(Integer.parseInt(arrivalTimeSeries[i]), properties);
                }

                if (clearway) {
                    String[] widths = width.split(",");
                    for (int i = 0; i < widths.length; i++) {
                        Properties properties = new Properties(Double.parseDouble(widths[i]));
                        edge.add_wideness_property(Integer.parseInt(widthTimeSeries[i]), properties);
                    }
                } else {
                    edge.setWidth(Double.parseDouble(width));
                }

                Node srcNode = Graph.get_node(source);
                Node dstNode = Graph.get_node(destination);
                if (srcNode != null && dstNode != null) {
                    srcNode.insert_outgoing_edge(edge);
                    dstNode.insert_incoming_edge(edge);
                }
            }
        }
    }

    private static void loadClusterInfo(Path clusterPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(clusterPath, StandardCharsets.UTF_8)) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] entries = line.split("\t");
                if (entries.length < 4) continue;
                int nodeId = Integer.parseInt(entries[0]);
                int clusterId = Integer.parseInt(entries[3]);

                Node node = Graph.get_node(nodeId);
                if (node != null) {
                    node.setClusterId(clusterId);
                    Cluster cluster = Graph.getCluster(clusterId);
                    if (cluster == null) {
                        cluster = new Cluster(clusterId);
                        Graph.addCluster(cluster);
                    }
                    cluster.addNode(node);
                }
            }
        }
    }

    private static void loadEdgeWidthInfo(Path edgeWidthPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(edgeWidthPath, StandardCharsets.UTF_8)) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] entries = line.split("\t");
                if (entries.length < 6) continue;
                int source = Integer.parseInt(entries[0]);
                int destination = Integer.parseInt(entries[1]);
                double baseWidth = Double.parseDouble(entries[4]);
                double rushWidth = Double.parseDouble(entries[5]);

                Node sourceNode = Graph.get_node(source);
                if (sourceNode != null) {
                    Edge edge = sourceNode.get_outgoing_edges().get(destination);
                    if (edge != null) {
                        edge.setWidth(baseWidth);
                        edge.add_wideness_property(0, new Properties(rushWidth));
                    }
                }
            }
        }
    }

    private static void addDemoEdge(int src, int dest, double distance, boolean clearway, double baseWidth, double rushWidth, double travelMinutes) {
        Edge edge = new Edge(src, dest, baseWidth, rushWidth);
        edge.add_time_property(0, new Properties(travelMinutes));
        edge.add_time_property(720, new Properties(travelMinutes));
        edge.add_time_property(1440, new Properties(travelMinutes));
        if (!clearway) {
            edge.setWidth(baseWidth);
        } else {
            edge.add_wideness_property(0, new Properties(baseWidth));
            edge.add_wideness_property(720, new Properties(rushWidth));
            edge.add_wideness_property(1440, new Properties(baseWidth));
        }
        Graph.get_node(src).insert_outgoing_edge(edge);
        Graph.get_node(dest).insert_incoming_edge(edge);
    }

    /**
     * Responds with a small list of node summaries. Filtering is intentionally
     * lightweight (ID substring match) so the endpoint remains responsive even
     * without a spatial index. The frontend debounces calls before hitting this.
     */
    private static void handleNodes(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        URI uri = exchange.getRequestURI();
        Map<String, String> queryParams = parseQueryParams(Optional.ofNullable(uri.getQuery()).orElse(""));
        String search = queryParams.getOrDefault("search", "").toLowerCase(Locale.ROOT);

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Map.Entry<Integer, Node> entry : Graph.get_nodes().entrySet()) {
            int id = entry.getKey();
            Node node = entry.getValue();
            if (search.length() > 1) {
                String idStr = Integer.toString(id);
                if (!idStr.contains(search)) continue;
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", id);
            payload.put("latitude", node.get_latitude());
            payload.put("longitude", node.get_longitude());
            payload.put("degree", node.get_outgoing_edges().size());
            nodes.add(payload);
        }

        writeJson(exchange, toJsonArray(nodes));
    }

    /**
     * Emits bounding box metadata for the currently loaded network so the
     * frontend can fit the map view and display the vertex count.
     */
    private static void handleNetworkMeta(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        DoubleSummaryStatistics latStats = Graph.get_nodes().values().stream()
                .collect(Collectors.summarizingDouble(Node::get_latitude));
        DoubleSummaryStatistics lonStats = Graph.get_nodes().values().stream()
                .collect(Collectors.summarizingDouble(Node::get_longitude));

        String body = String.format(Locale.ROOT,
                "{\"vertexCount\":%d,\"bounds\":[%.6f,%.6f,%.6f,%.6f]}",
                Graph.get_vertex_count(),
                lonStats.getMin(), latStats.getMin(), lonStats.getMax(), latStats.getMax());
        writeJson(exchange, body);
    }

    /**
     * Returns a lightweight snapshot of the currently loaded graph so the
     * frontend can render a preview without waiting for user input. The
     * response includes every node and a capped set of edges to keep payloads
     * small for large networks.
     */
    private static void handleGraphPreview(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = parseQueryParams(Optional.ofNullable(exchange.getRequestURI().getQuery()).orElse(""));
        int maxEdges = 800;
        if (params.containsKey("maxEdges")) {
            try {
                maxEdges = Math.max(1, Integer.parseInt(params.get("maxEdges")));
            } catch (NumberFormatException ignored) { }
        }

        StringBuilder json = new StringBuilder();
        json.append('{');

        json.append("\"nodes\":[");
        boolean first = true;
        for (Map.Entry<Integer, Node> entry : Graph.get_nodes().entrySet()) {
            Node node = entry.getValue();
            if (!first) json.append(',');
            first = false;
            json.append('{')
                    .append("\"id\":").append(entry.getKey()).append(',')
                    .append("\"coord\":[")
                    .append(String.format(Locale.ROOT, "%.6f,%.6f", node.get_longitude(), node.get_latitude()))
                    .append("]}");
        }
        json.append(']');

        json.append(",\"edges\":[");
        Set<String> seenEdges = new HashSet<>();
        int edgeCount = 0;
        boolean firstEdge = true;
        for (Map.Entry<Integer, Node> entry : Graph.get_nodes().entrySet()) {
            if (edgeCount >= maxEdges) break;
            int from = entry.getKey();
            Node source = entry.getValue();
            for (Edge edge : source.get_outgoing_edges().values()) {
                if (edgeCount >= maxEdges) break;
                int to = edge.get_destination();
                Node dest = Graph.get_node(to);
                if (dest == null) continue;

                String key = from + "->" + to;
                if (!seenEdges.add(key)) continue;

                if (!firstEdge) json.append(',');
                firstEdge = false;
                edgeCount++;
                json.append('{')
                        .append("\"from\":").append(from).append(',')
                        .append("\"to\":").append(to).append(',')
                        .append("\"line\":[")
                        .append(String.format(Locale.ROOT, "[%.6f,%.6f],[%.6f,%.6f]",
                                source.get_longitude(), source.get_latitude(),
                                dest.get_longitude(), dest.get_latitude()))
                        .append("]}");
            }
        }
        json.append(']');

        json.append('}');
        writeJson(exchange, json.toString());
    }

    /**
     * Runs the bidirectional query end-to-end and returns a GeoJSON feature plus
     * diagnostics. The handler is intentionally defensive: we default missing
     * fields, guard against solver errors, and always respond with a valid
     * JSON payload so the UI can render feedback instead of failing silently.
     */
    private static void handleRunQuery(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String body = readBody(exchange.getRequestBody());
        Map<String, Double> payload = parseNumericJson(body);

        int source = payload.getOrDefault("source", 0.0).intValue();
        int destination = payload.getOrDefault("destination", 0.0).intValue();
        double departure = payload.getOrDefault("startDepartureMinutes", 0.0);
        double budget = payload.getOrDefault("budgetMinutes", 60.0);
        double intervalDuration = payload.getOrDefault(
                "intervalDurationMinutes",
                BidirectionalAstar.interval_duration > 0 ? BidirectionalAstar.interval_duration : 360.0
        );

        if (!Graph.contains_node(source) || !Graph.contains_node(destination)) {
            writeError(exchange, 400, "Source or destination node is not present in the current graph.");
            return;
        }

        Result result = null;
        long start = System.currentTimeMillis();
        try {
            result = BidirectionalAstar.runSingleQuery(source, destination, departure, intervalDuration, budget);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        long elapsed = System.currentTimeMillis() - start;
        Runtime runtime = Runtime.getRuntime();
        double memoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0);

        if (result == null) {
            result = new Result(
                    departure,
                    0.0,
                    0,
                    0,
                    0.0,
                    Collections.emptyList(),
                    Collections.emptyList());
        }

        Node sourceNode = Graph.get_node(source);
        Node destinationNode = Graph.get_node(destination);
        double[][] coordinates = new double[][]{
                {sourceNode != null ? sourceNode.get_longitude() : 0.0, sourceNode != null ? sourceNode.get_latitude() : 0.0},
                {destinationNode != null ? destinationNode.get_longitude() : 0.0, destinationNode != null ? destinationNode.get_latitude() : 0.0}
        };
        double widthValue = Graph.get_node(source) != null && !Graph.get_node(source).get_outgoing_edges().isEmpty()
                ? Graph.get_node(source).get_outgoing_edges().values().iterator().next().get_width(departure)
                : 6.0;

        StringBuilder coordBuilder = new StringBuilder();
        coordBuilder.append("[");
        for (int i = 0; i < coordinates.length; i++) {
            coordBuilder.append(String.format(Locale.ROOT, "[%.6f,%.6f]", coordinates[i][0], coordinates[i][1]));
            if (i < coordinates.length - 1) coordBuilder.append(',');
        }
        coordBuilder.append("]");

        String responseJson = String.format(Locale.ROOT,
                "{\"result\":{\"departureTime\":%.2f,\"score\":%.2f,\"rightTurns\":%d},\"geometry\":{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":%s},\"properties\":{\"widthMeters\":[%.2f,%.2f],\"clearway\":[false,false],\"timeSeries\":[%.2f,%.2f],\"width\":%.2f}},\"diagnostics\":{\"elapsedSeconds\":%.3f,\"memoryMb\":%.3f}}",
                result.get_departureTime(), result.get_score(), result.get_right_turns(),
                coordBuilder.toString(),
                widthValue, widthValue,
                departure, departure + budget,
                widthValue,
                elapsed / 1000.0, memoryMb);

        writeJson(exchange, responseJson);
    }

    /**
     * Provides a tiny slice of JVM metrics for the live stats panel without
     * requiring JMX or external probes.
     */
    private static void handleMetrics(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        double memoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0);
        String json = String.format(Locale.ROOT, "{\"memoryMb\":%.2f,\"timestamp\":%d}", memoryMb, System.currentTimeMillis());
        writeJson(exchange, json);
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query.isEmpty()) return params;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private static Map<String, Double> parseNumericJson(String body) {
        Map<String, Double> values = new HashMap<>();
        String sanitized = body.trim();
        if (sanitized.startsWith("{") && sanitized.endsWith("}")) {
            sanitized = sanitized.substring(1, sanitized.length() - 1);
        }
        for (String part : sanitized.split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].replaceAll("[\\\"{} ]", "");
            String val = kv[1].replaceAll("[\\\"{} ]", "");
            try {
                values.put(key, Double.parseDouble(val));
            } catch (NumberFormatException ignored) { }
        }
        return values;
    }

    private static String readBody(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static void writeJson(HttpExchange exchange, String body) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Minimal JSON error helper to keep handler logic focused on validation
     * rather than response formatting.
     */
    private static void writeError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        String body = String.format(Locale.ROOT, "{\"error\":\"%s\"}", message.replace('"', '\''));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String toJsonArray(List<Map<String, Object>> items) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            sb.append('{');
            int idx = 0;
            for (Map.Entry<String, Object> ent : item.entrySet()) {
                sb.append('\"').append(ent.getKey()).append('\"').append(':');
                Object value = ent.getValue();
                if (value instanceof String) {
                    sb.append('\"').append(value).append('\"');
                } else {
                    sb.append(String.format(Locale.ROOT, "%s", value));
                }
                if (idx < item.size() - 1) sb.append(',');
                idx++;
            }
            sb.append('}');
            if (i < items.size() - 1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }
}
