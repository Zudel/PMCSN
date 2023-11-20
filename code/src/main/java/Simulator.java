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
 * evento 1: completamento server e arrivo picking
 * evento (1 , ServerPicking] completamento picking e arrivo packing
 * evento (ServerPicking ,ServerPacking] completamento packing e arrivo quality center o arrivo shipping
 * evento (ServerPacking, ServerQuality] completamento quality e arrivo shipping o ritorno picking
 * evento (ServerQuality, ServerShipping] completamento shipping
 */

public class Simulator {
    private static double service;
    private static double abandontTime;
    double sarrival = START;
    static List<FasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");
    public static void main(String[] args) { //start point of the program execution

        int numberJobsServerOrder = 0;
        int numberJobsPickingCenter = 0;
        int numberJobsPackingCenter = 0;
        int numberJobsQualityCenter = 0;
        int numberJobsShippingCenter = 0;
        int    e;                      /* next event index                   */

        int    sPickingCenter;                      /* picking index                       */
        int    sPackingCenter;                      /* packing index                       */
        int    sQualityCenter;                      /* quality center index                       */
        int    sShippingCenter;                      /* shipping center index                       */

        int abandonedPicking = 0;                   /* number of abandoned jobs                       */
        int abandonedPacking = 0;
        int abandonedQuality = 0;
        int abandonedShippingPrime = 0;
        int abandonedShippingNotPrime = 0;

        double areaServer   = 0.0;           /* time integrated number in the node */
        double areaPickingCenter   = 0.0;           /* time integrated number in the node */
        double areaPackingCenter   = 0.0;           /* time integrated number in the node */
        double areaQualityCenter   = 0.0;           /* time integrated number in the node */
        double areaShippingCenter   = 0.0;           /* time integrated number in the node */
        double serviceServer = 0.0;
        long indexServer   = 0;             /* used to count departed jobs         */
        long indexPickingCenter   = 0;             /* used to count departed jobs         */
        long indexPackingCenter   = 0;             /* used to count departed jobs         */
        long indexQualityCenter   = 0;             /* used to count departed jobs         */
        long indexShippingCenter   = 0;             /* used to count departed jobs         */
        List<Double> abandonmentPicking = new ArrayList<>();
        List<Double> abandonmentPacking = new ArrayList<>();
        List<Double> abandonmentQuality = new ArrayList<>();
        List<Double> abandonmentShippingPrime = new ArrayList<>();
        List<Double> abandonmentShippingNotPrime = new ArrayList<>();
        List<Double> serverResponseTimes = new ArrayList<>();

        Simulator sim = new Simulator();
        Rngs r = new Rngs();
        T t = new T();
        Event[] event = new Event[ALL_EVENTS];
        Sum[] sum = new Sum[ALL_EVENTS];
        for (int s = 0; s < ALL_EVENTS ; s++) {
            event[s] = new Event();
            sum [s]  = new Sum();
        }

        r.putSeed(123456789);

        //inizializzazione array eventi e sum
        t.current    = START;
        event[0].t   = sim.getArrivalServer(r, t.current, 1); //first arrival to the server node
        event[0].x   = 1;
        numberJobsServerOrder++;

        for (int i = 1; i <ALL_EVENTS; i++) {
            event[i].t     = START;          /* this value is arbitrary because */
            event[i].x     = 0;              /* all servers are initially idle  */
            sum[i].service = 0.0;
            sum[i].served  = 0;
        }

        // INIZIO SIMULAZIONE //
        while (event[0].x != 0) {

            e         = sim.nextEvent(event);
            t.next    = event[e].t;
            areaServer     += (t.next - t.current) * numberJobsServerOrder;     //update integral of server
            areaPickingCenter     += (t.next - t.current) * numberJobsPickingCenter;     //update integral of picking center
            areaPackingCenter     += (t.next - t.current) * numberJobsPackingCenter;     //update integral of packing center
            areaQualityCenter     += (t.next - t.current) * numberJobsQualityCenter;     //update integral of quality center
            areaShippingCenter     += (t.next - t.current) * numberJobsShippingCenter;     //update integral of shipping center
            t.current = t.next;                                                 //advance the simulation clock

            if (e == 0) { //arrivo al server
                if (event[0].t > STOP)
                    event[0].x      = 0; //close the door
                if (numberJobsServerOrder ==  1) {                                   //if there is only one job in the node, schedule a completion (ci sono due job in coda all'inizio)
                    serviceServer         = sim.getServiceServer(r, 2);

                    sum[1].service += serviceServer;
                    sum[1].served++;
                    event[1].t      = t.current + serviceServer;
                    event[1].x      = 1;
                }
                numberJobsServerOrder++;
                event[0].t        = sim.getArrivalServer(r, t.current, 1);
            }

            else if(e == 1) { //partenza dal server & arrivo al picking center
                indexServer++;
                numberJobsServerOrder--;
                if (numberJobsServerOrder > 0) {
                    serviceServer = sim.getServiceServer(r, 2);

                    sum[1].service += serviceServer;
                    sum[1].served++;
                    event[1].t = t.current + serviceServer;
                    event[1].x = 1;

                } else {
                    event[1].x = 0;
                }

                //arrivo al picking center
                numberJobsPickingCenter++;
                if (numberJobsPickingCenter <= SERVERS_PICKING) { // if there is a free server
                    service         = sim.getServiceMultiServer(r, 0);
                    sPickingCenter               = sim.findOne(event,1);
                    sum[sPickingCenter].service += service;
                    sum[sPickingCenter].served++;
                    event[sPickingCenter].t      = t.current + service;
                    event[sPickingCenter].x      = 1;
                }

            }
            else if( (1 < e) && (e <= SERVERS_PICKING + ALL_EVENTS_SERVER) ) { //partenza dal picking center e arrivo al packing center [2,41]
                indexPickingCenter++;
                numberJobsPickingCenter--;
                sPickingCenter = e;
                if (numberJobsPickingCenter > SERVERS_PICKING) {
                    service = sim.getServiceMultiServer(r,4);
                    sum[sPickingCenter].service += service;
                    sum[sPickingCenter].served++;
                    event[sPickingCenter].t = t.current + service;
                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sPickingCenter].x = 0;
                }

                //arrivo al packing center
                numberJobsPackingCenter++;
                if (numberJobsPackingCenter <= SERVERS_PACKING) { // if there is a free server
                    double service         = sim.getServiceMultiServer(r, 5);
                    sPackingCenter               = sim.findOne(event,PACKING);
                    sum[sPackingCenter].service += service;
                    sum[sPackingCenter].served++;
                    event[sPackingCenter].t      = t.current + service;
                    event[sPackingCenter].x      = 1;
                }
            }

            else if ( (SERVERS_PICKING + ALL_EVENTS_SERVER< e) && (e <= ALL_EVENTS_PICKING + SERVERS_PACKING )){ //partenza dal packing center e arrivo al quality center o shipping center [42, 62]
                indexPackingCenter++;
                numberJobsPackingCenter--;
                sPackingCenter = e;
                if (numberJobsPackingCenter > SERVERS_PACKING) {
                    double service = sim.getServiceMultiServer(r,5);
                    sum[sPackingCenter].service += service;
                    sum[sPackingCenter].served++;
                    event[sPackingCenter].t = t.current + service;
                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sPackingCenter].x = 0;
                }

                r.selectStream(18); //scelgo se andare al quality center o allo shipping center
                double prob = r.random();

                if(prob <= QUALITY_CENTER_PROB){        //arrivo al quality center
                    numberJobsQualityCenter++;
                    if (numberJobsQualityCenter <= SERVERS_QUALITY) { // if there is a free server
                        service         = sim.getServiceMultiServer(r, 6);
                        sQualityCenter               = sim.findOne(event,QUALITY);
                        sum[sQualityCenter].service += service;
                        sum[sQualityCenter].served++;
                        event[sQualityCenter].t      = t.current + service;
                        event[sQualityCenter].x      = 1;
                    }
                }
                else {                                  //arrivo allo shipping center
                    numberJobsShippingCenter++;
                    if(numberJobsShippingCenter <= SERVERS_SHIPPING){
                        double orderType = r.random();
                        if(orderType <= ORDER_PRIME)
                            orderType = 1;
                        else
                            orderType = 2;
                        service         = sim.getServiceMultiServerPriority(r, 7, orderType);
                        sShippingCenter               = sim.findOne(event,SHIPPING);
                        sum[sShippingCenter].service += service;
                        sum[sShippingCenter].served++;
                        event[sShippingCenter].t      = t.current + service;
                        event[sShippingCenter].x      = 1;
                    }
                }

            }
            else if((ALL_EVENTS_PICKING + SERVERS_PACKING < e) && (e <= ALL_EVENTS_PACKING + SERVERS_QUALITY)){ //partenza dal quality center e arrivo al shipping center [63, 82] o picking center
                indexQualityCenter++;
                numberJobsQualityCenter--;
                sQualityCenter = e;
                if (numberJobsQualityCenter > SERVERS_QUALITY) {

                    service = sim.getServiceMultiServer(r,6);
                    sum[sQualityCenter].service += service;
                    sum[sQualityCenter].served++;
                    event[sQualityCenter].t = t.current + service;

                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sQualityCenter].x = 0;
                }

                r.selectStream(19);
                double probPS = r.random();
                if(probPS <= PICKING_CENTER_PROB){ //arrivo al picking center se condizione verificata
                    numberJobsPickingCenter++;
                    if (numberJobsPickingCenter <= SERVERS_PICKING) { // if there is a free server
                        service         = sim.getServiceMultiServer(r, 4);
                        sPickingCenter               = sim.findOne(event,PICKING);
                        sum[sPickingCenter].service += service;
                        sum[sPickingCenter].served++;
                        event[sPickingCenter].t      = t.current + service;
                        event[sPickingCenter].x      = 1;
                    }
                }
                else{ //arrivo allo shipping center
                    numberJobsShippingCenter++;
                    double orderType = r.random();
                    if(orderType <= ORDER_PRIME)
                        orderType = 1;
                    else
                        orderType = 2;
                    if(numberJobsShippingCenter <= SERVERS_SHIPPING){
                        service         = sim.getServiceMultiServerPriority(r, 7, orderType);
                        sShippingCenter               = sim.findOne(event,SHIPPING);
                        sum[sShippingCenter].service += service;
                        sum[sShippingCenter].served++;
                        event[sShippingCenter].t      = t.current + service;
                        event[sShippingCenter].x      = 1;
                    }
                }
            }
            else if((e > ALL_EVENTS_PACKING + SERVERS_QUALITY) && (e <= ALL_EVENTS_QUALITY  + SERVERS_SHIPPING )){ //partenza dal shipping center
                indexShippingCenter++;
                numberJobsShippingCenter--;
                sShippingCenter = e;
                if (numberJobsShippingCenter > SERVERS_SHIPPING) {
                    double orderType = r.random();
                    if(orderType <= ORDER_PRIME)
                        orderType = 1;
                    else
                        orderType = 2;
                    service = sim.getServiceMultiServerPriority(r,7, orderType);
                    sum[sShippingCenter].service += service;
                    sum[sShippingCenter].served++;
                    event[sShippingCenter].t = t.current + service;
                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sShippingCenter].x = 0;
                }
            }
            else if( e == EVENT_ABANDONMENT_PICKING){
                numberJobsPickingCenter--;
                abandonedPicking++;
                event[e].t = t.current + sim.getAbandon(PATIENCE, r, 0);
                abandonmentPicking.add(event[e].t);
                event[e].x = 1;
            }
            else if( e == EVENT_ABANDONMENT_PACKING){
                //fai la stessa cosa di prima
                numberJobsPackingCenter--;
                abandonedPacking++;
                event[e].t = t.current + sim.getAbandon(PATIENCE, r, 0);
                abandonmentPacking.add(event[e].t);
                event[e].x = 1;
            }
            else if( e == EVENT_ABANDONMENT_QUALITY){
                numberJobsQualityCenter--;
                abandonedQuality++;
                event[e].t = t.current + sim.getAbandon(PATIENCE, r, 0);
                abandonmentQuality.add(event[e].t);
                event[e].x = 1;
            }
            else if( e == EVENT_ABANDONMENT_PRIME_QUEUE){
                numberJobsShippingCenter--;
                abandonedShippingPrime++;
                event[e].t = t.current + sim.getAbandon(PATIENCE_ORDER_PRIME, r, 0);
                abandonmentShippingPrime.add(event[e].t);
                event[e].x = 1;
            }
            else if( e == EVENT_ABANDONMENT_NOT_PRIME_QUEUE){
                numberJobsShippingCenter--;
                abandonedShippingNotPrime++;
                event[e].t = t.current + sim.getAbandon(PATIENCE_ORDER_NOT_PRIME, r, 0);
                abandonmentShippingNotPrime.add(event[e].t);
                event[e].x = 1;
            }
            if(numberJobsPickingCenter > 0 && numberJobsPickingCenter <= SERVERS_PICKING){
                event[ALL_EVENTS].x = 1;
            }
            else{
                event[EVENT_ABANDONMENT_PICKING].x = 0;
            }
            if(numberJobsPackingCenter > 0 && numberJobsPackingCenter > SERVERS_PACKING ){
                event[EVENT_ABANDONMENT_PACKING].x = 1;
            }
            else{
                event[EVENT_ABANDONMENT_PACKING].x = 0;
            }
            if(numberJobsQualityCenter > 0 && numberJobsQualityCenter > SERVERS_QUALITY){
                event[EVENT_ABANDONMENT_QUALITY].x = 1;
            }
            else{
                event[EVENT_ABANDONMENT_QUALITY].x = 0;
            }
            if(numberJobsShippingCenter > 0 && numberJobsShippingCenter > SERVERS_SHIPPING){
                event[EVENT_ABANDONMENT_PRIME_QUEUE].x = 1;
                event[EVENT_ABANDONMENT_NOT_PRIME_QUEUE].x = 1;
            }
            else{
                event[EVENT_ABANDONMENT_PRIME_QUEUE].x = 0;
                event[EVENT_ABANDONMENT_NOT_PRIME_QUEUE].x = 0;
            }


           /* for (int i = 0; i <ALL_EVENTS; i++) {
                System.out.println("event[" + i + "].t: " + event[i].t);
            }
            System.out.println("numero di job nel server: " + numberJobsServerOrder);
            System.out.println("partenze dal server: " + indexServer);
            System.out.println("numberJobsPickingCenter: " + numberJobsPickingCenter);
            System.out.println("partenze dal picking center: " + indexPickingCenter);*/

        } //end while

        //stampa un separatore
        System.out.println("----------------------------------------------------");

        //stampo i risultati
        System.out.println("numeri di job nel server: " + numberJobsServerOrder);
        System.out.println("partenze dal server: " + indexServer);
        System.out.println("numero di job nel centro di picking: " + numberJobsPickingCenter);
        System.out.println("partenze dal centro di picking: " + indexPickingCenter);
        System.out.println("numero di job nel centro di packing: " + numberJobsPackingCenter);
        System.out.println("partenze dal centro di packing: " + indexPackingCenter);
        System.out.println("numero di job nel centro di qualità: " + numberJobsQualityCenter);
        System.out.println("partenze dal centro di qualità: " + indexQualityCenter);
        System.out.println("numero di job nel centro di shipping: " + numberJobsShippingCenter);
        System.out.println("partenze dal centro di shipping: " + indexShippingCenter);
        //System.out.println("utilizzazione Server: " + sum[1].service / t.current);
        //lista di abbandoni
        System.out.println("numero di abbandoni nel centro di picking: " + abandonedPicking);
        System.out.println("numero di abbandoni nel centro di packing: " + abandonedPacking);
        System.out.println("numero di abbandoni nel centro di qualità: " + abandonedQuality);
        System.out.println("numero di abbandoni nel centro di shipping prime: " + abandonedShippingPrime);
        System.out.println("numero di abbandoni nel centro di shipping not prime: " + abandonedShippingNotPrime);
        //anche i tempi di abbandono nelle liste
        System.out.println("lista di abbandoni nel centro di picking: " + abandonmentPicking);
        System.out.println("lista di abbandoni nel centro di packing: " + abandonmentPacking);
        System.out.println("lista di abbandoni nel centro di qualità: " + abandonmentQuality);
        System.out.println("lista di abbandoni nel centro di shipping prime: " + abandonmentShippingPrime);
        System.out.println("lista di abbandoni nel centro di shipping not prime: " + abandonmentShippingNotPrime);



        DecimalFormat f = new DecimalFormat("###0.00");

    } //end main

    private double getServiceMultiServerPriority(Rngs r, int i, double orderType) {
        r.selectStream(20 + i );
        double m;
        if (orderType == 1)
            m = SERVICE_TIME_SHIPPING_PRIME;
        else
            m = SERVICE_TIME_SHIPPING;
        return (-m * Math.log(1.0 - r.random()));
    }

    private double getServiceMultiServer(Rngs r, int streamIndex) {
        r.selectStream(20 + streamIndex );
        double m= 0.0;
        switch (streamIndex) {
            case 4:
                m = SERVICE_TIME_PICKING;
                break;
            case 5:
                m = SERVICE_TIME_PACKING;
                break;
            case 6:
                m = SERVICE_TIME_QUALITY;
                break;
            default:
                new Exception("Errore nella selezione dello stream");
                break;
        }
        return (-m * Math.log(1.0 - r.random()));

    }

    private double getArrivalServer(Rngs r, double currentTime, int streamIndex) {
        /* --------------------------------------------------------------
         * generate the next arrival time with idfPoisson
         * --------------------------------------------------------------
         */
        r.selectStream(0 + streamIndex);

        Rvms rvms = new Rvms();
        int index = utils.fasciaOrariaSwitch(listaFasciaOraria, currentTime);
        sarrival += rvms.idfPoisson(listaFasciaOraria.get(index).getMeanPoisson(), r.random());
        return (sarrival);
    }

    int nextEvent(Event[] event) {
        /* ---------------------------------------
         * return the index of the next event type from the event list
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */

        e = i;

        while (i < ALL_EVENTS - 1) {         /* now, check the others to find which  */
            i++;
            /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
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
                i = 2;
                server = SERVERS_PICKING + ALL_EVENTS_SERVER;
                break;
            case 2: //packing center
                i = SERVERS_PICKING + ALL_EVENTS_SERVER + 1;
                server = ALL_EVENTS_PICKING + SERVERS_PACKING;
                break;
            case 3: //quality center
                i = ALL_EVENTS_PICKING + SERVERS_PACKING + 1;
                server = ALL_EVENTS_PACKING + SERVERS_QUALITY;
                break;
            case 4: //shipping center
                i = ALL_EVENTS_PACKING + SERVERS_QUALITY +1 ;
                server = ALL_EVENTS -1 - ALL_ABANDON_EVENTS ;
                break;
            default:
                new Exception("Errore nella selezione del centro");
                break;
        }
        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < server ) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }

    double getServiceServer(Rngs r, int streamIndex){
        //servizio del centro server --> Uniforme
        r.selectStream(20 + streamIndex);
        double a = 0.5;
        double b = 0.3;
        return (a + (b - a) * r.random());
    }

    double getAbandon(double patience, Rngs r, int streamIndex){

        r.selectStream(1 + streamIndex);
        //patience = 999999999; //pazienza alta = no abbandoni
        return (-patience * Math.log(1.0 - r.random())); //abbandoni reali

    }
}
