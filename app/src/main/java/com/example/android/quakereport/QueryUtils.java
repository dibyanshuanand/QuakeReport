package com.example.android.quakereport;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Quake} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Quake> extractEarthquakes(String jsonResponse) {
        Log.v(LOG_TAG, "extractEarthquakes called from QueryUtils ");

        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Quake> earthquakes = new ArrayList<>();

        try {
            /** Create a JSON object from the JSON response obtained in String             */
            JSONObject quakeObject = new JSONObject(jsonResponse);
            /** Navigate through the JSON response to obtain the required data using the
             *  proper path
             */
            JSONArray features = quakeObject.optJSONArray("features");

            for (int i = 0 ; i < features.length() ; ++i) {
                JSONObject element = features.optJSONObject(i);
                JSONObject properties = element.optJSONObject("properties");
                double quakeMag = properties.optDouble("mag");
                String quakePlace = properties.optString("place");
                String url = properties.optString("url");
                long quakeTime = properties.optLong("time"); // obtained in milliseconds
                // int quakeTime = features.optJSONObject(i).optJSONObject("properties").optInt("time");

                /** Convert the time into Date object by calling Date constructor             */
                Date dateObject = new Date(quakeTime);
                /** Initialize a SimpleDateFormat instance and configure it to provide a
                 *  more readable representation using the given format
                 */
                SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");
                String dateToDisplay = dateFormatter.format(dateObject);

                /** Create another instance of SimpleDateFormat to get the time            */
                SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
                String timeToDisplay = timeFormatter.format(dateObject);


                earthquakes.add(new Quake(quakeMag, quakePlace, dateToDisplay, timeToDisplay, url));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }


    // USER-DEFINED HELPER METHODS

    /** Helper method to convert the request url string to URL object to facilitate making
     *  HTTP request
     * @param requestUrl String url
     * @return Corresponding URL object
     */
    public static URL createUrl(String requestUrl) {
        Log.v(LOG_TAG, "createUrl called here from QueryUtils ");
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /** Helper method to make GET request to website and receive response in JSON
     *  HttpURLConnection is subclass of URLConnection used in context of HTTP requests
     *  200 response code denotes connection is successful and response is ready ro be received
     * @param url URL to make request to
     * @return Received JSON response in String
     * @throws IOException thrown at inputStream.close()
     */
    public static String makeHttpRequest(URL url) throws IOException{
        Log.v(LOG_TAG, "makeHttpRequest called here from QueryUtils ");
        String jsonResponse = "";

        // If url is null
        if (url == null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException exception) {
            Log.e(LOG_TAG, "Unable to make HTTP request", exception);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /** Helper method to facilitate reading of received response from request website using
     *  InputStream
     *  InputStreamReader is a bridge from byte to character streams, using the defined charset
     *  InputStreamReader is wrapped in BufferedReader to increase efficiency
     * @param inputStream Received data through InputStream
     * @return jsonResponse in String
     * @throws IOException thrown at bfReader.readLine()
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bfReader = new BufferedReader(inputStreamReader);
            String line = bfReader.readLine();
            while(line != null) {
                output.append(line);
                line = bfReader.readLine();
            }
        }
        return output.toString();
    }

}