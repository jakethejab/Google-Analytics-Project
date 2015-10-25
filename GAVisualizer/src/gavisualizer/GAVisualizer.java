/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

/**
 *
 * @author Jake
 */
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartUtilities;

import java.text.NumberFormat;
/**
 * A simple example of how to access the Google Analytics API using a service
 * account.
 */
public class GAVisualizer {

    private static final int SHOW_COUNTRY_COUNT = 10;
    
    public static void main(String[] args) {
        try {
            ChartGenerator generator = new ChartGenerator("", 1100, 900);
            GoogleApiManager api = new GoogleApiManager();
            
            // PieChart - Sessions by Country
            GaData raw = api.getSessionsByCountry();
            PieDataset ds1 = createDSSessionsByCountry(raw);
            String title = createTitleSessionsByCountry(raw);
            PieChart chart1 = new PieChart(title, ds1);
            
            generator.generateAndSaveChart(chart1, "sessions_by_country.png");
            
            // LineChart - Sessions by Country
            GaData raw2 = api.getWebsiteDownloads();
            CategoryDataset ds2 = createDSWebsiteDownloads(raw2);
            String title2 = createTitleWebsiteDownloads(raw2);
            LineChart chart2 = new LineChart(title2, ds2);
            
            generator.generateAndSaveChart(chart2, "website_downloads.png");            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String createTitleSessionsByCountry(GaData raw)
    {
        int total = 0;
        if (raw != null && !raw.getRows().isEmpty()) {
            List<List<String>> rows = raw.getRows();
            for (List<String> r : rows)
            {
                total += Integer.parseInt(r.get(1));
            }       
        }
        
        NumberFormat nf = NumberFormat.getInstance();
        
        return "Total " + nf.format(total) + " (1/1/2012 - 2/22/2015)"; // TODO: pull date range from args
    }
    
    private static PieDataset createDSSessionsByCountry(GaData raw)
    {
        DefaultPieDataset result = new DefaultPieDataset();
        if (raw != null && !raw.getRows().isEmpty()) {
            List<List<String>> rows = raw.getRows();
            
            int count = 0;
            int otherVal = 0;
            for (List<String> r : rows)
            {
                if (count <= SHOW_COUNTRY_COUNT) // Show the top 10 countries in chart
                {
                    result.setValue(r.get(0), Integer.parseInt(r.get(1)));                    
                }
                else
                {
                    otherVal += Integer.parseInt(r.get(1));
                }

                count++;
            }
            
            result.setValue("Other", otherVal);
        } else {
            System.out.println("No results found");
        }
        
        return result;
    }
    
   private static String createTitleWebsiteDownloads(GaData raw)
    {
        return "Total (1/1/2012 - 2/22/2015)";
    }
    
    private static CategoryDataset createDSWebsiteDownloads(GaData raw)
    {
        final String series1 = "First";
        final String series2 = "Second";
        final String series3 = "Third";

        // column keys...
        final String type1 = "Type 1";
        final String type2 = "Type 2";
        final String type3 = "Type 3";
        final String type4 = "Type 4";
        final String type5 = "Type 5";
        final String type6 = "Type 6";
        final String type7 = "Type 7";
        final String type8 = "Type 8";

        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(1.0, series1, type1);
        dataset.addValue(4.0, series1, type2);
        dataset.addValue(3.0, series1, type3);
        dataset.addValue(5.0, series1, type4);
        dataset.addValue(5.0, series1, type5);
        dataset.addValue(7.0, series1, type6);
        dataset.addValue(7.0, series1, type7);
        dataset.addValue(8.0, series1, type8);

        dataset.addValue(5.0, series2, type1);
        dataset.addValue(7.0, series2, type2);
        dataset.addValue(6.0, series2, type3);
        dataset.addValue(8.0, series2, type4);
        dataset.addValue(4.0, series2, type5);
        dataset.addValue(4.0, series2, type6);
        dataset.addValue(2.0, series2, type7);
        dataset.addValue(1.0, series2, type8);

        dataset.addValue(4.0, series3, type1);
        dataset.addValue(3.0, series3, type2);
        dataset.addValue(2.0, series3, type3);
        dataset.addValue(3.0, series3, type4);
        dataset.addValue(6.0, series3, type5);
        dataset.addValue(3.0, series3, type6);
        dataset.addValue(4.0, series3, type7);
        dataset.addValue(3.0, series3, type8);

        return dataset;
    }    
}
