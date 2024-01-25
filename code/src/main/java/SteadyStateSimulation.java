import mathLib.Rngs;
import utils.CSVLibrary;
import utils.Estimate;

import java.io.IOException;
public class SteadyStateSimulation {
    private static int batchSize=1024;
    private static int k=128;
    private static int flag=1;

    /**
     * if the flag variable is set to 0, then we are under steady state simulation; otherwise transient simulation
     * */
    public static void main(String[] args) throws IOException {
        Rngs r = new Rngs();
        Simulator sim;
        Estimate estimate = new Estimate();
        if(flag != 0){
            r.plantSeeds(12986789);
            String[] csvNames = new String[]{"picking.csv","packing.csv","quality.csv","fragile.csv","resistent.csv"};
            for (int j=0; j < 128; j++) {
                sim = new Simulator(batchSize, k, flag, j, r, csvNames);
                sim.main();
            }
            double[] array = CSVLibrary.readCSVFile(0,csvNames[0]);
            estimate.main(array);
            /*for (int j=0; j < 128; j++) {
                System.out.println(array[j]);
            }*/
        }
        else {
            sim = new Simulator(batchSize, k, flag);
            sim.main();
        }


    }
}
