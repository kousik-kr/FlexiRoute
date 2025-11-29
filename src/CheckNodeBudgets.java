public class CheckNodeBudgets {
    public static void main(String[] args) throws Exception {
        String basePath = "C:\\Users\\kousi\\eclipse-workspace\\Wide-Path\\";
        Graph.read_graph(basePath);
        
        int source = 72594;
        int dest = 72541;
        double budget = 100.0;
        int interval = 360;
        
        BidirectionalAstar.runtime = 450;
        BidirectionalAstar.interval = interval;
        Graph.forwardAstar(source, budget);
        Graph.backwardAstar(dest, budget);
        
        Node srcNode = Graph.get_node(source);
        Node dstNode = Graph.get_node(dest);
        
        System.out.println("Source node " + source + ":");
        System.out.println("  isFeasible: " + srcNode.isFeasible());
        System.out.println("  forward_hTime: " + srcNode.get_forward_hTime());
        System.out.println("  backward_hTime: " + srcNode.get_backward_hTime());
        
        System.out.println("\nDestination node " + dest + ":");
        System.out.println("  isFeasible: " + dstNode.isFeasible());
        System.out.println("  forward_hTime: " + dstNode.get_forward_hTime());
        System.out.println("  backward_hTime: " + dstNode.get_backward_hTime());
        
        var neighbors = srcNode.get_outgoing_edges();
        System.out.println("\nSource has " + neighbors.size() + " neighbors");
        if(!neighbors.isEmpty()) {
            int firstNeighbor = neighbors.entrySet().iterator().next().getKey();
            Node nbr = Graph.get_node(firstNeighbor);
            System.out.println("First neighbor " + firstNeighbor + ":");
            System.out.println("  isFeasible: " + nbr.isFeasible());
            System.out.println("  forward_hTime: " + nbr.get_forward_hTime());
            System.out.println("  backward_hTime: " + nbr.get_backward_hTime());
            System.out.println("  Passes forward_hTime <= budget/2? " + (nbr.get_forward_hTime() <= budget/2));
        }
    }
}
