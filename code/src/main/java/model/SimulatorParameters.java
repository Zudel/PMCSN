package model;

public class SimulatorParameters {

    public static final double START   = 0.0;            /* initial (open the door)        */
    public static final double STOP    = 55800;        /* terminal (close the door) time */ //dalle 7 alle 24 in sec 61200.0; 55800 tolte 3 fasce //80000 orizzionte infinito
    public static final double STOP_BATCH    = 10958; /* close the door batch*/
    public static final int    SERVERS_PICKING = 70;              /* number of servers        60-70-80      */

}
