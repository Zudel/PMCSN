import mathLib.Rngs;
import utils.*;

import java.util.ArrayList;
import java.util.List;

import static model.SimulatorParameters.*;

class MsqEvent {                     /* the next-event list    */
    double t;                         /*   next event time      */
    int x;                         /*   event status, 0 or 1 */
}
class MsqSum {                      /* accumulated sums of                */
    double service;                   /*   service times                    */
    long served;                    /*   number served                    */
}
class MsqT {
    double current;                   /* current time                       */
    double next;                      /* next (most imminent) event time    */
}

public class Simulator {
    static List<fasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");
    public static void main(String[] args) { //start point of the program execution

        int numberJobsServerOrder = 0;
        int numberJobsPickingCenter = 0;
        int numberJobsPackingCenter = 0;
        int numberJobsQualityCenter = 0;
        int numberJobsShippingCenter = 0;
        double areaServer   = 0.0;           /* time integrated number in the node */
        double areaPickingCenter   = 0.0;           /* time integrated number in the node */
        double areaPackingCenter   = 0.0;           /* time integrated number in the node */
        double areaQualityCenter   = 0.0;           /* time integrated number in the node */
        double areaShippingCenter   = 0.0;           /* time integrated number in the node */
        long indexServer   = 0;             /* used to count departed jobs         */
        long indexPickingCenter   = 0;             /* used to count departed jobs         */
        long indexPackingCenter   = 0;             /* used to count departed jobs         */
        long indexQualityCenter   = 0;             /* used to count departed jobs         */
        long indexShippingCenter   = 0;             /* used to count departed jobs         */

        Simulator sim = new Simulator();
        Rngs r = new Rngs();
        MsqT t = new MsqT();
        MsqEvent[] event = new MsqEvent[ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING + ALL_EVENTS_PICKING + ALL_EVENTS_SERVER];
        List<Double> abandonmentPicking = new ArrayList<>();
        List<Double> abandonmentPacking = new ArrayList<>();
        List<Double> abandonmentQuality = new ArrayList<>();
        List<Double> abandonmentShippingPrime = new ArrayList<>();
        List<Double> abandonmentShippingNotPrime = new ArrayList<>();

        r.putSeed(123456789);
        t.current    = START;
        event[0].t   = sim.getArrival(r, t.current, 1); //first arrival to the server node
        event[0].x   = 1;


        for (int i = 1; i < ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING; i++) { //messo il + 2 perchè ho aggiunto il dispatcher e +14 per i due centri dei guasti?
            event[i].t     = START;          /* this value is arbitrary because */
            event[i].x     = 0;              /* all servers are initially idle  */
            sum[i].service = 0.0;
            sum[i].served  = 0;
        }

        for (int i = 0; i < listaFasciaOraria.size(); i++) {
            System.out.println(listaFasciaOraria.get(i).getFasciaOraria() + " " + listaFasciaOraria.get(i).getFrequenza() + " " + listaFasciaOraria.get(i).getProporzione()+"%");
        }
        for (int i = 0; i < ALL_EVENTS_SERVER + ALL_EVENTS_PICKING + ALL_EVENTS_PACKING + ALL_EVENTS_QUALITY + ALL_EVENTS_SHIPPING; i++) {
            event[i] = new MsqEvent();
            sum[i] = new MsqSum();
        }

        while (t.current < STOP || (numberJobsServerOrder + numberJobsPickingCenter + numberJobsPackingCenter + numberJobsQualityCenter + numberJobsShippingCenter > 0)){

        }

    } //end main

    private double getArrival(Rngs r, double current, int i) {

        return 0;
    }

    int nextEvent(MsqEvent[] event, int SERVERS) {
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

    int findOne(MsqEvent [] event, int SERVERS) {
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

}
