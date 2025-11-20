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
        HashMap<Integer, ArrayList<Integer>> shortestPaths = Traversals.bfsPaths(this.graph,this.clients);
        HashMap<Integer, Integer> clientdelay = Traversals.bfs(this.graph, this.clients);
        PriorityQueue<Integer> priorityqueue = new PriorityQueue<>((x,y) -> x - y);//lower id values come first
        for(Client client: this.clients){
            priorityqueue.offer(client.id);
        }

        ArrayList<ArrayList<Integer>> storedpaths = new ArrayList<ArrayList<Integer>>();
        ArrayList<Packet> packets = new ArrayList<>(this.clients.size());
        while (!priorityqueue.isEmpty()){
            //clients.remove(priorityqueue.poll());
            int clientid = priorityqueue.poll();
            Client currentClient;
            for(Client client: clients){
                if(client.id == clientid){
                    currentClient = client;
                    break;
                }
            }
            Packet packet = new Packet(clientid, shortestPaths, graph.contentProvider);
            packets.add(packet);
            packet.location = graph.contentProvider;
            packet.delay = Integer.MAX_VALUE;
            Integer packet_last_loc = packet.path.get(packet.size()-1);
            ArrayList<ArrayList<Integer>> badpaths = new ArrayList<ArrayList<Integer>>();
            boolean dijkstrasvalid = true;
            wgraph wgraph = new wgraph(this.graph); 
            while((packet.delay > clientdelay.get(clientid) * client.alpha) && dijkstrasvalid){//client is not satisfied or dijkstras found a valid path
            int delayloc = -1;
            int time_tick = 0;
            while (packet.location != packet_last_loc.location){
                time_tick++;
                 ArrayList<Integer> Delayed = new ArrayList<Integer>();
                ArrayList<Integer> checkedNodes = new ArrayList<Integer>();
                for(Packet eachpacket : packets){
                    eachpacket.delay = time_tick;
                    if(eachpacket.location == eachpacket.path.get(eachpacket.size()-1)){
                        continue;
                    }
                    Integer node = packet.location;
                    if(Delayed.contains(eachpacket.id)){
                    continue;
                    }
                    if(!checkedNodes.contains(node)){
                    checkedNodes.add(node);
                    ArrayList<Integer> ClientsAtNode = new ArrayList<Integer>();
                    for(Packet xpacket : packets){
                        if(xpacket.location == node){
                            ClientsAtNode.add(xpacket);
                        }
                    }
                    if(ClientsAtNode.size() > info.bandwidths.get(node)){
                        //check
                        Int bandwidthcutoff = info.bandwidths.get(node);
                        for(int i= bandwidthcutoff; i<ClientsAtNode.size();i++){
                        Delayed.add(ClientAtNode.get(i));
                        }
                    }
                    //move here
                    Int index = eachpacket.path.indexOf(eachpacket.location);
                        eachpacket.location =  eachpacket.path.get(index+1);     //next location
                }
            }
        }
        boolean satisfied = packet.delay <= clientdelay.get(clientid) * client.alpha;
        if(satisfied){
            continue;
        }
        else{
            badpaths.add(packet.path);
            ArrayList<Integer> result = runDijkstras(packet);
            dijkstrasvalid = !badpaths.containing(result);
    }
}


}

for(Packet packet : packets){
    storedpaths.add(packet.path);
}


        //hashmap for priorities and bandwidths here
        HashMap<Integer, Integer> newpriorities = new HashMap<>(clients.size());
        for (Client client : this.clients) {
            newpriorities.put(client.id, 0);
        }
        sol.paths = storedpaths;
        sol.priorities = newpriorities;
        sol.bandwidths = this.bandwidths;
        return sol;
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
