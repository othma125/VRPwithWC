///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
///**
// *
// * @author hanan
// */
//
//import ilog.concert.IloException;
//import ilog.concert.IloIntVar;
//import ilog.concert.IloLinearIntExpr;
//import ilog.concert.IloLinearNumExpr;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloNumVar;
//import ilog.cplex.IloCplex;
//import java.util.stream.IntStream;
//
//public class CplexModel {
//    
//    private IloCplex Cplex;
//    private IloIntVar[][][] X;
//    private IloIntVar[][] D;
//    private IloNumVar[][] w;
//    private IloNumVar[][] I;
//    private final int RouteCount;
//    private final int M1;
//    private final int M2;
//    private InputData Data;
//
//    public CplexModel(InputData data, int rc) throws IloException {
//        this.RouteCount=rc;
//        this.Data=data;
//        this.Cplex = new IloCplex();
//        this.M1=this.Data.TimeWindows[0].Latest;
//
//        // Create decision variables
//        this.X=new IloIntVar[this.RouteCount][this.Data.StopsCount][];
//        for(int r=0;r<this.RouteCount;r++)
//            for(int i=0;i<this.Data.StopsCount;i++)
//                this.X[r][i]=this.Cplex.boolVarArray(this.Data.StopsCount);
//        
//        this.D=new IloIntVar[this.RouteCount][];
//        for(int r=0;r<this.RouteCount;r++)
//            this.D[r]=this.Cplex.intVarArray(this.Data.StopsCount,this.Data.VehicleInitialWeight,this.Data.VehicleInitialWeight+this.Data.VehicleCapacity);//constraints 3 and 4
//        
//        this.w=new IloNumVar[this.RouteCount][this.Data.StopsCount];
//        for(int r=0;r<this.RouteCount;r++)
//            for(int i=0;i<this.Data.StopsCount;i++)
//                this.w[r][i]=this.Cplex.numVar(this.Data.TimeWindows[i].Earliest,this.Data.TimeWindows[i].Latest);//Constraint 7 and 8
//        
//        this.I=new IloNumVar[this.RouteCount][];
//        for(int r=0;r<this.RouteCount;r++)
//            this.I[r]=this.Cplex.numVarArray(this.Data.StopsCount,this.Data.I_low,this.Data.I_high);//Constraint 11 and 12
//        this.M2=this.Data.BatteryCapacity;
//        
//        //Add objective function
//        this.ObjectiveFunction();
//        // Add constraints
//        this.Constraint_1();
//        this.Constraint_2();
//        this.Constraint_5();
//        this.Constraint_6();
//        this.Constraint_9();
//        this.Constraint_10();
//    }
//    
//    void Solve() throws IloException{
//        boolean condition=this.Cplex.solve();
//        if(condition){
//            System.out.println("Solution status = "+this.Cplex.getStatus());
//            System.out.println("Solution value = "+this.Cplex.getObjValue());
////            for(int r=0;r<this.RoutesCounter;r++){
////                System.out.println(Arrays.toString(this.cplex.getValues(this.H[r])));
////                for(int j=0;j<this.X[r].length;j++)
////                    System.out.println(Arrays.toString(this.cplex.getValues(this.X[r][j])));
//////                System.out.println(Arrays.toString(this.cplex.getValues(this.L[r])));
//////                System.out.println(Arrays.toString(this.cplex.getValues(this.W[r])));
//////                System.out.println(this.cplex.getValue(this.w[r]));
////                System.out.println();
////            }
//        }
//    }
//
//    private void ObjectiveFunction() throws IloException{
//        IloNumExpr obj=this.Cplex.numExpr();
//        for(int i=0;i<this.Data.StopsCount;i++)
//            for(int j=0;j<this.Data.StopsCount;j++)
//                for(int r=0;r<this.RouteCount;r++)
//                    obj=this.Cplex.sum(obj,this.Cplex.prod(this.X[r][i][j],this.Data.getDistance(i,j)));
//        this.Cplex.addMinimize(obj);
//    }
//    
//    private void Constraint_1() throws IloException {
//        for(int i=0;i<this.Data.StopsCount;i++){
//            if(i!=0 && this.Data.IsChargingStation[i])
//                continue;
//            IloLinearIntExpr expr=this.Cplex.linearIntExpr();
//            for(int r=0;r<this.RouteCount;r++)
//                for(int j=0;j<this.Data.StopsCount;j++)
//                    expr.addTerm(X[r][i][j],1);
//            this.Cplex.addEq(expr,1);
//        }
//    }
//    
//    private void Constraint_2() throws IloException {
//        for(int i=0;i<this.Data.StopsCount;i++)
//            for(int r=0;r<this.RouteCount;r++){
//                IloLinearIntExpr expr1=this.Cplex.linearIntExpr();
//                IloLinearIntExpr expr2=this.Cplex.linearIntExpr();
//                for(int j=0;j<this.Data.StopsCount;j++){
//                    expr1.addTerm(this.X[r][i][j],1);
//                    expr2.addTerm(this.X[r][j][i],1);
//                }
//                this.Cplex.addEq(expr1,expr2);
//            }
//    }
//    
//    private void Constraint_5() throws IloException {
//        for(int i=0;i<this.Data.StopsCount;i++)
//            for(int j=0;j<this.Data.StopsCount;j++)
//                for(int r=0;r<this.RouteCount;r++){
//                    IloNumExpr expr1=this.D[r][i];
//                    expr1=this.Cplex.diff(expr1,this.Data.Demand[j]);
//                    IloNumExpr expr2=this.D[r][j];
//                    expr2=this.Cplex.sum(expr2,this.Cplex.prod(this.M2,this.Cplex.diff(1,this.X[r][i][j])));
//                    this.Cplex.addLe(expr1,expr2);
//                }
//    }
//    
//    private void Constraint_6() throws IloException {
//        for(int i=0;i<this.Data.StopsCount;i++)
//            for(int j=0;j<this.Data.StopsCount;j++)
//                for(int r=0;r<this.RouteCount;r++){
//                    IloNumExpr expr=this.w[r][j];
//                    expr=this.Cplex.diff(expr,this.Data.Demand[i]);
//                    expr=this.Cplex.sum(expr,this.Cplex.prod(this.M2,this.Cplex.diff(1,this.X[r][i][j])));
//                    this.Cplex.addLe(this.Cplex.sum(this.Data.getDistance(i,j),this.w[r][i]),expr);
//                }
//    }
//    
//    private void Constraint_9() throws IloException {
//        for(int i=0;i<this.Data.StopsCount;i++)
//            if(this.Data.IsChargingStation[i])
//                for(int j=0;j<this.Data.StopsCount;j++)
//                    for(int r=0;r<this.RouteCount;r++){
//                        IloLinearNumExpr expr1=this.Cplex.linearNumExpr();
//                        expr1.addTerm(this.D[r][i],-this.Data.EnvergyConsumptionRate*this.Data.getDistance(i,j));
//                        IloNumExpr expr2=this.I[r][j];
//                        expr2=this.Cplex.sum(expr2,this.Cplex.prod(this.M2,this.Cplex.diff(1,this.X[r][i][j])));
//                        this.Cplex.addLe(this.Cplex.sum(this.Data.I_high+this.Data.WirelessChargingRate[i][j]*this.Data.getDistance(i,j),expr1),expr2);
//                    }
//    }
//    
//    private void Constraint_10() throws IloException {
//        for(int i=0;i<this.Data.StopsCount;i++)
//            if(!this.Data.IsChargingStation[i])
//                for(int j=0;j<this.Data.StopsCount;j++)
//                    for(int r=0;r<this.RouteCount;r++){
//                        IloLinearNumExpr expr1=this.Cplex.linearNumExpr();
//                        expr1.addTerm(this.I[r][i],1);
//                        expr1.addTerm(this.D[r][i],-this.Data.EnvergyConsumptionRate*this.Data.getDistance(i,j));
//                        IloNumExpr expr2=this.I[r][j];
//                        expr2=this.Cplex.sum(expr2,this.Cplex.prod(this.M2,this.Cplex.diff(1,this.X[r][i][j])));
//                        this.Cplex.addLe(this.Cplex.sum(this.Data.I_high+this.Data.WirelessChargingRate[i][j]*this.Data.getDistance(i,j),expr1),expr2);
//                    }
//    }
//}
