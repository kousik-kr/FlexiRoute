/**
 * 
 */

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * 
 */
public class Edge {

	private	int source;
	private	int destination;
	private	NavigableMap<Integer, Properties> time_property;
	private	NavigableMap<Integer, Properties> wideness_property;
	private double lowest_cost;
	private double distance;
	private double baseWidth; // Width during non-rush hours
	private double rushWidth; // Width during rush hours
	
	public int get_source(){
		return this.source;
	}

	public int get_destination(){
		return this.destination;
	}
	
	public double get_distance() {
		return this.distance;
	}

	public void add_time_property(int departure_time, Properties properties){
		if(this.time_property.size()==0)
			this.lowest_cost = properties.get_value();	//for lower bound Graph
			
		time_property.put(departure_time, properties);
	}
	
	public double getLowestCost() {
		return this.lowest_cost;
	}

        public Edge(int src, int dest, double baseWidth, double rushWidth, double dist){
                this.source = src;
                this.destination = dest;
				this.baseWidth = baseWidth;
				this.rushWidth = rushWidth;
				this.distance = dist;
                this.time_property = new TreeMap<Integer, Properties>();
		
        }

	public double get_arrival_time(double departure_time){
		double x1, x2, y1, y2;
		Entry<Integer, Properties> element = get_itr(departure_time);
		if(element == null) {
			return departure_time; // Return input time if no time property found
		}
		x1 = element.getKey();
		y1 = element.getKey() + element.getValue().get_value();

		if(this.time_property.higherEntry(element.getKey())!=null){
			Entry<Integer, Properties> next_element = this.time_property.higherEntry(element.getKey());
			x2 = next_element.getKey();
			y2 = next_element.getKey() + next_element.getValue().get_value();
		}
		else{
			x2 = 24*60;
			y2 = 24*60 + this.time_property.entrySet().iterator().next().getValue().get_value();
		}
		return linear_function(x1, x2, y1, y2, departure_time);
	}

	public double get_departure_time(double arrival_time){
		double x1, x2, y1, y2;
		Entry<Integer, Properties> element = get_itr(arrival_time);
		
		if(arrival_time==element.getKey()){
			x2 = element.getKey();
			y2 = element.getKey() + element.getValue().get_value();

			Entry<Integer, Properties> previous_element = this.time_property.lowerEntry(element.getKey());
			x1 = previous_element.getKey();
			y1 = previous_element.getKey() + previous_element.getValue().get_value();
		}
		else{
			x1 = element.getKey();
			y1 = element.getKey() + element.getValue().get_value();

			if(this.time_property.higherEntry(element.getKey())!=null){
				Entry<Integer, Properties> next_element = this.time_property.higherEntry(element.getKey());
				x2 = next_element.getKey();
				y2 = next_element.getKey() + next_element.getValue().get_value();
			}
			else{
				x2 = 24*60;
				y2 = 24*60 + this.time_property.entrySet().iterator().next().getValue().get_value();
			}
		}
		return linear_function(y1, y2, x1, x2, arrival_time);
	}

	public double get_width(double departure_time){
		if(inRushHour(departure_time)) {
			return this.rushWidth;
		}
		else {
			return this.baseWidth;
		}
	}

	private boolean inRushHour(double time) {
		// Assuming rush hours are from 7:30 to 9:30 and 16:00 to 18:30 (in minutes)
		int timeInMinutes = (int)(time % (24 * 60));
		return (timeInMinutes >= 7 * 60 + 30 && timeInMinutes < 9 * 60 + 30) ||
		       (timeInMinutes >= 16 * 60 && timeInMinutes < 18 * 60 + 30);
	}
	
	// Getter for base width
    public double getBaseWidth() {
        return baseWidth;
    }

    // Getter for rush width
    public double getRushWidth() {
        return rushWidth;
    }

	// Returns true if this is a clearway road (no width change during rush hour)
	public boolean is_clearway() {
		return baseWidth == rushWidth;
	}

	private	double linear_function(double x1, double x2, double y1, double y2, double x){
		return (y2-y1)*(x-x1)/(x2-x1) + y1;
	}

	private	Entry<Integer, Properties> get_itr(double time){
		Entry<Integer, Properties> final_itr = null;
		
		for(Entry<Integer, Properties> element : this.time_property.entrySet()) {
			if(time>=element.getKey())
				final_itr = element;
			else
				break;
		}
		return final_itr;
	}
}
