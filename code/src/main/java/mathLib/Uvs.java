package mathLib;/* -------------------------------------------------------------------------
 * This program reads a (text) data file and computes the mean, minimum, 
 * maximum, and standard deviation.   The one-pass algorithm used is due to 
 * B.P. Welford (Technometrics, vol. 4, no 3, August 1962.) 
 *
 * NOTE: the text data file is assumed to be in a one-value-per-line format
 * with NO blank lines in the file.   The data can be either fixed point 
 * (integer valued), or floating point (real valued). 
 *
 * To use the program, compile it to disk to produce uvs.  Then at a command 
 * line prompt, uvs can be used in three ways. 
 *
 * (1) To have uvs read a disk data file, say uvs.dat (in the format above),
 * at a command line prompt use '<' redirection as: 
 *
 *     uvs < uvs.dat
 *
 * (2) To have uvs filter the numerical output of a program, say test, at a
 * command line prompt use '|' pipe as: 
 *
 *     test | uvs
 *
 * (3) To use uvs with keyboard input, at a command line prompt enter:
 *
 *      uvs
 *
 * Then enter the data -- one value per line -- being sure to remember to
 * signify an end-of-file.  In Unix/Linux, signify an end-of-file by
 * entering ^d (Ctrl-d) as the last line of input.
 * 
 * Name              : Uvs.java (Multi-Server Queue)
 * Authors           : Steve Park & Dave Geyer
 * Translated by     : Jun Wang
 * Language          : Java
 * Latest Revision   : 6-16-06
 *
 * Program uvs       : Section 4.1, based on Algorithm 4.1.1
 * -------------------------------------------------------------------------
 */

import java.io.*;
import java.util.*;
import java.text.*;


public class Uvs {

  public  void compute(double[] records) throws IOException {
    double[][] dati = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
            // Aggiungi altri dati secondo le tue esigenze
    };

    long    index;
    double  data;
    double  sum = 0.0;
    double  mean;
    double  stdev;
    double  min;
    double  max;
    double  diff;

    index = 0;
    mean  = 0.0;
    min   = 0.0;
    max   = 0.0;
    
    try {
        System.out.println("/------/");
        for (double valore : records) {
          data = valore;
          index++;
          diff = data - mean;
          sum += diff * diff * (index - 1.0) / index;
          mean += diff / index;
          if (data > max)
            max = data;
          else if (data < min)
            min = data;
        }
    } catch (Exception nfe) {
       System.out.println("Uvs: " + nfe);
    }

    if (index > 0) {
      DecimalFormat f = new DecimalFormat("###0.000");
      stdev = Math.sqrt(sum / index);
      System.out.println("\nfor a sample of size (number of batches k) " + index);
      System.out.println("mean ................. =  " + f.format(mean));
      System.out.println("standard deviation ... =  " + f.format(stdev));
      System.out.println("minimum .............. =  " + f.format(min));
      System.out.println("maximum .............. =  " + f.format(max));
    }
  }
}
