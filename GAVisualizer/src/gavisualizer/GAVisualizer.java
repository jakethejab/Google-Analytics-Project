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
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartUtilities;

/**
 * A simple example of how to access the Google Analytics API using a service
 * account.
 */
public class GAVisualizer {

    private static final String APPLICATION_NAME = "Hello Analytics";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String KEY_FILE_LOCATION = "client_secrets.p12";
    private static final String SERVICE_ACCOUNT_EMAIL = "305995695505-e7pjemg78c8hhpk0j2p2f28gd5487eiv@developer.gserviceaccount.com";

    public static void main(String[] args) {
        try {
            Analytics analytics = initializeAnalytics();

            String profile = getFirstProfileId(analytics);
            System.out.println("First Profile Id: " + profile);
            printResults(getResults(analytics, profile));
            getSessionsByCountry(analytics, profile);
            chartTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void chartTest() throws IOException {
        DefaultPieDataset result = new DefaultPieDataset();
        result.setValue("Linux", 29);
        result.setValue("Mac", 20);
        result.setValue("Windows", 51);
        
        PieDataset dataset = result;
        
        JFreeChart chart = ChartFactory.createPieChart3D("Test",          // chart title
            dataset,                // data
            true,                   // include legend
            true,
            false);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        
        ChartUtilities.saveChartAsPNG(new File("yo.png"), chart, 400, 300);
    }

    private static Analytics initializeAnalytics() throws Exception {
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

    private static String getFirstProfileId(Analytics analytics) throws IOException {
        // Get the first view (profile) ID for the authorized user.
        String profileId = null;

        // Query for the list of all accounts associated with the service account.
        Accounts accounts = analytics.management().accounts().list().execute();

        if (accounts.getItems().isEmpty()) {
            System.err.println("No accounts found");
        } else {
            String firstAccountId = accounts.getItems().get(0).getId();

            // Query for the list of properties associated with the first account.
            Webproperties properties = analytics.management().webproperties()
                    .list(firstAccountId).execute();

            if (properties.getItems().isEmpty()) {
                System.err.println("No Webproperties found");
            } else {
                String firstWebpropertyId = properties.getItems().get(0).getId();

                // Query for the list views (profiles) associated with the property.
                Profiles profiles = analytics.management().profiles()
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

    private static GaData getResults(Analytics analytics, String profileId) throws IOException {
        GaData data = analytics.data().ga()
                .get("ga:" + profileId, "7daysAgo", "today", "ga:sessions")
                .execute();
    // Query the Core Reporting API for the number of sessions
        // in the past seven days.
        return data;
    }

    private static void getSessionsByCountry(Analytics analytics, String profileId) throws IOException {
        GaData data = analytics.data().ga()
                .get("ga:" + profileId, "40daysAgo", "today", "ga:sessions")
                .setDimensions("ga:country")
                .setSort("-ga:sessions")
                .execute();

        GaData test = data;
    }

    private static void printResults(GaData results) {
    // Parse the response from the Core Reporting API for
        // the profile name and number of sessions.
        if (results != null && !results.getRows().isEmpty()) {
            System.out.println("View (Profile) Name: "
                    + results.getProfileInfo().getProfileName());
            System.out.println("Total Sessions: " + results.getRows().get(0).get(0));
        } else {
            System.out.println("No results found");
        }
    }
}
