package utils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class CSVLibrary {
    private CSVLibrary() throws IOException {
    }
    public static void writeToCsv(double[] data, String csvName) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvName,true))) {
            // Scrivi i dati dell'array nel file CSV
            for (double value : data) {
                writer.write(String.valueOf(value)); //rciorda che se sul csv vuoi valori interi fai il cast!
                writer.write(",");
            }
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeToCsvProb(double data, String csvName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvName, true))) {
            writer.write(String.valueOf(data)); // Ricorda che se sul CSV vuoi valori interi fai il cast!
            writer.write(",");
            writer.newLine();
            // Aggiungi una nuova linea dopo ogni riga di dati
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static double[] readCSVFile(int index, String csvName) {
        CSVReader reader = null;
        double[] arrayValues = new double[128];
        try {
            reader = new CSVReader(new FileReader(csvName),',');
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<String[]> allRows = null;
        try {
            allRows = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i =0;
        //allRows.remove(0);
        for (String[] row : allRows) { //prendo una riga
            //System.out.println(row[index]);
            arrayValues[i] = Double.valueOf(row[index]);
            i++;
            if(i == 128) break;
        }

        return arrayValues;
    }
}
