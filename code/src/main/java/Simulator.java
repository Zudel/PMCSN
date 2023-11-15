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
 * evento 2: abbandono picking
 * evento 3: completamento picking e arrivo packing (il completamento va inteso come un unico evento , però si deve tener conto del numero di server del centro, e quindi di findOne adattare il contatore)
 * evento 4: abbandono packing
 * evento 5: completamento packing e arrivo quality
 * evento 6: abbandono quality
 * evento 7: completamento quality e arrivo shipping
 * evento 8: abbandono shipping
 * evento 9: completamento shipping
 * evento 10: completamento packing e arrivo shipping
 * */

public class Simulator {
    double sarrival = START;
    static List<FasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");
    public static void main(String[] args) { //start point of the program execution

        int numberJobsServerOrder = 0;
        int numberJobsPickingCenter = 0;
        int numberJobsPackingCenter = 0;
        int numberJobsQualityCenter = 0;
        int numberJobsShippingCenter = 0;
        int    e;                      /* next event index                   */

        int    sPickingCenter;                      /* server index                       */
        int    sPackingCenter;                      /* server index                       */
        int    sQualityCenter;                      /* server index                       */
        int    sShippingCenter;                      /* server index                       */

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
        Event[] event = new Event[ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING];
        Sum[] sum = new Sum[ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING + ALL_EVENTS_PICKING + ALL_EVENTS_SERVER];
        for (int s = 0; s < ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING + ALL_EVENTS_PICKING + ALL_EVENTS_SERVER; s++) {
            event[s] = new Event();
            sum [s]  = new Sum();
        }

        r.putSeed(123456789);

        //inizializzazione array eventi e sum
        t.current    = START;
        event[0].t   = sim.getArrivalServer(r, t.current, 1); //first arrival to the server node
        event[0].x   = 1;
        numberJobsServerOrder++;

        for (int i = 1; i < ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING; i++) { //messo il + 2 perchè ho aggiunto il dispatcher e +14 per i due centri dei guasti?
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
                    double service         = sim.getServiceMultiServer(r, 4);
                    sPickingCenter               = sim.findOne(event, PICKING);
                    sum[sPickingCenter].service += service;
                    sum[sPickingCenter].served++;
                    event[sPickingCenter].t      = t.current + service;
                    event[sPickingCenter].x      = 1;
                }

            }
            else if( 2 < e && e <= 43 ) { //partenza dal picking center e arrivo al packing center
                indexPickingCenter++;
                numberJobsPickingCenter--;
                sPickingCenter = e;
                if (numberJobsPickingCenter > SERVERS_PICKING) {
                    double service = sim.getServiceMultiServer(r,4);
                    sum[sPickingCenter].service += service;
                    sum[sPickingCenter].served++;
                    event[sPickingCenter].t = t.current + service;
                } else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[sPickingCenter].x = 0;
                }



            }
            /*System.out.println("(tempo arrivo al server )event[0].t: " + event[0].t);
            System.out.println("(tempo partenze dal server) event[1].t: " + event[1].t);
            System.out.println("(tempo arrivo al picking)event[2].t: " + event[2].t);
            System.out.println("tempo partenza dal picking event[ALL_EVENTS_SERVER + ALL_EVENTS_PICKING]: " + event[ALL_EVENTS_SERVER + ALL_EVENTS_PICKING ].t);
            */

            /*System.out.println("e: " + e);
            for (int i = 0; i < ALL_EVENTS_SERVER + ALL_EVENTS_PICKING; i++) {
                System.out.println("event[" + i + "].t: " + event[i].t);
            }
            System.out.println("numero di job nel server: " + numberJobsServerOrder);
            System.out.println("partenze dal server: " + indexServer);
            System.out.println("numberJobsPickingCenter: " + numberJobsPickingCenter);
            System.out.println("partenze dal picking center: " + indexPickingCenter);*/


        } //end while
        //stampo i risultati

        System.out.println("partenze dal server: " + indexServer);
        System.out.println("numeri di job nel server: " + numberJobsServerOrder);
        System.out.println("partenze dal centro di picking: " + indexPickingCenter);
        System.out.println("numero di job nel centro di picking: " + numberJobsPickingCenter);
        System.out.println("utilizzazione Server: " + sum[1].service / t.current);
        System.out.println("utilizzazione Picking Center: " + sum[ALL_EVENTS_SERVER + ALL_EVENTS_PICKING - 1].service / t.current);


        DecimalFormat f = new DecimalFormat("###0.00");


    } //end main



    private double getServiceMultiServer(Rngs r, int streamIndex) {
        r.selectStream(20 + streamIndex );
        double m= 0.0;
        switch (streamIndex) {
            case 4:
                m = SERVICE_TIME_PICKING;
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

        while (i < ALL_EVENTS_SERVER + ALL_EVENTS_PICKING ) {         /* now, check the others to find which  */
            i++;
            /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
                e = i;
        }

        return (e);
    }

    int findOne(Event [] event,int center){
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s, i;
        switch (center){
            case PICKING:
                i = 3;
                break;
            default:
                i = -1;
                System.out.println("Errore nella selezione del centro");
                break;
        }


        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_PICKING ) {         /* now, check the others to find which   */
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

}
