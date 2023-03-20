
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public abstract class MetaHeuristic{
    InputData Data;
    Long RunTime;// Run Time in milliseconds
    Long StartTime;// Start Time in milliseconds
    Long BestSolutionReachingTime;
    private Solution BestSolution=null;

    MetaHeuristic(InputData data){
        this.Data=data;
    }

    public Solution getBestSolution() {
        return this.BestSolution;
    }

    public void setBestSolution(Solution solution) {
        this.BestSolution=solution;
    }
    
    public boolean isFeasible(){
        return this.BestSolution!=null;
    }
    
    abstract void Run(int RunTime);
}

class GeneticAlgorithm extends MetaHeuristic{
    ReentrantLock GlobalLock=new ReentrantLock();
    Set<Thread> AliveThreads=new HashSet<>();
    final int PopulationSize=20;
    final double MutationRate=0.1d;
    final double CrossoverRate=0.8d;
    final Solution[] Population;
    
    GeneticAlgorithm(InputData data){
        super(data);
        this.Population=new Solution[this.PopulationSize];
    }
    
    @Override
    void Run(int RunTime/*in seconds*/){
        this.RunTime=RunTime*1000l;
        System.out.println("Solution approach = Genetic Algorithm");
        System.out.println();
        this.StartTime=System.currentTimeMillis();
        this.InitialPopulation();
        this.BestSolutionReachingTime=System.currentTimeMillis()-this.StartTime;
        if(this.Population[0].isFeasible()){
            System.out.println();
            System.out.println(this.Population[0].getFitness()+" after "+this.BestSolutionReachingTime+" ms");
        }
        else{
            System.out.println("No feasible solution found");
            return;
        }
        this.setBestSolution(this.Population[0]);
        while(System.currentTimeMillis()-this.StartTime<this.RunTime){
            this.Selection();
            synchronized(this){
                while(this.AliveThreads.stream()
                                        .filter(Thread::isAlive)
                                        .count()>2)
                    try{
                        this.wait();
                    }catch(InterruptedException ex){
                        Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
        this.Join();
        System.out.println();
        this.setBestSolution(this.Population[0]);
        this.getBestSolution().setVisitTimes();
    }
    
    private void Selection(){
        int half=this.PopulationSize/2;
        int i=(int)(Math.random()*half);
        int j;
        if(Math.random()<0.7d)
            do{
                j=(int)(Math.random()*half);
            }while(i==j);
        else
            j=half+(int)(Math.random()*(this.PopulationSize-half));
        this.Crossover(Math.random()<this.MutationRate,this.Population[i],this.Population[j]);
    }
    
    private void Crossover(boolean mutation,Solution ... parents){
        boolean CrossoverCondition=Math.random()<this.CrossoverRate;
        int CutPoint1=(int)(this.Data.CustomersCount*Math.random());
        int CutPoint2=(Math.random()<0.7d)?CutPoint1:CutPoint1+(int)((this.Data.CustomersCount-CutPoint1)*Math.random());
        Thread t=new Thread(()->{
            Solution Child=(CrossoverCondition)?parents[0].Crossover(this.Data,parents[1],mutation,CutPoint1,CutPoint2):new Solution(this.Data);
            Child.LocalSearch(this.Data);
            this.UpdatePopulation(Child);
            synchronized(this){
                this.notify();
            }
        });
        t.start();
        Solution Child=(CrossoverCondition)?parents[1].Crossover(this.Data,parents[0],mutation,CutPoint1,CutPoint2):new Solution(this.Data);
        Child.LocalSearch(this.Data);
        this.UpdatePopulation(Child);
    }
    
    private void UpdatePopulation(Solution newSolution){
        this.GlobalLock.lock();
        try{
            if(newSolution.isFeasible() && newSolution.getFitness()<this.Population[this.PopulationSize-1].getFitness()){
                int half=this.PopulationSize/2;
                int i=half+(int)(Math.random()*(this.Population.length-half));
                this.Population[i]=newSolution;
                if(newSolution.getFitness()<this.Population[0].getFitness()){
                    this.BestSolutionReachingTime=System.currentTimeMillis()-this.StartTime;
                    if((int)newSolution.getFitness()<(int)this.Population[0].getFitness())
                        System.out.println(newSolution.getFitness()+" after "+this.BestSolutionReachingTime+" ms");
                }
                Arrays.sort(this.Population,(s1,s2)->s1.Compare(s2));
            }
        }finally{
            this.GlobalLock.unlock();
        }
    }
    
    private void InitialPopulation(){
        System.out.println("Initial population");
        for(int i=0;i<this.PopulationSize;i++){
            if(i%2==0){
                int j=0;
                do{
                    this.Population[i]=new Solution(this.Data);
                    j++;
                    if(i==0 && !this.Population[i].isFeasible() && j>10)
                        return;
                }while(!this.Population[i].isFeasible());
                System.out.println(this.Population[i].getFitness());
            }
            else{
                final int index=i;
                Thread t=new Thread(()->{
                    do{
                        this.Population[index]=new Solution(this.Data);
                    }while(!this.Population[index].isFeasible());
                    synchronized(this){
                        this.notify();
                    }
                    System.out.println(this.Population[index].getFitness());
                });
                t.start();
                this.AliveThreads.add(t);
            }
            synchronized(this){
                while(this.AliveThreads.stream().filter(t->t.isAlive()).count()>2)
                    try{
                        this.wait();
                    }catch(InterruptedException ex){
                        Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
        this.Join();
        this.AliveThreads.clear();
        Arrays.sort(this.Population,(s1,s2)->s1.Compare(s2));
    }
    
    private void Join(){
        this.AliveThreads.forEach(t->{
                        if(t.isAlive())
                            try {
                                t.join();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                            }
                    });
    }
}