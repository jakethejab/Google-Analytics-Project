/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

import java.io.IOException;

/**
 *
 * @author Jake
 */
public class ChartGenerator {
    
    // Global variables needed for chart generation
    private String _filepath;
    private int _imageWidth;
    private int _imageHeight;
    
    // Constructor that sets arguments to the global variables
    ChartGenerator(String filepath, int imageWidth, int imageHeight)
    {
        _filepath = filepath;
        _imageWidth = imageWidth;
        _imageHeight = imageHeight;
    }
    
    // Generate the chart and output the file to a saved location
    public void generateAndSaveChart(IChart chart, String filename) throws IOException
    {
        // Generate chart
        chart.generate();
        
        // Save to file location
        String pathPNG = _filepath + filename + ".png";
        String pathSVG = _filepath + filename + ".svg";
        
        chart.saveAsImage(pathPNG, _imageWidth, _imageHeight);
        chart.saveAsSVG(pathSVG, _imageWidth, _imageHeight);
    }
}
