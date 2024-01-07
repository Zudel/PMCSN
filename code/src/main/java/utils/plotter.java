package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class plotter extends JFrame {


    public plotter(String title, String asseY, double[][] data, int k, int b, int center) {
        super(title);

        XYSeries series = new XYSeries("Funzione");
        for (int j=0; j < k ; j++) {
                series.add(j*b, data[center][j]); // Inserire qui la propria funzione
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, // Titolo del grafico
                "numero di job", // Etichetta asse X
                asseY, // Etichetta asse Y
                dataset, // Dati
                PlotOrientation.VERTICAL,
                true, // Mostra la legenda
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        setContentPane(chartPanel);
    }

    public plotter(String sistema, String asseY, double[] data, int k, int batchSize) {
        super(sistema);

        XYSeries series = new XYSeries("Funzione");
        for (int j=0; j < k ; j++) {
            series.add(j*batchSize, data[j]); // Inserire qui la propria funzione
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                sistema, // Titolo del grafico
                "numero di job", // Etichetta asse X
                asseY, // Etichetta asse Y
                dataset, // Dati
                PlotOrientation.VERTICAL,
                true, // Mostra la legenda
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        setContentPane(chartPanel);
    }

    /*public  void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            plotter example = new plotter("Grafico", );
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }*/
}
