import com.google.common.math.BigIntegerMath;
import mathLib.Rngs;

import java.math.BigInteger;

public class calcoli {

    public static void main(String[] args) {
        long m = 15;
        double rho = 0.71716;
        double prod =  (m*rho);
        double s = 0;
        for (long i =0; i < m; i++){
                    s += Math.pow(prod, i) / fact(i);
        }
        double totSum = s + Math.pow(prod, m)/(fact(m)/(1-rho));
        double p0= 1/totSum;
        System.out.println(p0*(Math.pow(prod, m)/(fact(m)/(1-rho))));

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
