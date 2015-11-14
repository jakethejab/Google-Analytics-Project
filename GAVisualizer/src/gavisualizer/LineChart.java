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
import java.awt.Paint;
import java.util.List;

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
import org.jfree.chart.renderer.AbstractRenderer;

/**
 *
 * @author Jake
 */
public class LineChart implements IChart {
    private CategoryDataset _dataset;
    private JFreeChart _chart;
    private String _title;
    private String _axisLabelDomain;
    private String _axisLabelRange;
    
    LineChart(String title, CategoryDataset dataset, String axisLabelDomain, String axisLabelRange) {
        _dataset = dataset;
        _title = title;
        _axisLabelDomain = axisLabelDomain;
        _axisLabelRange = axisLabelRange;
    }
    
    public void generate()
    {
        JFreeChart chart = ChartFactory.createLineChart(
            _title,                    // chart title
            _axisLabelDomain,          // domain axis label
            _axisLabelRange,           // range axis label
            _dataset,                  // data
            PlotOrientation.VERTICAL,  // orientation
            true,                      // include legend
            false,                     // tooltips
            false                      // urls
        );

        chart.setBackgroundPaint(Color.white);  

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setOutlinePaint(Color.white);
        
        // customize the line and the stroke width of the series
        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        ((AbstractRenderer)renderer).setAutoPopulateSeriesStroke(false);
        renderer.setBaseStroke(new BasicStroke(3));
        
        // include dashed lines if necessary
        List<String> rows = _dataset.getRowKeys();
        if(rows.get(rows.size()-1).endsWith("Downloads"))
        {
            int start = _dataset.getRowCount() / 2;
            int init = 0;
            while(start <= _dataset.getRowCount())
            {
                renderer.setSeriesStroke(start, 
                        new BasicStroke(
                        3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                        1.0f, new float[] {10.0f, 6.0f}, 0.0f
                        ));
                Paint p = renderer.getItemPaint(init, 1);
                renderer.setSeriesPaint(start, p);
                init++;
                start++;
            }
        } 

        plot.setRenderer(renderer);

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
