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
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItem;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
 

/**
 *
 * @author Jake
 */
public class LineChart implements IChart {
    
    // Global variables for chart Line Chart
    private final CategoryDataset _dataset;
    private JFreeChart _chart;
    private final String _title;
    private final String _axisLabelDomain;
    private final String _axisLabelRange;
    
    /* Custom global variables with set values for Line Chart */
    
    // Sets a chart stroke to dashed
    private static final BasicStroke DASHED_STROKE =  new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1.0f, new float[] {4.0f, 2.0f}, 0.0f);
    
    // Sets a chart stroke to increased width
    private static final BasicStroke STROKE_WIDTH = new BasicStroke(3);
    
    // Custom line paints for readability on white canvas
    private static final Paint[] PAINTS = new Paint[] {
                Color.red, Color.blue, Color.green, 
                Color.orange, Color.magenta, 
                Color.cyan, Color.DARK_GRAY, Color.pink
            };
    
    // Constructor for Line Chart that sets global variables from arguments
    LineChart(String title, CategoryDataset dataset, String axisLabelDomain, String axisLabelRange) {
        _dataset = dataset;
        _title = title;
        _axisLabelDomain = axisLabelDomain;
        _axisLabelRange = axisLabelRange;
    }
    
    @Override
    public void generate()
    {
        // Creates Line Chart from variables
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
        
        // Set the Plot to certain colors
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setOutlinePaint(Color.white);
        
        // Set paints for lines in the chart
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        DefaultDrawingSupplier dw = new DefaultDrawingSupplier(
                PAINTS, 
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, 
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, 
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, 
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE 
            );
        plot.setDrawingSupplier(dw);

        // Customize stroke width of the series
        ((AbstractRenderer)renderer).setAutoPopulateSeriesStroke(false);
        renderer.setBaseStroke(STROKE_WIDTH);
        renderer.setBaseShapesVisible(false);
        
        // Include dashed lines if necessary
        List<String> rows = _dataset.getRowKeys();
        
        // See if the title includes Downloads (Dashed lines are for Download lines)
        if(rows.get(rows.size()-1).endsWith("Downloads"))
        {  
            // Set the start to half the rows
            int start = _dataset.getRowCount() / 2;
            
            // Create an initial value
            int init = 0;
            
            // Iterate through the Download lines
            while(start < _dataset.getRowCount())
            {
                // Make sure series and legend have dashes
                renderer.setSeriesStroke(start, DASHED_STROKE); 
                LegendItemCollection legend = renderer.getLegendItems();
                LegendItem li = legend.get(start-1);
                li.setLineStroke(DASHED_STROKE);
                
                // Make sure the color matches the undashed year
                renderer.setSeriesPaint(start, renderer.getItemPaint(init, 0));
                init++;
                start++;
            }
        }
        
        // Customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        
        _chart = chart;
    }
    
    @Override
    public void saveAsImage(String path, int width, int height) throws IOException
    {
        // Save image as PNG
        ChartUtilities.saveChartAsPNG(new File(path), _chart, width, height);  
    }
    
    @Override
    public void saveAsSVG(String path, int width, int height) throws IOException
    {
        // Save image as SVG
        SVGGraphics2D g2 = new SVGGraphics2D(width, height);
        Rectangle r = new Rectangle(0, 0, width, height);
        _chart.draw(g2, r);
        File f = new File(path);
        SVGUtils.writeToSVG(f, g2.getSVGElement());
    }    
}
