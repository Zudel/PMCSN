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

    public static void LeggiCSV(String filePath) {
        List<String> fasceOrari = new ArrayList();
        List<Double> frequenze = new ArrayList();
        List<Double> proporzioni = new ArrayList();

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
            proporzioni.add(frequenze.get(i) / sommaFrequenze);
        }
        // Ora hai i dati nelle liste fasceOrari, frequenze e proporzioni
        for (int i = 0; i < fasceOrari.size(); i++) {
            System.out.println("Fascia oraria: " + fasceOrari.get(i) + ", Frequenza: " + frequenze.get(i) + ", Proporzione: " + proporzioni.get(i));

        }
        System.out.println("Somma delle frequenze: " + sommaFrequenze);
    }

}
