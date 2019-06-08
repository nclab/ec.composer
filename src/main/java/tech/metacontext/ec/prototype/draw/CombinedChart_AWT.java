/*
 * Copyright 2018 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.ScatterRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Rendering a Line Chart.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CombinedChart_AWT extends ApplicationFrame {

    public static void main(String[] args) {

        CombinedChart_AWT chart = new CombinedChart_AWT("Demo");
        Map<Integer, List<Double>> series1A = new HashMap<>(),
                series1B = new HashMap<>();
        Map<Integer, Double> series2 = new HashMap<>();
        IntStream.range(0, 50).forEach(i -> {
            series1A.put(i, new Random().doubles(10, Math.random(), (50.0 + i) / 50 + Math.random())
                    .boxed().collect(Collectors.toList()));
            if (i % 2 == 0) {
                series1B.put(i, new Random().doubles(10, Math.random(), 1.0 + Math.random())
                        .boxed().sorted().collect(Collectors.toList()));
            } else {
                series1B.put(i, new ArrayList<>());
            }
            series2.put(i, series1A.get(i).stream()
                    .mapToDouble(s -> s).average().getAsDouble());
        });
        double size = 4.0;
        double delta = size / 2.0;
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);
        var sr1 = new ScatterRenderer();
        var sr2 = new ScatterRenderer();

        sr1.setSeriesPaint(0, Color.LIGHT_GRAY);
        sr1.setSeriesShape(0, shape);
        sr2.setSeriesPaint(0, Color.RED);
        sr2.setSeriesShape(0, shape);
        chart.addRenderer(new String[]{"Series1A", "Series1B"},
                new CategoryItemRenderer[]{sr1, sr2}, series1A, series1B);

        var lasr = new LineAndShapeRenderer();
        lasr.setSeriesPaint(0, Color.BLUE);
        lasr.setSeriesShape(0, shape);
        lasr.setSeriesStroke(0, new BasicStroke(2.0f));
        chart.addRenderer(2, "Series2", lasr, series2);

        chart.createChart("HelloWorld", "Generation", "Score", 560, 367, true);
        chart.showChartWindow();
    }

    private final CategoryPlot plot;
    private JFreeChart chart;

    public CombinedChart_AWT(String applicationTitle) {

        super(applicationTitle);
        plot = new CategoryPlot();
    }

    public void addRenderer(String[] series,
            CategoryItemRenderer[] renderers,
            Map<Integer, List<Double>>... data) {

        IntStream.range(0, data.length).forEach(i -> {
            var dataset = new DefaultMultiValueCategoryDataset();
            data[i].entrySet().forEach(e
                    -> dataset.add(e.getValue(), series[i], e.getKey()));
            plot.setDataset(i, dataset);
            plot.setRenderer(i, renderers[i]);
        });
    }

    public void addRenderer(int index, String series,
            CategoryItemRenderer renderer,
            Map<Integer, Double> data) {

        var dataset = new DefaultCategoryDataset();
        data.forEach((key, value)
                -> dataset.addValue(value, series, key));
        plot.setDataset(index, dataset);
        plot.setRenderer(index, renderer);
    }

    public void createChart(String chartTitle, String xLabel, String yLabel,
            int x, int y, boolean legend) {

        Font largefont = new Font("Dialog", Font.PLAIN, 25);
        Font smallfont = new Font("Dialog", Font.PLAIN, 20);
        var xAxis = new CategoryAxis(xLabel);
        xAxis.setLabelFont(largefont);
        xAxis.setTickLabelFont(smallfont);
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        int count = plot.getCategories().size();
        int filter = (int) Math.pow(10, (int) Math.log10(count));
        IntStream.range(0, count)
                .filter(i -> i % filter != 0 && i != count - 1)
                .mapToObj(i -> plot.getCategories().get(i))
                .forEach(c -> xAxis.setTickLabelPaint((Comparable) c, new Color(0, 0, 0, 0)));
        plot.setDomainAxis(xAxis);
        var yAxis = new NumberAxis(yLabel);
        yAxis.setLabelFont(largefont);
        yAxis.setTickLabelFont(smallfont);
        plot.setRangeAxis(yAxis);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        chart = new JFreeChart(plot);
        chart.setTitle(chartTitle);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new java.awt.Dimension(x, y));
        setContentPane(panel);

        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
    }

    public void addMarker(double position, Color color) {
        ValueMarker marker = new ValueMarker(position);
        marker.setPaint(color);
        plot.addRangeMarker(marker);
    }

    public void showChartWindow() {

        this.setVisible(true);
    }

    /*
     * Default setters and getters.
     */
    public JFreeChart getChart() {
        return chart;
    }

    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }

    public CategoryPlot getPlot() {
        return plot;
    }

}
