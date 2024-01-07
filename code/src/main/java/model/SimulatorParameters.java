package model;

public class SimulatorParameters {
    public static final double ARRIVAL   = 2.0;      //una richiesta ogni 2 sec (qui va l'inverso) (ARRIVAL è lambda)
    public static final double START   = 0.0;            /* initial (open the door)        */
    public static final double STOP_BATCH    = 131072; /* close the door batch*/

    //probabilities
    public static final double QUALITY_CENTER_PROB = 0.33;
    public static final double ORDER_PRIME = 0.62;
    public static final double SORTING_FRAGILE_CENTER = 0.5;
    //SERVERS
    public static final int    SERVERS_PICKING = 35;              /* number of servers        60-70-80      ARE DEPARTURES*/
    public static final int   SERVERS_PACKING = 30;              /* number of servers        20-30-40      */
    public static final int    SERVERS_QUALITY = 70;
    public static final int    SERVERS_SORTING_FRAGILE_ORDERS = 150;
    public static final int    SERVERS_SORTING_NOT_FRAGILE_ORDERS = 150;

    //events
    public static final int EVENT_ARRIVAL_PICKING = 0;
    public static final int EVENT_DEPARTURE_PICKING = SERVERS_PICKING;
    public static final int EVENT_ARRIVAL_PACKING = SERVERS_PICKING + 1;
    public static final int EVENT_DEPARTURE_PACKING = (EVENT_ARRIVAL_PACKING + 1) + SERVERS_PACKING;
    public static final int EVENT_ARRIVAL_QUALITY = EVENT_DEPARTURE_PACKING + 1;
    public static final int EVENT_DEPARTURE_QUALITY = (EVENT_ARRIVAL_QUALITY + 1) + SERVERS_QUALITY;
    public static final int EVENT_ARRIVAL_SORTING_FRAGILE_PRIME_ORDERS = EVENT_DEPARTURE_QUALITY + 1;
    public static final int EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS = EVENT_DEPARTURE_QUALITY + 2;
    public static final int EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS = (EVENT_ARRIVAL_SORTING_FRAGILE_NOT_PRIME_ORDERS + 1) + SERVERS_SORTING_FRAGILE_ORDERS ;
    public static final int EVENT_ARRIVAL_SORTING_NOT_FRAGILE_PRIME_ORDERS = EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS + 1;
    public static final int EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS = EVENT_DEPARTURE_SORTING_FRAGILE_ORDERS + 2;
    public static final int EVENT_DEPARTURE_SORTING_NOT_FRAGILE_ORDERS = (EVENT_ARRIVAL_SORTING_NOT_FRAGILE_NOT_PRIME_ORDERS + 1) + SERVERS_SORTING_NOT_FRAGILE_ORDERS;

    //per findOne
    public static final int PICKING = 1;
    public static final int PACKING = 2;
    public static final int QUALITY = 3;
    public static final int SORTING_FRAGILE_ORDERS = 4;
    public static final int SORTING_NOT_FRAGILE_ORDERS = 5;

    //service time
    public static final double SERVICE_TIME_PICKING = 8;
    public static final double SERVICE_TIME_PACKING = 14;
    public static final double SERVICE_TIME_QUALITY = 150;//0.0066666
    public static final double SERVICE_TIME_SORTING_FRAGILE_ORDERS = 50; //0.02
    public static final double SERVICE_TIME_SORTING_NOT_FRAGILE_ORDERS = 50;
    public static final double STD_SERVICE_TIME_PICKING = 0.00886;
    public static final double STD_SERVICE_TIME_PACKING = 0.00932;
    public static final double STD_SERVICE_TIME_QUALITY = 0.25384;
    public static final double STD_SERVICE_SORTING_FRAGILE_ORDERS= 0.04292;
    public static final double STD_SERVICE_SORTING_NOT_FRAGILE_ORDERS = 0.13124;
    public static final double BERNOULLI_PROB_SUCCESS=  (double) (SERVERS_QUALITY  + SERVERS_SORTING_FRAGILE_ORDERS + SERVERS_SORTING_NOT_FRAGILE_ORDERS) /(SERVERS_PICKING + SERVERS_PACKING + SERVERS_QUALITY  + SERVERS_SORTING_FRAGILE_ORDERS + SERVERS_SORTING_NOT_FRAGILE_ORDERS);

}
