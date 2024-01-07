import mathLib.Rngs;
import mathLib.Rvms;
import mathLib.Uvs;
import org.jfree.data.function.NormalDistributionFunction2D;
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

//(b,k) = (256, 126) k = parte inferiore (n/b) = 32400/256 = 126

public class Simulator {

    private static double[] responseTimeCounter;
    private static double[] waitingTimerCounter;
    private static double[] utilizationCounter;
    private static double[] numberResponseTimeCounter;
    private static double[] numberWaitingTimeCounter;
    private static int batchSize;
    private static int k;
    private static double service;
    private  int flag;
    private static double pickingResponseTime; //contatori per la media di un batch
    private static double packingResponseTime;
    private static double qualityResponseTime;
    private static double fragileSortingResponseTime;
    private static double notFragileSortingResponseTime;
    private static double pickingWaitingTime;
    private static double packingWaitingTime;
    private static double qualityWaitingTime;
    private static double fragileSortingPrimeWaitingTime;
    private static double fragileSortingNotPrimeWaitingTime;
    private static double notFragileSortingPrimeWaitingTime;
    private static double notFragileSortingNotPrimeWaitingTime;

    private static double sarrival = START;
    private static double[][] responseTimerecords;
    private static double[][] waitingTimerecords;
    private static double[][] numberResponseTimerecords;
    private static double[][] numberWaitingTimerecords;
    private static double[][] utilizationRecords;
    private static double[]arrivalsCounter;
    private static double[][] arrivals;
    private double[] totalResponseTime;
    private double goBackProb;
    private double[] goBackProbRecord;


    //static List<FasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");

    public Simulator(int batchSize, int k, int flag){ //k is batches number
        this.batchSize = batchSize;
        //System.out.println("batchSize: "+ batchSize);
        this.k = k;
        //System.out.println("k: "+ this.k);
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
        int batchNumber ;             // numero k corrente
        boolean cond;
        int    sPickingCenter;                      /* picking index                       */
        int    sPackingCenter;                      /* packing index                       */
        int    sQualityCenter;                      /* quality center index                       */
        int    sSortingFragileOrders;
        int    sSortingNotFragileOrders;

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

        Simulator sim = new Simulator(batchSize, k, flag);
        Rngs r = new Rngs();
        Estimate estimate = new Estimate();
        plotter[] grafico = new plotter[5];
        String[]  centerNames = new String[]{"picking", "packing", "quality", "resistent order", "fragile order"};
        String[]  centerNamesQueue = new String[]{"picking queue", "packing queue", "quality queue", "resistent order Prime queue", "resistent order Not Prime queue", "fragile order Prime queue","fragile order not Prime queue" };

        T t = new T();
        Event[] event = new Event[EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1]; //considera che parte da zero! quindi è indicizzabile fino a dim -1!!!
        Sum[] sum = new Sum[EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1];
        for (int s = 0; s < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1; s++) {
            event[s] = new Event();
            sum [s]  = new Sum();
        }

        //r.putSeed(123456789);
        r.plantSeeds(123456789);
        //inizializzazione array eventi e sum
        t.current    = START;
        event[0].t   = sim.getArrival(r,  1); //first arrival to the server node
        event[0].x   = 1;
        numberJobsPickingCenter++;

        for (int i = 1; i < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS+1; i++) {
            event[i].t     = START;          /* this value is arbitrary because */
            event[i].x     = 0;              /* all servers are initially idle  */
            sum[i].service = 0.0;
            sum[i].served  = 0;
        }

        if(flag == 0)
            if(event[0].x != 0)
                cond = true;
            else
                cond = false;
        else
        if(event[0].x != 0 || (numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsSortingFragileCenter + numberJobsSortingNotFragileCenter) > 0)
            cond = true;
        else
            cond = false;
        // INIZIO SIMULAZIONE //
        while (cond) {
            e         = sim.nextEvent(event);
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

            batchNumber = (int) t.current / batchSize;
            //System.out.println(batchNumber);
            batchJob++;

            if (e == EVENT_ARRIVAL_PICKING) { //arrivo al picking center
                numberJobsPickingCenter++;
                event[0].t = sim.getArrival(r, 1);

                if (event[0].t > STOP_BATCH)
                    event[0].x = 0; //close the door

                if (numberJobsPickingCenter <= SERVERS_PICKING) {
                    service = sim.getServiceMultiServer(r, 2, PICKING);
                    sPickingCenter = sim.findOne(event, PICKING);
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
                    service = sim.getServiceMultiServer(r, 2, PICKING);
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
                arrivoPacking = t.current;


            }

            else if(e == EVENT_ARRIVAL_PACKING) { //arrivo al packing center

                event[e].x = 0;
                numberJobsPackingCenter++;
                if (numberJobsPackingCenter <= SERVERS_PACKING){
                    service = sim.getServiceMultiServer(r, 4,PACKING);
                    sPackingCenter = sim.findOne(event, PACKING);
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
                    service = sim.getServiceMultiServer(r,4, PACKING);
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
                        arrivoQualityControl = t.current;

                }
                else{ //66% di probabilità di andare al sorting center fragile oppure non fragile
                    random = r.random();
                    if (random <= SORTING_FRAGILE_CENTER) {  //qui decido se andare al sorting center fragile o not fragile (se la condizione è verficata vado al fragile)
                        //numberJobsSortingFragileCenter++;
                        arrivoFragile = t.current;
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
                        arrivoResistent = t.current;
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

                event[e].x = 0;
                numberJobsQualityCenter++;
                if (numberJobsQualityCenter <= SERVERS_QUALITY){
                    service = sim.getServiceMultiServer(r, 6,QUALITY);
                    sQualityCenter = sim.findOne(event, QUALITY);
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
                    service = sim.getServiceMultiServer(r, 6, QUALITY);

                    sum[sQualityCenter].service += service;
                    sum[sQualityCenter].served++;
                    event[sQualityCenter].t = t.current + service;
                    event[sQualityCenter].x = 1;
                } else {
                    event[sQualityCenter].x = 0;
                }


                System.out.println(idfBernoulli(BERNOULLI_PROB_SUCCESS, r.random()));

                if(idfBernoulli(BERNOULLI_PROB_SUCCESS, r.random()) == 0) { //probabilità di andare al picking center (TEST FALLITO)
                    numberFeedbackIsTrue++; //conto il numero di job che non passano il test
                    event[EVENT_ARRIVAL_PICKING].x = 1;
                    event[EVENT_ARRIVAL_PICKING].t = event[sQualityCenter].t;

                }
                else{ //probabilità di andare al sorting center fragile oppure non fragile (TEST SUPERATO)
                    random = r.random();

                    if (random <= SORTING_FRAGILE_CENTER) {  //qui decido se andare al sorting center fragile o not fragile (se la condizione è verficata vado al fragile)
                        //numberJobsSortingFragileCenter++;
                        arrivoFragile = t.current;
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
                        arrivoResistent = t.current;
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

                event[e].x =0;
                numberJobsSortingFragileCenter++;
                if (numberJobsSortingFragileCenter <= SERVERS_SORTING_FRAGILE_ORDERS) {
                    service = sim.getServiceMultiServer(r, 9, SORTING_FRAGILE_ORDERS);
                    sSortingFragileOrders = sim.findOne(event, SORTING_FRAGILE_ORDERS);
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
                    service = sim.getServiceMultiServer(r, 9, SORTING_FRAGILE_ORDERS);
                    sum[sSortingFragileOrders].service += service;
                    sum[sSortingFragileOrders].served++;
                    event[sSortingFragileOrders].t = t.current + service;
                    event[sSortingFragileOrders].x = 1;
                }
                else
                    event[sSortingFragileOrders].x = 0; //se non ci sono più job nel picking center, setto l'evento di partenza a zero

            }
            else if( e == EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS || e == EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS){ //arrivi al centro di smistamento ordini resistenti
                arrivoResistent = t.current;
                event[e].x = 0;
                numberJobsSortingNotFragileCenter++;
                if(numberJobsSortingNotFragileCenter <= SERVERS_SORTING_NOT_FRAGILE_ORDERS){
                    service = sim.getServiceMultiServer(r, 11, SORTING_NOT_FRAGILE_ORDERS);
                    sSortingNotFragileOrders = sim.findOne(event, SORTING_NOT_FRAGILE_ORDERS);
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
                    service = sim.getServiceMultiServer(r, 11, SORTING_NOT_FRAGILE_ORDERS);
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
            arrivalsCounter[0] += (sarrival - t.current);
            arrivalsCounter[1] += t.current - arrivoPacking;
            arrivalsCounter[2]  += Math.abs(arrivoQualityControl  - t.current);
            arrivalsCounter[3]  += Math.abs(arrivoFragile - t.current);
            arrivalsCounter[4]  += Math.abs(arrivoResistent - t.current);
            if(indexPickingCenter !=0 ) {
                responseTimeCounter[0] += areaPickingCenter / indexPickingCenter;
                waitingTimerCounter[0] += areaQueuePickingCenter / indexPickingCenter;
                numberResponseTimeCounter[0] += areaPickingCenter / t.current;
                numberWaitingTimeCounter[0] += areaQueuePickingCenter / t.current;
                for (int s = EVENT_ARRIVAL_PICKING + 1; s <= EVENT_DEPARTURE_PICKING; s++) //calcolo l'utilizzazione del centro facendo la media
                    utilizationCounter[0] += sum[s].service;
                utilizationCounter[0] = utilizationCounter[0] / SERVERS_PICKING;
                utilizationCounter[0] = (utilizationCounter[0]*1000) /t.current;
            }
            if(indexPackingCenter !=0 ) {
                responseTimeCounter[1] += areaPackingCenter / indexPackingCenter;
                waitingTimerCounter[1] += areaQueuePackingCenter / indexPackingCenter;
                numberResponseTimeCounter[1] += areaPackingCenter / t.current;
                numberWaitingTimeCounter[1] += areaQueuePackingCenter / t.current;
                for (int s = EVENT_ARRIVAL_PACKING + 1; s <= EVENT_DEPARTURE_PACKING; s++) //calcolo l'utilizzazione del centro facendo la media
                    utilizationCounter[1] += sum[s].service;
                utilizationCounter[1] = utilizationCounter[1] / SERVERS_PACKING;
                utilizationCounter[1] = (utilizationCounter[1]*1000) /t.current;
            }
            if(indexQualityCenter !=0 ) {
                responseTimeCounter[2] += areaQualityCenter / indexQualityCenter;
                waitingTimerCounter[2] += areaQueueQualityCenter / indexQualityCenter;
                numberResponseTimeCounter[2] += areaQualityCenter / t.current;
                numberWaitingTimeCounter[2] += areaQueueQualityCenter / t.current;
                for (int s = EVENT_ARRIVAL_QUALITY + 1; s <= EVENT_DEPARTURE_QUALITY; s++) //calcolo l'utilizzazione del centro facendo la media
                    utilizationCounter[2] += sum[s].service;
                utilizationCounter[2] = utilizationCounter[2] / SERVERS_QUALITY;
                utilizationCounter[2] = (utilizationCounter[2]*1000) /t.current;
                goBackProb += ((double )numberFeedbackIsTrue / (double) indexQualityCenter);

            }
            if(indexSortingFragileOrders !=0 ) {
                responseTimeCounter[3] += areaSortingFragileOrders / indexSortingFragileOrders;
                numberResponseTimeCounter[3] += areaSortingFragileOrders / t.current;
                waitingTimerCounter[3] += areaQueueFragilePrimeOrders / indexSortingFragileOrders;
                waitingTimerCounter[4] += areaQueueFragileNotPrimeOrders / indexSortingFragileOrders;
                numberWaitingTimeCounter[3] += areaQueueFragilePrimeOrders / t.current;
                numberWaitingTimeCounter[4] += areaQueueFragileNotPrimeOrders / t.current;
                for (int s = EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                    utilizationCounter[3] += sum[s].service;
                utilizationCounter[3] = utilizationCounter[3] / SERVERS_SORTING_FRAGILE_ORDERS;
                utilizationCounter[3] = (utilizationCounter[3]*1000) /t.current;
            }
            if(indexSortingNotFragileOrders !=0 ) {
                responseTimeCounter[4] += areaSortingNotFragileOrders / indexSortingNotFragileOrders;
                numberResponseTimeCounter[4] += areaSortingNotFragileOrders / t.current;
                waitingTimerCounter[5] += areaQueueNotFragilePrimeOrders / indexSortingNotFragileOrders;
                waitingTimerCounter[6] += areaQueueNotFragileNotPrimeOrders / indexSortingNotFragileOrders;
                numberWaitingTimeCounter[5] += areaQueueNotFragilePrimeOrders / t.current;
                numberWaitingTimeCounter[6] += areaQueueNotFragileNotPrimeOrders / t.current;
                for (int s = EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS + 1; s <= EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS; s++) //calcolo l'utilizzazione del centro facendo la media
                    utilizationCounter[4] += sum[s].service;
                utilizationCounter[4] = utilizationCounter[4] / SERVERS_SORTING_NOT_FRAGILE_ORDERS;
                utilizationCounter[4] = (utilizationCounter[4]*1000) /t.current;
            }

                if(batchJob == batchSize-1) {

                    goBackProbRecord[batchNumber] = goBackProb / batchSize;

                    for (int j =0; j < 5; j++){
                        //LOCALI
                        responseTimerecords[j][batchNumber] = responseTimeCounter[j] / batchSize; //(E(Ts))
                        numberResponseTimerecords[j][batchNumber] = numberResponseTimeCounter[j] / batchSize; //(E(Ns))
                        utilizationRecords[j][batchNumber] = utilizationCounter[j] / batchSize;
                        arrivals[j][batchNumber] = arrivalsCounter[j] / batchSize;
                        //globali
                        totalResponseTime[batchNumber] += responseTimerecords[j][batchNumber]; //calcolo i


                    }
                    for (int j =0; j < 7; j++){
                        waitingTimerecords[j][batchNumber] = waitingTimerCounter[j] / batchSize; //(E(Tq))
                        numberWaitingTimerecords[j][batchNumber] = numberWaitingTimeCounter[j] / batchSize;
                    }

                    for (int j =0; j < 5; j++){
                        responseTimeCounter[j] =0; //(E(Ts))
                        numberResponseTimeCounter[j] = 0; //(E(Ns))
                        utilizationCounter[j] = 0;
                        arrivalsCounter[j] = 0;
                    }
                    for (int j =0; j < 7; j++){
                        waitingTimerCounter[j]= 0; //(E(Tq))
                        numberWaitingTimeCounter[j]  = 0;
                    }
                goBackProb = 0.0;
                batchJob = 0; //passo al prossimo batch e salvo le statistiche nel batch associato
            }
            if(flag == 0)
               if(event[0].x != 0)
                   cond = true;
                else
                    cond = false;
            else
                if(event[0].x != 0 || (numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsSortingFragileCenter + numberJobsSortingNotFragileCenter) > 0)
                    cond = true;
                else
                    cond = false;
        } //end while
        DecimalFormat f = new DecimalFormat("###0.0000000");

        System.out.println("----------------------------------------------------");

        for (int j =0; j < 5; j++){
            System.out.println("\n");
            System.out.println(centerNames[j]);
            System.out.println("E(Ts)");
            estimate.main(responseTimerecords[j]);
            System.out.println("E(Ns)");
            estimate.main(numberResponseTimerecords[j]);
            System.out.println("E(utilizzazione)");
            estimate.main(utilizationRecords[j]);
        }
        for (int j =0; j < 7; j++){
            System.out.println("\n");
            System.out.println(centerNamesQueue[j]);
            System.out.println("E(Tq)");
            estimate.main(waitingTimerecords[j]);
            System.out.println("E(Nq)");
            estimate.main(numberWaitingTimerecords[j]);
        }

        SwingUtilities.invokeLater(() -> {
            for (int i =0;i < 5; i++) {
                grafico[i] = new plotter(centerNames[i],"tempo di riposta", responseTimerecords, this.k, this.batchSize, i);
                grafico[i].setSize(800, 600);
                grafico[i].setLocationRelativeTo(null);
                grafico[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                grafico[i].setVisible(true);
            }
        });
        SwingUtilities.invokeLater(() -> {
            for (int i =0;i < 5; i++) {
                grafico[i] = new plotter(centerNames[i],"lamda", arrivals, this.k, this.batchSize, i);
                grafico[i].setSize(800, 600);
                grafico[i].setLocationRelativeTo(null);
                grafico[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                grafico[i].setVisible(true);
            }
        });

        /*SwingUtilities.invokeLater(() -> {
            for (int i =0;i < 5; i++) {
                grafico[i] = new plotter(centerNames[i],"lamda", arrivals, this.k, this.batchSize, i);
                grafico[i].setSize(800, 600);
                grafico[i].setLocationRelativeTo(null);
                grafico[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                grafico[i].setVisible(true);
            }
        });*/

        SwingUtilities.invokeLater(() -> {
                plotter graficoRespTimeTot = new plotter("sistema","tempo di risposta totale", totalResponseTime, this.k, this.batchSize);
            graficoRespTimeTot.setSize(800, 600);
            graficoRespTimeTot.setLocationRelativeTo(null);
            graficoRespTimeTot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            graficoRespTimeTot.setVisible(true);

        });
        SwingUtilities.invokeLater(() -> {
            plotter graficoGoBackProb = new plotter("","probabilità ordine difettoso", goBackProbRecord, this.k, this.batchSize);
            graficoGoBackProb.setSize(800, 600);
            graficoGoBackProb.setLocationRelativeTo(null);
            graficoGoBackProb.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            graficoGoBackProb.setVisible(true);

        });

        //stampo i risultati
        System.out.println("numero di job nel centro di picking: " + numberJobsPickingCenter);
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
        System.out.println("PERCENTUALE DI JOB NON PASSATI: " +f.format((double ) 100*(numberFeedbackIsTrue/indexQualityCenter)));

        System.out.println("----------------------------------------------------");
        //nel nodo
        /*System.out.println("TEMPI E QUANTITA NEL NODO");
        System.out.println("\nfor " + indexPickingCenter + " jobs the service node PICKING's statistics are:\n");
        System.out.println("  avg interarrivals .. =   " + f.format(event[0].t / indexPickingCenter)); //E(interrarrivo)
        System.out.println("  avg wait (E(TS)) ... =   " + f.format(areaPickingCenter / indexPickingCenter));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaPickingCenter / t.current)); //E(Ns)

        /*System.out.println("\nfor " + indexPackingCenter + " jobs the service node PACKING's statistics are:\n");
        //System.out.println("  avg interarrivals .. =   " + f.format( / indexPackingCenter)); //E(interrarrivo)
        System.out.println("  avg wait ........... =   " + f.format(areaPackingCenter / indexPackingCenter));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaPackingCenter / t.current)); //E(Ns)

        System.out.println("\nfor " + indexQualityCenter + " jobs the service node QUALITY CENTER's statistics are:\n");
        //System.out.println("  avg interarrivals .. =   " + f.format( / indexQualityCenter)); //E(interrarrivo)
        System.out.println("  avg wait ........... =   " + f.format(areaQualityCenter / indexQualityCenter));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaQualityCenter / t.current)); //E(Ns)

        System.out.println("\nfor " + indexSortingFragileOrders + " jobs the service node SORTING FRAGILE ORDERS's statistics are:\n");
       // System.out.println("  avg interarrivals .. =   " + f.format( / indexSortingFragileOrders)); //E(interrarrivo)
        System.out.println("  avg wait ........... =   " + f.format(areaSortingFragileOrders / indexSortingFragileOrders));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaSortingFragileOrders / t.current)); //E(Ns)

        System.out.println("\nfor " + indexSortingNotFragileOrders + " jobs the service node SORTING RESISTENT ORDERS's statistics are:\n");
        //System.out.println("  avg interarrivals .. =   " + f.format(() / indexSortingNotFragileOrders)); //E(interrarrivo)
        System.out.println("  avg wait ........... =   " + f.format(areaSortingNotFragileOrders / indexSortingNotFragileOrders));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaSortingNotFragileOrders / t.current)); //E(Ns)*/

        for (int s = EVENT_ARRIVAL_PICKING + 1; s <= EVENT_DEPARTURE_PICKING; s++)          /* adjust area to calculate */
            areaPickingCenter -= sum[s].service;              /* averages for the queue   */

        for (int s = EVENT_ARRIVAL_PACKING + 1; s <= EVENT_DEPARTURE_PACKING; s++)          /* adjust area to calculate */
            areaPackingCenter -= sum[s].service;              /* averages for the queue   */

        for (int s = EVENT_ARRIVAL_QUALITY +1 ; s <= EVENT_DEPARTURE_QUALITY; s++)          /* adjust area to calculate */
            areaQualityCenter -= sum[s].service;

        for (int s = EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS+1; s <= EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS; s++) {          /* adjust area to calculate */
            areaSortingFragileNotPrimeOrders -= sum[s].service;
            areaSortingFragilePrimeOrders -= sum[s].service;
            areaSortingFragileOrders -= sum[s].service;
        }

        for (int s = EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS+1; s <= EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS; s++){          /* adjust area to calculate */
            areaSortingNotFragilePrimeOrders -= sum[s].service;              /* averages for the queue   */
            areaSortingNotFragileNotPrimeOrders -= sum[s].service;
            areaSortingNotFragileOrders -= sum[s].service;
        }
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
        System.out.println("Prime queue");
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
    } //end main

    private double getServiceMultiServer(Rngs r, int streamIndex, int center) {
        r.selectStream(streamIndex);
        Rvms rvms = new Rvms();
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
        return rvms.idfNormal(m, std,r.random());

    }

    private double getArrival(Rngs r, int streamIndex) {
        /* --------------------------------------------------------------
         * generate the next arrival time with idfPoisson
         * --------------------------------------------------------------
         */
        r.selectStream(0 + streamIndex);

        Rvms rvms = new Rvms();
        //int index = utils.fasciaOrariaSwitch(listaFasciaOraria, currentTime);
        sarrival += rvms.idfPoisson(ARRIVAL, r.random());
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

        while (event[i].x == 0 )       /* find the index of the first 'active' */
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

        while (event[i].x == 1 )       /* find the index of the first available */
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
