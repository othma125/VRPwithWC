
import java.io.File;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.IntStream;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hanan
 */
public class Solution{
    public int RoutesCount;
    public int[] GiantTour;
    public AuxiliaryGraph AuxiliaryGraph=null;
    
    private Solution(InputData data,LinkedList<Integer> GT,boolean mutation){
        this.GiantTour=GT.stream()
                        .flatMapToInt(x->IntStream.of(x))
                        .toArray();
        if(mutation)
            this.Mutation();
        this.Split(data);
    }
    
    Solution(InputData data){
        this.setRandomGiantTour(data);
        this.Split(data);
    }
    
    private void setRandomGiantTour(InputData data){
        this.GiantTour=IntStream.range(0,data.StopsCount)
                                .filter(i->!data.IsChargingStation[i])
                                .toArray();
        IntStream.range(0,this.GiantTour.length)
                .forEach(i->new Motion(i,(int)(Math.random()*this.GiantTour.length)).Swap(this.GiantTour));
    }
    
    private void Mutation(){
        int x=(int)(Math.random()*this.GiantTour.length);
        int y=(int)(Math.random()*this.GiantTour.length);
        new Motion(Math.min(x,y),Math.max(x,y))._2opt(this.GiantTour);
    }
    
    Solution Crossover(InputData data,Solution parent,boolean mutation,int ... cut_points){
        this.getNewGiantTour();
        parent.getNewGiantTour();
        LinkedList<Integer> GT=new LinkedList<>();
        int n=(cut_points.length==0)?0:cut_points[0];
        int p=cut_points[(cut_points.length==1)?0:1];
        int i=0;
        for(int j=n;j<p;j++)
            GT.add(parent.GiantTour[j]);
        for(int j=p;j<this.GiantTour.length;j++)
            if(!GT.contains(this.GiantTour[j]))
                if(GT.size()<this.GiantTour.length-n)
                    GT.add(this.GiantTour[j]);
                else{
                    GT.add(i,this.GiantTour[j]);
                    i++;
                }
        for(int j=0;j<p;j++)
            if(!GT.contains(this.GiantTour[j]))
                if(GT.size()<this.GiantTour.length-n)
                    GT.add(this.GiantTour[j]);
                else{
                    GT.add(i,this.GiantTour[j]);
                    i++;
                }
        return new Solution(data,GT,mutation);
    }  
    
    private void Split(InputData data){
        this.Split(data,Double.POSITIVE_INFINITY);
    }
    
    private void Split(InputData data,double bound){
        if(bound<Double.POSITIVE_INFINITY)
            this.getNewGiantTour();
        this.AuxiliaryGraph=this.getGraph(data,bound);
        if(this.getFitness()<bound)
            this.Split(data,this.getFitness());
    }
    
    private AuxiliaryGraph getGraph(InputData data,double bound){
        AuxiliaryGraph graph=new AuxiliaryGraph(data,this.GiantTour,bound);
        graph.setArcs();
        return graph;
    }
    
    private void setGraph(AuxiliaryGraph graph){
        this.AuxiliaryGraph=graph;
    }
    
    void LocalSearch(InputData data){
        this.LocalSearch(data,0,1);
    }
    
    void LocalSearch(InputData data,int i,int j){
        new Motion(i,j).Swap(this.GiantTour);
        AuxiliaryGraph graph=this.getGraph(data,this.getFitness());
        if(graph.getLabel()<this.getFitness()){
            this.setGraph(graph);
            if(j+1<this.GiantTour.length)
                this.LocalSearch(data,i,j+1);
            else if(i+2<this.GiantTour.length)
                this.LocalSearch(data,i+1,i+2);
        }
        else
            new Motion(i,j).Swap(this.GiantTour);
    }
    
    Set<Route> getRoutes(){
        return this.AuxiliaryGraph.getRoutes();
    }
    
    int getRoutesCount(){
        return this.AuxiliaryGraph.getRoutesCount();
    }
        
    int Compare(Solution s){
        return (int)(this.getFitness()*100d-s.getFitness()*100d);
    }
    
    @Override
    public String toString(){
        return this.AuxiliaryGraph.toString();
    }
    
    void setVisitTimes(){
        this.AuxiliaryGraph.setVisitTimes();
    }

    double getFitness(){
        return this.AuxiliaryGraph.getLabel();
    }

    boolean isFeasible(){
        if(this.AuxiliaryGraph!=null)
            return this.AuxiliaryGraph.isFeasible();
        return false;
    }

    int[] getNewGiantTour(){
        if(this.isFeasible())
            return this.AuxiliaryGraph.getGiantTour();
        return null;
    }
    
    void DrawGraph(InputData data){
        GraphViz gv=new GraphViz();
        gv.addln(gv.start_graph());
        for(Route route:this.getRoutes()){
            int previous=0;
            int index=0;
            for(int stop:route.Sequence){
                if(previous==0)
                    if(data.WirelessChargingRate[previous][stop]>0d)
                        gv.addln("D -> "+stop+"[color=Grey];");
                    else
                        gv.addln("D -> "+stop+";");
                else
                    if(data.WirelessChargingRate[previous][stop]>0d)
                        gv.addln(previous+" -> "+stop+"[color=green];");
                    else
                        gv.addln(previous+" -> "+stop+";");
                previous=stop;
                index++;
            }
            if(data.WirelessChargingRate[previous][0]>0d)
                gv.addln(previous+" -> D [color=green];");
            else
                gv.addln(previous+" -> D;");
        }
        gv.addln("D [color=lightblue2,shape=box,style=filled];");
        for(Route route:this.getRoutes())
            for(int stop:route.Sequence)
                if(data.IsChargingStation[stop])
                    gv.addln(stop+" [color=green,style=filled];");
                else
                    gv.addln(stop+" [color=white];");
        gv.end_graph();
        String desktop_path=System.getProperty("user.home") + "/Desktop";
        File out=new File(desktop_path.replace("/","\\")+"\\Graph (File Name = "+data.FileName+" ,Customers Count = "+data.CustomersCount+" ,Charging stations Count = "+data.ChargingStationsCount+" ,TotalTime = "+this.getFitness()+").jpg");
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(),"jpg"),out);         
    }
}