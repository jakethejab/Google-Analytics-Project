/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

import java.io.IOException;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Jake
 */
public interface IChart {
    public void generate();
    
    public void saveAsImage(String path, int width, int height) throws IOException;
}