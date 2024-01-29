import mathLib.Rngs;
import utils.CSVLibrary;
import utils.Estimate;

import java.io.IOException;

import static model.SimulatorParameters.REPLICATION;

public class SwitchSimulation {
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
        if(flag != 0) {
            r.plantSeeds(12986789);
            String[] csvNames = new String[]{"picking.csv", "packing.csv", "quality.csv", "fragile.csv", "resistent.csv"};
            for (int j = 0; j < REPLICATION; j++) {
                sim = new Simulator(batchSize, k, flag, j, r, csvNames);
                sim.main();
            }
            /** stampa le statistiche dello studio di analisi transiente */
            for (int j = 0; j < 5; j++) {
                for (int i = 0; i < 2; i++)
                    estimate.estimateFiniteHorizon(CSVLibrary.readCSVFile(i, csvNames[j]), csvNames[j]);

            }
            for (int i = 0; i < 3; i++) //glob
                estimate.estimateFiniteHorizon(CSVLibrary.readCSVFile(i, "globalStatistics.csv"),"globalStatistics.csv" );
        }
        else {
            /**analisi stazionaria utilizzando il metodo dei batch means*/
            sim = new Simulator(batchSize, k, flag);
            sim.main();
        }
    }
}
