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
    private String _filepath;
    private int _imageWidth;
    private int _imageHeight;
    
    ChartGenerator(String filepath, int imageWidth, int imageHeight)
    {
        _filepath = filepath;
        _imageWidth = imageWidth;
        _imageHeight = imageHeight;
    }
    
    public void generateAndSaveChart(IChart chart, String filename) throws IOException
    {
        chart.generate();
        
        String path = _filepath + filename;
        chart.saveAsImage(path, _imageWidth, _imageHeight);
    }
}
