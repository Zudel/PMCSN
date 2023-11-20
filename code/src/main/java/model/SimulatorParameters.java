package model;

public class SimulatorParameters {

    public static final double START   = 0.0;            /* initial (open the door)        */
    public static final double STOP    = 55800;        /* terminal (close the door) time */ //dalle 7 alle 24 in sec 61200.0; 55800 tolte 3 fasce //80000 orizzionte infinito
    public static final double    INFINITY =  100.0 * STOP;              /* number of servers              */
    public static final double STOP_BATCH    = 10958; /* close the door batch*/
    public static final double TOTAL_ARRIVAL_TO_SERVER    = 12145; /* close the door batch*/

    //probabilities
    public static final double QUALITY_CENTER_PROB = 0.34;
    public static final double PICKING_CENTER_PROB = 0.05;
    public static final double ORDER_PRIME = 0.62;

    //events
    public static final int    SERVERS_PICKING = 40;              /* number of servers        60-70-80      ARE DEPARTURES*/
    public static final int   SERVERS_PACKING = 20;              /* number of servers        20-30-40      */
    public static final int    SERVERS_QUALITY = 10;              /* number of servers        10-20-30      */
    public static final int    SERVERS_SHIPPING = 10;

    public static final int EVENT_DEPARTURE_SERVER = 1;
    public static final int EVENT_ARRIVE_SERVER = 1;
    public static final int EVENT_ABANDONMENT_PICKING = 1;
    public static final int EVENT_ABANDONMENT_PACKING = 1;
    public static final int EVENT_ABANDONMENT_QUALITY = 1;
    public static final int EVENT_ABANDONMENT_NOT_PRIME_QUEUE  = 1;
    public static final int EVENT_ABANDONMENT_PRIME_QUEUE = 1;

    public static final double PATIENCE = 300;
    public static final double PATIENCE_ORDER_PRIME = 240;
    public static final double PATIENCE_ORDER_NOT_PRIME = 180;

    public static final int ALL_EVENTS_SERVER = EVENT_ARRIVE_SERVER + EVENT_DEPARTURE_SERVER;
    public static final int ALL_EVENTS_PICKING = SERVERS_PICKING + ALL_EVENTS_SERVER + 1 ;
    public static final int ALL_EVENTS_PACKING = ALL_EVENTS_PICKING + SERVERS_PACKING + 1  ;
    public static final int ALL_EVENTS_QUALITY = ALL_EVENTS_PACKING + SERVERS_QUALITY + 1 ;
    public static final int ALL_ABANDON_EVENTS = EVENT_ABANDONMENT_PICKING + EVENT_ABANDONMENT_PACKING + EVENT_ABANDONMENT_QUALITY + EVENT_ABANDONMENT_NOT_PRIME_QUEUE + EVENT_ABANDONMENT_PRIME_QUEUE;
    public static final int ALL_EVENTS = ALL_EVENTS_QUALITY  + SERVERS_SHIPPING + ALL_ABANDON_EVENTS;

    //per findOne
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
    public static final double SERVICE_TIME_SHIPPING_PRIME = 1000;
    public static final int SERVICE_TIME_SERVER_LOWER = 2;
    public static final int SERVICE_TIME_SERVER_UPPER = 3;



}
