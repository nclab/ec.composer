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

import java.awt.Color;
import java.text.NumberFormat;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Rendering a Line Chart.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class LineChart_AWT extends ApplicationFrame {

    public static void main(String[] args) {

        LineChart_AWT chart = new LineChart_AWT("Demo");
        IntStream.range(0, 50)
                .forEach(i -> {
                    chart.addStatData(
                            Stream.generate(Math::random)
                                    .limit(10)
                                    .collect(Collectors.toList()),
                            "demo", "" + i);
                });
        chart.createStatLineChart("HelloWorld", "Generation", "Score", 560, 367, true);
        chart.showChartWindow();
    }

    private DefaultCategoryDataset dataset;
    private DefaultStatisticalCategoryDataset statDataset;
    private JFreeChart lineChart;

    public LineChart_AWT(String applicationTitle) {

        super(applicationTitle);
        dataset = new DefaultCategoryDataset();
        statDataset = new DefaultStatisticalCategoryDataset();
    }

    public void addData(Number value, String rowKey, String colKey) {

        dataset.addValue(value, rowKey, colKey);
    }

    public void addData(List<Double> data, String rowKey, String colKey) {

        dataset.addValue(
                data.stream().mapToDouble(v -> v).average().orElse(0.0),
                rowKey, colKey);
    }

    public void addStatData(List<Double> data, String rowKey, String colKey) {

        final DoubleAdder powSum = new DoubleAdder();
        final double avg = data.stream()
                .mapToDouble(v -> v)
                .average().orElse(0.0);
        DoubleSummaryStatistics stdss = data.stream()
                .mapToDouble(s -> s)
                .filter(v -> v > 0.0)
                .peek(value -> powSum.add(Math.pow(value, 2)))
                .summaryStatistics();
        double stdDev = Math.sqrt(powSum.sum() / stdss.getCount() - Math.pow(stdss.getAverage(), 2));
        stdDev = Double.isNaN(stdDev) ? 0.0 : stdDev;
        statDataset.add(avg, stdDev, rowKey, colKey);
    }

    public void createChart(JFreeChart chart, int x, int y) {

        lineChart = chart;

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(x, y));
        setContentPane(chartPanel);

        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);

        var plot = chart.getCategoryPlot();
        var xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        int count = plot.getCategories().size();
        int filter = (int) Math.pow(10, (int) Math.log10(count));
        IntStream.range(0, count)
                .filter(i -> i % filter != 0 && i != count - 1)
                .mapToObj(i -> plot.getCategories().get(i))
                .forEach(c -> xAxis.setTickLabelPaint((Comparable) c, new Color(0, 0, 0, 0)));
    }

    public void createLineChart(String chartTitle, String xLabel, String yLabel,
            int x, int y, boolean legend) {

        createChart(
                ChartFactory.createLineChart(
                        chartTitle, xLabel, yLabel, dataset,
                        PlotOrientation.VERTICAL,
                        legend, true, false),
                x, y
        );
    }

    public void createStatLineChart(String chartTitle, String xLabel, String yLabel,
            int x, int y, boolean legend) {

        createChart(
                ChartFactory.createLineChart(
                        chartTitle, xLabel, yLabel, statDataset,
                        PlotOrientation.VERTICAL,
                        legend, true, false),
                x, y
        );
        StatisticalLineAndShapeRenderer statisticalRenderer = new StatisticalLineAndShapeRenderer(true, false);
        lineChart.getCategoryPlot().setRenderer(statisticalRenderer);
        statisticalRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance()));
        statisticalRenderer.setDefaultItemLabelsVisible(true);
    }

    public void showChartWindow() {

        this.setVisible(true);
    }

    /*
     * Default setters and getters.
     */
    public DefaultCategoryDataset getDataset() {
        return dataset;
    }

    public void setDataset(DefaultCategoryDataset dataset) {
        this.dataset = dataset;
    }

    public JFreeChart getLineChart() {
        return lineChart;
    }

    public void setLineChart(JFreeChart lineChart) {
        this.lineChart = lineChart;
    }

    public DefaultStatisticalCategoryDataset getStatDataset() {
        return statDataset;
    }

    public void setStatDataset(DefaultStatisticalCategoryDataset statDataset) {
        this.statDataset = statDataset;
    }
}
