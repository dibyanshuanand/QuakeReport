package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

//import androidx.loader.content.AsyncTaskLoader;
import android.app.LoaderManager;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** We have used android.content.AsyncTaskLoader instead of androidx.loader.content.AsyncTaskLoader
 * because while initializing the loader using initLoader in EarthquakeActivity, there is error.
 * This is because LoaderManager is deprecated from API 28(using AndroidX)
 */
public class EarthquakeLoader extends AsyncTaskLoader<List<Quake>> {

    private static final String TAG = EarthquakeLoader.class.getSimpleName();
    private String mUrl = null;

    /** Constructor for EarthquakeLoader class
     * @param context COntext from where Loader is called
     * @param url The request url ot make request
     *            We cannot directly pass params like AsyncTask, thus we pass it through constructor
     */
    public EarthquakeLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    /** LoadInBackground is equivalent to AsyncTask's doInBackground
     * @return List of earthquakes
     */
    @Override
    public List<Quake> loadInBackground() {
        Log.v(TAG, "loadInBackground called here ");
        if (mUrl == null) {
            return null;
        }

        // Create URL object from request url string
        URL url = QueryUtils.createUrl(mUrl);

        String jsonResponse = "";
        try {
            jsonResponse = QueryUtils.makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error in getting earthquake data", e);
        }

        ArrayList<Quake> earthquakeList = QueryUtils.extractEarthquakes(jsonResponse);

        return earthquakeList;
    }

    /** This method is implemented to trigger start of Loader by calling forceLoad(), which in
     *  turn invokes the onForceLoad() callback that in turn calls the loadInBackground() on
     *  a worker thread to start background task
     */
    @Override
    protected void onStartLoading() {
        Log.v(TAG, "onStartLoading called here ");
        forceLoad();
    }
}
