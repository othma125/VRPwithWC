
import java.io.File;
//import ilog.concert.IloException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hanan
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
                                    //    throws IloException
                                                            {
        // TODO code application logic here
        InputData data=new InputData(new File("Instances\\Solomon\\c101.txt"),50/*stops count*/);
        System.out.println(data);
        
        MetaHeuristic algorithm=new GeneticAlgorithm(data);
        algorithm.Run(5/*run time in second*/);

        if(algorithm.isFeasible()){
            System.out.println(algorithm.getBestSolution());
//            algorithm.getBestSolution().DrawGraph(data);
//            new CplexModel(data,algorithm.getBestSolution().getRoutesCount()).Solve();
        }
    }
}