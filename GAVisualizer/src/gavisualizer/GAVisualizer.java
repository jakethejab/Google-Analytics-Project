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
import com.google.api.services.analytics.model.GaData;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.io.FileReader;

import org.apache.log4j.Logger;

/**
 * Creates statistic charts by accessing Google Analytics repository from 
 * the provided credentials:
 *      Service Account Email
 *      Certificate
 * 
 * Allows for manipulation of statistic chart properties as defined in 
 * app.properties file by the following arguments:
 *      (*) The width of the statistic chart image files
 *      (*) The height of the statistic chart image files
 *      (*) Where the statistic chart image files are saved to
 */
public class GAVisualizer {

    // Number of pie pieces for the pie charts as defined by the customer
    private static final int MAX_COUNT = 10;
    private static final int MAX_CATEGORIES = 6;
    
    // The log4j global variable
    private static Logger _log = Logger.getLogger(GAVisualizer.class.getName()); 
    
    public static void main(String[] args) {
        try {
            
            boolean isPropertyFileSpecified = false;
            
            // Captures arguments from app.properties
            args = new String[1];
            args[0] = "src\\app.properties";
            
            String configFilePath = "";
            if (args.length > 0)
            {
                configFilePath = args[0];
                isPropertyFileSpecified = true;
            }
            else
            {
                _log.error("The path to the properties file has not been specified as an argument.");
            }
            
            if (isPropertyFileSpecified)
            {
                // Loads in the app.properties file
                Properties prop = new Properties();
                FileReader reader = new FileReader(configFilePath);
                prop.load(reader);

                // Get/Set arguments from app.properties file   
                int imageWidth = Integer.parseInt(prop.getProperty("imageWidth"));
                int imageHeight = Integer.parseInt(prop.getProperty("imageHeight"));
                String outputPath = prop.getProperty("outputPath");
                String certificatePath = prop.getProperty("certificatePath");
                String serviceAccountEmail = prop.getProperty("serviceAccountEmail");

                _log.debug("Property: imageWidth: " + imageWidth);
                _log.debug("Property: imageHeight: " + imageHeight);
                _log.debug("Property: outputPath: " + outputPath);
                _log.debug("Property: certificatePath: " + certificatePath);
                _log.debug("Property: serviceAccountEmail: " + serviceAccountEmail);

                // Create instances for chart generation and Google Analytics API access
                ChartGenerator generator = new ChartGenerator(outputPath, imageWidth, imageHeight);
                GoogleApiManager api = new GoogleApiManager(certificatePath, serviceAccountEmail);

                /**
                 * Here the statistic chart will be created.
                 * To create each chart, the following steps are done:
                 *       (1) Extract data from query for specific statistic chart
                 *       (2) Convert data set to specific chart type (ex. pie chart)
                 *       (3) Create chart title
                 *       (4) Create chart
                 *       (5) Generate and save chart to output folder
                 */

                // PieChart - Website visits by Country
                _log.info("Starting generation of Website visits by Country chart");
                try
                {
                    GaData raw = api.getSessionsByCountry();           
                    PieDataset ds1 = createPieDataset(raw);
                    _log.debug(getAPILogEntry(raw));
                    String title = createTitleSessionsByCountry(raw);   
                    PieChart chart1 = new PieChart(title, ds1);         

                    generator.generateAndSaveChart(chart1, "sessions_by_country");

                    _log.info("Website visits by Country chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }
                
                _log.info("Starting generation of Website Visits and Cytoscape Downloads");
                try
                {
                    // LineChart - Website Visits and Cytoscape Downloads
                    GaData raw2 = api.getWebsiteDownloads();
                    _log.debug(getAPILogEntry(raw2));
                    GaData rawWebsiteSessionsByWeek = api.getWebsiteSessionsByWeek();
                    _log.debug(getAPILogEntry(rawWebsiteSessionsByWeek));
                    CategoryDataset ds2 = createDSWebsiteDownloads(raw2, rawWebsiteSessionsByWeek);
                    String title2 = createTitleWebsiteDownloads(raw2);
                    LineChart chart2 = new LineChart(title2, ds2, "Week", "Count");

                    generator.generateAndSaveChart(chart2, "website_downloads");
                    
                    _log.info("Website Visits and Cytoscape Downloads chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }                

                _log.info("Starting generation of Website Referral Sources");
                try
                {                
                    // PieChart - Website Referral Sources
                    GaData raw3 = api.getWebsiteReferralSources();
                    _log.debug(getAPILogEntry(raw3));
                    PieDataset ds3 = createPieDataset(raw3);
                    String title3 = createTitleWebsiteReferralSources(raw3);
                    PieChart chart3 = new PieChart(title3, ds3);

                    generator.generateAndSaveChart(chart3, "website_referral_sources");  
                    
                    _log.info("Website Referral Sources chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }
                
                _log.info("Starting generation of App Store Visits by Country");
                try
                {                   
                    // PieChart - App Store Visits by Country
                    GaData raw4 = api.getAppSessionsByCountry();
                    _log.debug(getAPILogEntry(raw4));
                    PieDataset ds4 = createPieDataset(raw4);
                    String title4 = createTitleAppSessionsByCountry(raw4);
                    PieChart chart4 = new PieChart(title4, ds4);

                    generator.generateAndSaveChart(chart4, "appstore_sessions_by_country");
                    
                    _log.info("App Store Visits by Country chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }
                
                _log.info("Starting generation of App Store Referral Sources");
                try
                {                  
                    // PieChart - App Store Referral Sources
                    GaData raw5 = api.getAppReferralSources();
                    _log.debug(getAPILogEntry(raw5));
                    PieDataset ds5 = createPieDataset(raw5);
                    String title5 = createTitleAppReferralSources(raw5);
                    PieChart chart5 = new PieChart(title5, ds5);

                    generator.generateAndSaveChart(chart5, "appstore_referrals_by_source");
                    
                    _log.info("App Store Referral Sources chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }
                
                _log.info("Starting generation of App Store Visits per Week");
                try
                {                
                    // LineChart - App Store Visits per Week
                    GaData raw6 = api.getAppSessionsByWeek();
                    _log.debug(getAPILogEntry(raw6));
                    CategoryDataset ds6 = createDSVisits(raw6);
                    String title6 = createTitleAppSessionsByWeek(raw6);
                    LineChart chart6 = new LineChart(title6, ds6, "Week", "Count");

                    generator.generateAndSaveChart(chart6, "appstore_visits_per_week");
                    
                    _log.info("App Store Visits per Week chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }
                
                _log.info("Starting generation of Top App Store Attractions by Category");
                try
                {                    
                    // PieChart - Top App Store Attractions by Category
                    GaData raw7 = api.getAppAttractionsByCategory();
                    _log.debug(getAPILogEntry(raw7));
                    PieDataset ds7 = createPieDatasetCategories(raw7);
                    String title7 = createTitleAppAttractionsByCategory(raw7);
                    PieChart chart7 = new PieChart(title7, ds7);

                    generator.generateAndSaveChart(chart7, "top_appstore_attractions_by_category");
                    
                    _log.info("Top App Store Attractions by Category chart successfully saved");
                }
                catch (Exception ex)
                {
                    _log.error(ex.getMessage());
                }                
            }
        } 
        catch (Exception ex) {
            _log.error(ex.getMessage());
        }
    }

    // Default creation for each pie data set
    private static PieDataset createPieDataset(GaData raw)
    {
        // Create the dataset
        DefaultPieDataset result = new DefaultPieDataset();
        
        // Make sure statistic chart data is available
        if (raw != null && !raw.getRows().isEmpty()) {
            
            // Get/Set all rows from the data
            List<List<String>> rows = raw.getRows();
            
            // Variables to iterate each pie slice
            int count = 0;
            int otherVal = 0;
            
            // Go through each row
            for (List<String> r : rows)
            {
                // Determine which rows are the top 10 (first 10 rows are) 
                if (count < MAX_COUNT)
                    result.setValue(r.get(0), Integer.parseInt(r.get(1)));                    
                else
                    otherVal += Integer.parseInt(r.get(1));

                count++;
            }
            
            result.setValue("Other", otherVal);
            
        }
        
        // Return the pie chart pieces for chart creation
        return result;
    }
    
    // Specific pie data set creation based on top categories (customer defined 6)
    private static PieDataset createPieDatasetCategories(GaData raw)
    {
        // Create the dataset
        DefaultPieDataset result = new DefaultPieDataset();
        
        // Make sure statistic chart data is available
        if (raw != null && !raw.getRows().isEmpty()) {
            
            // Get/Set all rows from the data
            List<List<String>> rows = raw.getRows();
            
            // Variable to iterate each category
            int count = 0;
            
            // Go through each row
            for (List<String> r : rows)
            {
                // Remove unwanted text
                if(r.get(0).contains((CharSequence)"Cytoscape App Store - "))
                {
                    r.set(0, r.get(0).replace("Cytoscape App Store -", ""));
                }
                
                if(r.get(0).contains((CharSequence)"category"))
                {
                    r.set(0, r.get(0).replace(" category", ""));
                }
                
                // Determine which rows are the top 6 (first 6 rows are)
                if (count <= MAX_CATEGORIES)
                {
                    result.setValue(r.get(0), Integer.parseInt(r.get(1)));                    
                }
                else
                {
                    // Exit after getting top 6
                    break;
                }

                count++;
            }
        }
        
        return result;
    }
    
    // Create dataset for visits
    private static CategoryDataset createDSVisits(GaData raw)
    {
        // Create the dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get the start date from the data query to progress to end date year
        int year = Integer.parseInt(getYear(raw.getQuery().getStartDate()));
        
        // Set variable for number of weeks in a year
        final int weeksInYear = 52;
        
        // Make sure statistic chart data is available
        if (raw != null && !raw.getRows().isEmpty()) {
            
            // Get/Set all rows from the data
            List<List<String>> rows = raw.getRows();
            
            // Variable to iterate through each week
            int count = 0;
            
            // Go through each row 
            for (List<String> r : rows)
            {
                // Add week's data to dataset
                dataset.addValue(Integer.parseInt(r.get(1)), year + " Visits", Integer.toString(count));
                count++;
                
                // Check if a new year has been reached
                if (count >= weeksInYear)
                {
                    // Reset count and increase year
                    count = 0;
                    year++;
                }
            }
        }
        
        return dataset;
    }  
    
    // Create dataset for website downloads
    private static CategoryDataset createDSWebsiteDownloads(GaData raw, GaData rawWebsiteSessionsByWeek)
    {
        // Create the dataset
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Get the start date from the data query to progress to end date year
        int year = Integer.parseInt(getYear(raw.getQuery().getStartDate()));
        
        // Set variable for number of weeks in a year
        final int weeksInYear = 52;
        
        // Make sure statistic chart data is available
        if (rawWebsiteSessionsByWeek != null && !rawWebsiteSessionsByWeek.getRows().isEmpty()) {
            
            // Get/Set all rows from the data
            List<List<String>> rows = rawWebsiteSessionsByWeek.getRows();
            
            // Variable to iterate through each week
            int count = 0;
            
             // Go through each row 
            for (List<String> r : rows)
            {
                // Add week's data to dataset
                dataset.addValue(Integer.parseInt(r.get(1)), year + " Visits", Integer.toString(count));
                count++;
                
                // Check if a new year has been reached
                if (count >= weeksInYear)
                {
                    // Reset count and increase year
                    count = 0;
                    year++;
                }
            }
        }        
        
        // Go through the other data if if its available
        if (raw != null && !raw.getRows().isEmpty()) {
            
            // Reset the start date
            year = Integer.parseInt(getYear(raw.getQuery().getStartDate()));
            
            // Get rows of from data
            List<List<String>> rows = raw.getRows();
            
            // Set variable to get each download for each week
            int count = 0;
            
            // Go through the rows
            for (List<String> r : rows)
            {
                // Add week's data to dataset
                dataset.addValue(Integer.parseInt(r.get(2)), year + " Downloads", Integer.toString(count));
                count++;
                
                // Check if a new year has been reached
                if (count >= weeksInYear)
                {
                    // Reset count and increase year
                    count = 0;
                    year++;
                }
            }
        }
        
        return dataset;
    }
    
    /** 
     *  The following methods set the unique titles for each statistic chart
     *  They all need the total number of data points and the start/end date
     */
    
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
    
    /**
     *  End Title Methods
     */
    
    // Get total number of rows
    private static String getTotal(GaData raw)
    {
        // Create number format instance
        NumberFormat nf = NumberFormat.getInstance();
        
        // Set counter for total
        int total = 0;
        
        // Make sure data exists
        if (raw != null && !raw.getRows().isEmpty()) {
            
            // Get the rows
            List<List<String>> rows = raw.getRows();
            
            // Go through each row
            for (List<String> r : rows)
            {
                // Add to total
                total += Integer.parseInt(r.get(1));
            }       
        }
        
        // Return total in number format
        return nf.format(total);
    }
    
    // Format the date from the Google Analytics query with expected output date
    private static String formatDate(String date)
    {
        // Create a format to read Google Analytics date format
        DateFormat read = new SimpleDateFormat("yyyy-MM-dd");
        
        // Create a date with Google Analytics date format
        Date d = new Date();
        try {
            d = read.parse(date);
        }
        catch(Exception ex) {
            _log.error(ex.getMessage());
        }
        
        // Create a format to write the output format
        DateFormat write = new SimpleDateFormat("MM/dd/yyyy");
        
        // Return the date with the expected output format
        return write.format(d);
    }
    
    // Get the year from the Google Analytics query
    private static String getYear(String date)
    {
        // Create a format to read Google Analytics date format
        DateFormat read = new SimpleDateFormat("yyyy-MM-dd");
        
        // Create a date with Google Analytics date format
        Date d = new Date();
        try {
            d = read.parse(date);
        }
        catch(Exception ex) {
            _log.error(ex.getMessage());
        }
        
        // Create a format to write the output format
        DateFormat write = new SimpleDateFormat("yyyy");
        
        // Return formatted year
        return write.format(d);
    }
    
    private static String getAPILogEntry(GaData raw)
    {
        return "API returned " + (raw.getRows().isEmpty() ? "0" : raw.getRows().size()) + " rows";        
    }
}
