package ub.cse.algo;

import java.io.Console;
import java.util.*;
import java.util.PriorityQueue;
import java.util.HashMap;

public class Solution {

    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;

    /**
     * Basic Constructor
     *
     * @param info: data parsed from input file
     */
    public Solution(Info info) {
        this.info = info;
        this.graph = info.graph;
        this.clients = info.clients;
        this.bandwidths = info.bandwidths;
    }

    /**
     * Method that returns the calculated
     * SolutionObject as found by your algorithm
     *
     * @return SolutionObject containing the paths, priorities and bandwidths
     */
    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();
        /* TODO: Your solution goes here */
        HashMap<Integer, Integer> shortestPathLengths = Traversals.bfs(this.graph, this.clients);//lengths of shortest paths
        HashMap<Integer, ArrayList<Integer>> shortestPaths = Traversals.bfsPaths(this.graph,this.clients);// creates original shortest paths for all clients
        sol.paths = shortestPaths; //assigns that to the sol object
        HashMap<Integer, Integer> newpriorities = new HashMap<>(clients.size());
        for (Client client : this.clients) {
            newpriorities.put(client.id, 0);
        }
        sol.priorities = newpriorities; //assigns empty priorities to sol object
        sol.bandwidths = this.bandwidths; //assigns bandwiths to the inital bandwidths given
        ArrayList<Client> sortedClients = new ArrayList<>(this.clients);
        sortedClients.sort(Comparator.comparingInt(client -> client.id));//should be from lowest to highest client now
        //now clientDelays has all the delays for each client
       //for(client : this.clients){//algorithm relies on clients being from lowest to highest id, organize if necessary
        for(Client client: sortedClients){
        HashMap<Integer, Integer> clientDelays = Simulator.run(this.graph, this.clients, sol); // executes run from Simulator and stores result in clientDelays

        int cDelay = currentDelays.get(client.id);
        int unsubscribe = (int) (client.alpha * shortestPathLengths.get(client.id));
        int complaining = (int) (client.beta * shortestPathLengths.get(client.id));
        if (cDelay > unsubscribe) {
                improve(sol, client);
            } 
        else if (cDelay > complaining && isAtLegalRisk(currentDelays, shortestPathLengths)) {
                improve(sol, client);
            }
        else{
            //do nothing
         }
        }

        return sol;
    }



    void improve(SolutionObject sol, Client client) {
        // adds bandwidth to overloaded node
        ArrayList<Integer> path = sol.paths.get(client.id);
        int worstNode = -1;
        int delayedNode = 0;
        
        for (int nodeId : path) {
            if (nodeId == graph.contentProvider){
                continue; //we dont care about the content provider node
            }
            int bandwidth = sol.bandwidths.get(nodeId);
            int usage = countNode(sol.paths, nodeId);
            int overload = usage - bandwidth;
            
            if (overload > delayedNode) {
                delayedNode = overload;
                worstNode = nodeId;
            }
        }
        
        // Upgrade the worst node (if it even exists)
        if (worstNode != -1) {
            int cBandwidth = sol.bandwidths.get(worstNode);
            sol.bandwidths.set(worstNode, cBandwidth++);
        }
    }

    boolean isAtLegalRisk(HashMap<Integer, Integer> delays, HashMap<Integer, Integer> shortestLengths) {
        int totalComplaints = 0;
        int fccComplaints = 0;
        for (Client client : clients) {
            int delay = delays.get(client.id);
            int complaintThreshold = (int) (client.beta * shortestLengths.get(client.id));
            if (delay > complaintThreshold) {
                totalComplaints++;
                if (client.isFcc) {
                    fccComplaints++;
                }
            }
        }
        int lawsuit = (int) (info.rho1 * clients.size());
        int fcc = (int) (info.rho2 * countFccClients());
        
        return totalComplaints >= lawsuit || fccComplaints >= fcc;
    }


    int countFccClients(ArrayList<Client> clients) {
        return (int) clients.stream().filter(client -> client.isFcc).count();
    }

int countNode(HashMap<Integer, ArrayList<Integer>> paths, int nodeId) {//checks how many time a certain node was used
    int count = 0;

    for (ArrayList<Integer> path : paths.values()) {
        if (path.contains(nodeId)){
            count++;}//path used tha node
    }  
    return count;
}

    /*//made a data structure that is essentially the graph but with weights for dijkstras
    public class wgraph extends Graph{
        private HashMap<Integer, Integer> nodeWeights;//seperate hashmap with keys of nodes and values of their weights

        public wgraph (Graph graph){
            super(graph.contentProvider, graph);
            this.nodeWeights = new HashMap<>();
            addweightstograph();
        }

        //used to initialize weight of all nodes to 1
        public void addweightstograph(){
            //set weight to 1 for all nodes
            for(Integer node : this.keySet()){
                nodeWeights.put(node, 1);}
        }

        //returns weight of a node(id), using the id of any node router/client will give that weight
        public Integer getWeight(Integer node){
            return nodeWeights.get(node);
        }

        //adds one to a node's weight
        //called after delay has been confirmed to be higher than expected and tested path was valid, change weight then run dijkstras after this
        public void addWeight(Integer node){
            nodeWeights.put(node,nodeWeights.get(node)+1);
        }

        //returns entire node weight list across the graph
        public HashMap<Integer, Integer> getWeights(){
            return new HashMap<>(nodeWeights);
        }
    }

*/

}