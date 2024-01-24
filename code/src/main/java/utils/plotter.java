package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.jfree.chart.ChartUtils.saveChartAsPNG;

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
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(204, 204, 204));
        plot.setBackgroundPaint(Color.WHITE); // Imposta il colore di sfondo del grafico a bianco
        // Impostare la larghezza della linea
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Imposta la larghezza della linea della serie 0
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        setContentPane(chartPanel);
        // Salva il grafico come PNG
        // Salva il grafico come PNG
        File file = new File("C:\\Users\\Roberto\\Desktop\\center"+center);
        try {
            saveChartAsPNG(file, chartPanel.getChart(), 560, 370);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(204, 204, 204));
        plot.setBackgroundPaint(Color.WHITE); // Imposta il colore di sfondo del grafico a bianco
        // Impostare la larghezza della linea
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Imposta la larghezza della linea della serie 0
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        setContentPane(chartPanel);
        File file = new File("C:\\Users\\Roberto\\Desktop\\difetto.png");
        try {
            saveChartAsPNG(file, chartPanel.getChart(), 560, 370);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
