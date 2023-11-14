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
    double completionServer;              /* next completion time server               */
}

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
        while ( t.current < 86.400 ) {

            e         = Math.min(event[0].t, event[1].t) == event[0].t ? 0 : 1; //e = 0 o 1
            t.next    = event[e].t;
            areaServer     += (t.next - t.current) * numberJobsServerOrder;     //update integral of server
            t.current = t.next;                                                 //advance the simulation clock


            if (e == 0) { //arrivo al server
                numberJobsServerOrder++;
                event[0].t        = sim.getArrivalServer(r, t.current, 1);

                if (numberJobsServerOrder == 1) {                                   //if there is only one job in the node, schedule a completion
                    serviceServer         = sim.getServiceServer(r, 2);

                    sum[1].service += serviceServer;
                    sum[1].served++;
                    event[1].t      = t.current + serviceServer;
                    event[1].x      = 1;
                }
            }
            else { //partenza dal server
                indexServer++;
                numberJobsServerOrder--;
                if (numberJobsServerOrder > 0) {
                    serviceServer         = sim.getServiceServer(r, 2); //aggiustare il 1

                    sum[1].service += serviceServer;
                    sum[1].served++;
                    event[1].t      = t.current + serviceServer;
                    event[1].x      = 1;
                }
                else { //the queue is empty so make the node idle and eliminate the completion event from consideration
                    event[1].x = 0;
                    event[1].t = INFINITY; //se non ci sono job in coda, il server rimane inattivo
                }
                    /*System.out.println("departed job: " + indexServer);
                    System.out.println("numberJobsServerOrder: " + numberJobsServerOrder);
                    System.out.println("indexServer: " + indexServer);
                    System.out.println("event[1].t: " + event[1].t);
                    System.out.println("event[1].x: " + event[1].x);
                    System.out.println("event[0].t: " + event[0].t);
                    System.out.println("event[0].x: " + event[0].x);*/

            }



        } //end while
        //stampo i risultati
        System.out.println("indexServer: " + indexServer);
        System.out.println("numberJobsServerOrder: " + numberJobsServerOrder);
        System.out.println("utilizationServer: " + areaServer / indexServer);

        DecimalFormat f = new DecimalFormat("###0.00");

       //System.out.println("   utilization ............. =   " + f.format(sum[1].service / t.current));



    } //end main

    private double getService(Rngs r) {

        return 0.0;
    }

    private double getArrivalServer(Rngs r, double currentTime, int streamIndex) {
        /* --------------------------------------------------------------
         * generate the next arrival time with idfPoisson
         * --------------------------------------------------------------
         */
        r.selectStream(0 + streamIndex);

        Rvms rvms = new Rvms();
        int index = utils.fasciaOrariaSwitch(listaFasciaOraria, currentTime);
        sarrival += rvms.idfPoisson(0.030/*listaFasciaOraria.get(index).getMeanPoisson()*/, r.random());
        return (sarrival);
    }

    int nextEvent(Event[] event, int SERVERS) {
        /* ---------------------------------------
         * return the index of the next event type from the event list
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */
        e = i;
        while (i < SERVERS) {         /* now, check the others to find which  */
            i++;                        /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
                e = i;
        }
        return (e);
    }

    int findOne(Event [] event, int SERVERS) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = 1;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }

    double getServiceServer(Rngs r, int streamIndex){
        //servizio del centro server --> Uniforme
        r.selectStream(20 + streamIndex);
        int a = SERVICE_TIME_SERVER_LOWER;
        int b = SERVICE_TIME_SERVER_UPPER;
        return (a + (b - a) * r.random());
    }

}
