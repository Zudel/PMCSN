package utils;/* ----------------------------------------------------------------------
 * This program reads a data sample from a text file in the format
 *                         one data point per line 
 * and calculates an interval estimate for the mean of that (unknown) much 
 * larger set of data from which this sample was drawn.  The data can be 
 * either discrete or continuous.  A compiled version of this program 
 * supports redirection and can used just like program uvs.c. 
 * 
 * Name              : Estimate.java (Interval Estimation) 
 * Authors           : Steve Park & Dave Geyer 
 * Translated By     : Richard Dutton & Jun Wang
 * Language          : Java
 * Latest Revision   : 6-16-06 
 * ----------------------------------------------------------------------
 */

import com.opencsv.CSVReader;
import mathLib.Rvms;

import java.lang.Math;
import java.io.*;
import java.text.*;
import java.util.List;
import java.util.StringTokenizer;

public class Estimate {

	static final double LOC = 0.95;    /* level of confidence,        */
	/* use 0.95 for 95% confidence */

	public void main(double[] array) {
		long n = 0;                     /* counts data points */
		double sum = 0.0;
		double mean = 0.0;
		double data;
		double stdev;
		double u, t, w;
		double diff;

		Rvms rvms = new Rvms();

		for (int i = 0; i < array.length; i++) { /* use Welford's one-pass method */
			data = array[i];

			n++; /* and standard deviation */
			diff = data - mean;
			sum += diff * diff * (n - 1.0) / n;
			mean += diff / n;
		}

		stdev = Math.sqrt(sum / n);

		DecimalFormat df = new DecimalFormat("###0.00000");

		if (n > 1) {
			u = 1.0 - 0.5 * (1.0 - LOC);              /* interval parameter  */
			t = rvms.idfStudent(n - 1, u);            /* critical value of t */
			w = t * stdev / Math.sqrt(n - 1);         /* interval half width */

			System.out.println();
			System.out.print("\nbased upon " + n + " data points");
			System.out.print(" and with " + (int) (100.0 * LOC + 0.5) +
					"% confidence\n");
			System.out.print("the expected value is in the interval ");
			System.out.print(df.format(mean) + " +/- " + df.format(w) + "\n");
		} else {
			System.out.print("ERROR - insufficient data\n");
		}
	}

	public void estimateFiniteHorizon(double[] array, String name) {
		long n = 0;                     /* counts data points */
		double sum = 0.0;
		double mean = 0.0;
		double data;
		double stdev;
		double u, t, w;
		double diff;

		Rvms rvms = new Rvms();

		for (int i = 0; i < array.length; i++) { /* use Welford's one-pass method */
			data = array[i];
			n++; /* and standard deviation */
			diff = data - mean;
			sum += diff * diff * (n - 1.0) / n;
			mean += diff / n;
		}

		stdev = Math.sqrt(sum / n);

		DecimalFormat df = new DecimalFormat("###0.00000");

		if (n > 1) {
			u = 1.0 - 0.5 * (1.0 - LOC);              /* interval parameter  */
			t = rvms.idfStudent(n - 1, u);            /* critical value of t */
			w = t * stdev / Math.sqrt(n - 1);         /* interval half width */

			System.out.println(name);
			System.out.print("\nbased upon " + n + " data points");
			System.out.print(" and with " + (int) (100.0 * LOC + 0.5) +
					"% confidence\n");
			System.out.print("the expected value is in the interval ");
			System.out.print(df.format(mean) + " +/- " + df.format(w) + "\n");
		} else {
			System.out.print("ERROR - insufficient data\n");
		}
	}

	public static void estimateOnFile(String csvName) {
		long n = 0;                     // Conta i punti dati
		double sum = 0.0;
		double mean = 0.0;
		double data;
		double stdev;
		double diff;
		final double LOC = 0.95;        // Livello di confidenza
		Rvms rvms = new Rvms();

		try (BufferedReader br = new BufferedReader(new FileReader(csvName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				System.out.print(line);  // Stampa la riga (dato)
				for(int i =0 ; i < 128;i++){
				data = Double.parseDouble(values[0]);

				n++;  // Incrementa il conteggio dei punti dati
				diff = data - mean;
				sum += diff * diff * (n - 1.0) / n;
				mean += diff / n;
				}

			}

			stdev = Math.sqrt(sum / n);

			DecimalFormat df = new DecimalFormat("###0.00");

			if (n > 1) {
				double u = 1.0 - 0.5 * (1.0 - LOC);       // Parametro di intervallo
				double t = rvms.idfStudent(n - 1, u);          // Valore critico di t
				double w = t * stdev / Math.sqrt(n - 1);  // Larghezza dell'intervallo

				System.out.print("\nBasato su " + n + " punti dati");
				System.out.print(" e con " + (int) (100.0 * LOC + 0.5) + "% di confidenza\n");
				System.out.print("il valore atteso è nell'intervallo ");
				System.out.print(df.format(mean) + " +/- " + df.format(w) + "\n");
			} else {
				System.out.print("ERRORE - dati insufficienti\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
