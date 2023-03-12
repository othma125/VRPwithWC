
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hanan
 */
public class AuxiliaryGraphNode{
    public final int NodeIndex;
    public int NodeProcessingWith;
    public double Label;
    public AuxiliaryGraphNode Posterior;
    public Set<Route> Routes;
    public final ReentrantLock Lock;
    
    AuxiliaryGraphNode(int NodeIndex){
        this.NodeIndex=NodeIndex;
        this.NodeProcessingWith=NodeIndex;
        this.Lock=new ReentrantLock();
        if(this.NodeIndex>0)
            this.Label=Double.POSITIVE_INFINITY;
        else{
            this.Routes=new HashSet<>();
            this.Posterior=null;
            this.Label=0d;
        }
    }
    
    void UpdateLabel(AuxiliaryGraphNode Posterior,Route route){
        if(route==null)
            return;
        this.Lock.lock();
        try{
            double label=Posterior.Label+route.RouteTravelDistance;
            if(label<this.Label){
                this.Label=label;
                this.Posterior=Posterior;
                this.Routes=new HashSet<>();
                this.Posterior.Routes.forEach(r->this.Routes.add(r));
                this.Routes.add(route);
            }
        }
        finally{
            this.Lock.unlock();
        }
    }
    
    void UpdateLabel(AuxiliaryGraphNode Posterior,Route old_route,Route new_route){
        if(new_route==null)
            return;
        this.Lock.lock();
        try{
            double label=Posterior.Label-old_route.RouteTravelDistance+new_route.RouteTravelDistance;
            if(label<this.Label){
                this.Label=label;
                this.Posterior=Posterior;
                this.Routes=new HashSet<>();
                for(Route r:Posterior.getRoutes())
                    this.Routes.add((r==old_route)?new_route:r);
            }
        }
        finally{
            this.Lock.unlock();
        }
    }
    
    public String toString(InputData data){
        String str="";
        int i=1;
        for(Route r:this.Routes){
            str+="Route "+i+" contains "+r.Length+" stops";
            str+=" :\n";
            str+=r.toString(data);
            i++;
        }
        str+="Total traveled distance = "+this.Label;
        return str;
    }
    
    void setVisitTimes(InputData data){
        this.Routes.forEach(r->r.setVisitTimes(data));
    }
    
    AuxiliaryGraphNode getPosterior(){
        return this.Posterior;
    }

    Set<Route> getRoutes(){
        return this.Routes;
    }

    int getRoutesCount(){
        return this.Routes.size();
    }
    
    boolean isFeasible(){
        return this.Posterior!=null;
    }

    int[] getGiantTour(InputData data){
        int i=0;
        int[] gt=new int[data.CustomersCount];
        for(Route route:this.Routes)
            for(int stop:route.Sequence)
                if(!data.IsChargingStation[stop]){
                    gt[i]=stop;
                    i++;
                }
        return gt;
    }
}