/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

/**
 *
 * @author Jake
 */
public class PieChart implements IChart {
    private PieDataset _dataset;
    private JFreeChart _chart;
    private String _title;
    
    PieChart(String title, PieDataset dataset) {
        _dataset = dataset;
        _title = title;
    }
    
    public void generate()
    {
        JFreeChart chart = ChartFactory.createPieChart(_title,          // chart title
            _dataset,                // data
            true,                   // include legend
            true,
            false);

        PiePlot  plot = (PiePlot) chart.getPlot();
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}"));
        
        _chart = chart;
    }
    
    public void saveAsImage(String path, int width, int height) throws IOException
    {
        ChartUtilities.saveChartAsPNG(new File(path), _chart, width, height);  
    }
}
