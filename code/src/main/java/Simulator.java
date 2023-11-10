import mathLib.Rngs;
import mathLib.Rvms;
import utils.*;

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
        double serviceServer;
        long indexServer   = 0;             /* used to count departed jobs         */
        long indexPickingCenter   = 0;             /* used to count departed jobs         */
        long indexPackingCenter   = 0;             /* used to count departed jobs         */
        long indexQualityCenter   = 0;             /* used to count departed jobs         */
        long indexShippingCenter   = 0;             /* used to count departed jobs         */

        Simulator sim = new Simulator();
        Rngs r = new Rngs();
        T t = new T();
        Event[] event = new Event[ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING];
        Sum[] sum = new Sum[ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING + ALL_EVENTS_PICKING + ALL_EVENTS_SERVER];
        for (int s = 0; s < ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING + ALL_EVENTS_PICKING + ALL_EVENTS_SERVER; s++) {
            event[s] = new Event();
            sum [s]  = new Sum();
        }

        List<Double> abandonmentPicking = new ArrayList<>();
        List<Double> abandonmentPacking = new ArrayList<>();
        List<Double> abandonmentQuality = new ArrayList<>();
        List<Double> abandonmentShippingPrime = new ArrayList<>();
        List<Double> abandonmentShippingNotPrime = new ArrayList<>();
        List<Double> serverResponseTimes = new ArrayList<>();
        r.putSeed(123456789);
        t.current    = START;
        t.completionServer = INFINITY;
        event[0].t   = sim.getArrivalServer(r, t.current, 1); //first arrival to the server node
        event[0].x   = 1;

        for (int i = 1; i < ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING; i++) { //messo il + 2 perchè ho aggiunto il dispatcher e +14 per i due centri dei guasti?
            event[i].t     = START;          /* this value is arbitrary because */
            event[i].x     = 0;              /* all servers are initially idle  */
            sum[i].service = 0.0;
            sum[i].served  = 0;
        }

        for (int i = 0; i < listaFasciaOraria.size(); i++) { //stampa la lista delle fasce orarie
            System.out.println(listaFasciaOraria.get(i).getFasciaOraria() + " " + listaFasciaOraria.get(i).getFrequenza() + " " + listaFasciaOraria.get(i).getProporzione()+"%");
        }


        while (t.current < STOP || (numberJobsServerOrder + numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsShippingCenter > 0)){
            e         = sim.nextEvent(event, 1);
            t.next    = event[e].t;
            areaServer     += (t.next - t.current) * numberJobsServerOrder;
            t.current = t.next;

            if (e == 0) { //e == 0 //arrivo al server
                numberJobsServerOrder++; //arrived job
                event[0].t        = sim.getArrivalServer(r, t.current, 1); //aggiustare il 1
                if (event[0].t > STOP)
                    event[0].x      = 0;
                if (numberJobsServerOrder <= 1) {
                    serviceServer         = sim.getServiceField(r, 2); //aggiustare il 1

                    sum[1].service += serviceServer;
                    sum[1].served++;
                    event[1].t      = t.current + serviceServer;
                    event[1].x      = 1;
                }
            }
            else { //e == 1
                indexServer++; //departed job
                numberJobsServerOrder--; //decremento il numero di job nel server
                if (numberJobsServerOrder >= 1) {
                    serviceServer         = sim.getServiceField(r, 2); //aggiustare il 1
                    sum[1].service += serviceServer;
                    sum[1].served++;
                    event[1].t      = t.current + serviceServer;
                }
                else
                    event[1].x      = 0;
            }

            /**
             e         = m.nextEvent(event);
            t.next    = event[e].t;
            area     += (t.next - t.current) * number;
            t.current = t.next;

            if (e == 0) {
                number++;
                event[0].t        = m.getArrival(r);
                if (event[0].t > STOP)
                    event[0].x      = 0;
                if (number <= SERVERS) {
                    service         = m.getService(r);
                    s               = m.findOne(event);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t      = t.current + service;
                    event[s].x      = 1;
                }
            }
            else {
                index++;
                number--;
                s                 = e;
                if (number >= SERVERS) {
                    service         = m.getService(r);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t      = t.current + service;
                }
                else
                    event[s].x      = 0;
            }
            */


        }

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
        sarrival += rvms.idfPoisson(listaFasciaOraria.get(index).getMeanPoisson(), r.random());
        return (sarrival);
    }

    int nextEvent(Event[] event, int SERVERS) {
        /* ---------------------------------------
         * return the index of the next event type
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

    double getServiceField(Rngs r, int streamIndex){
        //servizio del centro on field --> esponenziale
        r.selectStream(20 + streamIndex);
        double m = SERVICE_TIME_SERVER;
        return (-m * Math.log(1.0 - r.random()));
    }

}
