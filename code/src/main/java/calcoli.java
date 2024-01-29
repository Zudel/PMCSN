import com.google.common.math.BigIntegerMath;
import mathLib.Rngs;

import java.math.BigInteger;

public class calcoli {

    public static void main(String[] args) {
        double m = 150.0;
        double rho = 0.3407;
        double prod =  (m*rho);
        double s = 0;
        for (long i =0; i < m; i++){
                    s += (double)  Math.pow(prod, i) / (double) fact(i);
        }
        double totSum = s + Math.pow(prod, m)/ (fact((int )m)/(1-rho));
        double p0= (double) 1/totSum;
        System.out.println(1-(p0*(Math.pow(prod, m)/(fact((int)m)/(1-rho)))));

    }

    private static long fact(long n) {
        long fact = 1;
        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }

    public void pickingQueue(){

    }
}
