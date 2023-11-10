package utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Dato un CSV con 2 colonne (fascia oraria, frequenza) legge il CSV e calcolando il totale delle frequenze e
 * le proporzioni, stampando a video i risultati.
 * */
public class utils {
    static List<String> fasceOrari = new ArrayList();
    static List<Integer> frequenze = new ArrayList();
    static List<Double> proporzioni = new ArrayList();

    public static List<fasciaOraria> LeggiCSV(String filePath) {

        double sommaProporzioni = 0;

        try {
            FileReader fileReader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build(); // Ignora l'intestazione

            String[] line;
            //csvReader.readNext(); // Ignora l'intestazione
            while ((line = csvReader.readNext()) != null) {
                    fasceOrari.add(line[0]); // Aggiungi la fascia oraria alla lista
                    frequenze.add(Integer.valueOf(line[1])); // Aggiungi la frequenza alla lista
                    //proporzioni.add(Double.valueOf(line[2]));
            }

            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //calcola la somma delle frequenze
        int sommaFrequenze = 0;
        for (int i = 0; i < frequenze.size(); i++) {
            sommaFrequenze += frequenze.get(i);
        }
        //calcola le proporzioni
        for (int i = 0; i < frequenze.size(); i++) {
            proporzioni.add(Double.valueOf(frequenze.get(i)) / sommaFrequenze);
            sommaProporzioni += proporzioni.get(i);
        }


        //crea una lista di ritorno che crea oggetti fasciaOraria con i valori letti dal csv
        List<fasciaOraria> lista = new ArrayList<>();
        for (int i = 0; i < fasceOrari.size(); i++) {

            lista.add(new fasciaOraria(fasceOrari.get(i), frequenze.get(i), proporzioni.get(i),  i, 0  ,0+ 3600*i, 3600+3600*i));//3600 secondi in un ora
        }


        //stampa a video i risultati


        /*for (int i = 0; i < fasceOrari.size(); i++) {
            System.out.println("Fascia oraria: " + fasceOrari.get(i) + ", Frequenza: " + frequenze.get(i) + ", Proporzione: " + proporzioni.get(i));

        System.out.println("Somma delle frequenze: " + sommaFrequenze
            + ", Somma delle proporzioni: " + sommaProporzioni);
    }*/


    return lista;
}

}
