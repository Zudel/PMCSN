package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class plotter extends JFrame {

    public plotter(String title) {
        super(title);

        XYSeries series = new XYSeries("Funzione");
        for (int x = 0; x <= 200; x++) {
            series.add(x, Math.pow(x, 2)); // Inserire qui la propria funzione
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Grafico di una Funzione Quadratica", // Titolo del grafico
                "X", // Etichetta asse X
                "Y", // Etichetta asse Y
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            plotter example = new plotter("Grafico di una Funzione Quadratica");
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
