/**
 * 
 */

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class Node {

	private Map<Integer, Edge> outgoing_edges;
	private	Map<Integer, Edge> incoming_edges;
	private	double latitude;
	private	double longitude;
	private boolean backward_reachebility;
	private double backward_hTime;
	private double forward_hTime;//fastest path cost for forward search
	private double backward_hWideDistance;
	private double forward_hWideDistance;//fastest path cost for forward search
	private double backward_hDistance;
	private double forward_hDistance;//fastest path cost for forward search
	private int backward_hRightTurn;
	private int forward_hRightTurn;//fastest path cost for forward search
	private boolean forward_reachebility;//fastest path cost for backward search
	private boolean feasible;
	private int clusterId = -1; // Default cluster ID is -1 (unclustered)

	public void setForwardHTime(double hTime) {
		this.forward_hTime = hTime;
	}

	public void setBackwardHTime(double hTime) {
		this.backward_hTime = hTime;
	}

	public void setForwardHWideDistance(double hWide) {
		this.forward_hWideDistance = hWide;
	}

	public void setBackwardHWideDistance(double hWide) {
		this.backward_hWideDistance = hWide;
	}

	public void setForwardHDistance(double hDist) {
		this.forward_hDistance = hDist;
	}

	public void setBackwardHDistance(double hDist) {
		this.backward_hDistance = hDist;
	}

	public void setForwardHRightTurn(int hRightTurn) {
		this.forward_hRightTurn = hRightTurn;
	}

	public void setBackwardHRightTurn(int hRightTurn) {
		this.backward_hRightTurn = hRightTurn;
	}
	
	public void setBackwardReachebility() {
		this.backward_reachebility = true;
		setFeasibility();
	}

	public void setForwardReachebility() {
		this.forward_reachebility = true;
		setFeasibility();
	}
	
	private void setFeasibility() {
		if(this.forward_reachebility && this.backward_reachebility) {
			this.feasible = true;
		}
	}
	
	public boolean isFeasible() {
		return this.feasible;
	}

	public boolean isForwardReacheble() {
		return this.forward_reachebility;
	}

	public boolean isBackwardReacheble() {
		return this.backward_reachebility;
	}

	public double get_backward_hTime() {
		return this.backward_hTime;
	}

	public double get_forward_hTime() {
		return this.forward_hTime;
	}

	public double get_backward_hWideDistance() {
		return this.backward_hWideDistance;
	}

	public double get_forward_hWideDistance() {
		return this.forward_hWideDistance;
	}

	public double get_backward_hDistance() {
		return this.backward_hDistance;
	}

	public double get_forward_hDistance() {
		return this.forward_hDistance;
	}

	public int get_backward_hRightTurn() {
		return this.backward_hRightTurn;
	}

	public int get_forward_hRightTurn() {
		return this.forward_hRightTurn;
	}
	
	public void reset() {
		this.backward_reachebility = false;
		this.backward_hTime=Double.MAX_VALUE;
		this.forward_hTime=Double.MAX_VALUE;
		this.backward_hWideDistance=Double.MAX_VALUE;
		this.forward_hWideDistance=Double.MAX_VALUE;
		this.backward_hDistance=Double.MAX_VALUE;
		this.forward_hDistance=Double.MAX_VALUE;
		this.backward_hRightTurn=Integer.MAX_VALUE;
		this.forward_hRightTurn=Integer.MAX_VALUE;
		this.forward_reachebility=false;
		this.feasible=false;
	}
	
	public double get_latitude(){
		return latitude;
	}

	public double get_longitude(){
		return longitude;
	}

	public void insert_incoming_edge(Edge edge){
		incoming_edges.put(edge.get_source(),edge);
	}

	public void insert_outgoing_edge(Edge edge){
		outgoing_edges.put(edge.get_destination(), edge);
	}

	public Map<Integer, Edge> get_incoming_edges(){
		return incoming_edges;
	}

	public Map<Integer, Edge> get_outgoing_edges(){
		return outgoing_edges;
	}

	public double euclidean_distance(Node node){
		double x1 = latitude;
		double y1 = longitude;
		double x2 = node.get_latitude();
		double y2 = node.get_longitude();

		return Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
	}

	public Node(double lat, double longi){
		this.latitude = lat;
		this.longitude = longi;
		this.backward_reachebility = false;
		this.incoming_edges = new HashMap<Integer, Edge>();
		this.outgoing_edges = new HashMap<Integer, Edge>();
	}

	// Getter for cluster ID
    public int getClusterId() {
        return clusterId;
    }

    // Setter for cluster ID
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }
}
