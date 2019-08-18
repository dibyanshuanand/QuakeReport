/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Quake>> {

    private static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final int EARTHQUAKE_LOADER_ID = 1;
    private static String requestUrl = "https://earthquake.usgs.gov/fdsnws/event/1/query"; // ?format=geojson&orderby=time&minmag=6&limit=20
    private QuakeAdapter earthquakeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // EarthquakeAsyncTask task = new EarthquakeAsyncTask();
        // task.execute(requestUrl);

        ConnectivityManager connManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        /** Get a reference to the LoaderManager, in order to interact with loaders.
         *  Initialize the loader. Pass in the int ID constant defined above and pass in null for
         *  the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
         *  because this activity implements the LoaderCallbacks interface).
         */
        if (isConnected) {
            Log.v(LOG_TAG, "initLoader called here ");
            getLoaderManager().initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            ProgressBar spinner = findViewById(R.id.progress_circular);
            spinner.setVisibility(View.GONE);

            TextView emptyView = findViewById(R.id.emptyView);
            emptyView.setText(R.string.no_internet);
            Log.i(LOG_TAG, "No Internet Connection");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Method to update UI after list of earthquakes is fetched from request url
     *  Uses custom ArrayAdapter to dissplay list of earthquakes
     *  OnItemClickListener handles touch input on a specific earthquake
     * @param quakes List of earthquakes fetched
     */
    private void updateUi(final List<Quake> quakes) {

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link ArrayAdapter} of earthquakes
        earthquakeAdapter = new QuakeAdapter(this, quakes);

        // Set the TextView for empty screen to be shown when there is no data in the adapter
        earthquakeListView.setEmptyView(findViewById(R.id.emptyView));

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(earthquakeAdapter);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Quake currQuake = quakes.get(position);
                String url = currQuake.getUrl(EarthquakeActivity.this);

                Intent quakeData = new Intent(Intent.ACTION_VIEW);
                quakeData.setData(Uri.parse(url));
                startActivity(quakeData);
            }
        });
    }

    // ASYNC TASK IMPLEMENTED METHODS

    private class EarthquakeAsyncTask extends AsyncTask<String, Void, List<Quake>> {

        @Override
        protected List<Quake> doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            // Create URL object from request url string
            URL url = QueryUtils.createUrl(urls[0]);

            String jsonResponse = "";
            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error in getting earthquake data", e);
            }

            ArrayList<Quake> earthquakeList = QueryUtils.extractEarthquakes(jsonResponse);
            //earthquakeList = QueryUtils.extractEarthquakes(jsonResponse);
            earthquakeList.add(new Quake(0.0,
                    "#Test Data of Test Data Location#",
                    "Jul 23, 2019",
                    "01:59 AM",
                    "https://www.google.co.in"));

            return earthquakeList;
        }

        @Override
        protected void onPostExecute(List<Quake> earthquakes) {
            if (earthquakes == null) {
                return;
            }

            updateUi(earthquakes);
        }
    }

    // LOADER IMPLEMENTED METHODS

    @Override
    public Loader<List<Quake>> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader called here ");

        /** SharedPreferences stores key-value pair to supply to the query parameter of URL
         *  URI builder is used to create coomplete url with queries, taken from user, built upon
         *  the base URI
         *  The queries are then appended to the base URL
         */
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));
        String limit = sharedPrefs.getString(
                getString(R.string.settings_limit_key),
                getString(R.string.settings_limit_default));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        Uri baseUri = Uri.parse(requestUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("orderby", "time");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("limit", limit);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(android.content.Loader<List<Quake>> loader, List<Quake> quakes) {
        Log.v(LOG_TAG, "onLoadFinished called here ");
        if (quakes == null) {
            return;
        }
        ProgressBar spinnerProgress = findViewById(R.id.progress_circular);
        spinnerProgress.setVisibility(View.GONE);

        if (quakes.isEmpty()) {
            TextView emptyView = findViewById(R.id.emptyView);
            emptyView.setText(R.string.empty_view);
        }

        updateUi(quakes);
    }

    @Override
    public void onLoaderReset(android.content.Loader<List<Quake>> loader) {
        Log.v(LOG_TAG, "onLoaderReset called here ");
        earthquakeAdapter.clear();
    }


}
