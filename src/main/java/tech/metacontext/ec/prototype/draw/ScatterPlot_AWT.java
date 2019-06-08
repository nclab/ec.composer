/*
 * Copyright 2018 Jonathan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.metacontext.ec.prototype.draw;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ScatterPlot_AWT extends ApplicationFrame {

    public static void main(String[] args) {

        ScatterPlot_AWT plot = new ScatterPlot_AWT("Demo");
        List<SimpleEntry<Integer, Double>> series1 = generate(), series2 = generate();
        plot.addSeries("series1", series1);
        plot.addSeries("series2", series2);
        plot.createScatterPlot("HelloWorld", "Generation", "Score", 560, 367, true);
        plot.addHorizontalLine(0.9, Color.yellow);
        plot.showPlotWindow();
    }

    private static List<SimpleEntry<Integer, Double>> generate() {
        return Stream.generate(() -> new SimpleEntry<>(new Random().nextInt(50), Math.random()))
                .limit(50)
                .collect(Collectors.toList());
    }

    XYSeriesCollection dataset;
    JFreeChart scatterPlot;

    public ScatterPlot_AWT(String applicationTitle) {

        super(applicationTitle);
        dataset = new XYSeriesCollection();
    }

    public void addSeries(String series_name,
            List<SimpleEntry<Integer, Double>> scores) {

        XYSeries series = new XYSeries(series_name);
        scores.stream().forEach(e -> series.add(e.getKey(), e.getValue()));
        dataset.addSeries(series);
    }

    public void setSeriesDot(int series, double size, Color color) {

        XYItemRenderer renderer = scatterPlot.getXYPlot().getRenderer();
        renderer.setSeriesPaint(series, color);
        double delta = size / 2.0;
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);
        renderer.setSeriesShape(series, shape);
    }

    public void addHorizontalLine(double position, Color color) {
        
        ValueMarker marker = new ValueMarker(position);
        marker.setPaint(color);
        scatterPlot.getXYPlot().addRangeMarker(marker);
    }

    public void createScatterPlot(String chartTitle, String xLabel, String yLabel,
            int x, int y, boolean legend) {

        scatterPlot = ChartFactory.createScatterPlot(
                chartTitle, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL,
                legend, true, false);
        this.setSeriesDot(0, 3.0, Color.BLUE);
        this.setSeriesDot(1, 3.0, Color.RED);
        ChartPanel chartPanel = new ChartPanel(scatterPlot);
        chartPanel.setPreferredSize(new java.awt.Dimension(x, y));
        setContentPane(chartPanel);
    }

    public void showPlotWindow() {

        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

}
