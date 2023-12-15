import mathLib.Rngs;
import mathLib.Rvms;
import utils.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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

    private static int batchSize;
    private static int k;
    private static double service;
    double sarrival = START;
    //static List<FasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");

    public Simulator(int batchSize, int k){
        this.batchSize = batchSize;
        this.k = k;
    }

    public static void main(String[] args) { //start point of the program execution

        int numberJobsPickingCenter = 0;
        int numberJobsPackingCenter = 0;
        int numberJobsQualityCenter = 0;
        int numberJobsSortingFragileCenter = 0;
        int numberJobsSortingNotFragileCenter = 0;
        int    e;                      /* next event index                   */
        int batchJob=0;                 // numero dell'i-esimo job di un batch, va da 0 a 255

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

        Simulator sim = new Simulator(batchSize, k);
        Rngs r = new Rngs();
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

        // INIZIO SIMULAZIONE //
        while (event[0].x != 0 /*|| (numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsSortingFragileCenter + numberJobsSortingNotFragileCenter) > 0*/) {
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



            t.current = t.next;                                                 //advance the simulation clock
            batchJob++;
            if (e == EVENT_ARRIVAL_PICKING) { //arrivo al picking center
                numberJobsPickingCenter++;
                event[0].t = sim.getArrival(r, 1);

                if (event[0].t > STOP)
                    event[0].x = 0; //close the door

                if (numberJobsPickingCenter <= SERVERS_PICKING) {
                    service = sim.getServiceMultiServer(r, 5, PICKING);
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
                    service = sim.getServiceMultiServer(r, 5, PICKING);
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
                numberJobsPackingCenter++;

            }

            else if(e == EVENT_ARRIVAL_PACKING) { //arrivo al packing center
                event[e].x = 0;

                if (numberJobsPackingCenter <= SERVERS_PACKING){
                    service = sim.getServiceMultiServer(r, 5,PACKING);
                    sPackingCenter = sim.findOne(event, PACKING);
                    sum[sPackingCenter].service += service;
                    sum[sPackingCenter].served++;
                    event[sPackingCenter].t = t.current + service;
                    event[sPackingCenter].x = 1;
                }
            }
            else if( (e > EVENT_ARRIVAL_PACKING ) && (e <= EVENT_DEPARTURE_PACKING)  ) {  //partenza dal packing center
                indexPackingCenter++;
                numberJobsPackingCenter--;

                double random = r.random();

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
                        numberJobsQualityCenter++;

                }
                else{ //66% di probabilità di andare al sorting center fragile oppure non fragile
                    random = r.random();
                    if (random <= SORTING_FRAGILE_CENTER) {  //qui decido se andare al sorting center fragile o not fragile (se la condizione è verficata vado al fragile)
                        numberJobsSortingFragileCenter++;
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
                        numberJobsSortingNotFragileCenter++;
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
                if (numberJobsQualityCenter <= SERVERS_QUALITY){
                    service = sim.getServiceMultiServer(r, 5,QUALITY);
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
                    service = sim.getServiceMultiServer(r, 5, QUALITY);

                    sum[sQualityCenter].service += service;
                    sum[sQualityCenter].served++;
                    event[sQualityCenter].t = t.current + service;
                    event[sQualityCenter].x = 1;
                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sQualityCenter].x = 0;
                }
                if(random <= PICKING_CENTER_PROB) { //5% di probabilità di andare al picking center
                    numberFeedbackIsTrue++; //conto il numero di job che non passano il test
                    numberJobsPickingCenter++;
                    event[EVENT_ARRIVAL_PICKING].x = 1;
                    event[EVENT_ARRIVAL_PICKING].t = event[sQualityCenter].t;

                }
                else{ //95% di probabilità di andare al sorting center fragile oppure non fragile
                    random = r.random();

                    if (random <= SORTING_FRAGILE_CENTER) {  //qui decido se andare al sorting center fragile o not fragile (se la condizione è verficata vado al fragile)
                        numberJobsSortingFragileCenter++;
                        random = r.random();
                        if (random <= ORDER_PRIME) { //qui decido in quale coda del sorting center fragile andare (prime o not prime)
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS].t = event[sQualityCenter].t;
                            numberPrimeJobsSortingFragileCenter++;

                        }
                        else {
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS].t = event[sQualityCenter].t;
                            numberNotPrimeJobsSortingFragileCenter++;
                        }
                    }
                    else {
                        random = r.random();
                        numberJobsSortingNotFragileCenter++;
                        if (random <= ORDER_PRIME) { //qui decido in quale coda del sorting center NON fragile andare (prime o not prime)
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS].t = event[sQualityCenter].t;
                            numberPrimeJobsSortingNotFragileCenter++;
                        }
                        else{
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS].x = 1;
                            event[EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS].t = event[sQualityCenter].t;
                            numberNotPrimeJobsSortingNotFragileCenter++;
                        }
                    }

                }
            }
            else if(e == EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS || e == EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS){ //arrivi al centro smistamento ordini fragili
                event[e].x =0;
                if (numberJobsSortingFragileCenter <= SERVERS_SORTING_FRAGILE_ORDERS) {
                    service = sim.getServiceMultiServer(r, 5, SORTING_FRAGILE_ORDERS);
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
                    service = sim.getServiceMultiServer(r, 5, SORTING_FRAGILE_ORDERS);
                    sum[sSortingFragileOrders].service += service;
                    sum[sSortingFragileOrders].served++;
                    event[sSortingFragileOrders].t = t.current + service;
                    event[sSortingFragileOrders].x = 1;
                }
                else
                    event[sSortingFragileOrders].x = 0; //se non ci sono più job nel picking center, setto l'evento di partenza a zero

            }

            else if( e == EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS || e == EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS){ //arrivi al centro di smistamento ordini resistenti
                event[e].x = 0;
                if(numberJobsSortingNotFragileCenter <= SERVERS_SORTING_NOT_FRAGILE_ORDERS){
                    service = sim.getServiceMultiServer(r, 5, SORTING_NOT_FRAGILE_ORDERS);
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
                    service = sim.getServiceMultiServer(r, 5, SORTING_NOT_FRAGILE_ORDERS);
                    sum[sSortingNotFragileOrders].service += service;
                    sum[sSortingNotFragileOrders].served++;
                    event[sSortingNotFragileOrders].t = t.current + service;
                    event[sSortingNotFragileOrders].x = 1;
                }
                else
                    event[sSortingNotFragileOrders].x = 0; //se non ci sono più job nel picking center, setto l'evento di partenza a zero
            }

           /*for (int i = 0; i < EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS + 1; i++) {
                System.out.println("event[" + i + "].t: " + event[i].t);
            }
            System.out.println("numberJobsPickingCenter: " + numberJobsPickingCenter);
            System.out.println("partenze dal picking center: " + indexPickingCenter);
            System.out.println("numberJobsPackingCenter: " + numberJobsPackingCenter);
            System.out.println("partenze dal packing center: " + indexPackingCenter);
            System.out.println("numberJobsQualityCenter: " + numberJobsQualityCenter);
            System.out.println("partenze dal quality center: " + indexQualityCenter);
            System.out.println("numero di job nel centro di smistamento ordini fragili: " + numberJobsSortingFragileCenter);
            System.out.println("partenze nel centro di smistamento ordini fragili: " + indexSortingFragileOrders);
            System.out.println("numero di job nel centro di smistamento ordini NON fragili: " + numberJobsSortingNotFragileCenter);
            System.out.println("partenze nel centro di smistamento ordini NON fragili: " + indexSortingNotFragileOrders);*/

        } //end while
        DecimalFormat f = new DecimalFormat("###0.00");

        System.out.println("----------------------------------------------------");

        /*//stampo i risultati
        //System.out.println("numero di job nel centro di picking: " + numberJobsPickingCenter);
        System.out.println("partenze dal centro di picking: " + indexPickingCenter);
        System.out.println("numero di job nel centro di packing: " + numberJobsPackingCenter);
        System.out.println("partenze dal centro di packing: " + indexPackingCenter);
        //System.out.println("numero di job nel centro di qualità: " + numberJobsQualityCenter);
        System.out.println("partenze dal centro di qualità: " + indexQualityCenter);
        //System.out.println("numero di job nel centro di smistamento ordini fragili: " + numberJobsSortingFragileCenter);
        System.out.println("partenze nel centro di smistamento ordini fragili: " + indexSortingFragileOrders);
        System.out.println("partenze di tipo PRIME nel centro di smistamento ordini fragili: " + indexSortingFragilePrimeOrders);
        System.out.println("partenze di tipo NON PRIME nel centro di smistamento ordini fragili: " + indexSortingFragileNotPrimeOrders);
        //System.out.println("numero di job nel centro di smistamento ordini NON fragili: " + numberJobsSortingNotFragileCenter);
        System.out.println("partenze nel centro di smistamento ordini NON fragili: " + indexSortingNotFragileOrders);
        System.out.println("partenze di tipo PRIME nel centro di smistamento ordini NON fragili: " + indexSortingNotFragilePrimeOrders);
        System.out.println("partenze di tipo NON PRIME nel centro di smistamento ordini NON fragili: " + indexSortingNotFragileNotPrimeOrders);
        System.out.println("numeri di job non passati: " +numberFeedbackIsTrue);*/

        System.out.println("----------------------------------------------------");
        //nel nodo
        System.out.println("TEMPI E QUANTITA NEL NODO");
        System.out.println("\nfor " + indexPickingCenter + " jobs the service node PICKING's statistics are:\n");
        System.out.println("  avg interarrivals .. =   " + f.format(event[0].t / indexPickingCenter)); //E(interrarrivo)
        System.out.println("  avg wait ........... =   " + f.format(areaPickingCenter / indexPickingCenter));  //E(Ts)
        System.out.println("  avg # in node ...... =   " + f.format(areaPickingCenter / t.current)); //E(Ns)

        System.out.println("\nfor " + indexPackingCenter + " jobs the service node PACKING's statistics are:\n");
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
        System.out.println("  avg # in node ...... =   " + f.format(areaSortingNotFragileOrders / t.current)); //E(Ns)

        for (int s = 1; s <= SERVERS_PICKING; s++)          /* adjust area to calculate */
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
        double m= 0.0;
        switch (center) {
            case 1: //picking
                m = SERVICE_TIME_PICKING;
                break;
            case 2: //packing
                m = SERVICE_TIME_PACKING;
                break;
            case 3: //quality
                m = SERVICE_TIME_QUALITY;
                break;
            case 4: //sorting fragile
                m = SERVICE_TIME_SORTING_FRAGILE_ORDERS;
                break;
            case 5: //sorting not fragile
                m = SERVICE_TIME_SORTING_NOT_FRAGILE_ORDERS;
                break;
            default:
                new Exception("Errore nella selezione dello stream");
                break;
        }
        return (-m * Math.log(1.0 - r.random()));

    }

    private double getArrival(Rngs r, int streamIndex) {
        /* --------------------------------------------------------------
         * generate the next arrival time with idfPoisson
         * --------------------------------------------------------------
         */
        r.selectStream(0 + streamIndex);

        Rvms rvms = new Rvms();
        //int index = utils.fasciaOrariaSwitch(listaFasciaOraria, currentTime);
        sarrival += rvms.idfPoisson(2.0092, r.random());
        return (sarrival);
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
        while (i < 130) {         /* now, check the others to find which  */
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
