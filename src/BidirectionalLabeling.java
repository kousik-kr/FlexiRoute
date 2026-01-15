import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinTask;


public class BidirectionalLabeling implements Runnable{
        private int goal;
        private Label topLabel;
        private double budget;
        BidirectionalDriver.SharedState shared;
        private boolean isForward;
        private boolean master = false;

        public BidirectionalLabeling(int goal, double b, Label label, BidirectionalDriver.SharedState shared, boolean is_forward){
                this.goal = goal;
                this.topLabel = label;
                this.budget = b;
		this.shared = shared;
		this.isForward = is_forward;
	}

	@Override
	public void run(){
	
		List<ForkJoinTask<?>> labelQueue = new ArrayList<ForkJoinTask<?>>();
		int current_vertex = topLabel.get_nodeID();
		Node node = Graph.get_node(current_vertex);
		
		if(isForward) {
			Map<Integer, Edge> temp_outgoing_edge = node.get_outgoing_edges();
			
			for(Entry<Integer, Edge> entry : temp_outgoing_edge.entrySet()) {
				Edge edge = entry.getValue();
				int j = edge.get_destination();
				double distance = edge.get_distance();

                                if(Graph.get_node(j).isFeasible() && !topLabel.getVisited(j)) {
                                        Function current_arrivaltime_function = topLabel.get_arrivalTime();//current function at node i
                                        Function current_width_function = topLabel.get_wide_distance();
					
					List<BreakPoint> arrival_time_breakpoints = new ArrayList<BreakPoint>();//to store the breakpoints at node j
					List<BreakPoint> width_breakpoints = new ArrayList<BreakPoint>();
					double max_width = 0;
					
					Function arrivalTime = null;//function to form label at j
					Function width = null; 
					Function currentArrivalTime = null;//function to store new function after split at j
					Function currentWidth = null; 
					
					while(current_arrivaltime_function!=null) {
						
						boolean previous_status = false;//previous breakpoint's status: true - within budget, false - outside budget
						boolean is_first = true;
						
						int arrival_time_point=0, width_time_point =0;
						for(;arrival_time_point<current_arrivaltime_function.getBreakpoints().size();arrival_time_point++) {
							
							//breakpoints of current function at i
							BreakPoint arrival_time_breakpoint = current_arrivaltime_function.getBreakpoints().get(arrival_time_point);
							BreakPoint width_breakpoint = null;
							if(current_width_function.getBreakpoints().get(width_time_point).getX() ==	arrival_time_breakpoint.getX()) {
								width_breakpoint = current_width_function.getBreakpoints().get(width_time_point);
							}
							
							double current_time = arrival_time_breakpoint.getY();
							double new_arrival_time = edge.get_arrival_time(current_time);
							double new_width;
							
							//to reach j to d
							double min_required_budget = Graph.get_node(j).get_backward_hTime();
							//new breakpoints at node j
							BreakPoint new_arrival_breakpoint = new BreakPoint(arrival_time_breakpoint.getX(), new_arrival_time);
							if((new_arrival_time - arrival_time_breakpoint.getX())<=budget && (new_arrival_time + min_required_budget - arrival_time_breakpoint.getX())<=2*budget)	{
								BreakPoint new_width_breakpoint = null;
								if(width_breakpoint!=null) {
									new_width = width_breakpoint.getY() + edge.get_width(current_time);
									new_width_breakpoint = new BreakPoint(width_breakpoint.getX(), new_width);
								}
								
								
								if(!previous_status){//outside to inside....starting new function
									previous_status = true;
									if(!is_first) {
										double x1 = current_arrivaltime_function.getBreakpoints().get(arrival_time_point-1).getX();
										double y1 = edge.get_arrival_time(current_arrivaltime_function.getBreakpoints().get(arrival_time_point-1).getY());
										double x2 = new_arrival_breakpoint.getX();
										double y2 = new_arrival_breakpoint.getY(); 
										
										//TODO verify all
										double allotted_budget = (min_required_budget>budget) ? 2*budget - min_required_budget : budget; 
										BreakPoint boundary_breakpoint = computeBoundaryBreakpoint(x1, y1, x2, y2, allotted_budget);
										arrival_time_breakpoints.add(boundary_breakpoint);
										
										// Safe bounds check for width function access
										int widthIdx = Math.min(arrival_time_point-1, current_width_function.getBreakpoints().size()-1);
										if (widthIdx >= 0) {
											double tmp_width = current_width_function.getBreakpoints().get(widthIdx).getY() + edge.get_width(current_arrivaltime_function.getBreakpoints().get(arrival_time_point-1).getY());
											BreakPoint boundary_width = new BreakPoint(boundary_breakpoint.getX(), tmp_width);
											width_breakpoints.add(boundary_width);
									
											if(boundary_width.getY()>max_width)
												max_width = boundary_width.getY();
										}
									}
									else {
										is_first = false;
									}
								}
								
								arrival_time_breakpoints.add(new_arrival_breakpoint);
								
								if(new_width_breakpoint!=null) {
									width_breakpoints.add(new_width_breakpoint);
									
									if(new_width_breakpoint.getY()>max_width)
										max_width = new_width_breakpoint.getY();
								}
								
							}
							else if(is_first || !previous_status) {
								if(is_first)
									is_first = false;
								continue;
							}
							else {
								previous_status = false;
								//TODO inside to outside....split function
								double x1 = arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getX();
								double y1 = arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getY();
								double x2 = new_arrival_breakpoint.getX();
								double y2 = new_arrival_breakpoint.getY();
								
								//TODO verify all
								double allotted_budget = (min_required_budget>budget) ? 2*budget - min_required_budget : budget; 
								BreakPoint boundary_breakpoint = computeBoundaryBreakpoint(x1, y1, x2, y2, allotted_budget);
								arrival_time_breakpoints.add(boundary_breakpoint);
								double tmp_width = width_breakpoints.get(width_breakpoints.size()-1).getY() + edge.get_width(boundary_breakpoint.getY());
								BreakPoint boundary_width = new BreakPoint(boundary_breakpoint.getX(), tmp_width);
								width_breakpoints.add(boundary_width);
								
								if(boundary_width.getY()>max_width)
									max_width = boundary_width.getY();
								
								computeAndUpdateBreakpoints(arrival_time_breakpoints, width_breakpoints, j, max_width);
								if(arrivalTime==null) {
									arrivalTime = new Function(arrival_time_breakpoints, -1);
									width = new Function(width_breakpoints, max_width);
									currentArrivalTime = arrivalTime;
									currentWidth = width;
								}
								else {
									Function newArrivalTime = new Function(arrival_time_breakpoints, -1);
									Function newWidth = new Function(width_breakpoints, max_width);
									currentArrivalTime.setNextFunction(newArrivalTime);
									currentWidth.setNextFunction(newWidth);
									arrivalTime.updateValue(max_width);
									
									currentArrivalTime = newArrivalTime;
									currentWidth = newWidth;
								}
								arrival_time_breakpoints.clear();
								width_breakpoints.clear();
								max_width=0;
							}
		
						}
					//if((!IntervalCPO.optimization && arrival_time_breakpoints.size()>1) || (IntervalCPO.optimization && arrival_time_breakpoints.size()==topLabel.get_arrivalTime().getBreakpoints().size())) {
						if(arrival_time_breakpoints.size()>0) {	
							computeAndUpdateBreakpoints(arrival_time_breakpoints, width_breakpoints, j, max_width);
							
							if(arrivalTime==null) {
								arrivalTime = new Function(arrival_time_breakpoints, -1);
								width = new Function(width_breakpoints, max_width);
								currentArrivalTime = arrivalTime;
								currentWidth = width;
							}
							else {
								Function newArrivalTime = new Function(arrival_time_breakpoints, -1);
								Function newWidth = new Function(width_breakpoints, max_width);
								currentArrivalTime.setNextFunction(newArrivalTime);
								currentWidth.setNextFunction(newWidth);
								
								arrivalTime.updateValue(max_width);
								
								currentArrivalTime = newArrivalTime;
								currentWidth = newWidth;
							}
						}
						
						
						current_arrivaltime_function = current_arrivaltime_function.getNextFunction();
						current_width_function = current_width_function.getNextFunction();
					}
					if(arrivalTime!=null) {
						double current_distance = topLabel.getDistance();
						HashMap<Integer, Integer> current_visitedList = topLabel.getVisitedList();
						int current_right_turns= topLabel.getRightTurns();
						if (current_distance!=0 && Graph.isSharpRightTurn(Graph.get_node(current_visitedList.get(current_vertex)), node, Graph.get_node(j))) {
							current_right_turns++;
						}
							
						Label newLabel = new Label(j, arrivalTime, width, current_right_turns, current_distance+distance);
						newLabel.copyLists(current_visitedList);//, topLabel.getPredecessorList());, topLabel.getTrace()
						newLabel.setVisited(j, current_vertex);
						//newLabel.setPredecessor(j, current_vertex);
//						if(shared.forwardVisited.containsKey(j)) {
//							shared.forwardVisited.get(j).add(newLabel);
//						}
//						else {
//							List<Label> label_list = new ArrayList<Label>();
//							label_list.add(newLabel);
							shared.addForwardLabel(j, newLabel);
//						}
						if(shared.isIntersection(j)) {
							shared.addIntersectionNode(j);
						}
						if(j!=goal) {
							BidirectionalLabeling newthread = new BidirectionalLabeling(goal, budget, newLabel, shared, isForward);
							ForkJoinTask<?> task = BidirectionalAstar.pool.submit(newthread);
							labelQueue.add(task);
							//newthread.run();
						}
						//newthread.fork();
					}
				}
			}
		}
		else {
			Map<Integer, Edge> temp_incoming_edge = node.get_incoming_edges();
			
			for(Entry<Integer, Edge> entry : temp_incoming_edge.entrySet()) {
				Edge edge = entry.getValue();
				int j = edge.get_source();
				double distance = edge.get_distance();

                                if(Graph.get_node(j).isFeasible() && !topLabel.getVisited(j)) {
                                        Function current_arrivaltime_function = topLabel.get_arrivalTime();//current function at node i
                                        Function current_width_function = topLabel.get_wide_distance();
					
					List<BreakPoint> arrival_time_breakpoints = new ArrayList<BreakPoint>();//to store the breakpoints at node j
					List<BreakPoint> width_breakpoints = new ArrayList<BreakPoint>();
					double max_width =0;
					
					Function arrivalTime = null;//function to form label at j
					Function width = null; 
					Function currentArrivalTime = null;//function to store new function after split at j
					Function currentWidth = null; 
					
					while(current_arrivaltime_function!=null) {
						
						boolean previous_status = false;//previous breakpoint's status: true - within budget, false - outside budget
						boolean is_first = true;
						
						int arrival_time_point=0, width_time_point =0;
						for(;arrival_time_point<current_arrivaltime_function.getBreakpoints().size();arrival_time_point++) {
							
							//breakpoints of current function at i
							BreakPoint arrival_time_breakpoint = current_arrivaltime_function.getBreakpoints().get(arrival_time_point);
							BreakPoint width_breakpoint = null;
							
							if(current_width_function.getBreakpoints().get(width_time_point).getX() ==	arrival_time_breakpoint.getX()) {
								width_breakpoint = current_width_function.getBreakpoints().get(width_time_point);
							}
							
							double current_time = arrival_time_breakpoint.getX();
							double new_departure_time = edge.get_departure_time(current_time);
							double new_width;
							
							//to reach j to d
							double min_required_budget = Graph.get_node(j).get_forward_hTime();
							//new breakpoints at node j
							BreakPoint new_arrival_breakpoint = new BreakPoint(new_departure_time, arrival_time_breakpoint.getY());
							if((arrival_time_breakpoint.getY() - new_departure_time)<=budget && (arrival_time_breakpoint.getY() + min_required_budget - new_departure_time)<=2*budget)	{
								
								BreakPoint new_width_breakpoint = null;
								if(width_breakpoint!=null) {
									new_width = width_breakpoint.getY() + edge.get_width(new_departure_time);
									new_width_breakpoint = new BreakPoint(new_departure_time, new_width);
								}
								
								
								if(!previous_status){//outside to inside....starting new function
									previous_status = true;
									if(!is_first) {
										double x1 = edge.get_departure_time(current_arrivaltime_function.getBreakpoints().get(arrival_time_point-1).getX());
										double y1 = current_arrivaltime_function.getBreakpoints().get(arrival_time_point-1).getY();
										double x2 = new_arrival_breakpoint.getX();
										double y2 = new_arrival_breakpoint.getY(); 
										
										//TODO verify all
										double allotted_budget =  (min_required_budget>budget) ? 2*budget - min_required_budget : budget; 
										BreakPoint boundary_breakpoint = computeBoundaryBreakpoint(x1, y1, x2, y2, allotted_budget);
										arrival_time_breakpoints.add(boundary_breakpoint);
										
										// Safe bounds check for width function access
										int widthIdx = Math.min(arrival_time_point-1, current_width_function.getBreakpoints().size()-1);
										if (widthIdx >= 0) {
											double tmp_width = current_width_function.getBreakpoints().get(widthIdx).getY() + edge.get_width(boundary_breakpoint.getX());
											BreakPoint boundary_width = new BreakPoint(boundary_breakpoint.getX(), tmp_width);
											width_breakpoints.add(boundary_width);
									
											if(boundary_width.getY()>max_width)
												max_width = boundary_width.getY();
										}
									}
									else {
										is_first = false;
									}
								}
								
								arrival_time_breakpoints.add(new_arrival_breakpoint);
								
								if(new_width_breakpoint!=null) {
									width_breakpoints.add(new_width_breakpoint);
									
									if(new_width_breakpoint.getY()>max_width)
										max_width = new_width_breakpoint.getY();
								}
								
							}
							else if(is_first || !previous_status) {
								if(is_first)
									is_first = false;
								continue;
							}
							else {
								previous_status = false;
								//TODO inside to outside....split function
								double x1 = arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getX();
								double y1 = arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getY();
								double x2 = new_arrival_breakpoint.getX();
								double y2 = new_arrival_breakpoint.getY();
								
								//TODO verify all
								double allotted_budget = (min_required_budget>budget) ? 2*budget - min_required_budget : budget; 
								BreakPoint boundary_breakpoint = computeBoundaryBreakpoint(x1, y1, x2, y2, allotted_budget);
								arrival_time_breakpoints.add(boundary_breakpoint);
								double tmp_width = width_breakpoints.get(width_breakpoints.size()-1).getY() + edge.get_width(boundary_breakpoint.getX());
								BreakPoint boundary_width = new BreakPoint(boundary_breakpoint.getX(), tmp_width);
								width_breakpoints.add(boundary_width);
								
								if(boundary_width.getY()>max_width)
									max_width = boundary_width.getY();
								
								
								computeAndUpdateBreakpoints(arrival_time_breakpoints, width_breakpoints, j, max_width);
								if(arrivalTime==null) {
									arrivalTime = new Function(arrival_time_breakpoints, -1);
									width = new Function(width_breakpoints, max_width);
									currentArrivalTime = arrivalTime;
									currentWidth = width;
								}
								else {
									Function newArrivalTime = new Function(arrival_time_breakpoints, -1);
									Function newWidth = new Function(width_breakpoints, max_width);
									currentArrivalTime.setNextFunction(newArrivalTime);
									currentWidth.setNextFunction(newWidth);
									arrivalTime.updateValue(max_width);
									currentArrivalTime = newArrivalTime;
									currentWidth = newWidth;
								}
								arrival_time_breakpoints.clear();
								width_breakpoints.clear();
								max_width=0;
							}
		
						}
					//if((!IntervalCPO.optimization && arrival_time_breakpoints.size()>1) || (IntervalCPO.optimization && arrival_time_breakpoints.size()==topLabel.get_arrivalTime().getBreakpoints().size())) {
						if(arrival_time_breakpoints.size()>0) {	
							computeAndUpdateBreakpoints(arrival_time_breakpoints, width_breakpoints, j, max_width);
							
							if(arrivalTime==null) {
								arrivalTime = new Function(arrival_time_breakpoints, -1);
								width = new Function(width_breakpoints, max_width);
								currentArrivalTime = arrivalTime;
								currentWidth = width;
							}
							else {
								Function newArrivalTime = new Function(arrival_time_breakpoints, -1);
								Function newWidth = new Function(width_breakpoints, max_width);
								currentArrivalTime.setNextFunction(newArrivalTime);
								currentWidth.setNextFunction(newWidth);
								arrivalTime.updateValue(max_width);
								arrivalTime.updateValue(max_width);
								
								currentArrivalTime = newArrivalTime;
								currentWidth = newWidth;
							}
						}
						
						
						current_arrivaltime_function = current_arrivaltime_function.getNextFunction();
						current_width_function = current_width_function.getNextFunction();
					}
					if(arrivalTime!=null) {
						double current_distance = topLabel.getDistance();
						HashMap<Integer, Integer> current_visitedList = topLabel.getVisitedList();
						int current_right_turns= topLabel.getRightTurns();
						if (current_distance!=0 && Graph.isSharpRightTurn(Graph.get_node(j), node, Graph.get_node(current_visitedList.get(current_vertex)))) {
							current_right_turns++;
						}
						
						Label newLabel = new Label(j, arrivalTime, width, current_right_turns, current_distance+distance);
						newLabel.copyLists(topLabel.getVisitedList());//, topLabel.getPredecessorList());, topLabel.getTrace()
						newLabel.setVisited(j, current_vertex);
						//newLabel.setPredecessor(j, current_vertex);
//						if(shared.backwardVisited.containsKey(j)) {
//							shared.backwardVisited.get(j).add(newLabel);
//						}
//						else {
//							List<Label> label_list = new ArrayList<Label>();
//							label_list.add(newLabel);
							shared.addBackwardLabel(j, newLabel);
						//}
						if(shared.isIntersection(j)) {
							shared.addIntersectionNode(j);
						}
						if(j!= goal) {
							BidirectionalLabeling newthread = new BidirectionalLabeling(goal, budget, newLabel, shared, isForward);
							ForkJoinTask<?> task = BidirectionalAstar.pool.submit(newthread);
							labelQueue.add(task);
							//newthread.run();
						}
						//newthread.fork();
					}
				}
			}
		}
		
                if(master) {
                        if(!BidirectionalAstar.isMemoryUpdated())
                                BidirectionalAstar.updateMemory();
                }
                if(labelQueue.size()>0) {
			for(ForkJoinTask<?> task : labelQueue) {
				//while(!x.isDone()) continue;
				task.join();//x.get();
				
			}	
//			if(master) {
//				if(!BidirectionalAstar.isMemoryUpdated()) 
//					BidirectionalAstar.updateMemory();
//			}
			//nodeWiselabels.clear();
                        labelQueue.clear();
                }
        }

        public void setMaster() {
                this.master = true;
        }
	
	private BreakPoint computeBoundaryBreakpoint(double x1, double y1, double x2, double y2, double allotted_budget) {
		double x = (allotted_budget+x1*((y2-y1)/(x2-x1))-y1)/(-1+(y2-y1)/(x2-x1));
		double y = -x1*(y2-y1)/(x2-x1) + y1 + ((y2-y1)/(x2-x1))*x;
		return new BreakPoint(x, y);
	}

//
//	public Label get_result() {
//		return result;
//	}

	private void computeAndUpdateBreakpoints(List<BreakPoint> arrival_time_breakpoints, List<BreakPoint> width_breakpoints, int next_vertex, double max_width) {
		if(isForward) {
			List<Double> arrival_time_series = Graph.getArrivalTimeSeries(arrival_time_breakpoints.get(0).getY(), 
					arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getY());
			List<Double> width_time_series = Graph.getWidthTimeSeries(arrival_time_breakpoints.get(0).getY(), 
					arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getY());
			
			List<BreakPoint> tmp_arrival_time_breakpoints = new ArrayList<BreakPoint>();
			List<BreakPoint> tmp_width_breakpoints = new ArrayList<BreakPoint>();
			
			int i = 0, j = 0, k=0, l=0;	//i & j for arrival time breakpoints and series, k & l for width breakpoints and series
	
	        // Merge the lists while both have elements
	        while (i < arrival_time_breakpoints.size() && j < arrival_time_series.size()) {
	            if (arrival_time_breakpoints.get(i).getY() <= arrival_time_series.get(j)) {
	            	
	            	if(tmp_arrival_time_breakpoints.size()>0 && arrival_time_breakpoints.get(i).getX()-tmp_arrival_time_breakpoints.get(tmp_arrival_time_breakpoints.size()-1).getX()<BidirectionalAstar.THRESHOLD) {
		            	if(k < width_breakpoints.size() && arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {	
	            		
		            		if(width_breakpoints.get(k).getY()>tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).getY()) {
		            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width_breakpoints.get(k).getY());
		            			
		            		}
		            		k++;
		            	}
		            	}else {
			            	tmp_arrival_time_breakpoints.add(arrival_time_breakpoints.get(i));
	            			if(k < width_breakpoints.size() && arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {
	            				tmp_width_breakpoints.add(width_breakpoints.get(k));
	            				k++;
	            			}
		            	}
	
	                i++;
	            } 
	            else {
	            	int current_vertex = topLabel.get_nodeID();
	            	int tmp_next_vertex = next_vertex;
	            	boolean is_width = false;
	            	if(l < width_time_series.size() && arrival_time_series.get(j)==width_time_series.get(l)) {
	            		is_width = true;
	            	}
	            	Node nextNode = Graph.get_node(tmp_next_vertex);
	            	Node curNode = Graph.get_node(current_vertex);
	            	Edge edge = (nextNode != null && nextNode.get_outgoing_edges() != null) ? nextNode.get_outgoing_edges().get(current_vertex) : null;
	            	if(edge == null) {
	            		j++;
	            		continue; // missing edge; skip this breakpoint
	            	}
	            	double dep_time = edge.get_departure_time(arrival_time_series.get(j));
	            	int width = 0;
	            	Map<Integer, Integer> predList = topLabel.getVisitedList();
	            	boolean pathValid = true;
	            	
	            	while(predList.get(current_vertex)!=-1) {
	            		tmp_next_vertex = current_vertex;
	            		current_vertex = predList.get(tmp_next_vertex);
	            		Node tmpNode = Graph.get_node(tmp_next_vertex);
	            		Edge incoming = (tmpNode != null && tmpNode.get_incoming_edges() != null) ? tmpNode.get_incoming_edges().get(current_vertex) : null;
	            		if(incoming == null) {
	            			pathValid = false;
	            			break;
	            		}
	            		dep_time = incoming.get_departure_time(dep_time);
	            		if(is_width)
	            			width += incoming.get_width(dep_time);
	            	}
	            	if(!pathValid) {
	            		j++;
	            		continue;
	            	}
	            	
	            	if(dep_time - arrival_time_breakpoints.get(i).getX()<BidirectionalAstar.THRESHOLD) {
	            		if(is_width && k < width_breakpoints.size() && tmp_width_breakpoints.size() > 0) {
	            			if(width>width_breakpoints.get(k).getY())
	            				tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width);
	            			l++;
	            		}
	            	}
	            	else {
	            		BreakPoint new_arrival_time_breakpoint = new BreakPoint(dep_time, arrival_time_series.get(j));
	            		tmp_arrival_time_breakpoints.add(new_arrival_time_breakpoint);
	            		
	            		if(is_width) {
	            			BreakPoint new_width_breakpoint = new BreakPoint(dep_time, width);
		            		tmp_width_breakpoints.add(new_width_breakpoint);
		            		l++;
	            		}
	            	}
	                j++;
	            }
	        }
	
	        // Add remaining elements from list1
	        while (i < arrival_time_breakpoints.size()) {
		        	tmp_arrival_time_breakpoints.add(arrival_time_breakpoints.get(i));
	            i++;
	        }
	        
	        // Add remaining elements from list2
	        while ( k< width_breakpoints.size()) {
		        	tmp_width_breakpoints.add(width_breakpoints.get(k));
	            k++;
	        }
	        
	        arrival_time_breakpoints.clear();
	        arrival_time_breakpoints.addAll(tmp_arrival_time_breakpoints);
	        width_breakpoints.clear();
	        width_breakpoints.addAll(tmp_width_breakpoints);
		}
		else {
			List<Double> arrival_time_series = Graph.getArrivalTimeSeries(arrival_time_breakpoints.get(0).getX(), 
					arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getX());
			List<Double> width_time_series = Graph.getWidthTimeSeries(arrival_time_breakpoints.get(0).getX(), 
					arrival_time_breakpoints.get(arrival_time_breakpoints.size()-1).getX());
			
			List<BreakPoint> tmp_arrival_time_breakpoints = new ArrayList<BreakPoint>();
			List<BreakPoint> tmp_width_breakpoints = new ArrayList<BreakPoint>();
			
			int i = 0, j = 0, k=0, l=0;

	        // Merge the lists while both have elements
	        while (i < arrival_time_breakpoints.size() && j < arrival_time_series.size()) {
	            if (arrival_time_breakpoints.get(i).getX() <= arrival_time_series.get(j)) {
	            	if(tmp_arrival_time_breakpoints.size()>0 && arrival_time_breakpoints.get(i).getY()-
	            			tmp_arrival_time_breakpoints.get(tmp_arrival_time_breakpoints.size()-1).getY()<BidirectionalAstar.THRESHOLD) {
	            		
	            		if(k < width_breakpoints.size() && arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {	
	            			
		            		if(width_breakpoints.get(k).getY()>tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).getY())
		            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width_breakpoints.get(k).getY());
	            		k++;
	            	}
	            	}else {
		            	tmp_arrival_time_breakpoints.add(arrival_time_breakpoints.get(i));
		            	if(k < width_breakpoints.size() && arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {
		            		tmp_width_breakpoints.add(width_breakpoints.get(k));
		            		k++;
		            	}
	            	}

                i++;
            } 
	            else {
	            	int current_vertex = topLabel.get_nodeID();
		            	int tmp_next_vertex = next_vertex;
		            	boolean is_width = false;
	            	if(l < width_time_series.size() && arrival_time_series.get(j)==width_time_series.get(l)) {
	            		is_width = true;
	            	}		            	Node nextNode = Graph.get_node(tmp_next_vertex);
		            	Edge edge = (nextNode != null && nextNode.get_outgoing_edges() != null) ? nextNode.get_outgoing_edges().get(current_vertex) : null;
		            	if(edge == null) {
		            		j++;
		            		continue;
		            	}
		            	double arr_time = edge.get_arrival_time(arrival_time_series.get(j));
		            	int width = 0;
		            	Map<Integer, Integer> successorList = topLabel.getVisitedList();
		            	boolean pathValid = true;
		            	
		            	while(successorList.get(current_vertex)!=-1) {
		            		tmp_next_vertex = current_vertex;
		            		current_vertex = successorList.get(tmp_next_vertex);
		            		Node tmpNode = Graph.get_node(tmp_next_vertex);
		            		Edge outgoing = (tmpNode != null && tmpNode.get_outgoing_edges() != null) ? tmpNode.get_outgoing_edges().get(current_vertex) : null;
		            		if(outgoing == null) {
		            			pathValid = false;
		            			break;
		            		}
		            		if(is_width)
		            			width += outgoing.get_width(arr_time);
		            		arr_time = outgoing.get_arrival_time(arr_time);
		            	}
		            	if(!pathValid) {
		            		j++;
		            		continue;
		            	}
		            	
	            	if(arr_time - arrival_time_breakpoints.get(i).getY()<BidirectionalAstar.THRESHOLD) {
	            		if(is_width) {
	            			
		            		if(k < width_breakpoints.size() && width>width_breakpoints.get(k).getY())
		            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width);
		            		l++;
	            		}
	            	}
	            	else {
	            		BreakPoint new_arrival_time_breakpoint = new BreakPoint(arrival_time_series.get(j), arr_time);
	            		tmp_arrival_time_breakpoints.add(new_arrival_time_breakpoint);
	            		
	            		if(is_width) {
		            		BreakPoint new_width_breakpoint = new BreakPoint(width_time_series.get(l), width);
		            		
		            		tmp_width_breakpoints.add(new_width_breakpoint);
		            		l++;
	            		}
	            	}
	                j++;
	            }
	        }

	        // Add remaining elements from list1
	        while (i < arrival_time_breakpoints.size()) {
		        	tmp_arrival_time_breakpoints.add(arrival_time_breakpoints.get(i));
	            i++;
	        }
	        
	        // Add remaining elements from list2
	        while (k < width_breakpoints.size()) {
		        	tmp_width_breakpoints.add(width_breakpoints.get(k));
	            k++;
	        }
	        
	        arrival_time_breakpoints.clear();
	        arrival_time_breakpoints.addAll(tmp_arrival_time_breakpoints);
	        width_breakpoints.clear();
	        width_breakpoints.addAll(tmp_width_breakpoints);
		}
	}
	
//	private boolean checkDomination(Label newLabel, List<Label> labels) {
//		for(Label label:labels) {
//			if((newLabel.get_arrivalTime() > label.get_arrivalTime()) && 
//					(newLabel.get_score() < label.get_score()))
//				return true;
//			
//		}
//		return false;
//	}
}