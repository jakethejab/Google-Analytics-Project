/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

import java.io.File;
import java.io.IOException;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtilities;

/**
 *
 * @author Jake
 */
public class LineChart implements IChart {
    private CategoryDataset _dataset;
    private JFreeChart _chart;
    private String _title;
    
    LineChart(String title, CategoryDataset dataset) {
        _dataset = dataset;
        _title = title;
    }
    
    public void generate()
    {
        JFreeChart chart = ChartFactory.createLineChart(
            "Line Chart Demo 1",       // chart title
            "Type",                    // domain axis label
            "Value",                   // range axis label
            _dataset,                   // data
            PlotOrientation.VERTICAL,  // orientation
            true,                      // include legend
            true,                      // tooltips
            false                      // urls
        );

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);

        // customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        
        _chart = chart;
    }
    
    public void saveAsImage(String path, int width, int height) throws IOException
    {
        ChartUtilities.saveChartAsPNG(new File(path), _chart, width, height);  
    }
}
