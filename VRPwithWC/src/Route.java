
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
public class Route{
    public final int Length;
    public final int EmptySpaceInVehicle;
    public final int[] Sequence;
    public double RouteTravelDistance=0d;
    public double StartTime;
    public double EndTime;
    public double[] VisitTimes;
    
    Route(InputData data,int[] gt,int empty_space){
        this.Sequence=gt;
        this.StartTime=data.TimeWindows[0].Earliest;
        this.EmptySpaceInVehicle=empty_space;
        this.Length=this.Sequence.length;
    }
    
    Route getFeasibleRoute(InputData data){
        return this.getFeasibleRoute(data,true);
    }
    
    Route getFeasibleRoute(InputData data,boolean c){
        if(this.EmptySpaceInVehicle<0)
            return null;
        return this.getFeasibleRoute(data,c,0,0d,this.StartTime,data.VehicleCapacity-this.EmptySpaceInVehicle,data.I_high);
    }
    
    private Route getFeasibleRoute(InputData data,boolean c,int index,double cumulated_traveled_distance,double availability_time,int loaded_weight,double I){
        int stop=(index==this.Length)?0:this.Sequence[index];
        int previous_stop=(index==0)?0:this.Sequence[index-1];
        double current_time=availability_time+data.getDistance(previous_stop,stop);
        if(current_time<data.TimeWindows[stop].Earliest)
            current_time=data.TimeWindows[stop].Earliest;
        else if(current_time>data.TimeWindows[stop].Latest)
            return null;
        current_time+=data.ServiceTime[stop];
        double current_I=I;
        current_I-=data.EnvergyConsumptionRate*(data.VehicleInitialWeight+loaded_weight)*data.getDistance(previous_stop,stop);
        current_I+=data.WirelessChargingRate[previous_stop][stop]*data.getDistance(previous_stop,stop);
        if(current_I>=data.I_low && current_I<=data.I_high)
            if(stop==0){
                this.RouteTravelDistance=cumulated_traveled_distance+data.getDistance(previous_stop,stop);
                this.EndTime=current_time;
                return this;
            }
            else
                return this.getFeasibleRoute(data,c,index+1,cumulated_traveled_distance+data.getDistance(previous_stop,stop),current_time,loaded_weight-data.Demand[stop],(data.IsChargingStation[stop])?data.I_high:current_I);
        else if(c && current_I<data.I_low && stop!=0 && !data.IsChargingStation[stop])
            return IntStream.range(1,index+1)
                            .mapToObj(k->IntStream.range(1,data.StopsCount)
                                                .filter(i->data.IsChargingStation[i])
                                                .filter(i->(k==index)?data.getDistance(previous_stop,i)<data.getDistance(previous_stop,stop):true)
                                                .mapToObj(i->{
                                                    int[] new_sequence=IntStream.range(0,this.Length+1)
                                                                                .map(j->{
                                                                                    if(j<k)
                                                                                        return this.Sequence[j];
                                                                                    else if(j==k)
                                                                                        return i;
                                                                                    else
                                                                                        return this.Sequence[j-1];
                                                                                })
                                                                                .toArray();
                                                    return new Route(data,new_sequence,this.EmptySpaceInVehicle);
                                                })
                                                .map(r->(k==index)?r.getFeasibleRoute(data,false,index,cumulated_traveled_distance,availability_time,loaded_weight,I):r.getFeasibleRoute(data,false))
                                                .filter(r->r!=null)
                                                .reduce(null,(r1,r2)->(r1==null || r1.RouteTravelDistance>r2.RouteTravelDistance)?r2:r1))
                            .filter(r->r!=null)
                            .reduce(null,(r1,r2)->(r1==null || r1.RouteTravelDistance>r2.RouteTravelDistance)?r2:r1);
        return null;
    }
    
    void setVisitTimes(InputData data){
        this.VisitTimes=new double[this.Length];
        double current_time=this.StartTime;
        int previous_stop=0;
        for(int i=0;i<this.Sequence.length;i++){
            int stop=this.Sequence[i];
            current_time+=data.getDistance(previous_stop,stop);
            if(current_time<data.TimeWindows[stop].Earliest)
                current_time=data.TimeWindows[stop].Earliest;
            this.VisitTimes[i]=current_time;
            current_time+=data.ServiceTime[stop];
            previous_stop=stop;
        }
    }
    
    public String toString(InputData data){
        String str="";
        int previous_customer=0;
        for(int i=0;i<this.Sequence.length;i++){
            int stop=this.Sequence[i];
            if(data.IsChargingStation[stop])
                str+="Charging point "+stop+" is visitied at "+(int)this.VisitTimes[i]+"\n";
            else
                str+="Customer "+stop+" is visitied at "+(int)this.VisitTimes[i]+"\n";
            previous_customer=stop;
        }
        str+="\n";
        return str;
    }
}