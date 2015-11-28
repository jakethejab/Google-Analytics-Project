/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavisualizer;

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
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Jake
 */
public class GoogleApiManager {
    private static final String APPLICATION_NAME = "Hello Analytics";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    // default start dates defined by the customer and 
    private static final String START_DATE = "2012-01-01";
    
    // max results for each query
    private static final int MAX_RESULTS = 100000;
    
    private Analytics _analytics;       // instance of Google Analytics
    private String _cytoscapeProfile;   // customer defined profile for cytoscape
    private String _appstoreProfile;    // customer defined profile for appstore
    private String _certificatePath;    // certificate required for authentication
    private String _serviceAccountEmail;// service account email assoiated with Google Analytics
    private String _endDate;            // the end date for all queries
    
    // constructor for the Google API Manager
    GoogleApiManager(String certificatePath, String serviceAccountEmail) {
        try
        {
            // sets global variables based on parameters and internal methods
            _certificatePath = certificatePath;
            _serviceAccountEmail = serviceAccountEmail;            
            _analytics = initializeAnalytics();
            _cytoscapeProfile = getFirstProfileId();
            _appstoreProfile = getSecondProfileId();
            _endDate = getToday();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    // query to extract website visits by country
    public GaData getSessionsByCountry() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _cytoscapeProfile, START_DATE, _endDate, "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    // query to extract website referral sources
    public GaData getWebsiteReferralSources() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _cytoscapeProfile, START_DATE, _endDate, "ga:sessions")
                .setDimensions("ga:sourceMedium")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    // query to extract website sessions by week
    public GaData getWebsiteSessionsByWeek() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _cytoscapeProfile, START_DATE, _endDate, "ga:sessions")
                .setDimensions("ga:nthWeek")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }     
    
    // query to extract website downloads
    public GaData getWebsiteDownloads() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _cytoscapeProfile, START_DATE, _endDate, "ga:pageviews")
                .setFilters("ga:pagePath==/download.php")
                .setDimensions("ga:pagePath,ga:nthWeek")
                .setSort("ga:nthWeek")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
      
    // query to extract app store visits by country
    public GaData getAppSessionsByCountry() throws IOException {  
        return _analytics.data().ga()
                .get("ga:" + _appstoreProfile, START_DATE, _endDate, "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    // query to extract app store referral sources
    public GaData getAppReferralSources() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _appstoreProfile, START_DATE, _endDate, "ga:sessions")
                .setDimensions("ga:sourceMedium")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    // query to extract app sessions by week
    public GaData getAppSessionsByWeek () throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _appstoreProfile, START_DATE, _endDate, "ga:sessions")
                .setDimensions("ga:nthWeek")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    // query to extract app store attractions by category
    public GaData getAppAttractionsByCategory () throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _appstoreProfile, START_DATE, _endDate, "ga:pageviews")
                .setDimensions("ga:pageTitle")
                .setSort("-ga:pageviews")
                .setFilters("ga:pageTitle=~category")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }

    private Analytics initializeAnalytics() throws Exception {
        // Initializes an authorized analytics service object.

        // Construct a GoogleCredential object with the service account email
        // and p12 file downloaded from the developer console.
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(_serviceAccountEmail)
                .setServiceAccountPrivateKeyFromP12File(new File(_certificatePath))
                .setServiceAccountScopes(AnalyticsScopes.all())
                .build();

        // Construct the Analytics service object.
        return new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    private String getFirstProfileId() throws IOException {
        // Get the first view (profile) ID for the authorized user.
        String profileId = null;

        // Query for the list of all accounts associated with the service account.
        Accounts accounts = _analytics.management().accounts().list().execute();

        if (accounts.getItems().isEmpty()) {
            System.err.println("No accounts found");
        } else {
            String firstAccountId = accounts.getItems().get(0).getId();

            // Query for the list of properties associated with the first account.
            Webproperties properties = _analytics.management().webproperties()
                    .list(firstAccountId).execute();

            if (properties.getItems().isEmpty()) {
                System.err.println("No Webproperties found");
            } else {
                String firstWebpropertyId = properties.getItems().get(0).getId();

                // Query for the list views (profiles) associated with the property.
                Profiles profiles = _analytics.management().profiles()
                        .list(firstAccountId, firstWebpropertyId).execute();

                if (profiles.getItems().isEmpty()) {
                    System.err.println("No views (profiles) found");
                } else {
                    // Return the first (view) profile associated with the property.
                    profileId = profiles.getItems().get(0).getId();
                }
            }
        }
        return profileId;
    }   
    
    private String getSecondProfileId() throws IOException {
        // Get the second view (profile) ID for the authorized user.
        String profileId = null;

        // Query for the list of all accounts associated with the service account.
        Accounts accounts = _analytics.management().accounts().list().execute();

        if (accounts.getItems().isEmpty()) {
            System.err.println("No accounts found");
        } else {
            String firstAccountId = accounts.getItems().get(0).getId();

            // Query for the list of properties associated with the first account.
            Webproperties properties = _analytics.management().webproperties()
                    .list(firstAccountId).execute();

            if (properties.getItems().isEmpty()) {
                System.err.println("No Webproperties found");
            } else {
                String secondWebpropertyId = properties.getItems().get(1).getId();

                // Query for the list views (profiles) associated with the property.
                Profiles profiles = _analytics.management().profiles()
                        .list(firstAccountId, secondWebpropertyId).execute();

                if (profiles.getItems().isEmpty()) {
                    System.err.println("No views (profiles) found");
                } else {
                    // Return the first (view) profile associated with the property.
                    profileId = profiles.getItems().get(0).getId();
                }
            }
        }
        return profileId;
    }
    
    private String getToday() throws IOException {
        
        // create a date with Google Analytics date format
        Date d = new Date();     
        
        // create a format to read Google Analytics date format
        DateFormat write = new SimpleDateFormat("yyyy-MM-dd");
        
        // return the date with the expected output format
        return write.format(d);
        
        /**
         * Examples of different end dates for testing:
        
            return "2015-02-22"; // customer example
            return "2015-11-14"; // software delivery test
            
         * Use these to edit the end date.
         */
    }
}
