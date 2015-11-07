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
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
/**
 * A simple example of how to access the Google Analytics API using a service
 * account.
 */
public class GAVisualizer {

    private static final int MAX_COUNT = 10;
    private static final int MAX_CATEGORIES = 6;
    
    public static void main(String[] args) {
        try {
            ChartGenerator generator = new ChartGenerator("", 1200, 800);
            GoogleApiManager api = new GoogleApiManager();
            
            // PieChart - Sessions by Country
            GaData raw = api.getSessionsByCountry();
            PieDataset ds1 = createPieDataset(raw);
            String title = createTitleSessionsByCountry(raw);
            PieChart chart1 = new PieChart(title, ds1);
            
            generator.generateAndSaveChart(chart1, "sessions_by_country.png");
            
            // LineChart - Sessions by Country
            GaData raw2 = api.getWebsiteDownloads();
            GaData rawWebsiteSessionsByWeek = api.getWebsiteSessionsByWeek();
            CategoryDataset ds2 = createDSWebsiteDownloads(raw2, rawWebsiteSessionsByWeek);
            String title2 = createTitleWebsiteDownloads(raw2);
            LineChart chart2 = new LineChart(title2, ds2, "Week", "Count");
            
            generator.generateAndSaveChart(chart2, "website_downloads.png");
            
            // PieChart - Website Referral Sources
            GaData raw3 = api.getWebsiteReferralSources();
            PieDataset ds3 = createPieDataset(raw3);
            String title3 = createTitleWebsiteReferralSources(raw3);
            PieChart chart3 = new PieChart(title3, ds3);
            
            generator.generateAndSaveChart(chart3, "website_referral_sources.png");  
            
            // PieChart - App Store Sessions by Country
            GaData raw4 = api.getAppSessionsByCountry();
            PieDataset ds4 = createPieDataset(raw4);
            String title4 = createTitleAppSessionsByCountry(raw4);
            PieChart chart4 = new PieChart(title4, ds4);
            
            generator.generateAndSaveChart(chart4, "appstore_sessions_by_country.png");
            
            // PieChart - App Store Referral Sources
            GaData raw5 = api.getAppReferralSources();
            PieDataset ds5 = createPieDataset(raw5);
            String title5 = createTitleAppReferralSources(raw5);
            PieChart chart5 = new PieChart(title5, ds5);
            
            generator.generateAndSaveChart(chart5, "appstore_referrals_by_source.png");
            
            // LineChart - App Store Visits per Week
            GaData raw6 = api.getAppSessionsByWeek();
            CategoryDataset ds6 = createDSVisits(raw6);
            String title6 = createTitleAppSessionsByWeek(raw6);
            LineChart chart6 = new LineChart(title6, ds6, "Week", "Count");
            
            generator.generateAndSaveChart(chart6, "appstore_visits_per_week.png");
            
            // PieChart - Top App Store Attractions by Category
            GaData raw7 = api.getAppAttractionsByCategory();
            PieDataset ds7 = createPieDatasetCategories(raw7);
            String title7 = createTitleAppAttractionsByCategory(raw7);
            PieChart chart7 = new PieChart(title7, ds7);
            
            generator.generateAndSaveChart(chart7, "top_appstore_attractions_by_category.png");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //NOTE! I created one method for this so we don't repeat it unnecessarily (George) 
    private static PieDataset createPieDataset(GaData raw)
    {
        DefaultPieDataset result = new DefaultPieDataset();
        if (raw != null && !raw.getRows().isEmpty()) {
            List<List<String>> rows = raw.getRows();
            
            int count = 0;
            int otherVal = 0;
            for (List<String> r : rows)
            {
                if (count < MAX_COUNT) // Show the top 10
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
    
    private static PieDataset createPieDatasetCategories(GaData raw)
    {
        DefaultPieDataset result = new DefaultPieDataset();
        if (raw != null && !raw.getRows().isEmpty()) {
            List<List<String>> rows = raw.getRows();
            
            int count = 0;
            for (List<String> r : rows)
            {
                if (count <= MAX_CATEGORIES) // Show the top 6
                {
                    result.setValue(r.get(0), Integer.parseInt(r.get(1)));                    
                }
                else
                {
                    break;
                }

                count++;
            }
        } else {
            System.out.println("No results found");
        }
        
        return result;
    }
    
    private static CategoryDataset createDSVisits(GaData raw)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        int year = Integer.parseInt(getYear(raw.getQuery().getStartDate()));
        final int weeksInYear = 52;
        
        if (raw != null && !raw.getRows().isEmpty()) {
            List<List<String>> rows = raw.getRows();
            
            int count = 0;
            for (List<String> r : rows)
            {
                dataset.addValue(Integer.parseInt(r.get(1)), year + " Visits", Integer.toString(count));
                count++;
                if (count >= weeksInYear)
                {
                    count = 0;
                    year++;
                }
            }
        }
        
        return dataset;
    }  
    
    private static CategoryDataset createDSWebsiteDownloads(GaData raw, GaData rawWebsiteSessionsByWeek)
    {
        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int year = Integer.parseInt(getYear(raw.getQuery().getStartDate()));
        final int weeksInYear = 52;
        
        if (rawWebsiteSessionsByWeek != null && !rawWebsiteSessionsByWeek.getRows().isEmpty()) {
            List<List<String>> rows = rawWebsiteSessionsByWeek.getRows();
            
            int count = 0;
            for (List<String> r : rows)
            {
                dataset.addValue(Integer.parseInt(r.get(1)), year + " Visits", Integer.toString(count));
                count++;
                if (count >= weeksInYear)
                {
                    count = 0;
                    year++;
                }
            }
        }        
        
        if (raw != null && !raw.getRows().isEmpty()) {
            year = Integer.parseInt(getYear(raw.getQuery().getStartDate()));
            
            List<List<String>> rows = raw.getRows();
            
            int count = 0;
            for (List<String> r : rows)
            {
                dataset.addValue(Integer.parseInt(r.get(2)), year + " Downloads", Integer.toString(count));
                count++;
                if (count >= weeksInYear)
                {
                    count = 0;
                    year++;
                }
            }
        }
        
        return dataset;
    }
    
    private static String createTitleWebsiteReferralSources(GaData raw)
    {
        return "Web Site Referral Sources (" + getTotal(raw) + ") (" + formatDate(raw.getQuery().getStartDate()) + " through " + formatDate(raw.getQuery().getEndDate()) + ")";
    }    

    private static String createTitleSessionsByCountry(GaData raw)
    {
        return "Web Site Sessions (" + getTotal(raw) + ") (" + formatDate(raw.getQuery().getStartDate()) + " through " + formatDate(raw.getQuery().getEndDate()) + ")";
    }
    
    private static String createTitleWebsiteDownloads(GaData raw)
    {
        return "Web Site Visits (cytoscape.org) and Cytoscape Downloads (via download.php) (" + getYear(raw.getQuery().getStartDate()) + " - " + getYear(raw.getQuery().getEndDate()) + ")";
    }
      
    private static String createTitleAppSessionsByCountry(GaData raw)
    {     
        return "App Store Visits (" + getTotal(raw) + ") (" + formatDate(raw.getQuery().getStartDate()) + " through " + formatDate(raw.getQuery().getEndDate()) + ")";
    }
    
    private static String createTitleAppReferralSources(GaData raw)
    {
        return "App Store Referral Sources (" + getTotal(raw) + ") (" + formatDate(raw.getQuery().getStartDate()) + " through " + formatDate(raw.getQuery().getEndDate()) + ")";
    }
    
    private static String createTitleAppSessionsByWeek(GaData raw)
    {
        return "App Store Visits per Week (" + getTotal(raw) + ") (" + formatDate(raw.getQuery().getStartDate()) + " through " + formatDate(raw.getQuery().getEndDate()) + ")";
    }
    
    private static String createTitleAppAttractionsByCategory(GaData raw)
    {
        return "Top App Store Attractions by Category (" + getTotal(raw) + ") (" + formatDate(raw.getQuery().getStartDate()) + " through " + formatDate(raw.getQuery().getEndDate()) + ")";
    }
    
    //NOTE! I added this to reduce redundancy. I will add to SDD and the two below (George)
    private static String getTotal(GaData raw)
    {
        NumberFormat nf = NumberFormat.getInstance();
        
        int total = 0;
        if (raw != null && !raw.getRows().isEmpty()) {
            List<List<String>> rows = raw.getRows();
            for (List<String> r : rows)
            {
                total += Integer.parseInt(r.get(1));
            }       
        }
        return nf.format(total);
    }
    
    //NOTE! I added this to format dates different than GA keeps them (George)
    private static String formatDate(String date)
    {
        //create a format to read Google Analytics date format
        DateFormat read = new SimpleDateFormat("yyyy-MM-dd");
        
        //create a date with Google Analytics date format
        Date d = new Date();
        try {
            d = read.parse(date);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        //create a format to write the output format
        DateFormat write = new SimpleDateFormat("MM/dd/yyyy");
        
        //return the date with the expected output format
        return write.format(d).toString();
    }
    
    //NOTE! I added this to format year different than GA keeps them (George)
    private static String getYear(String date)
    {
        //create a format to read Google Analytics date format
        DateFormat read = new SimpleDateFormat("yyyy-MM-dd");
        
        //create a date with Google Analytics date format
        Date d = new Date();
        try {
            d = read.parse(date);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        //create a format to write the output format
        DateFormat write = new SimpleDateFormat("yyyy");
        
        //return year
        return write.format(d).toString();
    }
}
