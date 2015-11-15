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
 

/**
 *
 * @author Jake
 */
public class LineChart implements IChart {
    private final CategoryDataset _dataset;
    private JFreeChart _chart;
    private final String _title;
    private final String _axisLabelDomain;
    private final String _axisLabelRange;
    
    private static final BasicStroke DASHED_STROKE =  new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1.0f, new float[] {4.0f, 2.0f}, 0.0f);
    private static final BasicStroke STROKE_WIDTH = new BasicStroke(3);
    private static final Paint[] PAINTS = new Paint[] {
                Color.red, Color.blue, Color.green, 
                Color.orange, Color.magenta, 
                Color.cyan, Color.DARK_GRAY, Color.pink
            };
    
    LineChart(String title, CategoryDataset dataset, String axisLabelDomain, String axisLabelRange) {
        _dataset = dataset;
        _title = title;
        _axisLabelDomain = axisLabelDomain;
        _axisLabelRange = axisLabelRange;
    }
    
    @Override
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

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setOutlinePaint(Color.white);
        
        // set paints for chart
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        DefaultDrawingSupplier dw = new DefaultDrawingSupplier(
                PAINTS, 
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, 
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, 
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, 
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE 
            );
        plot.setDrawingSupplier(dw);

        // customize stroke width of the series
        ((AbstractRenderer)renderer).setAutoPopulateSeriesStroke(false);
        renderer.setBaseStroke(STROKE_WIDTH);
        renderer.setBaseShapesVisible(false);
        
        // include dashed lines if necessary
        List<String> rows = _dataset.getRowKeys();
        if(rows.get(rows.size()-1).endsWith("Downloads"))
        {            
            int start = _dataset.getRowCount() / 2;
            int init = 0;
            while(start < _dataset.getRowCount())
            {
                // make sure series and legend have dashes
                renderer.setSeriesStroke(start, DASHED_STROKE); 
                LegendItemCollection legend = renderer.getLegendItems();
                LegendItem li = legend.get(start-1);
                li.setLineStroke(DASHED_STROKE);
                
                // make sure the color matches the undashed year
                renderer.setSeriesPaint(start, renderer.getItemPaint(init, 0));
                init++;
                start++;
            }
        }
        
        // customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        
        _chart = chart;
    }
    
    @Override
    public void saveAsImage(String path, int width, int height) throws IOException
    {
        ChartUtilities.saveChartAsPNG(new File(path), _chart, width, height);  
    }
}
