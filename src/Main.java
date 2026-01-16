import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;
//import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
//import java.util.Stack;
import java.util.TreeMap;

/**
 * ------------------------------------------London Network------------------------------------
 * 250-400 meter/mins speed for each edge
 * --------Rush Hour-------------------
 * 7.30am-9.30am && 4.00pm-6.30pm
 * 00.00	7.30	8.00	8.30	9.00	9.30	4.00	4.30	5.00	5.30	6.00	6.30
 */

/**
 * @author kkdutta
 *
 */
public class Main {

	/**
	 * @param args
	 */
	static final String currentDirectory = System.getProperty("user.dir");
	private static int n = 264346;
	//static final String dataFile = currentDirectory + "/Moscow_Edgelist.csv";
	private static String EdgeFile = currentDirectory + "/USA-road-t.NY.gr";
	private static String NodeFile = currentDirectory + "/USA-road-d.NY.co";
	private static List<Edge> edges = new ArrayList<Edge>();
//	private static ArrayList<Node> nodeList = new ArrayList<Node>();
//	private static ArrayList<Node> new_nodeList = null;
//	private static ArrayList<ArrayList<Node>> connected_components = new ArrayList<ArrayList<Node>>();
//	private static ArrayList<Node> trans_nodeList = null;
//	private static ArrayList<Integer> nodes = null;
//	private static ArrayList<Integer> tmp_nodes = new ArrayList<Integer>();
//	private static int nEdge = 0;
//	private static ArrayList<ArrayList<Integer>> connected_nodes = new ArrayList<ArrayList<Integer>>();
	private static List<RushHour> rush_hours = new ArrayList<RushHour>();
	//private static int max_speed = 400;
	//private static int min_speed = 250;		//both speed limits are in meter/mins
	private static List<Integer> time_series = new ArrayList<Integer>();
	private static int density;	//density of edges with positive value
	private static double timeUnitConvertor = 6000;
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		
		RushHour rush1 = new RushHour(7*60+30, 9*60+30);
		rush_hours.add(rush1);
		RushHour rush2 = new RushHour(16*60, 18*60+30);
		rush_hours.add(rush2);
		fill_time_series();
		density = 20;//Integer.parseInt(args[0]);
		extractEdgeFile();
		generateTDCostNScore();
		printEdgeFile();
		createNodeFile();
		//n = Integer.parseInt(args[0]);
		//createNodeList();
//		try {
//			createNodeFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Node File is created");
//		try {
//			createEdgeFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		//compute_Edge_files("edges_" + n + "_" + density + ".txt");
		System.out.println("Edge File is created");
	}
	
	private static void printEdgeFile() throws IOException {
		FileWriter fedge = null;
		try {
			fedge = new FileWriter("edges_" + n + "_" + density + ".txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter edgeWriter = new BufferedWriter(fedge);
		
		for(int i=0;i<time_series.size();i++) {
			edgeWriter.write(time_series.get(i) + " ");
		}
		edgeWriter.write("\n");
		
		for(Edge edg : edges) {
	    	edgeWriter.write(edg.get_source() + " " + edg.get_destination() + " ");

			for(int j=0;j<time_series.size()-1;j++) {
				edgeWriter.write(edg.getProperty(time_series.get(j)).get_travel_cost() + ",");
			}
			edgeWriter.write(edg.getProperty(time_series.get(time_series.size() - 1)).get_travel_cost() + " ");
			
			for(int j=0;j<time_series.size()-1;j++) {
				edgeWriter.write(edg.getProperty(time_series.get(j)).get_score() + ",");
			}
			edgeWriter.write(edg.getProperty(time_series.get(time_series.size() - 1)).get_score() + "\n");
        }
		edgeWriter.close();
		fedge.close();
	}

	private static void generateTDCostNScore() {

		List<Integer> score = new ArrayList<Integer>();
		for(int i=0;i<edges.size();i++) {
			if(i<(int)Math.ceil((double)edges.size()*density/100)) score.add(1);
			else score.add(0);
		}
		Collections.shuffle(score);
		Random rand = new Random();
		
		for(int ind =0;ind<edges.size();ind++) {
			Edge edg = edges.get(ind);
			double cost = edg.get_distance()/timeUnitConvertor;
			int rush =0;
			boolean insideRush = false;
					
			for(int i=0;i<time_series.size();i++) {
				if(time_series.get(i) == rush_hours.get(rush).start_time)
					insideRush = true;
				if(time_series.get(i) == rush_hours.get(rush).end_time) {
					insideRush = false;
					rush++;
				}
				double temp_cost = cost;
				
				if(insideRush) {
					int difference = time_series.get(i)-rush_hours.get(rush).start_time;
					if(difference/30 == 0 || difference/30 == 4) {
						int x = rand.nextInt(10,15);
						temp_cost += cost*x/100;
					}
					if(difference/30 == 1 || difference/30 == 3) {
						int x = rand.nextInt(20,25);
						temp_cost += cost*x/100;
					}
					if(difference/30 == 2) {
						int x = rand.nextInt(30,40);
						temp_cost += cost*x/100;
					}
				}
				
				int temp_score = 0;
				if(score.get(ind)==1) {
					temp_score = rand.nextInt(1,15);
				}
				Properties property = new Properties(temp_cost, temp_score);
				edg.add_property(time_series.get(i), property);
			}
		}
		
	}

	private static void createNodeFile() throws NumberFormatException, IOException {
		File fin = new File(NodeFile);
		BufferedReader br = new BufferedReader(new FileReader(fin));
		String line;
		
		FileWriter fnode = null;
		try {
			fnode = new FileWriter("nodes_" + n + ".txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter nodeWriter = new BufferedWriter(fnode);
            
		while((line = br.readLine()) != null){
			String[] entries = null;
			entries = line.split(" ");
			
			if(entries[0].equals("v")) {
				int id = Integer.parseInt(entries[1]);
				nodeWriter.write((id-1) + " " + entries[2] + " " + entries[3] + "\n");
			}

		}
		br.close();
		nodeWriter.close();
		fnode.close();
	}
	
	private static void extractEdgeFile() throws NumberFormatException, IOException {
		File fin = new File(EdgeFile);
		BufferedReader br = new BufferedReader(new FileReader(fin));
		String line;
		//double edgeCost = 0;
		while((line = br.readLine()) != null){
			String[] entries = null;
			entries = line.split(" ");

			if(entries[0].equals("a")) {
				int source = Integer.parseInt(entries[1]);
				int destination = Integer.parseInt(entries[2]);
				double travel_cost = Double.parseDouble(entries[3]);
				Edge edge = new Edge(source, destination, travel_cost);
				//edgeCost += travel_cost;
				edges.add(edge);
				//System.out.println(edges.size());
			}

		}
		
		//System.out.println(edgeCost/edges.size());
		br.close();
		
	}
//
//	private static void compute_Edge_files(String string) throws IOException {
//		String time = null;
//		List<String> edges = new ArrayList<String>();
//		try {
//			File file = new File(string);
//			BufferedReader br = new BufferedReader(new FileReader(file));
//			time = br.readLine();
//			
//			String st = null;
//			while((st = br.readLine()) != null) {
//				String s[] = st.split(" ");
//				int src = Integer.parseInt(s[0]);
//				int dest = Integer.parseInt(s[1]);
//				String costs = s[2];
//				String edge = src + " " + dest + " " + costs;
//				edges.add(edge);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		int [] dens = {10, 20, 30, 40};
//		for(int density:dens) {
//			FileWriter fedge = null;
//			try {
//				fedge = new FileWriter("edges_" + n + "_" + density + ".txt");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//			List<Integer> score = new ArrayList<Integer>();
//			Random rand = new Random();
//			for(int i=0;i<edges.size();i++) {
//				if(i<(int)Math.ceil((double)edges.size()*density/100)) score.add(rand.nextInt(15));
//				else score.add(0);
//			}
//			Collections.shuffle(score);
//			BufferedWriter edgeWriter = new BufferedWriter(fedge);
//			
//			
//			edgeWriter.write(time + "\n");
//			
//			int k=0;
//			for (String edge: edges){
//	            edgeWriter.write(edge + " " + score.get(k) + "\n");
//	            k++;
//	        }
//			edgeWriter.close();
//			fedge.close();
//		}
//		
//	}
//
//	private static void createNodeList() throws NumberFormatException, IOException {
//		FileReader reader = null;
//
//		try {
//
//			reader = new FileReader(dataFile);
//
//		} catch (FileNotFoundException e) {
//
//			e.printStackTrace();
//
//		}
//
//		BufferedReader br = new BufferedReader(reader);		
//		String[] s = null;
//		try {
//			s = br.readLine().split(",");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		
//		String str =null;
//		while((str = br.readLine())!=null) {
//			s = str.split(",");
//			if(!tmp_nodes.contains(Integer.parseInt(s[2]))){
//				tmp_nodes.add(Integer.parseInt(s[2]));
//				double latitude = Double.parseDouble(s[0]);
//				double longitude = Double.parseDouble(s[1]);
//				Node node = new Node(latitude, longitude);
//				nodeList.add(node);
//			}
//			
//			int src = Integer.parseInt(s[2]);
//			int dest = Integer.parseInt(s[3]);
//			double distance = Double.parseDouble(s[5]);
//			Edge adjEdg = new Edge(src, dest);
//
//			Random random = new Random();
//			int speed = random.nextInt(min_speed, max_speed);
//			double cost = distance/speed;
//			int rush =0;
//			boolean insideRush = false;
//			double temp_cost = cost;
//					
//			for(int i=0;i<time_series.size();i++) {
//				if(time_series.get(i) == rush_hours.get(rush).start_time)
//					insideRush = true;
//				if(time_series.get(i) == rush_hours.get(rush).end_time) {
//					insideRush = false;
//					rush++;
//				}
//				
//				if(insideRush) {
//					int difference = time_series.get(i)-rush_hours.get(rush).start_time;
//					if(difference/30 == 0 || difference/30 == 4) {
//						temp_cost = cost;
//						int x = random.nextInt(10,15);
//						cost += cost*x/100;
//					}
//					if(difference/30 == 1 || difference/30 == 3) {
//						temp_cost = cost;
//						int x = random.nextInt(20,25);
//						cost += cost*x/100;
//					}
//					if(difference/30 == 2) {
//						temp_cost = cost;
//						int x = random.nextInt(30,40);
//						cost += cost*x/100;
//					}
//				}
//				
//				adjEdg.cost.put(time_series.get(i), cost);
//				cost = temp_cost;
//			}
//			nodeList.get(nodeList.size()-1).adjacencies.add(adjEdg);
//		}
//		
//		n = nodeList.size();
//		remove_edge(tmp_nodes, nodeList);
//		find_largest_connected_component();
//			
//	}
//	
//	
//	private static void remove_edge(ArrayList<Integer> tmp_nodes, ArrayList<Node> nodeList) {
//		for(int i=0;i<n;i++) {
//			Iterator<Edge> itr = nodeList.get(i).adjacencies.listIterator();
//			while(itr.hasNext()) {
//				Edge edg = itr.next();
//				if(!tmp_nodes.contains(edg.dest)) {
//					itr.remove();
//					//nodeList.get(i).adjacencies.remove(edg);
//				}
//			}
//		}
//		
//	}
//
//	static void DFSUtil(int v,boolean visited[], ArrayList<Node> tmp_nodeList, ArrayList<Integer> tmp_node){
//        visited[v] = true;
//        tmp_nodeList.add(nodeList.get(v));
//        if(nodeList.get(v).adjacencies.size()>0) tmp_node.add(nodeList.get(v).adjacencies.get(0).src);
//        int nd;
//        Iterator<Edge> i = trans_nodeList.get(v).adjacencies.iterator();
//        while (i.hasNext()) {
//            nd = tmp_nodes.indexOf(i.next().dest);
//            if (!visited[nd])
//                DFSUtil(nd,visited, tmp_nodeList, tmp_node);
//        }
//    }
//  
//    static void getTranspose(){
//    	
//    	for (int v = 0; v < n; v++){
//    		Node node = new Node(nodeList.get(v).X, nodeList.get(v).Y);
//    		trans_nodeList.add(node);
//    	}
//    	
//        for (int v = 0; v < n; v++){
//            Iterator<Edge> i =nodeList.get(v).adjacencies.listIterator();
//            while(i.hasNext()) {
//            	Edge tmp_edg = i.next();
//            	Edge edg  = new Edge(tmp_edg.dest, tmp_edg.src);
//            	trans_nodeList.get(tmp_nodes.indexOf(tmp_edg.dest)).adjacencies.add(edg);
//            }
//        }
//    }
//  
//    static void fillOrder(int v, boolean visited[], Stack<Integer> stack){
//        visited[v] = true;
//  
//        Iterator<Edge> i = nodeList.get(v).adjacencies.iterator();
//        while (i.hasNext()){
//            int nd = tmp_nodes.indexOf(i.next().dest);
//            if(!visited[nd])
//                fillOrder(nd, visited, stack);
//        }
//  
//        stack.push(v);
//    }
//  
//    static void find_largest_connected_component(){
//        Stack<Integer> stack = new Stack<Integer>();
//  
//        boolean visited[] = new boolean[n];
//        for(int i = 0; i < n; i++)
//            visited[i] = false;
//        
//        for(int i=0;i<n;i++)
//        	if(!visited[i]) fillOrder(i, visited, stack);
//  
//	
//	System.out.println("DFS is applied for first time successfully.");
//        trans_nodeList = new ArrayList<Node>();
//        getTranspose();
//  	
//	System.out.println("Reverse graph is created successfully.");
//        for (int i = 0; i < n; i++)
//            visited[i] = false;
//        
//        while (stack.empty() == false){
//            int v = (int)stack.pop();
//  
//            if (visited[v] == false){
//
//            	ArrayList<Node> tmp_nodeList = new ArrayList<Node>();
//            	ArrayList<Integer> tmp_node = new ArrayList<Integer>();
//                DFSUtil(v, visited, tmp_nodeList, tmp_node);
//                connected_components.add(tmp_nodeList);
//                connected_nodes .add(tmp_node);
//            }
//        }
//	System.out.println("DFS is applied for second time successfully.");
//        
//        new_nodeList = new ArrayList<Node>();
//        nodes = new ArrayList<Integer>();
//        for(ArrayList<Node> tmp_nodeList: connected_components) {
//        	if(tmp_nodeList.size()>new_nodeList.size()) {
//        		new_nodeList.clear();
//        		nodes.clear();
//        		new_nodeList.addAll(tmp_nodeList);
//        		nodes.addAll(connected_nodes.get(connected_components.indexOf(tmp_nodeList)));
//        	}
//        }
//
//        n = new_nodeList.size();
//        remove_edge(nodes, new_nodeList);
//    }
//
//
//	private static void createNodeFile() throws IOException {
//		
//		System.out.println("Number of Nodes : " + n);
//		FileWriter fnode = null;
//		try {
//			fnode = new FileWriter("nodes_" + n + ".txt");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		BufferedWriter nodeWriter = new BufferedWriter(fnode);
//		for (int v = 0; v < n; v++){
//            Iterator<Edge> i = new_nodeList.get(v).adjacencies.listIterator();
//            while(i.hasNext()) {
//            	nEdge++;
//            	Edge tmp_edg = i.next();
//            }
//            nodeWriter.write(v + " " + new_nodeList.get(v).X + " " + new_nodeList.get(v).Y + "\n");
//            	
//        }
//		System.out.println("Number of Edges : " + nEdge);
//		nodeWriter.close();
//		fnode.close();
//	}
//
//	private static void createEdgeFile() throws IOException {
//		FileWriter fedge = null;
//		try {
//			fedge = new FileWriter("edges_" + n + "_" + density + ".txt");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		List<Integer> score = new ArrayList<Integer>();
//		Random rand = new Random();
//		for(int i=0;i<nEdge;i++) {
//			if(i<(int)Math.ceil((double)nEdge*density/100)) score.add(rand.nextInt(15));
//			else score.add(0);
//		}
//		Collections.shuffle(score);
//		BufferedWriter edgeWriter = new BufferedWriter(fedge);
//		
//		for(int i=0;i<time_series.size();i++) {
//			edgeWriter.write(time_series.get(i) + " ");
//		}
//		edgeWriter.write("\n");
//		
//		int k=0;
//		for (int v = 0; v < n; v++){
//            Iterator<Edge> i = new_nodeList.get(v).adjacencies.listIterator();
//            while(i.hasNext()) {
//            	Edge tmp_edg = i.next();
//            	edgeWriter.write(nodes.indexOf(tmp_edg.src) + " " + nodes.indexOf(tmp_edg.dest) + " ");
//            	
//            	for(int j=0;j<time_series.size()-1;j++) {
//            		edgeWriter.write(tmp_edg.cost.get(time_series.get(j)) + ",");
//            	}
//            	edgeWriter.write(tmp_edg.cost.get(time_series.get(time_series.size() - 1)) + " ");
//            	
//            	for(int j=0;j<time_series.size()-1;j++) {
//            		edgeWriter.write(score.get(k) + ",");
//            	}
//            	edgeWriter.write(score.get(k) + "\n");
//            	k++;
//            }
//            	
//        }
//		edgeWriter.close();
//		fedge.close();
//	}
//
	private static void fill_time_series() {
		time_series.add(0);
		
		for(RushHour rush: rush_hours) {
			int time = rush.start_time;
			
			while(time<=rush.end_time) {
				time_series.add(time);
				time += 30;
			}
		}
	}
//	
//	

}

// Legacy classes removed - using separate Edge.java and Properties.java files