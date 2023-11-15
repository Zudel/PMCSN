package model;

public class SimulatorParameters {

    public static final double START   = 0.0;            /* initial (open the door)        */
    public static final double STOP    = 55800;        /* terminal (close the door) time */ //dalle 7 alle 24 in sec 61200.0; 55800 tolte 3 fasce //80000 orizzionte infinito
    public static final double    INFINITY =  100.0 * STOP;              /* number of servers              */
    public static final double STOP_BATCH    = 10958; /* close the door batch*/
    public static final double TOTAL_ARRIVAL_TO_SERVER    = 12145; /* close the door batch*/
    public static final double DELAY_NEXT_CENTER = 0.0001; /* ritardo nel passaggio da un centro all'altro*/


    //probability
    public static final double GOBACK_PROBABILITY = 0.16;
    public static final double LEAVE_PROBABILTY = 0.03;

    //events
    public static final int    SERVERS_PICKING = 40;              /* number of servers        60-70-80      ARE DEPARTURES*/
    public static final int   SERVERS_PACKING = 20;              /* number of servers        20-30-40      */
    public static final int    SERVERS_QUALITY = 10;              /* number of servers        10-20-30      */
    public static final int    SERVERS_SHIPPING = 10;

    public static final int EVENT_DEPARTURE_SERVER = 1;
    public static final int EVENT_ARRIVE_SERVER = 1;
    public static final int EVENTS_ARRIVE_PICKING = 1;
    public static final int EVENTS_ARRIVE_PACKING = 1;
    public static final int EVENTS_ARRIVE_QUALITY = 1;
    public static final int EVENTS_ARRIVE_SHIPPING = 2;
    public static final int EVENTS_ABANDONMENT_PICKING = 1;
    public static final int EVENTS_ABANDONMENT_PACKING = 1;
    public static final int EVENTS_ABANDONMENT_QUALITY = 1;
    public static final int EVENTS_ABANDONMENT_SHIPPING = 2;

    public static final int ALL_EVENTS_SERVER = EVENT_ARRIVE_SERVER + EVENT_DEPARTURE_SERVER;
    public static final int ALL_EVENTS_PICKING = EVENTS_ARRIVE_PICKING + EVENTS_ABANDONMENT_PICKING + SERVERS_PICKING ;
    public static final int ALL_EVENTS_PACKING = EVENTS_ARRIVE_PACKING + EVENTS_ABANDONMENT_PACKING + SERVERS_PACKING ;
    public static final int ALL_EVENTS_QUALITY = EVENTS_ARRIVE_QUALITY + EVENTS_ABANDONMENT_QUALITY + SERVERS_QUALITY ;
    public static final int ALL_EVENTS_SHIPPING = EVENTS_ARRIVE_SHIPPING + EVENTS_ABANDONMENT_SHIPPING + SERVERS_SHIPPING ;

    //per findOne
    public static final int SERVER = 0;
    public static final int PICKING = 1;
    public static final int PACKING = 2;
    public static final int QUALITY = 3;
    public static final int SHIPPING = 4;
    //service time
    public static final double SERVICE_TIME_SERVER = 2.5;
    public static final double SERVICE_TIME_PICKING = 240;
    public static final double SERVICE_TIME_PACKING = 120;
    public static final double SERVICE_TIME_QUALITY = 150;
    public static final double SERVICE_TIME_SHIPPING = 2400;
    public static final int SERVICE_TIME_SERVER_LOWER = 2;
    public static final int SERVICE_TIME_SERVER_UPPER = 3;



}
