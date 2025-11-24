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
        while(true){
        HashMap<Integer, Integer> clientDelays = Simulator.run(this.graph, this.clients, sol); // executes run from Simulator and stores result in clientDelays
       //now clientDelays has all the delays for each client
       //for(client : this.clients){//algorithm relies on clients being from lowest to highest id, organize if necessary
       ArrayList<Client> sortedClients = new ArrayList<>(this.clients);
        sortedClients.sort(Comparator.comparingInt(client -> client.id));//should be from lowest to highest client now
        Client currentClient = null;
        HashMap<Integer, ArrayList<Integer>> complainingClients;
        HashMap<Integer, ArrayList<Integer>> complainingFccClients;
        for(client : sortedClients){
            clientDelay = clientDelays.get(client.id);//now we have the delay of each client in order from least to greatest id
            if((clientDelay > shortestPathLengths.get(client.id) * client.beta) && clientDelay <= shortestPathLengths.get(client.id) * client.alpha){
                //client is complaining but not unsubscribing yet, add them to list of complaining clients
                //add client to complainingClients here
                if(client.isFcc){
                //add client to complainingFccClients here
                }
            }
            else if(clientDelay > shortestPathLengths.get(client.id) * client.alpha){
                //client is going to unsubscribe, gotta deal with them
                currentClient = client;
                break;
            }
        }
        if (currentClient == null && complainingClients.size() < (int) (this.info.rho1 * clients.size()) && complainingClients.size() < (int) (this.info.rho2 * clients.size())){
            //there is no unsubscribing client, the complaining clients arent complaining too much to trigger lawsuits or fcc
            //should be good then
            return sol;
        }
        else{
            //something went wrong, check the above if conditions
            if(currentClient != null){//client thats going to unsubscribe

            }
            else if(complainingClients.size() < (int) (this.info.rho1 * clients.size())){
               currentClient = removeComplainingClient();
            }
            else if(complainingClients.size() < (int) (this.info.rho2 * clients.size())){
               currentClient = removeComplainingFccClient();
            }
        }
    }

        return sol;
    }


    //helper to remove a client from complaining clients hashmaps
    public ArrayList<Integer> removeComplainingClient() {
    Set<Integer> allKeys = new HashSet<>();
    allKeys.addAll(complainingClients.keySet());
    if (allKeys.isEmpty()) {
        return null;//edge case
    }
    
    Integer lowestKey = Collections.min(allKeys);
    if (complainingClients.containsKey(lowestKey) && !complainingFccClients.containsKey(lowestKey)) {
        return complainingClients.remove(lowestKey);
    }
    if (complainingFccClients.containsKey(lowestKey)) {
        complainingClients.remove(lowestKey);
        return complainingFccClients.remove(lowestKey);
    }
    return null; // Shouldnt be able to reach this but just for safekeeping
}
    //helper to remove a client from complaining fcc clients hashmaps
    public ArrayList<Integer> removeComplainingFccClient() {
    Set<Integer> allKeys = new HashSet<>();
    allKeys.addAll(complainingFccClients.keySet());
    if (allKeys.isEmpty()) {
        return null;//edge case
    }
    
    Integer lowestKey = Collections.min(allKeys);

    if (complainingFccClients.containsKey(lowestKey)) {
        complainingClients.remove(lowestKey);
        return complainingFccClients.remove(lowestKey);
    }
    return null; // Shouldnt be able to reach this but just for safekeeping
}

    //made a data structure that is essentially the graph but with weights for dijkstras
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



    public ArrayList<Integer> runDijkstras(Packet packet){
            int _startNode = graph.contentProvider;
            int _endnode = packet.path.get(packet.size()-1);
            wgraph.nodeWeights.addWeight(delayloc);
            ArrayList<Integer> resultPath = new ArrayList<>();
            sgraph = wgraph.size();
            int[] totalweight = new int[sgraph];
            int[] previous = new int[sgraph];
            boolean[] seen = new boolean[sgraph];
        for(int i=0;i<sgraph;i++){
            totalweight[i] = wgraph.nodeWeights.getWeight(i);
            previous[i] = -1;
            seen[i] = false;
        }
        //dijkstra algorithm happens here
        //pq will store node ids and prioritize order them based on their totalweight value
        PriorityQueue<Integer> spriorityqueue = new PriorityQueue<>((x,y) -> totalweight[x] - totalweight[y]);
        
        int initialweight = graph.get(_startNode).get(0);
        totalweight[_startNode] = initialweight;
        spriorityqueue.offer(_startNode);

        while (!spriorityqueue.isEmpty()){
            int cNode = spriorityqueue.poll();
            //int cNode = current[0]; ok i totally forgot to remove this after changing current to recieve the node
            int cWeight = totalweight[cNode];

            if(cNode == _endNode){
                break;//found end node
            }
            if(seen[cNode] || cWeight > totalweight[cNode]){
                continue; //node already seen
            }
            seen[cNode] = true;
            ArrayList<Integer> neighbors = graph.get(cNode);
            if(neighbors == null || neighbors.size() <= 1){
                continue; //no neighbors aka dead end
            }
            //check neighnors
            for (int i=1; i<neighbors.size();i++){
                int neighbor = neighbors.get(i);
                if(!graph.containsKey(neighbor)){
                    continue;//this needed to check if neighbor even exists in the graph
                }    
                    int neighborWeight = graph.get(neighbor).get(0);
                int newWeight = cWeight + neighborWeight;
                if(newWeight<totalweight[neighbor]){
                    totalweight[neighbor] = newWeight;
                    previous[neighbor] = cNode;
                    spriorityqueue.offer(neighbor);
                }
            }
        }
        if(totalweight[_endNode] == Integer.MAX_VALUE){
            return new ArrayList<>();//endnode was never found
        }
        /*else{
        ArrayList<Int    
        }*/
        int current = _endNode;
        while(current!=-1){
            result.add(current);
            current=previous[current];
        }
        //Collections.reverse reverses the priorityqueue order, Collections are beautiful
        Collections.reverse(result);
        return result;
    }

}
