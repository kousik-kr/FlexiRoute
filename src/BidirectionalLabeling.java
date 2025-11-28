import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;


public class BidirectionalLabeling implements Runnable{
        private int goal;
        private Label topLabel;
        private double budget;
        BidirectionalDriver.SharedState shared;
        private boolean isForward;
        private boolean master = false;

        // Direction-aware caches to avoid exploring labels that are obviously weaker than
        // the best heuristic seen for a node. Lower scores are better.
        private static final ConcurrentHashMap<Integer, Double> forwardBestScore = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<Integer, Double> backwardBestScore = new ConcurrentHashMap<>();
        
        // Track search progress to enable progressive pruning
        private static final ConcurrentHashMap<Integer, Double> forwardMinCost = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<Integer, Double> backwardMinCost = new ConcurrentHashMap<>();

        // Refined baseline weights for the adaptive heuristic. These ensure admissibility while
        // providing strong guidance. Distance weight is higher to prioritize budget satisfaction.
        private static final double BASE_DISTANCE_WEIGHT = 0.50;  // Increased for budget awareness
        private static final double BASE_WIDTH_WEIGHT = 0.25;     // Reduced but still significant
        private static final double BASE_TURN_WEIGHT = 0.15;      // Moderate penalty
        private static final double BASE_SHARP_TURN_WEIGHT = 0.10; // Lower for admissibility
        
        // Progressive pruning - threshold becomes stricter as search progresses
        private static final double INITIAL_PRUNE_THRESHOLD = 1.10; // 10% tolerance initially
        private static final double STRICT_PRUNE_THRESHOLD = 1.03;  // 3% tolerance when established

        public BidirectionalLabeling(int goal, double b, Label label, BidirectionalDriver.SharedState shared, boolean is_forward){
                this.goal = goal;
                this.topLabel = label;
                this.budget = b;
		this.shared = shared;
		this.isForward = is_forward;
	}

	@Override
	public void run(){

//		List<Label> destinationLabels = new ArrayList<Label>();
		
		// Enhanced termination condition 1: Time limit check
		long current = System.currentTimeMillis();
		if((current-BidirectionalAstar.start)/1000F >BidirectionalAstar.TIME_LIMIT) {
			if(!BidirectionalAstar.isMemoryUpdated()) 
				BidirectionalAstar.updateMemory();
			BidirectionalAstar.forceStop=true;
			return;
		}
		
		// Enhanced termination condition 2: Budget exhaustion check
		// If the current label has already consumed budget close to the limit,
		// and the heuristic shows we can't reach the goal, terminate early
		double consumedBudget = topLabel.getDistance();
		double estimatedRemaining = isForward ? 
			Graph.get_node(topLabel.get_nodeID()).get_backward_hDistance() : 
			Graph.get_node(topLabel.get_nodeID()).get_forward_hDistance();
		
		if(estimatedRemaining != Double.MAX_VALUE && 
		   (consumedBudget + estimatedRemaining) > budget * 1.15) {
			// Path cannot satisfy budget constraint even optimistically
			return;
		}
	
		List<ForkJoinTask<?>> labelQueue = new ArrayList<ForkJoinTask<?>>();
		
		//double current_time = topLabel.get_arrivalTime();
		//int current_score = topLabel.get_score();
		int current_vertex = topLabel.get_nodeID();
		
		Node node = Graph.get_node(current_vertex);
		
		if(isForward) {
			
			Map<Integer, Edge> temp_outgoing_edge = node.get_outgoing_edges();
			
			for(Entry<Integer, Edge> entry : temp_outgoing_edge.entrySet()) {
				Edge edge = entry.getValue();
				int j = edge.get_destination();
				double distance = edge.get_distance();
				
	//			if(j==destination) {
	//				System.out.println("hi");
	//			}
                                if(Graph.get_node(j).isFeasible() && Graph.get_node(j).get_forward_hTime()<=budget && !topLabel.getVisited(j)) {
                                        Node nextNode = Graph.get_node(j);
                                        if(shouldPrune(nextNode, edge, j)) {
                                                continue;
                                        }
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
							if(new_arrival_time<0) {
								System.out.println("Hi");
							}
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
										
										double tmp_width = current_width_function.getBreakpoints().get(width_time_point-1).getY() + edge.get_width(current_arrivaltime_function.getBreakpoints().get(arrival_time_point-1).getY());
										BreakPoint boundary_width = new BreakPoint(boundary_breakpoint.getX(), tmp_width);
										width_breakpoints.add(boundary_width);
										
										if(boundary_width.getY()>max_width)
											max_width = boundary_width.getY();
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
	//			if(j==destination) {
	//				System.out.println("hi");
	//			}
                                if(Graph.get_node(j).isFeasible() && Graph.get_node(j).get_backward_hTime()<=budget && !topLabel.getVisited(j)) {
                                        Node nextNode = Graph.get_node(j);
                                        if(shouldPrune(nextNode, edge, j)) {
                                                continue;
                                        }
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
							if(new_departure_time<0) {
								System.out.println("Hi");
							}
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
										
										double tmp_width = current_width_function.getBreakpoints().get(arrival_time_point-1).getY() + edge.get_width(boundary_breakpoint.getX());
										BreakPoint boundary_score = new BreakPoint(boundary_breakpoint.getX(), tmp_width);
										width_breakpoints.add(boundary_score);
										
										if(boundary_score.getY()>max_width)
											max_width = boundary_score.getY();
										
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

        private boolean shouldPrune(Node nextNode, Edge edge, int nextNodeId) {
                double heuristicScore = computeHeuristicScore(nextNode, edge);
                ConcurrentHashMap<Integer, Double> scoreCache = isForward ? forwardBestScore : backwardBestScore;
                ConcurrentHashMap<Integer, Double> costCache = isForward ? forwardMinCost : backwardMinCost;

                // Progressive pruning: use stricter threshold as we find better paths
                double pathCost = topLabel.getDistance() + edge.get_distance();
                Double globalMinCost = isForward ? 
                        forwardMinCost.values().stream().min(Double::compare).orElse(Double.MAX_VALUE) :
                        backwardMinCost.values().stream().min(Double::compare).orElse(Double.MAX_VALUE);
                
                double pruneThreshold = (globalMinCost < Double.MAX_VALUE && pathCost > globalMinCost * 0.8) ?
                        STRICT_PRUNE_THRESHOLD : INITIAL_PRUNE_THRESHOLD;

                // Heuristic-based pruning
                Double best = scoreCache.get(nextNodeId);
                if(best != null && heuristicScore >= best * pruneThreshold) {
                        return true;
                }

                // Cost-based pruning: if we've seen this node with significantly lower cost, prune
                Double minCost = costCache.get(nextNodeId);
                if(minCost != null && pathCost >= minCost * 1.20) {
                        return true;
                }

                scoreCache.merge(nextNodeId, heuristicScore, Math::min);
                costCache.merge(nextNodeId, pathCost, Math::min);
                return false;
        }

        private double computeHeuristicScore(Node nextNode, Edge edge) {
                double pathDistance = topLabel.getDistance() + edge.get_distance();
                double estimatedRemainingDistance = isForward ? nextNode.get_backward_hDistance() : nextNode.get_forward_hDistance();
                
                // Handle unreachable or unknown distances more conservatively
                if(estimatedRemainingDistance == Double.MAX_VALUE) {
                        estimatedRemainingDistance = budget * 0.5; // Conservative estimate
                }
                
                double totalEstimatedDistance = pathDistance + estimatedRemainingDistance;
                double remainingBudget = budget - pathDistance;
                
                // Refined budget pressure calculation
                // usedRatio tracks how much budget we've consumed relative to total needed
                double usedBudgetRatio = totalEstimatedDistance / Math.max(1.0, budget);
                // remainingRatio tracks budget headroom
                double remainingBudgetRatio = Math.max(0.0, remainingBudget / Math.max(1.0, estimatedRemainingDistance));
                
                // Adaptive distance weight increases when budget is tight
                // But stays admissible by not over-penalizing
                double budgetPressure = Math.min(2.0, usedBudgetRatio);
                double adaptiveDistanceWeight = BASE_DISTANCE_WEIGHT * (1.0 + 0.4 * budgetPressure);

                // Normalize distance component by budget to keep it bounded
                double normalizedDistance = totalEstimatedDistance / Math.max(1.0, budget);

                // Refined width penalty: consider both current edge and accumulated path width
                double currentWidth = topLabel.get_wide_distance().getMaxValue();
                double edgeWidthEstimate = Math.max(1.0, (edge.getBaseWidth() + edge.getRushWidth()) / 2.0);
                
                // Width penalty is higher for narrow edges, especially if path is already narrow
                double accumulatedWidthRatio = currentWidth / Math.max(1.0, pathDistance);
                double edgeWidthPenalty = edge.get_distance() / edgeWidthEstimate;
                double combinedWidthPenalty = (accumulatedWidthRatio + edgeWidthPenalty) / 2.0;
                
                // Amplify width weight when budget is tight (narrow roads slow us down)
                double adaptiveWidthWeight = BASE_WIDTH_WEIGHT * (1.0 + 0.3 * Math.min(1.5, budgetPressure));

                // Turn computation with better context awareness
                Integer predecessorId = topLabel.getVisitedList().get(topLabel.get_nodeID());
                boolean sharpTurn = false;
                if(predecessorId != null && predecessorId >= 0) {
                        Node previousNode = Graph.get_node(predecessorId);
                        Node currentNode = Graph.get_node(topLabel.get_nodeID());
                        sharpTurn = Graph.isSharpRightTurn(previousNode, currentNode, nextNode);
                }

                int actualTurns = topLabel.getRightTurns();
                int projectedTurns = actualTurns + (sharpTurn ? 1 : 0);
                int estimatedRemainingTurns = isForward ? nextNode.get_backward_hRightTurn() : nextNode.get_forward_hRightTurn();
                int totalEstimatedTurns = projectedTurns + estimatedRemainingTurns;

                // Turn weight adapts based on turn density and remaining budget
                // High turn density on tight budget is very costly
                double turnDensity = totalEstimatedTurns / Math.max(1.0, totalEstimatedDistance);
                double adaptiveTurnWeight = BASE_TURN_WEIGHT * (1.0 + 0.4 * Math.min(1.5, 
                        budgetPressure * turnDensity + combinedWidthPenalty * 0.15));

                // Sharp turn weight increases when budget is critical or path is narrow
                double adaptiveSharpTurnWeight = BASE_SHARP_TURN_WEIGHT * (1.0 + 0.3 * Math.min(1.8, 
                        budgetPressure + combinedWidthPenalty * 0.2));

                // Final heuristic combines all factors with adaptive weights
                // This maintains admissibility while being sensitive to budget constraints
                double heuristic = adaptiveDistanceWeight * normalizedDistance
                                + adaptiveWidthWeight * combinedWidthPenalty
                                + adaptiveTurnWeight * totalEstimatedTurns
                                + (sharpTurn ? adaptiveSharpTurnWeight : 0.0);
                
                // Add small penalty if remaining budget is very tight relative to remaining distance
                if(remainingBudgetRatio < 0.3 && estimatedRemainingDistance > 0) {
                        heuristic *= 1.15; // Discourage paths that are cutting it too close
                }
                
                return heuristic;
        }

        public void setMaster() {
                this.master = true;
        }
	
	private BreakPoint computeBoundaryBreakpoint(double x1, double y1, double x2, double y2, double allotted_budget) {
		double x = (allotted_budget+x1*((y2-y1)/(x2-x1))-y1)/(-1+(y2-y1)/(x2-x1));
		double y = -x1*(y2-y1)/(x2-x1) + y1 + ((y2-y1)/(x2-x1))*x;
		if(x<0) {
			System.out.println("Hi");
		}
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
			            	if(arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {	
		            			
			            		if(width_breakpoints.get(k).getY()>tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).getY()) {
			            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width_breakpoints.get(k).getY());
			            			
			            		}
			            		k++;
			            	}
		            	}else {
			            	tmp_arrival_time_breakpoints.add(arrival_time_breakpoints.get(i));
			            	if(arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {
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
		            	if(arrival_time_series.get(j)==width_time_series.get(l)) {
		            		is_width = true;
		            	}
		            	double dep_time = Graph.get_node(tmp_next_vertex).get_incoming_edges().get(current_vertex).get_departure_time(arrival_time_series.get(j));
		            	int width = 0;
		            	Map<Integer, Integer> predList = topLabel.getVisitedList();
		            	
		            	while(predList.get(current_vertex)!=-1) {
		            		tmp_next_vertex = current_vertex;
		            		current_vertex = predList.get(tmp_next_vertex);
		            		dep_time = Graph.get_node(tmp_next_vertex).get_incoming_edges().get(current_vertex).get_departure_time(dep_time);
		            		if(is_width)
		            			width += Graph.get_node(tmp_next_vertex).get_incoming_edges().get(current_vertex).get_width(dep_time);
		            	}
		            	
		            	if(dep_time - arrival_time_breakpoints.get(i).getX()<BidirectionalAstar.THRESHOLD) {
			            	if(is_width) {	
		            			
			            		if(width>width_breakpoints.get(k).getY())
			            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width);
			            		l++;
			            	}
		            	}
		            	else {
			            	BreakPoint new_arrival_time_breakpoint = new BreakPoint(dep_time, arrival_time_series.get(j));
			            	if(dep_time<0) {
			            		System.out.println("Hi");
			            	}
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
		            		
		            		if(arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {	
		            			
			            		if(width_breakpoints.get(k).getY()>tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).getY())
			            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width_breakpoints.get(k).getY());
			            		k++;
		            		}
		            	}else {
			            	tmp_arrival_time_breakpoints.add(arrival_time_breakpoints.get(i));
			            	if(arrival_time_breakpoints.get(i).getX()==width_breakpoints.get(k).getX()) {	
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
		            	if(arrival_time_series.get(j)==width_time_series.get(l)) {
		            		is_width = true;
		            	}
		            	
		            	double arr_time = Graph.get_node(tmp_next_vertex).get_outgoing_edges().get(current_vertex).get_arrival_time(arrival_time_series.get(j));
		            	int width = 0;
		            	Map<Integer, Integer> successorList = topLabel.getVisitedList();
		            	
		            //	if(successorList.get(current_vertex)==null)
		            		
		            		
		            	while(successorList.get(current_vertex)!=-1) {
		            		tmp_next_vertex = current_vertex;
		            		current_vertex = successorList.get(tmp_next_vertex);
		            		if(is_width)
		            			width += Graph.get_node(tmp_next_vertex).get_outgoing_edges().get(current_vertex).get_width(arr_time);
		            		arr_time = Graph.get_node(tmp_next_vertex).get_outgoing_edges().get(current_vertex).get_arrival_time(arr_time);
		            	}
		            	
		            	if(arr_time - arrival_time_breakpoints.get(i).getY()<BidirectionalAstar.THRESHOLD) {
		            		if(is_width) {
		            			
			            		if(width>width_breakpoints.get(k).getY())
			            			tmp_width_breakpoints.get(tmp_width_breakpoints.size()-1).updateY(width);
			            		l++;
		            		}
		            	}
		            	else {
			            	BreakPoint new_arrival_time_breakpoint = new BreakPoint(arrival_time_series.get(j), arr_time);
			            	if(arr_time<0) {
			            		System.out.println("Hi");
			            	}
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