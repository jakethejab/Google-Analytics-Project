/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

/**
 *
 * @author Jake
 */
public class PieChart implements IChart {
    private final PieDataset _dataset;
    private JFreeChart _chart;
    private final String _title;
    
    PieChart(String title, PieDataset dataset) {
        _dataset = dataset;
        _title = title;
    }
    
    @Override
    public void generate()
    {
        JFreeChart chart = ChartFactory.createPieChart(_title,          // chart title
            _dataset,                // data
            false,                   // include legend
            false,
            false);
        
        PiePlot  plot = (PiePlot) chart.getPlot();
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}"));
        plot.setOutlinePaint(Color.white);
        
        _chart = chart;
    }
    
    @Override
    public void saveAsImage(String path, int width, int height) throws IOException
    {
        ChartUtilities.saveChartAsPNG(new File(path), _chart, width, height);  
    }
}
