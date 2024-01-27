package model;

public class SimulatorParameters {
    public static final double ARRIVAL_L   = 2.0;      //(ARRIVAL = 2.0, 0.66 lambda = 0.5, 1.5)
    public static final double ARRIVAL_H   = 0.6666;
    public static final double ARRIVAL_M   = 1.0;
    public static final double START   = 0.0;            /* initial (open the door)        */
    public static final double STOP_BATCH    = 131072; /* close the door infinite horizon*/
    public static final double STOP    = 86400; /* close the door finite horizone 86400*/
    public static final int REPLICATION = 128;
    //probabilities
    public static final double QUALITY_CENTER_PROB = 0.33;
    public static final double ORDER_PRIME = 0.62;
    public static final double SORTING_FRAGILE_CENTER = 0.5;
    //SERVERS
    public static final int    SERVERS_PICKING = 64;
    public static final int   SERVERS_PACKING = 76;
    public static final int    SERVERS_QUALITY = 118;
    public static final int    SERVERS_SORTING_FRAGILE_ORDERS = 64;
    public static final int    SERVERS_SORTING_NOT_FRAGILE_ORDERS = 64;
    /**
     * M(48,60,85,48,49);}
     * H(74,86,112,64,64)
     L(44,47,44,30,30).}*/

    /**
     * provate nel finite horizon
     * H
     * CONFIGURAZIONE (64,76,118,64,64)
     * CONFIGURAZIONE (,,,,)
     * CONFIGURAZIONE (,,,,)
     * CONFIGURAZIONE (,,,,)
     * CONFIGURAZIONE (,,,,)
     * CONFIGURAZIONE (,,,,)
     * CONFIGURAZIONE (,,,,)
     * */

    public static final int    SERVERS_PICKING2 = 28;
    public static final int   SERVERS_PACKING2 = 37;
    public static final int    SERVERS_QUALITY2 = 44;
    public static final int    SERVERS_SORTING_FRAGILE_ORDERS2 = 30;
    public static final int    SERVERS_SORTING_NOT_FRAGILE_ORDERS2 = 30;
    public static final int    SERVERS_PICKING3 = 28;
    public static final int   SERVERS_PACKING3 = 37;
    public static final int    SERVERS_QUALITY3 = 44;
    public static final int    SERVERS_SORTING_FRAGILE_ORDERS3 = 30;
    public static final int    SERVERS_SORTING_NOT_FRAGILE_ORDERS3 = 30;

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
    public static final double STD_SERVICE_TIME_PICKING = 3.0;
    public static final double STD_SERVICE_TIME_PACKING = 5.0;
    public static final double STD_SERVICE_TIME_QUALITY = 30.0;
    public static final double STD_SERVICE_SORTING_FRAGILE_ORDERS= 10.7;
    public static final double STD_SERVICE_SORTING_NOT_FRAGILE_ORDERS = 10.1;
    public static final double BERNOULLI_PROB_SUCCESS= 1 - (1.0/ (double) (SERVERS_PICKING+ SERVERS_PACKING));
    public static final int LOWER_BOUND_NORMAL = 0;

}
