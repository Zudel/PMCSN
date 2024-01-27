import mathLib.Rngs;
import mathLib.Rvms;
import utils.CSVLibrary;
import utils.Estimate;
import utils.plotter;

import javax.swing.*;
import java.io.IOException;
import java.text.DecimalFormat;
import org.apache.commons.statistics.distribution.TruncatedNormalDistribution;
import static model.SimulatorParameters.*;
class Event {                     /* the next-event list    */
    double t;                         /*   next event time      */
    int x;                         /*   event status, 0 or 1 */
}
class Sum {                      /* accumulated sums of                */
    double service;                   /*   service times                    */
    long served;                    /*   number served                    */
}
class T {
    double current;                   /* current time                       */
    double next;                      /* next (most imminent) event time    */
}

/**
 * evento 0: arrivo ordine
 * evento 1: completamento picking [1,40]
 * evento 2: arrivo packing 41
 * evento 3: completamento packing [42, 62]
 * evento arrivo quality 63
 * evento completamento quality [65, 75]
 * evento arrivo sorting fragile (coda prime) 76
 * evento arrivo sorting fragile (coda NON prime) 77
 * evento completamento sorting fragile [78, 88]
 * evento arrivo sorting NON fragile (coda prime)  89
 * evento arrivo sorting NON fragile (coda NON prime)  90
 * evento completamento sorting not fragile [91, 101]
 */

public class Simulator {

    private  double[] responseTimeCounter;
    private  double[] waitingTimerCounter;
    private  double[] utilizationCounter;
    private  double[] numberResponseTimeCounter;
    private  double[] numberWaitingTimeCounter;
    private  int batchSize;
    private  int k;
    private static double service;
    private static int[][] fasciaServer;
    private  int flag;
    private  double pickingResponseTime; //contatori per la media di un batch
    private  double packingResponseTime;
    private  double qualityResponseTime;
    private  double fragileSortingResponseTime;
    private  double notFragileSortingResponseTime;

    private  double sarrival = START;
    private  double[][] responseTimerecords;
    private  double[][] waitingTimerecords;
    private  double[][] numberResponseTimerecords;
    private  double[][] numberWaitingTimerecords;
    private  double[][] utilizationRecords;
    private  double[]arrivalsCounter;
    private  double[][] arrivals;
    private  double[][] domandaMedia;
    private double[] totalResponseTime;
    private double goBackProb;
    private double[] goBackProbRecord;
    private double pSuccess;
    private double stop;
    private  int rep;
    private Rngs r = new Rngs();
    private  static String[] csvNames;
    private static int  fasciaIndex;
    private int z=0;


    //static List<FasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");

    public Simulator(int batchSize, int k, int flag){ //k is batches number
        this.batchSize = batchSize;
        this.k = k;
        this.flag = flag;

        responseTimerecords = new double[5][k]; //ongi riga corrisponde a un centro, ogni colonna corrisponde alla media di un batch
        waitingTimerecords = new double[7][k];
        numberResponseTimerecords = new double[5][k];
        numberWaitingTimerecords = new double[7][k];
        utilizationRecords = new double[5][k];
        arrivals = new double[5][k];
        arrivalsCounter = new double[k];
        responseTimeCounter = new double[5]; //sono i contatori per ogni batch
        waitingTimerCounter = new double[7];
        numberResponseTimeCounter = new double[5];
        numberWaitingTimeCounter = new double[7];
        utilizationCounter = new double[5];
        totalResponseTime = new double[k];
        goBackProbRecord = new double[k];
        domandaMedia = new double[5][k];
        if(SERVERS_PICKING + SERVERS_PACKING <= 20)
            pSuccess = 0.950;
        else
            pSuccess = BERNOULLI_PROB_SUCCESS;
        stop = STOP_BATCH;

    }

    public Simulator(int batchSize, int k, int flag, int j, Rngs r, String[] csvNames) {
        this.batchSize = batchSize;
        //System.out.println("batchSize: "+ batchSize);
        this.k = k;
        //System.out.println("k: "+ this.k);
        this.flag = flag;
        this.rep = j;
        this.r = r;
        this.csvNames = csvNames;
        responseTimerecords = new double[5][k]; //ongi riga corrisponde a un centro, ogni colonna corrisponde alla media di un batch
        waitingTimerecords = new double[7][k];
        numberResponseTimerecords = new double[5][k];
        numberWaitingTimerecords = new double[7][k];
        utilizationRecords = new double[5][k];
        arrivals = new double[5][k];
        arrivalsCounter = new double[k];
        responseTimeCounter = new double[5]; //sono i contatori per ogni batch
        waitingTimerCounter = new double[7];
        numberResponseTimeCounter = new double[5];
        numberWaitingTimeCounter = new double[7];
        utilizationCounter = new double[5];
        totalResponseTime = new double[k];
        goBackProbRecord = new double[k];
        domandaMedia = new double[5][k];
        fasciaServer = new int[3][5];
        fasciaIndex =0;
        if(SERVERS_PICKING + SERVERS_PACKING <= 20)
            pSuccess = 0.950;
        else
            pSuccess = BERNOULLI_PROB_SUCCESS;
        stop = STOP;
    }


    public void main() throws IOException { //start point of the program execution
        double arrivoPacking = 0.0;
        double arrivoQualityControl = 0.0;
        double arrivoFragile =0.0;
        double arrivoResistent =0.0;
        int numberJobsPickingCenter = 0;
        int numberJobsPackingCenter = 0;
        int numberJobsQualityCenter = 0;
        int numberJobsSortingFragileCenter = 0;
        int numberJobsSortingNotFragileCenter = 0;
        int    e;                      /* next event index                   */
        int batchJob=0;                 // numero dell'i-esimo job di un batch, va da 0 a 255
        int batchNumber = 0 ;             // numero k corrente
        boolean cond;
        int    sPickingCenter;                      /* picking index                       */
        int    sPackingCenter;                      /* packing index                       */
        int    sQualityCenter;                      /* quality center index                       */
        int    sSortingFragileOrders;
        int    sSortingNotFragileOrders;
        long iteration =0;
        double areaPickingCenter   = 0.0;           /* time integrated number in the node */
        double areaPackingCenter   = 0.0;           /* time integrated number in the node */
        double areaQualityCenter   = 0.0;           /* time integrated number in the node */
        double areaSortingFragileOrders   = 0.0;           /* time integrated number in the node */
        double areaSortingNotFragileOrders   = 0.0;           /* time integrated number in the node */
        double areaSortingFragilePrimeOrders =0;
        double areaSortingFragileNotPrimeOrders =0;
        double areaSortingNotFragilePrimeOrders=0;
        double areaSortingNotFragileNotPrimeOrders=0;

        double areaQueuePickingCenter =0.0;
        double areaQueueQualityCenter =0.0;
        double areaQueueNotFragilePrimeOrders =0.0;
        double areaQueueFragilePrimeOrders =0.0;
        double areaQueueFragileNotPrimeOrders =0.0;
        double areaQueueNotFragileNotPrimeOrders =0.0;
        double areaQueuePackingCenter =0.0;

        long indexPickingCenter   = 0;             /* used to count departed jobs         */
        long indexPackingCenter   = 0;             /* used to count departed jobs         */
        long indexQualityCenter   = 0;             /* used to count departed jobs         */
        long indexSortingFragileOrders   = 0;             /* used to count departed jobs         */
        long indexSortingNotFragileOrders   = 0;             /* used to count departed jobs         */
        long numberFeedbackIsTrue = 0;                 /*numero di job che non passano il test nel qualty control*/
        long numberPrimeJobsSortingFragileCenter = 0;
        long numberNotPrimeJobsSortingFragileCenter =0;
        long numberPrimeJobsSortingNotFragileCenter =0;
        long numberNotPrimeJobsSortingNotFragileCenter =0;
        long indexSortingFragilePrimeOrders =0;
        long indexSortingFragileNotPrimeOrders = 0;
        long indexSortingNotFragilePrimeOrders=0;
        long indexSortingNotFragileNotPrimeOrders=0;

        //Simulator sim = new Simulator(batchSize, k, flag);

        Estimate estimate = new Estimate();
        plotter[] grafico = new plotter[5];
        String[]  centerNames = new String[]{"picking", "packing", "quality", "resistent order", "fragile order"};
        String[]  centerNamesQueue = new String[]{"picking queue", "packing queue", "quality queue", "resistent order Prime queue", "resistent order Not Prime queue", "fragile order Prime queue","fragile order not Prime queue" };
        double[] serviceTimeCenter = new double[]{SERVICE_TIME_PICKING, SERVICE_TIME_PACKING, SERVICE_TIME_QUALITY, SERVICE_TIME_SORTING_FRAGILE_ORDERS, SERVICE_TIME_SORTING_NOT_FRAGILE_ORDERS};
        T t = new T();
        Event[] event = new Event[EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1]; //considera che parte da zero! quindi è indicizzabile fino a dim -1!!!
        Sum[] sum = new Sum[EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1];
        for (int s = 0; s < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1; s++) {
            event[s] = new Event();
            sum [s]  = new Sum();
        }

        r.putSeed(123456789);
        //inizializzazione array eventi e sum
        t.current    = START;
        event[0].t   = getArrival(r,  1+k,flag,t.current); //first arrival to the server node
        event[0].x   = 1;
        numberJobsPickingCenter++;

        for (int i = 1; i < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1; i++) {
            event[i].t     = START;          /* this value is arbitrary because */
            event[i].x     = 0;              /* all servers are initially idle  */
            sum[i].service = 0.0;
            sum[i].served  = 0;
        }

        if(flag == 0) {
            if (event[0].x != 0)
                cond = true;
            else
                cond = false;
        }
        else {
            if (event[0].x != 0 || (numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsSortingFragileCenter + numberJobsSortingNotFragileCenter) > 0)
                cond = true;
            else
                cond = false;
        }
        // INIZIO SIMULAZIONE //
        while (cond) {
            e         = nextEvent(event);
            t.next    = event[e].t;                                              //next event time
            areaPickingCenter     += (t.next - t.current) * numberJobsPickingCenter;     //update integral of picking center
            areaPackingCenter     += (t.next - t.current) * numberJobsPackingCenter;     //update integral of packing center
            areaQualityCenter     += (t.next - t.current) * numberJobsQualityCenter;     //update integral of quality center
            areaSortingFragileOrders     += (t.next - t.current) * numberJobsSortingFragileCenter;     //update integral of quality center
            areaSortingNotFragileOrders     += (t.next - t.current) * numberJobsSortingNotFragileCenter;     //update integral of quality center

            areaSortingFragilePrimeOrders += (t.next - t.current) * numberPrimeJobsSortingFragileCenter;
            areaSortingFragileNotPrimeOrders += (t.next - t.current) * numberNotPrimeJobsSortingFragileCenter;
            areaSortingNotFragilePrimeOrders += (t.next - t.current) * numberPrimeJobsSortingNotFragileCenter;
            areaSortingNotFragileNotPrimeOrders += (t.next - t.current) * numberNotPrimeJobsSortingNotFragileCenter;

            if(numberJobsPickingCenter > SERVERS_PICKING)
                areaQueuePickingCenter += (t.next - t.current) * (numberJobsPickingCenter - SERVERS_PICKING);
            if(numberJobsPackingCenter > SERVERS_PACKING)
                areaQueuePackingCenter += (t.next - t.current) * (numberJobsPackingCenter - SERVERS_PICKING);

            if(numberPrimeJobsSortingFragileCenter > SERVERS_SORTING_FRAGILE_ORDERS)
                areaQueueFragilePrimeOrders += (t.next - t.current) * (numberPrimeJobsSortingFragileCenter );
            if(numberNotPrimeJobsSortingFragileCenter > SERVERS_SORTING_FRAGILE_ORDERS)
                areaQueueFragileNotPrimeOrders += (t.next - t.current) * (numberNotPrimeJobsSortingFragileCenter );

            if(numberNotPrimeJobsSortingNotFragileCenter > SERVERS_SORTING_NOT_FRAGILE_ORDERS)
                areaQueueNotFragileNotPrimeOrders += (t.next - t.current) * (numberNotPrimeJobsSortingNotFragileCenter );
            if(numberPrimeJobsSortingNotFragileCenter > SERVERS_SORTING_NOT_FRAGILE_ORDERS)
                 areaQueueNotFragilePrimeOrders += (t.next - t.current) * (numberPrimeJobsSortingNotFragileCenter );

            t.current = t.next;                                                 //advance the simulation clock
            iteration++;
            if(t.current <= STOP_BATCH)
                batchNumber = (int) t.current / batchSize;
            batchJob++;
            if (e == EVENT_ARRIVAL_PICKING) { //arrivo al picking center
                numberJobsPickingCenter++;
                event[0].t = getArrival(r, 1+rep,flag, t.current);
                arrivalsCounter[0] += (sarrival - t.current);

                if (event[0].t > stop)
                    event[0].x = 0; //close the door

                if (numberJobsPickingCenter <= SERVERS_PICKING) {
                    service = getServiceMultiServer(r, 2+rep, PICKING);
                    sPickingCenter = findOne(event, PICKING);
                    sum[sPickingCenter].service += service;
                    sum[sPickingCenter].served++;
                    event[sPickingCenter].t = t.current + service;
                    event[sPickingCenter].x = 1;
                }
            }
            else if((e > EVENT_ARRIVAL_PICKING) && (e <= EVENT_DEPARTURE_PICKING)){ //partenza dal picking center
                indexPickingCenter++;
                numberJobsPickingCenter--;

                sPickingCenter = e;
                if (numberJobsPickingCenter > SERVERS_PICKING) {
                    service = getServiceMultiServer(r, 2+rep, PICKING);
                    sum[sPickingCenter].service += service;
                    sum[sPickingCenter].served++;
                    event[sPickingCenter].t = t.current + service;
                    event[sPickingCenter].x = 1;

                }
                else
                    event[sPickingCenter].x = 0; //se non ci sono più job nel picking center, setto l'evento di partenza a zero

                /* ogni partenza è un arrivo, quindi */
                event[EVENT_ARRIVAL_PACKING].x =1;
                event[EVENT_ARRIVAL_PACKING].t = event[sPickingCenter].t;
                arrivalsCounter[1] += Math.abs(t.current - arrivoPacking);
                arrivoPacking = t.current;


            }

            else if(e == EVENT_ARRIVAL_PACKING) { //arrivo al packing center

                event[e].x = 0;
                numberJobsPackingCenter++;
                if (numberJobsPackingCenter <= SERVERS_PACKING){
                    service = getServiceMultiServer(r, 4+rep,PACKING);
                    sPackingCenter = findOne(event, PACKING);
                    sum[sPackingCenter].service += service;
                    sum[sPackingCenter].served++;
                    event[sPackingCenter].t = t.current + service;
                    event[sPackingCenter].x = 1;
                }
            }
            else if( (e > EVENT_ARRIVAL_PACKING ) && (e <= EVENT_DEPARTURE_PACKING) ) {  //partenza dal packing center
                indexPackingCenter++;
                numberJobsPackingCenter--;
                double random;
                random = r.random();
                //System.out.println("random in quality center" + random );
                /*genero una partenza */
                sPackingCenter = e;
                if (numberJobsPackingCenter > SERVERS_PACKING) {
                    service = getServiceMultiServer(r,4+rep, PACKING);
                    sum[sPackingCenter].service += service;
                    sum[sPackingCenter].served++;
                    event[sPackingCenter].t = t.current + service;
                    event[sPackingCenter].x = 1; //setto l'evento di partenza dal picking center a zero, non è più attivo perche ora è attivo quello di arrivo al packing center di arriv
                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sPackingCenter].x = 0;
                }

                if(random <= QUALITY_CENTER_PROB) { //34% di probabilità di andare al quality center
                        event[EVENT_ARRIVAL_QUALITY].x = 1;
                        event[EVENT_ARRIVAL_QUALITY].t = event[sPackingCenter].t;


                }
                else{ //66% di probabilità di andare al sorting center fragile oppure non fragile
                    random = r.random();
                    if (random <= SORTING_FRAGILE_CENTER) {  //qui decido se andare al sorting center fragile o not fragile (se la condizione è verficata vado al fragile)
                        //numberJobsSortingFragileCenter++;

                        random = r.random();
                        if (random <= ORDER_PRIME) { //qui decido in quale coda del sorting center fragile andare (prime o not prime)
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS].t = event[sPackingCenter].t;
                            numberPrimeJobsSortingFragileCenter++;
                        }
                        else{
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS].t = event[sPackingCenter].t;
                            numberNotPrimeJobsSortingFragileCenter++;
                        }
                    }
                    else { //qui vado al centro di smistamento resistenti
                        //numberJobsSortingNotFragileCenter++;

                        random = r.random();
                        if (random <=ORDER_PRIME){ //qui decido in quale coda del sorting center NON fragile andare (prime o not prime)
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS].t = event[sPackingCenter].t;
                            numberPrimeJobsSortingNotFragileCenter++;
                        }
                        else {
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS].t = event[sPackingCenter].t;
                            numberNotPrimeJobsSortingNotFragileCenter++;
                        }
                    }
                }

            }
            else if( e == EVENT_ARRIVAL_QUALITY ){ //arrivo al quality center
                arrivalsCounter[2]  += Math.abs(t.current - arrivoQualityControl ) ;
                arrivoQualityControl = t.current;
                event[e].x = 0;
                numberJobsQualityCenter++;
                if (numberJobsQualityCenter <= SERVERS_QUALITY){
                    service = getServiceMultiServer(r, 6+rep,QUALITY);
                    sQualityCenter = findOne(event, QUALITY);
                    sum[sQualityCenter].service += service;
                    sum[sQualityCenter].served++;
                    event[sQualityCenter].t = t.current + service;
                    event[sQualityCenter].x = 1;
                }
            }
            else if((e > EVENT_ARRIVAL_QUALITY) && (e <= EVENT_DEPARTURE_QUALITY)){ //partenza dal quality center
                indexQualityCenter++;
                numberJobsQualityCenter--;
                double random = r.random();

                sQualityCenter = e;
                if (numberJobsQualityCenter > SERVERS_QUALITY) {
                    service = getServiceMultiServer(r, 6+rep, QUALITY);

                    sum[sQualityCenter].service += service;
                    sum[sQualityCenter].served++;
                    event[sQualityCenter].t = t.current + service;
                    event[sQualityCenter].x = 1;
                } else {
                    event[sQualityCenter].x = 0;
                }

                if(idfBernoulli(BERNOULLI_PROB_SUCCESS, r.random()) == 0) { //probabilità di andare al picking center (TEST FALLITO)
                    numberFeedbackIsTrue++; //conto il numero di job che non passano il test
                    event[EVENT_ARRIVAL_PICKING].x = 1;
                    event[EVENT_ARRIVAL_PICKING].t = event[sQualityCenter].t;

                }
                else{ //probabilità di andare al sorting center fragile oppure non fragile (TEST SUPERATO)
                    random = r.random();

                    if (random <= SORTING_FRAGILE_CENTER) {  //qui decido se andare al sorting center fragile o not fragile (se la condizione è verficata vado al fragile)
                        //numberJobsSortingFragileCenter++;
                        random = r.random();
                        if (random <= ORDER_PRIME) { //qui decido in quale coda del sorting center fragile andare (prime o not prime)
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS].t = event[sQualityCenter].t;
                            //numberPrimeJobsSortingFragileCenter++;


                        }
                        else {
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS].t = event[sQualityCenter].t;
                            //numberNotPrimeJobsSortingFragileCenter++;
                        }
                    }
                    else {
                        random = r.random();

                        //numberJobsSortingNotFragileCenter++;
                        if (random <= ORDER_PRIME) { //qui decido in quale coda del sorting center NON fragile andare (prime o not prime)
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS].t = event[sQualityCenter].t;
                            //numberPrimeJobsSortingNotFragileCenter++;
                        }
                        else{
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS].t = event[sQualityCenter].t;
                            //numberNotPrimeJobsSortingNotFragileCenter++;
                        }
                    }
                }
            }
            else if(e == EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS || e == EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS){ //arrivi al centro smistamento ordini fragili
                arrivalsCounter[3]  += Math.abs(arrivoFragile - t.current) ;
                arrivoFragile = t.current;
                event[e].x =0;
                numberJobsSortingFragileCenter++;
                if (numberJobsSortingFragileCenter <= SERVERS_SORTING_FRAGILE_ORDERS) {
                    service = getServiceMultiServer(r, 9+rep, SORTING_FRAGILE_ORDERS);
                    sSortingFragileOrders =findOne(event, SORTING_FRAGILE_ORDERS);
                    sum[sSortingFragileOrders].service += service;
                    sum[sSortingFragileOrders].served++;
                    event[sSortingFragileOrders].t = t.current + service;
                    event[sSortingFragileOrders].x = 1;
                }
            }
            else if ((e > EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS) && ( e <= EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS)){ //partenze dal centro smistamento ordini fragili
                indexSortingFragileOrders++;
                numberJobsSortingFragileCenter--;
                if( numberPrimeJobsSortingFragileCenter > 0) {
                    indexSortingFragilePrimeOrders++;
                    numberPrimeJobsSortingFragileCenter--;
                }
                else {
                    indexSortingFragileNotPrimeOrders++;
                    numberNotPrimeJobsSortingFragileCenter--;
                }
                sSortingFragileOrders = e;
                if(numberJobsSortingFragileCenter > SERVERS_SORTING_FRAGILE_ORDERS){
                    service = getServiceMultiServer(r, 9+rep, SORTING_FRAGILE_ORDERS);
                    sum[sSortingFragileOrders].service += service;
                    sum[sSortingFragileOrders].served++;
                    event[sSortingFragileOrders].t = t.current + service;
                    event[sSortingFragileOrders].x = 1;
                }
                else
                    event[sSortingFragileOrders].x = 0; //se non ci sono più job nel picking center, setto l'evento di partenza a zero

            }
            else if( e == EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS || e == EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS){ //arrivi al centro di smistamento ordini resistenti
                arrivalsCounter[4]  += Math.abs(arrivoResistent - t.current) ;
                arrivoResistent = t.current;
                event[e].x = 0;
                numberJobsSortingNotFragileCenter++;
                if(numberJobsSortingNotFragileCenter <= SERVERS_SORTING_NOT_FRAGILE_ORDERS){
                    service = getServiceMultiServer(r, 11+rep, SORTING_NOT_FRAGILE_ORDERS);
                    sSortingNotFragileOrders = findOne(event, SORTING_NOT_FRAGILE_ORDERS);
                    sum[sSortingNotFragileOrders].service += service;
                    sum[sSortingNotFragileOrders].served++;
                    event[sSortingNotFragileOrders].t = t.current + service;
                    event[sSortingNotFragileOrders].x = 1;
                }
            }
            else if((e > EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS) && ( e <= EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS)){ //partenze dal centro di smistamento ordini resistenti
                indexSortingNotFragileOrders++;
                numberJobsSortingNotFragileCenter--;
                if( numberPrimeJobsSortingNotFragileCenter> 0){
                    numberPrimeJobsSortingNotFragileCenter--;
                    indexSortingNotFragilePrimeOrders++;
                }
                else {
                    numberNotPrimeJobsSortingNotFragileCenter--;
                    indexSortingNotFragileNotPrimeOrders++;
                }
                sSortingNotFragileOrders = e;
                if (numberJobsSortingNotFragileCenter > SERVERS_SORTING_NOT_FRAGILE_ORDERS) {
                    service = getServiceMultiServer(r, 11+rep, SORTING_NOT_FRAGILE_ORDERS);
                    sum[sSortingNotFragileOrders].service += service;
                    sum[sSortingNotFragileOrders].served++;
                    event[sSortingNotFragileOrders].t = t.current + service;
                    event[sSortingNotFragileOrders].x = 1;
                }
                else
                    event[sSortingNotFragileOrders].x = 0; //se non ci sono più job nel picking center, setto l'evento di partenza a zero
            }
            /*System.out.println("numberJobsPickingCenter: " + numberJobsPickingCenter);
            System.out.println("partenze dal picking center: " + indexPickingCenter);
            System.out.println("numberJobsPackingCenter: " + numberJobsPackingCenter);
            System.out.println("partenze dal packing center: " + indexPackingCenter);
            System.out.println("numberJobsQualityCenter: " + numberJobsQualityCenter);
            System.out.println("partenze dal quality center: " + indexQualityCenter);
            System.out.println("numero di job nel centro di smistamento ordini fragili: " + numberJobsSortingFragileCenter);
            System.out.println("partenze nel centro di smistamento ordini fragili: " + indexSortingFragileOrders);
            System.out.println("numero di job nel centro di smistamento ordini NON fragili: " + numberJobsSortingNotFragileCenter);
            System.out.println("partenze nel centro di smistamento ordini NON fragili: " + indexSortingNotFragileOrders);*/
            if(flag == 0 ) {
                if (indexPickingCenter != 0) {
                    responseTimeCounter[0] += areaPickingCenter / indexPickingCenter;
                    waitingTimerCounter[0] += areaQueuePickingCenter / indexPickingCenter;
                    numberResponseTimeCounter[0] += areaPickingCenter / t.current;
                    numberWaitingTimeCounter[0] += areaQueuePickingCenter / t.current;
                    for (int s = EVENT_ARRIVAL_PICKING + 1; s <= EVENT_DEPARTURE_PICKING; s++) //calcolo l'utilizzazione del centro facendo la media
                        utilizationCounter[0] += sum[s].service;
                    utilizationCounter[0] = utilizationCounter[0] / SERVERS_PICKING;
                    utilizationCounter[0] = (utilizationCounter[0]) / t.current;
                }
                if (indexPackingCenter != 0) {
                    responseTimeCounter[1] += areaPackingCenter / indexPackingCenter;
                    waitingTimerCounter[1] += areaQueuePackingCenter / indexPackingCenter;
                    numberResponseTimeCounter[1] += areaPackingCenter / t.current;
                    numberWaitingTimeCounter[1] += areaQueuePackingCenter / t.current;
                    for (int s = EVENT_ARRIVAL_PACKING + 1; s <= EVENT_DEPARTURE_PACKING; s++) //calcolo l'utilizzazione del centro facendo la media
                        utilizationCounter[1] += sum[s].service;
                    utilizationCounter[1] = utilizationCounter[1] / SERVERS_PACKING;
                    utilizationCounter[1] = (utilizationCounter[1]) / t.current;
                }
                if (indexQualityCenter != 0) {
                    responseTimeCounter[2] += areaQualityCenter / indexQualityCenter;
                    waitingTimerCounter[2] += areaQueueQualityCenter / indexQualityCenter;
                    numberResponseTimeCounter[2] += areaQualityCenter / t.current;
                    numberWaitingTimeCounter[2] += areaQueueQualityCenter / t.current;
                    for (int s = EVENT_ARRIVAL_QUALITY + 1; s <= EVENT_DEPARTURE_QUALITY; s++) //calcolo l'utilizzazione del centro facendo la media
                        utilizationCounter[2] += sum[s].service;
                    utilizationCounter[2] = utilizationCounter[2] / SERVERS_QUALITY;
                    utilizationCounter[2] = (utilizationCounter[2]) / t.current;
                    goBackProb += ((double) numberFeedbackIsTrue / (double) indexQualityCenter);

                }
                if (indexSortingFragileOrders != 0) {
                    responseTimeCounter[3] += areaSortingFragileOrders / indexSortingFragileOrders;
                    numberResponseTimeCounter[3] += areaSortingFragileOrders / t.current;
                    waitingTimerCounter[3] += areaQueueFragilePrimeOrders / indexSortingFragileOrders;
                    waitingTimerCounter[4] += areaQueueFragileNotPrimeOrders / indexSortingFragileOrders;
                    numberWaitingTimeCounter[3] += areaQueueFragilePrimeOrders / t.current;
                    numberWaitingTimeCounter[4] += areaQueueFragileNotPrimeOrders / t.current;
                    for (int s = EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                        utilizationCounter[3] += sum[s].service;
                    utilizationCounter[3] = utilizationCounter[3] / SERVERS_SORTING_FRAGILE_ORDERS;
                    utilizationCounter[3] = (utilizationCounter[3]) / t.current;
                }
                if (indexSortingNotFragileOrders != 0) {
                    responseTimeCounter[4] += areaSortingNotFragileOrders / indexSortingNotFragileOrders;
                    numberResponseTimeCounter[4] += areaSortingNotFragileOrders / t.current;
                    waitingTimerCounter[5] += areaQueueNotFragilePrimeOrders / indexSortingNotFragileOrders;
                    waitingTimerCounter[6] += areaQueueNotFragileNotPrimeOrders / indexSortingNotFragileOrders;
                    numberWaitingTimeCounter[5] += areaQueueNotFragilePrimeOrders / t.current;
                    numberWaitingTimeCounter[6] += areaQueueNotFragileNotPrimeOrders / t.current;
                    for (int s = EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                        utilizationCounter[4] += sum[s].service;
                    utilizationCounter[4] = utilizationCounter[4] / SERVERS_SORTING_NOT_FRAGILE_ORDERS;
                    utilizationCounter[4] = (utilizationCounter[4]) / t.current;
                }

                if (batchJob == batchSize - 1) {
                    goBackProbRecord[batchNumber] = goBackProb / (double) batchSize;

                    for (int j = 0; j < 5; j++) {
                        //LOCALI
                        responseTimerecords[j][batchNumber] = responseTimeCounter[j] / batchSize; //(E(Ts))
                        numberResponseTimerecords[j][batchNumber] = numberResponseTimeCounter[j] / batchSize; //(E(Ns))
                        utilizationRecords[j][batchNumber] = utilizationCounter[j] / batchSize;
                        arrivals[j][batchNumber] = arrivalsCounter[j] / batchSize;
                        //globali
                        domandaMedia[j][batchNumber] = (arrivalsCounter[j] / batchSize) * ARRIVAL_L * serviceTimeCenter[j];
                        totalResponseTime[batchNumber] += responseTimerecords[j][batchNumber]; //calcolo i


                    }
                    for (int j = 0; j < 7; j++) {
                        waitingTimerecords[j][batchNumber] = waitingTimerCounter[j] / batchSize; //(E(Tq))
                        numberWaitingTimerecords[j][batchNumber] = numberWaitingTimeCounter[j] / batchSize;
                    }

                    for (int j = 0; j < 5; j++) {
                        responseTimeCounter[j] = 0; //(E(Ts))
                        numberResponseTimeCounter[j] = 0; //(E(Ns))
                        utilizationCounter[j] = 0;
                        arrivalsCounter[j] = 0;
                    }
                    for (int j = 0; j < 7; j++) {
                        waitingTimerCounter[j] = 0; //(E(Tq))
                        numberWaitingTimeCounter[j] = 0;
                    }
                    goBackProb = 0.0;
                    batchJob = 0; //passo al prossimo batch e salvo le statistiche nel batch associato
                }
            }
            if(flag == 0) {
                if (event[0].x != 0)
                    cond = true;
                else
                    cond = false;
            }
            else {
                if (event[0].x != 0 || (numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsSortingFragileCenter + numberJobsSortingNotFragileCenter) > 0)
                    cond = true;
                else
                    cond = false;
                //raccolgo i dati
                if(iteration % 2 ==0){
                    totalResponseTime[batchNumber] = areaPackingCenter/ indexPackingCenter + areaPickingCenter / indexPickingCenter + areaQualityCenter / indexQualityCenter + areaSortingFragileOrders / indexSortingFragileOrders;
                }
            }

        } //end while

        //System.out.println("----------------------------------------------------");
        if(flag == 0) {
            for (int j = 0; j < 5; j++) {
                System.out.println("\n");
                System.out.println(centerNames[j]);
                System.out.println("E(Ts)");
                estimate.main(responseTimerecords[j]);
                System.out.println("E(Ns)");
                estimate.main(numberResponseTimerecords[j]);
                System.out.println("E(utilizzazione)");
                estimate.main(utilizationRecords[j]);
            }
        /*for (int j =0; j < 7; j++){
            System.out.println("\n");
            System.out.println(centerNamesQueue[j]);
            System.out.println("E(Tq)");
            estimate.main(waitingTimerecords[j]);
            System.out.println("E(Nq)");
            estimate.main(numberWaitingTimerecords[j]);
        }*/

            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < 5; i++) {
                    grafico[i] = new plotter(centerNames[i], "tempo di riposta", responseTimerecords, this.k, this.batchSize, i);
                    grafico[i].setSize(800, 600);
                    grafico[i].setLocationRelativeTo(null);
                    grafico[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    grafico[i].setVisible(true);
                }
            });

        /*SwingUtilities.invokeLater(() -> {
                plotter graficoRespTimeTot = new plotter("sistema","tempo di risposta totale", totalResponseTime, this.k, this.batchSize);
            graficoRespTimeTot.setSize(800, 600);
            graficoRespTimeTot.setLocationRelativeTo(null);
            graficoRespTimeTot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            graficoRespTimeTot.setVisible(true);

        });*/
            SwingUtilities.invokeLater(() -> {
                plotter graficoGoBackProb = new plotter("", "probabilità ordine difettoso", goBackProbRecord, this.k, this.batchSize);
                graficoGoBackProb.setSize(800, 600);
                graficoGoBackProb.setLocationRelativeTo(null);
                graficoGoBackProb.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                graficoGoBackProb.setVisible(true);
            });
        }
        //stampo i risultati
        /*System.out.println("numero di job nel centro di picking: " + numberJobsPickingCenter);
        System.out.println("partenze dal centro di picking: " + indexPickingCenter);
        System.out.println("numero di job nel centro di packing: " + numberJobsPackingCenter);
        System.out.println("partenze dal centro di packing: " + indexPackingCenter);
        System.out.println("numero di job nel centro di qualità: " + numberJobsQualityCenter);
        System.out.println("partenze dal centro di qualità: " + indexQualityCenter);
        System.out.println("numero di job nel centro di smistamento ordini fragili: " + numberJobsSortingFragileCenter);
        //System.out.println("partenze nel centro di smistamento ordini fragili: " + indexSortingFragileOrders);
        System.out.println("partenze di tipo PRIME nel centro di smistamento ordini fragili: " + indexSortingFragilePrimeOrders);
        System.out.println("partenze di tipo NON PRIME nel centro di smistamento ordini fragili: " + indexSortingFragileNotPrimeOrders);
        //System.out.println("numero di job nel centro di smistamento ordini NON fragili: " + numberJobsSortingNotFragileCenter);
        System.out.println("partenze nel centro di smistamento ordini NON fragili: " + indexSortingNotFragileOrders);
        System.out.println("partenze di tipo PRIME nel centro di smistamento ordini NON fragili: " + indexSortingNotFragilePrimeOrders);
        System.out.println("partenze di tipo NON PRIME nel centro di smistamento ordini NON fragili: " + indexSortingNotFragileNotPrimeOrders);
        System.out.println("numeri di job non passati: " +numberFeedbackIsTrue);
        System.out.println("PERCENTUALE DI JOB NON PASSATI: " +f.format((double ) 100*(numberFeedbackIsTrue/indexQualityCenter)));*/
        //System.out.println("----------------------------------------------------");
        if(flag !=0){
            /*SwingUtilities.invokeLater(() -> {
                plotter graficoRespTimeTot = new plotter("sistema","tempo di risposta totale", totalResponseTime, this.k, this.batchSize);
                graficoRespTimeTot.setSize(800, 600);
                graficoRespTimeTot.setLocationRelativeTo(null);
                //graficoRespTimeTot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                graficoRespTimeTot.setVisible(true);
            });

            SwingUtilities.invokeLater(() -> {
                plotter graficoGoBackProb = new plotter("", "probabilità ordine difettoso", goBackProbRecord, this.k, this.batchSize);
                graficoGoBackProb.setSize(800, 600);
                graficoGoBackProb.setLocationRelativeTo(null);
                graficoGoBackProb.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                graficoGoBackProb.setVisible(true);
            });
            */
            CSVLibrary.writeToCsv(totalResponseTime, "totalResponseTime"+z+".csv");
            double[] pickingStatistics = new double[3];
            double[] packingStatistics = new double[3];
            double[] qualityStatistics = new double[3];
            double[] fragileStatistics = new double[3];
            double[] resistentStatistics = new double[3];
            pickingResponseTime = areaPickingCenter / indexPickingCenter;
            packingResponseTime = areaPackingCenter / indexPackingCenter;
            qualityResponseTime = areaQualityCenter / indexQualityCenter;
            fragileSortingResponseTime = areaSortingFragileOrders / indexSortingFragileOrders;
            notFragileSortingResponseTime = areaSortingNotFragileOrders / indexSortingNotFragileOrders;
            //totalResponseTime[0] = pickingResponseTime + packingResponseTime + qualityResponseTime + fragileSortingResponseTime ; //con fragile
            //totalResponseTime[1] = pickingResponseTime + packingResponseTime + qualityResponseTime+ notFragileSortingResponseTime;//senza fragile
            for (int s = EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                utilizationCounter[4] += sum[s].service;
            utilizationCounter[4] = utilizationCounter[4] / SERVERS_SORTING_NOT_FRAGILE_ORDERS;
            utilizationCounter[4] = (utilizationCounter[4]) / t.current;
            for (int s = EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                utilizationCounter[3] += sum[s].service;
            utilizationCounter[3] = utilizationCounter[3] / SERVERS_SORTING_FRAGILE_ORDERS;
            utilizationCounter[3] = (utilizationCounter[3]) / t.current;
            for (int s = EVENT_ARRIVAL_QUALITY + 1; s <= EVENT_DEPARTURE_QUALITY; s++) //calcolo l'utilizzazione del centro facendo la media
                utilizationCounter[2] += sum[s].service;
            utilizationCounter[2] = utilizationCounter[2] / SERVERS_QUALITY;
            utilizationCounter[2] = (utilizationCounter[2]) / t.current;
            for (int s = EVENT_ARRIVAL_PACKING + 1; s <= EVENT_DEPARTURE_PACKING; s++) //calcolo l'utilizzazione del centro facendo la media
                utilizationCounter[1] += sum[s].service;
            utilizationCounter[1] = utilizationCounter[1] / SERVERS_PACKING;
            utilizationCounter[1] = (utilizationCounter[1]) / t.current;
            for (int s = EVENT_ARRIVAL_PICKING + 1; s <= EVENT_DEPARTURE_PICKING; s++) //calcolo l'utilizzazione del centro facendo la media
                utilizationCounter[0] += sum[s].service;
            utilizationCounter[0] = utilizationCounter[0] / SERVERS_PICKING;
            utilizationCounter[0] = (utilizationCounter[0]) / t.current;

            pickingStatistics[0] = pickingResponseTime;
            packingStatistics[0] = packingResponseTime;
            qualityStatistics[0] = qualityResponseTime;
            fragileStatistics[0] = fragileSortingResponseTime;
            resistentStatistics[0] = notFragileSortingResponseTime;
            pickingStatistics[2] = utilizationCounter[0];
            packingStatistics[2] = utilizationCounter[1];
            qualityStatistics[2] = utilizationCounter[2];
            fragileStatistics[2] = utilizationCounter[3];
            resistentStatistics[2] = utilizationCounter[4];

            //nel nodo
        /* System.out.println("TEMPI E QUANTITA NEL NODO");
        System.out.println("\nfor " + indexPickingCenter + " jobs the service node PICKING's statistics are:\n");
        //System.out.println("  avg interarrivals .. =   " + f.format(event[0].t / indexPickingCenter)); //E(interrarrivo)
        System.out.println("  avg wait (E(TS)) ... =   " + f.format(pickingResponseTime));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaPickingCenter / t.current)); //E(Ns)
        System.out.println("  utilization..... =   " + f.format(utilizationCounter[0]));

        System.out.println("\nfor " + indexPackingCenter + " jobs the service node PACKING's statistics are:\n");
        System.out.println("  avg wait ........... =   " + f.format(packingResponseTime));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaPackingCenter / t.current)); //E(Ns)
        System.out.println("  utilization..... =   " + f.format(utilizationCounter[1]));

        System.out.println("\nfor " + indexQualityCenter + " jobs the service node QUALITY CENTER's statistics are:\n");
        System.out.println("  avg wait ........... =   " + f.format(qualityResponseTime));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaQualityCenter / t.current)); //E(Ns)
            System.out.println("  utilization..... =   " + f.format(utilizationCounter[2]));

        System.out.println("\nfor " + indexSortingFragileOrders + " jobs the service node SORTING FRAGILE ORDERS's statistics are:\n");
        System.out.println("  avg wait ........... =   " + f.format(fragileSortingResponseTime));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaSortingFragileOrders / t.current)); //E(Ns)
            System.out.println("  utilization..... =   " + f.format(utilizationCounter[3]));

        System.out.println("\nfor " + indexSortingNotFragileOrders + " jobs the service node SORTING RESISTENT ORDERS's statistics are:\n");
        System.out.println("  avg wait ........... =   " + f.format(notFragileSortingResponseTime));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaSortingNotFragileOrders / t.current)); //E(Ns)
        System.out.println("  utilization..... =   " + f.format(utilizationCounter[4]));

        System.out.println("\nTEMPI E QUANTITA NEL SISTEMA");
        System.out.println("  avg wait ........... =   " + f.format(totalResponseTime[0]));*/  //E(Ts)

            for (int s = EVENT_ARRIVAL_PICKING + 1; s <= EVENT_DEPARTURE_PICKING; s++) //calcolo l'utilizzazione del centro facendo la media
                areaPickingCenter -= sum[s].service;
            for (int s = EVENT_ARRIVAL_PACKING + 1; s <= EVENT_DEPARTURE_PACKING; s++) //calcolo l'utilizzazione del centro facendo la media
                areaPackingCenter -= sum[s].service;
            for (int s = EVENT_ARRIVAL_QUALITY + 1; s <= EVENT_DEPARTURE_QUALITY; s++) //calcolo l'utilizzazione del centro facendo la media
                areaQualityCenter -= sum[s].service;
            for (int s = EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                areaSortingFragileOrders -= sum[s].service;
            for (int s = EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                areaSortingNotFragileOrders -= sum[s].service;
            pickingStatistics[1] = areaPickingCenter / indexPickingCenter;
            packingStatistics[1] = areaPackingCenter / indexPackingCenter;
            qualityStatistics[1] = areaQualityCenter / indexQualityCenter;
            fragileStatistics[1] = areaSortingFragileOrders / indexSortingFragileOrders;
            resistentStatistics[1] = areaSortingNotFragileOrders / indexSortingNotFragileOrders;

            CSVLibrary.writeToCsv(pickingStatistics,csvNames[0]);
            CSVLibrary.writeToCsv(packingStatistics,csvNames[1]);
            CSVLibrary.writeToCsv(qualityStatistics,csvNames[2]);
            CSVLibrary.writeToCsv(fragileStatistics,csvNames[3]);
            CSVLibrary.writeToCsv(resistentStatistics,csvNames[4]);

            //nella coda
            /*System.out.println("\nTEMPI E QUANTITA NELLA CODA");
            System.out.println("PICKING CENTER");
            System.out.println("  E(Tq) .......... =   " + f.format(areaPickingCenter / indexPickingCenter));
            System.out.println("  E(Nq) ..... =   " + f.format(areaPickingCenter / t.current));

            System.out.println("\nPACKING CENTER");
            System.out.println("  E(Tq) .......... =   " + f.format(areaPackingCenter / indexPackingCenter));
            System.out.println("  E(Nq) ..... =   " + f.format(areaPackingCenter / t.current));

            System.out.println("\nQUALITY CENTER");
            System.out.println("  E(Tq) .......... =   " + f.format(areaQualityCenter / indexQualityCenter));
            System.out.println("  E(Nq) ..... =   " + f.format(areaQualityCenter / t.current));

            System.out.println("\nFRAGILE ORDERS CENTER");
            System.out.println("  E(Tq) .......... =   " + f.format(areaSortingFragileOrders / indexSortingFragileOrders));
            System.out.println("  E(Nq) ..... =   " + f.format(areaSortingFragileOrders / t.current));
            /*System.out.println("Prime queue");
            System.out.println("  E(Tq) .......... =   " + f.format(areaSortingFragilePrimeOrders / indexSortingFragilePrimeOrders));
            System.out.println("  E(Nq) ..... =   " + f.format(areaSortingFragilePrimeOrders / t.current));
            System.out.println("Not Prime queue");
            System.out.println("  E(Tq) .......... =   " + f.format(areaSortingFragileNotPrimeOrders / indexSortingFragileNotPrimeOrders));
            System.out.println("  E(Nq) ..... =   " + f.format(areaSortingFragileNotPrimeOrders / t.current));

            System.out.println("\nRESISTENT ORDERS CENTER");
            System.out.println("  E(Tq) .......... =   " + f.format(areaSortingNotFragileOrders / indexSortingNotFragileOrders));
            System.out.println("  E(Nq) ..... =   " + f.format(areaSortingNotFragileOrders / t.current));
            System.out.println("Prime queue");
            System.out.println("  E(Tq) .......... =   " + f.format(areaSortingNotFragilePrimeOrders / indexSortingNotFragilePrimeOrders));
            System.out.println("  E(Nq) ..... =   " + f.format(areaSortingNotFragilePrimeOrders / t.current));
            System.out.println("Not Prime queue");
            System.out.println("  E(Tq) .......... =   " + f.format(areaSortingNotFragileNotPrimeOrders / indexSortingNotFragileNotPrimeOrders));
            System.out.println("  E(Nq) ..... =   " + f.format(areaSortingNotFragileNotPrimeOrders / t.current));*/



        }

    } //end main

    public static double fasciaOrariaSwitch(double currentTime, int fascia){
        double arrival;

        if(0.0 <= currentTime && currentTime < 28800.0){
            arrival = ARRIVAL_M;
            fascia =0;
        } else if (28800.0 <= currentTime && currentTime < 57600.0) {
            arrival = ARRIVAL_H;
            fascia = 1;
        } else {
            arrival = ARRIVAL_L;
        }
        return arrival;
    }


    private double getServiceMultiServer(Rngs r, int streamIndex, int center) {
        r.selectStream(streamIndex);
        double m= 0.0;
        double std = 0;
        switch (center) {
            case 1: //picking
                m = SERVICE_TIME_PICKING;
                std =STD_SERVICE_TIME_PICKING;
                break;
            case 2: //packing
                m = SERVICE_TIME_PACKING;
                std =STD_SERVICE_TIME_PACKING;
                break;
            case 3: //quality
                m = SERVICE_TIME_QUALITY;
                std =STD_SERVICE_TIME_QUALITY;
                break;
            case 4: //sorting fragile
                m = SERVICE_TIME_SORTING_FRAGILE_ORDERS;
                std =STD_SERVICE_SORTING_FRAGILE_ORDERS;
                break;
            case 5: //sorting not fragile
                m = SERVICE_TIME_SORTING_NOT_FRAGILE_ORDERS;
                std =STD_SERVICE_SORTING_NOT_FRAGILE_ORDERS;
                break;
            default:
                new Exception("Errore nella selezione dello stream");
                break;
        }
        return TruncatedNormalDistribution.of(m,std,LOWER_BOUND_NORMAL,60*m).inverseCumulativeProbability(r.random());
    }

    private double getArrival(Rngs r, int streamIndex, int flag, double clockTime) {
        /* --------------------------------------------------------------
         * generate the next arrival time with idfPoisson
         * --------------------------------------------------------------
         */
        r.selectStream(0 + streamIndex);
        double arrival;
        Rvms rvms = new Rvms();
        if(flag !=0) {
            arrival = fasciaOrariaSwitch(clockTime, fasciaIndex);
        }
        else
            arrival = ARRIVAL_L;
        sarrival += rvms.idfPoisson(arrival, r.random());
        return (sarrival);
    }

    public long idfBernoulli(double p, double u)
        /* =========================================
         * NOTE: use 0.0 < p < 1.0 and 0.0 < u < 1.0
         * =========================================
         */
    {
        return ((u < 1.0 - p) ? 0 : 1);
    }

    int nextEvent(Event[] event) {
        /* ---------------------------------------
         * return the index of the next event type from the event list
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0 && i < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */

        e = i;
        while (i < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS ) {         /* now, check the others to find which  */
            i++;
            /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t <= event[e].t))
                e = i;
        }
        return (e);
    }

    int findOne(Event [] event, int center){ //center rappresenta il numero di server del centro
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = 0, server = 0;
        switch (center)
        {
            case 1: //picking center
                i = EVENT_ARRIVAL_PICKING + 1; //i = 1
                server = SERVERS_PICKING ;     //server = 40
                break;
            case 2: //packing center
                i = EVENT_ARRIVAL_PACKING + 1; //i = 42
                server = i +  SERVERS_PACKING; //server = 62
                break;
            case 3: //quality center
                i = EVENT_ARRIVAL_QUALITY + 1; //i = 65
                server = i + SERVERS_QUALITY;
                break;
            case 4: // fragile orders
                i = EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS + 1;
                server = i + SERVERS_SORTING_FRAGILE_ORDERS;
                break;
            case 5: // not fragile orders
                i = EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS + 1;
                server = i + SERVERS_SORTING_NOT_FRAGILE_ORDERS;
                break;
            default:
                new Exception("Errore nella selezione del centro");
                break;
        }

        while (event[i].x == 1)       /* find the index of the first available */
            i++;
        s = i;
        while (i < server) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }


}
