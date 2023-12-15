public class SteadyStateSimulation {
    private static Simulator sim;
    private int batchSize;
    private int k;
    private int i; //to count the number of jobs of batches at runtime
    public SteadyStateSimulation()
    {
        batchSize = 256;
        k = 128;
    }

    public static void main(String[] args) {
         sim = new Simulator(batchSize, k);


    }
}
