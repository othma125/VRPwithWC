
import java.io.File;
import java.util.stream.Stream;
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
public class main2 {
    public static void main(String[] args)
                                    //    throws IloException
                                                            {
        Stream.of(new File("instances\\Solomon").listFiles())
                .map(file->new InputData(file,50/*stops count*/))
                .forEach(data->{
                    System.out.println(data);

                    MetaHeuristic algorithm=new GeneticAlgorithm(data);
                    algorithm.Run(5/*run time in second*/);

                    if(algorithm.isFeasible()){
                        System.out.println(algorithm.getBestSolution());
//                        algorithm.getBestSolution().DrawGraph(data);
//                        new CplexModel(data,algorithm.getBestSolution().getRoutesCount()).Solve();
                    }
                    System.out.println();
                    System.gc();
                });
    }
}
