package utils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class CSVLibrary {
    String csvName;
    public CSVLibrary(String csvName) throws IOException {
        this.csvName =csvName;
    }
    public void writeToCsv(double[] data) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvName))) {
            // Scrivi l'header, se necessario
             writer.write("E(Ts),E(Ns),rho,E(Tq),E(Nq)"); // Sostituisci con i nomi delle colonne

            // Vai a capo dopo l'header
            // writer.newLine();

            // Scrivi i dati dell'array nel file CSV
            for (double value : data) {
                writer.write(String.valueOf(value));
                writer.write(",");
            }

            // Vai a capo alla fine di ogni riga
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Double[] readCSVFile() {
        CSVReader reader = null;
        Double[] arrayValues = new Double[128];
        try {
            reader = new CSVReader(new FileReader(csvName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<String[]> allRows = null;
        try {
            allRows = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        allRows.remove(0);
        for (String[] row : allRows) { //prendo una riga
            System.out.println(row[1]);
            System.out.println(Arrays.toString(row));
        }
        return arrayValues;
    }
}
