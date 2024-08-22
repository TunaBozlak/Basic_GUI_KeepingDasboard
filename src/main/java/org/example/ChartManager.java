package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.general.DefaultPieDataset;



import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChartManager {
    private JPanel chartPanelContainer;

    public void setChartPanelContainer(JPanel chartPanelContainer) {
        this.chartPanelContainer = chartPanelContainer;
    }

    public void updateChart(Map<String, Double> operatorAverages) {
        updateChart(operatorAverages, false);
    }

    public void updateChart(Map<String, Double> operatorAverages, boolean isPieChart) {
        SwingUtilities.invokeLater(() -> {
            chartPanelContainer.removeAll();

            if (isPieChart) {
                DefaultPieDataset pieDataset = new DefaultPieDataset();
                pieDataset.setValue("Turkcell", operatorAverages.get("Turkcell"));
                pieDataset.setValue("Vodafone", operatorAverages.get("Vodafone"));
                pieDataset.setValue("TurkTelekom", operatorAverages.get("TurkTelekom"));

                JFreeChart pieChart = ChartFactory.createPieChart(
                        "Operatör Dağılımı",
                        pieDataset,
                        true, true, false);

                pieChart.setBackgroundPaint(new Color(143,188,143));

                PiePlot piePlot = (PiePlot) pieChart.getPlot();
                piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
                piePlot.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
                piePlot.setLabelBackgroundPaint(Color.white);
                piePlot.setLabelOutlinePaint(null);
                piePlot.setLabelShadowPaint(null);

                piePlot.setSectionPaint("Turkcell", Color.BLUE);
                piePlot.setSectionPaint("Vodafone", Color.RED);
                piePlot.setSectionPaint("TurkTelekom", Color.ORANGE);

                ChartPanel pieChartPanel = new ChartPanel(pieChart);
                pieChartPanel.setPreferredSize(new Dimension(500, 300));
                pieChartPanel.setMouseWheelEnabled(true);

                chartPanelContainer.add(pieChartPanel);
            } else {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                dataset.addValue(operatorAverages.get("Turkcell"), "Turkcell", "Operatörler");
                dataset.addValue(operatorAverages.get("Vodafone"), "Vodafone", "Operatörler");
                dataset.addValue(operatorAverages.get("TurkTelekom"), "TurkTelekom", "Operatörler");

                JFreeChart barChart = ChartFactory.createBarChart(
                        "Operatör Performansı",
                        "Operatör",
                        "Ortalama Değer",
                        dataset,
                        PlotOrientation.VERTICAL,
                        true, true, false);

                CategoryPlot plot = barChart.getCategoryPlot();
                barChart.setBackgroundPaint(new Color(143,188,143));

                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setRange(0.0, rangeAxis.getUpperBound());

                BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
                barRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                barRenderer.setDefaultItemLabelsVisible(true);
                barRenderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 12));
                barRenderer.setSeriesPaint(0, Color.BLUE);
                barRenderer.setSeriesPaint(1, Color.RED);
                barRenderer.setSeriesPaint(2, Color.ORANGE);
                barRenderer.setMaximumBarWidth(0.1);

                ChartPanel barChartPanel = new ChartPanel(barChart);
                barChartPanel.setPreferredSize(new Dimension(500, 300));
                barChartPanel.setMouseWheelEnabled(true);

                chartPanelContainer.add(barChartPanel);
            }

            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();
        });
    }
}
