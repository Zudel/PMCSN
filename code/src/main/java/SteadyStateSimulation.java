import mathLib.Rngs;
import utils.CSVLibrary;

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
        if(flag != 0){
            r.plantSeeds(12986789);
            String[] csvNames = new String[]{"picking.csv","packing.csv","quality.csv","fragile.csv","resistent.csv"};
            CSVLibrary[] csvStatistics = new CSVLibrary[5];
            for (int j =0;j < 5;j++ ){
                csvStatistics[j] = new CSVLibrary(csvNames[j]);
            }
            for (int j=0; j < k; j++) {
                sim = new Simulator(batchSize, k, flag, j, r, csvStatistics);
                sim.main();
            }
        }
        else {
            sim = new Simulator(batchSize, k, flag);
            sim.main();
        }




    }
}
