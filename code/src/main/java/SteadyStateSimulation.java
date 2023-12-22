import java.io.IOException;

public class SteadyStateSimulation {
    private static Simulator sim;
    private static int batchSize=1024;
    private static   int k=128;
    private int streamIndex; //per le repliche
    private static int flag=0;

    /**
     * if the flag variable is set to 0, then we are under steady state simulation; otherwise transient simulation
     * */

    public static void main(String[] args) throws IOException {

         sim = new Simulator(batchSize, k, flag);
         sim.main();


    }
}
