import models.RoutingMode;

/**
 * Query class for FlexiRoute Navigation System
 * Stores query parameters including user-selected routing preferences
 */
public class Query {
	private int source;
	private int destination;
	private double start_departure_time;
	private double end_departure_time;
	private double budget;
	private RoutingMode routingMode;

	public int get_source() {
		return source;
	}

	public int get_destination() {
		return destination;
	}

	public double get_start_departure_time() {
		return start_departure_time;
	}

	public double get_end_departure_time() {
		return end_departure_time;
	}

	public double get_budget() {
		return budget;
	}

	public RoutingMode getRoutingMode() {
		return routingMode;
	}

	public void setRoutingMode(RoutingMode mode) {
		this.routingMode = mode;
	}

	/**
	 * Original constructor - defaults to ALL_OBJECTIVES mode
	 */
	public Query(int src, int dest, double s_dep_time, double e_dep_time, double b) {
		this.source = src;
		this.destination = dest;
		this.start_departure_time = s_dep_time;
		this.end_departure_time = e_dep_time;
		this.budget = b;
		this.routingMode = RoutingMode.ALL_OBJECTIVES; // Default behavior
	}

	/**
	 * New constructor with routing mode selection
	 */
	public Query(int src, int dest, double s_dep_time, double e_dep_time, double b, RoutingMode mode) {
		this.source = src;
		this.destination = dest;
		this.start_departure_time = s_dep_time;
		this.end_departure_time = e_dep_time;
		this.budget = b;
		this.routingMode = mode != null ? mode : RoutingMode.ALL_OBJECTIVES;
	}

	@Override
	public String toString() {
		return String.format("Query{%d->%d, dep=%.0f-%.0f, budget=%.0f, mode=%s}",
			source, destination, start_departure_time, end_departure_time, budget, routingMode);
	}
}
