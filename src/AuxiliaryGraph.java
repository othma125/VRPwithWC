
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class AuxiliaryGraph{
    private int ThreadsCounter;
    private final int Length;
    private final double BoundingCost;
    private final int[] GiantTour;
    private final AuxiliaryGraphNode[] Nodes;
    private final InputData Data;
    

    AuxiliaryGraph(InputData data,int[] GT,double bound){
        this.Data=data;
        this.BoundingCost=bound;
        this.ThreadsCounter=0;
        this.GiantTour=GT;
        this.Length=this.GiantTour.length;
        this.Nodes=new AuxiliaryGraphNode[this.Length+1]; 
        for(int i=0;i<this.Nodes.length;i++)
            this.Nodes[i]=new AuxiliaryGraphNode(i);
    }

    void setArcs(){
        this.ThreadsCounter++;
        this.run(this.Nodes[0]);
    }

    void setNewThread(AuxiliaryGraphNode node,Set<Thread> Threads){
        for(int i=(node.isFeasible())?node.Posterior.NodeIndex:0;i<node.NodeIndex;i++)
            if(this.Nodes[i].NodeProcessingWith<node.NodeIndex)
                return;
        boolean c;
        c=this.ThreadsCounter==node.NodeIndex;
        if(c){
            this.ThreadsCounter++;
            if(node.isFeasible() && node.Label<this.BoundingCost){
                Thread t=new Thread(()->this.run(node));
                Threads.add(t);
                node.Lock.lock();
                try {
                    t.start();
                } finally {
                    node.Lock.unlock();
                }
            }
            else
                node.NodeProcessingWith=this.Length;
        }
        if(c && node.NodeIndex+1<this.Length)
            this.setNewThread(this.Nodes[node.NodeIndex+1],Threads);
    }
    
    void run(AuxiliaryGraphNode StartingNode){
        int customer;
        int sum_demand=0;
        AuxiliaryGraphNode EndingNode;
        Set<Thread> Threads=new HashSet<>();
        for(int i=StartingNode.NodeIndex;i+1<this.Nodes.length;i++){
            EndingNode=this.Nodes[i+1];
            customer=this.GiantTour[i];
            sum_demand+=this.Data.Demand[customer];
            int[] sequence=IntStream.range(StartingNode.NodeIndex,EndingNode.NodeIndex)
                                    .map(index->this.GiantTour[index])
                                    .toArray();
            if(this.Data.VehicleCapacity>=sum_demand){
                Route new_route=new Route(this.Data,sequence,this.Data.VehicleCapacity-sum_demand);
                EndingNode.UpdateLabel(StartingNode,new_route.getFeasibleRoute(this.Data));
                for(Route old_route:StartingNode.Routes)
                    if(old_route.EmptySpaceInVehicle>=sum_demand){
                        int[] new_sequence=IntStream.range(0,old_route.Sequence.length+sequence.length)
                                                .map(index->{
                                                    if(index<old_route.Length)
                                                        return old_route.Sequence[index];
                                                    else
                                                        return sequence[index-old_route.Sequence.length];
                                                })
                                                .toArray();
                        new_route=new Route(this.Data,new_sequence,old_route.EmptySpaceInVehicle-sum_demand);
                        EndingNode.UpdateLabel(StartingNode,old_route,new_route.getFeasibleRoute(this.Data));
                    }
            }
            else{
                StartingNode.NodeProcessingWith=this.Length;
                if(EndingNode.NodeIndex<this.Length)
                    this.setNewThread(EndingNode,Threads);
                break;
            }
            StartingNode.NodeProcessingWith++;
            if(EndingNode.NodeIndex<this.Length)
                this.setNewThread(EndingNode,Threads);
        }
        Threads.forEach(t->{
                        if(t.isAlive())
                            try{
                                t.join();
                            }catch(InterruptedException ex){
                                Logger.getLogger(Solution.class.getName()).log(Level.SEVERE,null,ex);
                            }
                    });
        Threads.clear();
    }

    AuxiliaryGraphNode getLastNode(){
        return this.Nodes[this.Length];
    }
    
    void setVisitTimes(){
        this.getLastNode().setVisitTimes(this.Data);
    }

    boolean isFeasible(){
        return this.getLastNode().isFeasible();
    }

    double getLabel(){
        return this.getLastNode().Label;
    }

    Set<Route> getRoutes(){
        return this.getLastNode().getRoutes();
    }

    int getRoutesCount(){
        return this.getLastNode().getRoutesCount();
    }

    int getRoutesCounter(){
        return this.getRoutes().size();
    }
    
    int[] getGiantTour(){
        return this.getLastNode().getGiantTour(this.Data);
    }
    
    @Override
    public String toString(){
        return this.getLastNode().toString(this.Data);
    }
}