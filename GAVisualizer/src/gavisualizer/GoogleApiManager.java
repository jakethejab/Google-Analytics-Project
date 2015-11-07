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
    private static final String KEY_FILE_LOCATION = "client_secrets.p12";
    private static final String SERVICE_ACCOUNT_EMAIL = "305995695505-e7pjemg78c8hhpk0j2p2f28gd5487eiv@developer.gserviceaccount.com";
    private Analytics _analytics;
    private String _CytoscapeProfile;
    private String _AppstoreProfile;
    
    GoogleApiManager() {
        try
        {
            _analytics = initializeAnalytics();
            _CytoscapeProfile = getFirstProfileId();
            _AppstoreProfile = getSecondProfileId();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    public GaData getSessionsByCountry() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, "2012-01-01", getToday(), "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .execute();
    }
    
    public GaData getWebsiteReferralSources() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, "2012-01-01", getToday(), "ga:sessions")
                .setDimensions("ga:sourceMedium")
                .setSort("-ga:sessions")
                .execute();
    }
    
    public GaData getWebsiteSessionsByWeek() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, "2012-01-01", getToday(), "ga:sessions")
                .setDimensions("ga:nthWeek")
                .execute();
    }     
    
    public GaData getWebsiteDownloads() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _CytoscapeProfile, "2012-01-01", getToday(), "ga:pageviews")
                .setFilters("ga:pagePath==/download.php")
                .setDimensions("ga:pagePath,ga:nthWeek")
                .setSort("ga:nthWeek")
                .execute();
    }
      
    //Extracts data for App Store Sessions by Country
    public GaData getAppSessionsByCountry() throws IOException {  
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, "2012-06-01", getToday(), "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .execute();
    }
    
    //Extracts data for App Store Referral Sources
    public GaData getAppReferralSources() throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, "2012-06-01", getToday(), "ga:sessions")
                .setDimensions("ga:sourceMedium")
                .setSort("-ga:sessions")
                .execute();
    }
    
    //Extracts data for App Store Visits by Week
    public GaData getAppSessionsByWeek () throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, "2012-01-01", getToday(), "ga:sessions")
                .setDimensions("ga:nthWeek")
                .execute();
    }
    
    //Extracts data for App Store Visits by Week
    public GaData getAppAttractionsByCategory () throws IOException {
        return _analytics.data().ga()
                .get("ga:" + _AppstoreProfile, "2012-06-01", getToday(), "ga:pageviews")
                .setDimensions("ga:pageTitle")
                .setSort("-ga:pageviews")
                .setFilters("ga:pageTitle=~category")
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
                .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                .setServiceAccountPrivateKeyFromP12File(new File(KEY_FILE_LOCATION))
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
    }
}
