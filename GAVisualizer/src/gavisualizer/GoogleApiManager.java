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
    private static final String START_DATE_WEBSITE = "2012-01-01";
    private static final String START_DATE_APPSTORE = "2012-06-01";
    private static final int MAX_RESULTS = 100000;
    private String _END_DATE;
    private Analytics _analytics;
    private String _CytoscapeProfile;
    private String _AppstoreProfile;
    private String _certificatePath;
    private String _serviceAccountEmail;
    
    GoogleApiManager(String certificatePath, String serviceAccountEmail) {
        try
        {
            _certificatePath = certificatePath;
            _serviceAccountEmail = serviceAccountEmail;            
            _analytics = initializeAnalytics();
            _CytoscapeProfile = getFirstProfileId();
            _AppstoreProfile = getSecondProfileId();
            _END_DATE = getToday();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    public GaData getSessionsByCountry() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, START_DATE_WEBSITE, _END_DATE, "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    public GaData getWebsiteReferralSources() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, START_DATE_WEBSITE, _END_DATE, "ga:sessions")
                .setDimensions("ga:sourceMedium")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    public GaData getWebsiteSessionsByWeek() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, START_DATE_WEBSITE, _END_DATE, "ga:sessions")
                .setDimensions("ga:nthWeek")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }     
    
    public GaData getWebsiteDownloads() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, START_DATE_WEBSITE, _END_DATE, "ga:pageviews")
                .setFilters("ga:pagePath==/download.php")
                .setDimensions("ga:pagePath,ga:nthWeek")
                .setSort("ga:nthWeek")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
      
    //Extracts data for App Store Sessions by Country
    public GaData getAppSessionsByCountry() throws IOException {  
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, START_DATE_APPSTORE, _END_DATE, "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    //Extracts data for App Store Referral Sources
    public GaData getAppReferralSources() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, START_DATE_APPSTORE, _END_DATE, "ga:sessions")
                .setDimensions("ga:sourceMedium")
                .setSort("-ga:sessions")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    //Extracts data for App Store Visits by Week
    public GaData getAppSessionsByWeek () throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, START_DATE_APPSTORE, _END_DATE, "ga:sessions")
                .setDimensions("ga:nthWeek")
                .setMaxResults(MAX_RESULTS)
                .execute();
    }
    
    //Extracts data for App Store Visits by Week
    public GaData getAppAttractionsByCategory () throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, START_DATE_APPSTORE, _END_DATE, "ga:pageviews")
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
        
        //create a date with Google Analytics date format
        Date d = new Date();     
        
        //create a format to read Google Analytics date format
        DateFormat write = new SimpleDateFormat("yyyy-MM-dd");
        
        //return the date with the expected output format
        return write.format(d);
        //return "2015-02-22"; // customer example
        //return "2015-11-14";
    }
}
