package ub.cse.algo;

import java.io.Console;
import java.lang.reflect.Array;
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

        
        int bandwidthTracker=0;        
        HashMap<Integer, Integer> shortestPathLengths = Traversals.bfs(this.graph, this.clients);//lengths of shortest paths
        HashMap<Integer, ArrayList<Integer>> shortestPaths = Traversals.bfsPaths(this.graph,this.clients);// creates original shortest paths for all clients
        sol.paths = shortestPaths; //assigns that to the sol object
        HashMap<Integer, Integer> newpriorities = new HashMap<>(clients.size());
        for (Client client : this.clients) {
            newpriorities.put(client.id, 0);
        }
        sol.priorities = newpriorities; //assigns empty priorities to sol object
        sol.bandwidths = this.bandwidths; //assigns bandwiths to the inital bandwidths given
         // System.out.println("Initial bandwidths: " + sol.bandwidths.toString());
        ArrayList<Client> sortedClients = new ArrayList<>(this.clients);
        sortedClients.sort(Comparator.comparingInt(client -> client.id));//should be from lowest to highest client now
        //now clientDelays has all the delays for each client
         //for(client : this.clients){//algorithm relies on clients being from lowest to highest id, organize if necessary
       HashMap<Integer, Client> complainingClients = new HashMap<>(clients.size());


        //
        HashMap<Integer, ArrayList<Integer>> complainingPaths = new HashMap<>(clients.size());
        HashMap<Integer, ArrayList<Integer>> badNodes = new HashMap<>(clients.size());
        for(int i = 0; i < sortedClients.size();){//searches through all clients from first to last, only moving on to next when confirmed subscribed
        Client client = sortedClients.get(i);
        HashMap<Integer, Integer> clientDelays = Simulator.run(this.graph, this.clients, sol); // executes run from Simulator and stores result in clientDelays

        int cDelay = clientDelays.get(client.id);
        int unsubscribe = (int) (client.alpha * shortestPathLengths.get(client.id));
        int complaining = (int) (client.beta * shortestPathLengths.get(client.id));
        boolean improved = false;
        //System.out.println(shortestPathLengths.get(client.id) + " is the shortest path versus " +cDelay + " current delay for client " + client.id + " versus unsubscribe " + unsubscribe + " and complaining " + complaining);
        //block for unsub
        if (cDelay > unsubscribe) {
            ArrayList<Integer> currentPath = sol.paths.get(client.id);
            //System.out.println(shortestPaths.get(client.id).toString() + " shortest path for client " + client.id);
            //System.out.println(currentPath == shortestPaths.get(client.id));
           // if (currentPath == shortestPaths.get(client.id)){ // comparing our current list, and adding worst node
                boolean satisfyPossible = currentPath.size() <= (int) (client.beta * shortestPathLengths.get(client.id));//checks if the path currently being used can even satisfy complaining client
         
            if(!satisfyPossible){
                //CODE RED 
                //clean complainingPaths of the path of this client
                sol.paths.put(client.id,shortestPaths.get(client.id));
                improve(sol,client);
                badNodes.put(client.id, new ArrayList<Integer>());
                continue;
            }
                int worstNode = worstNodeFinder(sol, client);
                ArrayList<Integer> badNodeList = badNodes.get(client.id);
                if(badNodeList == null){
                    badNodeList = new ArrayList<Integer>();
                }
                //System.out.println(badNodeList);
                badNodeList.add(worstNode);
                badNodes.put(client.id,badNodeList);
                //System.out.println(badNodes.toString() + " bad nodes");
         //  }
            
                //search check
                boolean searchSuccess=false;
                //do search here
                
                ArrayList<Integer> newPath = searchPath(this.graph, client, true, searchSuccess, shortestPathLengths, badNodes);
                //System.out.println();
                if (newPath != null && newPath.get(0) == 1){
                    searchSuccess = true;
                }
                if (newPath != null){
                   newPath.remove(0);
                   newPath.set(0, graph.contentProvider);
                    sol.paths.put(client.id,newPath);
                } 
                

                if(!searchSuccess){
                //clean bad nodes of client here
                badNodes.remove(client.id);
                sol.paths.put(client.id,shortestPaths.get(client.id));
                improve(sol, client);
                 bandwidthTracker++;
            }
                improved = true;
                //System.out.println(" This is our client delay " + cDelay + "This is our unsubscribe thresold " + unsubscribe);
            } 
        else if (cDelay > complaining) {
            //client is no longer unsubscribing but is now complaining still
             complainingClients.put(client.id,client);
             complainingPaths.put(client.id,sol.paths.get(client.id));   
             //clean hashmap of bad nodes here
             badNodes.remove(client.id);
            }
        else{
            //client is no longer unsubscribing, and also not complaining
            //clean hashmap of bad nodes here
            badNodes.remove(client.id);            
        }

        //seperate check that deals with complaining clients
        HashMap<Integer, Integer> currentPathLengths = new HashMap<>(clients.size());
        for(Client clientx : this.clients){
            currentPathLengths.put(clientx.id,sol.paths.get(clientx.id).size());
        }

        if(isAtLegalRisk(clientDelays, currentPathLengths) && !complainingClients.isEmpty()){
            Client clientOfInterest = complainingClients.get(findClosestComplainer(complainingClients, clientDelays, currentPathLengths));

            int interestId = clientOfInterest.id;
            boolean satisfyPossible = complainingPaths.get(interestId).size() <= (int) (clientOfInterest.beta * shortestPathLengths.get(interestId));//checks if the path currently being used can even satisfy complaining client
         
            if(!satisfyPossible){
                //CODE RED 
                //clean complainingPaths of the path of this client
                sol.paths.put(clientOfInterest.id,shortestPaths.get(clientOfInterest.id));
                improve(sol,clientOfInterest);
                i = clientOfInterest.id;
                badNodes.remove(clientOfInterest.id);
                continue;
            }
            ArrayList<Integer> currentPath = sol.paths.get(clientOfInterest.id);

            if (currentPath != complainingPaths.get(client.id)){ // comparing our current path against paths of complaining clients
                int worstNode = worstNodeFinder(sol, client);
                ArrayList<Integer> badNodeList = badNodes.get(client.id);
                badNodeList.add(worstNode);
                badNodes.put(client.id,badNodeList);
            }

            boolean searchSuccess = false;
            //search here

            ArrayList<Integer> newPath = searchPath(this.graph, clientOfInterest, false, searchSuccess, shortestPathLengths, badNodes);
            if (newPath != null && newPath.get(0) == 1){
                    searchSuccess = true;
                }
                if (newPath != null){
                newPath.remove(0);
                newPath.set(0, graph.contentProvider);
                sol.paths.put(clientOfInterest.id,newPath);
                }
                

            if(!searchSuccess){
            sol.paths.put(clientOfInterest.id,complainingPaths.get(clientOfInterest.id));
            improve(sol, clientOfInterest);
            badNodes.remove(clientOfInterest.id);
             bandwidthTracker++;
            }
            //improved = true;
           
        }
        else{
            if(!improved){
            i++;}
         }

        }
        //System.out.println("Final bandwidths: " + sol.bandwidths.toString());\
        //System.out.println("Bandwidth added: "+bandwidthTracker);
        
        int totalClients = clients.size();
        int isSubbed = 0;
        int isCompl = 0;
        for (Client client : clients) {
            HashMap<Integer, Integer> clientDelays = Simulator.run(this.graph, this.clients, sol); // executes run from Simulator and stores result in clientDelays
            boolean isSubscribed = clientDelays.get(client.id) <= (shortestPathLengths.get(client.id) * client.alpha);
            if(isSubscribed){
                isSubbed++;
            }
            boolean isComplaining = clientDelays.get(client.id) <= (shortestPathLengths.get(client.id) * client.beta);
            if(isComplaining){
                isCompl++;
            }
            
            
            //System.out.println(totalClients + " " + isSubbed + " " + isCompl);
            //System.out.println(isComplaining);
        }
        /*
        HashMap<Integer, Integer> clientDelays2 = Simulator.run(this.graph, this.clients, sol);
        for(Client client2 : clients){
            System.out.println(shortestPathLengths.get(client2.id) + " is the shortest path versus " +clientDelays2.get(client2.id) + " current delay for client " + client2.id + " versus unsubscribe " + (int)(shortestPathLengths.get(client2.id) * client2.alpha )+ " and complaining " + (int)(shortestPathLengths.get(client2.id) * client2.beta) );
            }
        */
        return sol;
    }

    ArrayList<Integer> searchPath(Graph graph, Client client, boolean isUnsub, boolean searchSuccess, HashMap<Integer, Integer> shortestPathLengths, HashMap<Integer, ArrayList<Integer>> badNodes){
        int[] priors = new int[graph.size()];
        Arrays.fill(priors, -2);

        Queue<Integer> searchQueue = new LinkedList<>();
        searchQueue.add(graph.contentProvider);
        while (!searchQueue.isEmpty()) {
            int node = searchQueue.poll();
            for (int neighbor : graph.get(node)) {
                if (priors[neighbor] == -2) {
                    if (neighbor == graph.contentProvider){
                        priors[neighbor] = -1;
                        continue;
                    }
                    boolean isBadNode = false;
                    if (!badNodes.isEmpty()){
                        for(Integer badNode : badNodes.get(client.id)){
                    if (neighbor == badNode) {
                        isBadNode = true;
                    }
                } 
                    }
                    
                    if(!isBadNode) {
                        priors[neighbor] = node;
                        searchQueue.add(neighbor);
                    }
                    //searchQueue.add(neighbor);
                }
            }
        }
            // Get all the distances to the clients only
        //HashMap<Integer, Integer> clientDistances = new HashMap<>(clients.size());
        //for (Client client : clients) {
         //   clientDistances.put(client.id, distances[client.id]);
        //}
        //return clientDistances;
        float multiplier;
        if (isUnsub){
            multiplier = client.alpha;
        }
        else{
            multiplier = client.beta;
        }
        ArrayList<Integer> returnToResult = searchNodes(client, priors);
        //check
        if(returnToResult != null && returnToResult.size() <= multiplier * shortestPathLengths.get(client.id)){
            returnToResult.add(0, 1);
        }
        else if(returnToResult != null){
            returnToResult.add(0, 0);
        }
        //println("Searched path: " + returnToResult.toString() + " for client " + client.id);
        return returnToResult;
    }


    ArrayList<Integer> searchNodes(Client client, int[] priors) {
        //HashMap<Integer, ArrayList<Integer>> paths = new HashMap<>(clients.size());
        // For every client, traverse the prior array, creating the path
        //for (Client client : clients) {
        
            ArrayList<Integer> path = new ArrayList<>();
            int currentNode = client.id;
            while (currentNode != -1) {
                //System.out.println(currentNode);
                //System.out.println(priors[currentNode]);
                if (priors[currentNode] == -2){
                    return null;
                }
                else{
                    path.add(0, currentNode);
                    currentNode = priors[currentNode];
                }
                /*
                    currentNode = client.id
                    Add this ID to the beginning of the
                    path so the path ends with the client
                 */
                //path.add(0, currentNode);
                //currentNode = priors[currentNode];
            }
            //System.out.println("Hi ");
        //System.out.println(path.toString() + " path for client " + client.id);
        return path;
    }

    int findClosestComplainer(HashMap<Integer, Client> complainingClients, HashMap<Integer, Integer> cDelays, HashMap<Integer, Integer> shortestPathLengths){
        int result=-1;
        int gap=-1;
        for(Client client : complainingClients.values()){
        int complaining = (int) (client.beta * shortestPathLengths.get(client.id));
        int complainGap = cDelays.get(client.id) - complaining;
        if(gap == -1 || gap > complainGap){
            result = client.id;
            //System.out.println(result);
            gap = complainGap;
        }
        }
        if(gap != -1){
            return result;
        }
        else 
        return result;
    }

    
    int worstNodeFinder(SolutionObject sol, Client client) {
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
        return worstNode;
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
            sol.bandwidths.set(worstNode, cBandwidth+1);
           // System.out.println("Increased bandwidth of node " + worstNode + " to " + (cBandwidth+1));
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
        int fcc = (int) (info.rho2 * (int) clients.stream().filter(client -> client.isFcc).count());
        
        return totalComplaints >= lawsuit || fccComplaints >= fcc;
    }

/*
    int countFccClients(ArrayList<Client> clients) {
        return (int) clients.stream().filter(client -> client.isFcc).count();
    }\
    */

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