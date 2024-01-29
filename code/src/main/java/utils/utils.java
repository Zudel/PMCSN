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
    static List<Double> frequenze = new ArrayList();
    static List<Double> proporzioni = new ArrayList();
   

    public static List<FasciaOraria> LeggiCSV(String filePath) {

        double sommaProporzioni = 0;
        double frequenzaOraria = 0;

        try {
            FileReader fileReader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build(); // Ignora l'intestazione

            String[] line;
            //csvReader.readNext(); // Ignora l'intestazione
            while ((line = csvReader.readNext()) != null) {
                    fasceOrari.add(line[0]); // Aggiungi la fascia oraria alla lista
                    frequenze.add(Double.valueOf(line[1])); // Aggiungi la frequenza alla lista
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
        List<FasciaOraria> lista = new ArrayList<>();
        for (int i = 0; i < fasceOrari.size(); i++) {
            frequenzaOraria = frequenze.get(i) / 3600;
            lista.add(new FasciaOraria(fasceOrari.get(i), frequenzaOraria,  proporzioni.get(i),  i  ,0+ 3600*i, 3600+3600*i));//3600 secondi in un ora
        }



    return lista;
}
}

