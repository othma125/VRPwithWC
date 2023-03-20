
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;



/**
 *
 * @author Othmane
 */
public class InputData {
    int CustomersCount;
    int ChargingStationsCount;
    int StopsCount;//=StopsCount+ChargingStationsCount
    final int BatteryCapacity=50000;
    int VehicleCapacity;
    final int VehicleInitialWeight=300;
    double I_high;
    double I_low;
    final double EnvergyConsumptionRate=1.25;//consumption rate by WH/Km/Kg
    double[] ServiceTime;
    private double[][] DistanceMatrix;
    double[][] WirelessChargingRate;
    private Stop[] Coordinates;
    boolean[] IsChargingStation;
    int[] Demand;
    TimeWindow[] TimeWindows;
    public String FileName;

    @Override
    public String toString() {
        return "FileName = " + this.FileName+"\n"
                +"Costumers Count = " + this.CustomersCount
                +"\nCharging Stations Count = " + this.ChargingStationsCount
                +"\nBattery Capacity = " + this.BatteryCapacity+ " WH\n";
    }
    
    InputData(File file,int n){
        try{
            this.StopsCount=n;
            this.FileName=file.getName();
            Scanner scanner=new Scanner(file);
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            String line=scanner.nextLine();
            StringTokenizer st=new StringTokenizer(line);
            st.nextToken();
            this.VehicleCapacity=Integer.valueOf(st.nextToken());
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            this.I_high=0.8d*this.BatteryCapacity;
            this.I_low=0.2d*this.BatteryCapacity;
            this.Coordinates=new Stop[this.StopsCount];
            this.Demand=new int[this.StopsCount];
            this.ServiceTime=new double[this.StopsCount];
            this.TimeWindows=new TimeWindow[this.StopsCount];
            this.IsChargingStation=new boolean[this.StopsCount];
            this.CustomersCount=0;
            this.ChargingStationsCount=0;
            for(int i=0;i<this.StopsCount;i++){
                line=scanner.nextLine();
                st=new StringTokenizer(line,"\t "); 
                st.nextToken();
                this.IsChargingStation[i]=i%5==0;
                if(this.IsChargingStation[i])
                    this.ChargingStationsCount++;
                else
                    this.CustomersCount++;
                this.Coordinates[i]=new Stop(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));
                if(this.IsChargingStation[i])
                    st.nextToken();
                else
                    this.Demand[i]=Integer.valueOf(st.nextToken());
                if(i==0)
                    this.TimeWindows[i]=new TimeWindow(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));//working day time window is the depot time window
                else if(this.IsChargingStation[i]){
                    this.TimeWindows[i]=this.TimeWindows[0];
                    st.nextToken();
                    st.nextToken();
                }
                else
                    this.TimeWindows[i]=new TimeWindow(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));
                this.ServiceTime[i]=Integer.valueOf(st.nextToken())/60d;
            }
            scanner.close();
            this.DistanceMatrix=new double[this.StopsCount][this.StopsCount];
            this.WirelessChargingRate=new double[this.StopsCount][this.StopsCount];
            for(int i=0;i<this.StopsCount;i++)
                for(int j=i+1;j<this.StopsCount;j++)
                    this.WirelessChargingRate[i][j]=
                            this.WirelessChargingRate[j][i]=(this.IsChargingStation[i]
                                                                || this.IsChargingStation[j]
                                                                ||(j%2==0 && i%2==0)
                                                                ||(j%2==1 && i%2==1))?0d:2d;//electric energy by distance
        }        
        catch(FileNotFoundException e){
            System.out.println("file not found");
            System.exit(0);              
        }
    }
    
    double getDistance(int i,int j){
        if(this.DistanceMatrix[i][j]==0d && i!=j)
            this.DistanceMatrix[i][j]=this.DistanceMatrix[j][i]=this.Coordinates[i].getDistance(this.Coordinates[j]);
        return this.DistanceMatrix[i][j];
    }
}
class Stop{
    int Abscissa,Ordinate;
    
    Stop(int x,int y){
        this.Abscissa=x;
        this.Ordinate=y;
    }
    
    double getDistance(Stop stop){
        return Math.sqrt(Math.pow(this.Abscissa-stop.Abscissa,2)+Math.pow(this.Ordinate-stop.Ordinate,2));
    }
}
class TimeWindow{
    int Earliest,Latest;

    public TimeWindow(int Earliest, int Latest) {
        this.Earliest = Earliest;
        this.Latest = Latest;
    }
}