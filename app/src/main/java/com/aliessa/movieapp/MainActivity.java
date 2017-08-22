package com.aliessa.movieapp;


import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aliessa.movieapp.adapters.ForcastRecycleAdapter;
import com.aliessa.movieapp.data.Contract;
import com.aliessa.movieapp.model.Movie;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

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
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    final int LOADER_ID = 1;
    public static ArrayList<Movie> Movies = new ArrayList<>();
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.Img_NoInternet)
    ImageView Img_NoInternet;
    private static final String POPULARITY = "popular";
    private static final String RATING = "top_rated";
    private String mSortBy = POPULARITY;
    private String apiKey;
    private static final String[] MOVIE_COLUMNS = {
            Contract.MovieEntry._ID,
            Contract.MovieEntry.id_rw,
            Contract.MovieEntry.title_rw,
            Contract.MovieEntry.image,
            Contract.MovieEntry.image2,
            Contract.MovieEntry.overview_rw,
            Contract.MovieEntry.rate_rw,
            Contract.MovieEntry.data_name
    };
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_IMAGE = 3;
    public static final int COL_IMAGE2 = 4;
    public static final int COL_OVERVIEW = 5;
    public static final int COL_RATING = 6;
    public static final int COL_DATE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        apiKey = getResources().getString(R.string.api_key);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//        call AsyncTask to lode data from the internet
        if (isNetworkConnected()) {
            MovieAsyncTask movieAsyncTask = new MovieAsyncTask(apiKey, POPULARITY);
            movieAsyncTask.execute();
            mFirebaseAnalytics.setCurrentScreen(this, getLocalClassName(), null);
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
            Bundle params = new Bundle();
            params.putString(getString(R.string.image_name), "image_name");
            params.putString(getString(R.string.full_text), "full_text");
            mFirebaseAnalytics.logEvent(getString(R.string.share_image), params);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() !=null){
            Img_NoInternet.setVisibility(View.GONE);
            return true;
        }else {
            Img_NoInternet.setVisibility(View.VISIBLE);
            Toast.makeText(this, getString(R.string.No_Inetnet_Connecion), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // AsyncTask use to get data in Background and show in Main Thread

    private class MovieAsyncTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = MovieAsyncTask.class.getCanonicalName();

        private final String mApiKey;
        private String sortBy;

        //         constructor to customize and set parameter when call
        private MovieAsyncTask(String mApiKey, String sortBy) {
            this.sortBy = sortBy;
            this.mApiKey = mApiKey;
        }

        //        Show The ProgressBar when app do in BackGround
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        //        Method Do in BackGround To Get Data From The Internet
        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
//            Start Connecting To Get Data JSON From api
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonString = null;
            try {
//                Start Method To Build URL
                URL url = getApiUrl();
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("");
                }
                if (stringBuilder.length() == 0) {
                    return null;
                }
//                Set Data In Object
                moviesJsonString = stringBuilder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, getString(R.string.Buffer_Error), e);
                return null;
//                Close Connecting
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, getString(R.string.Close_Error));
                    }
                }
            }
            try {
//                Return From BackGround
                return getMoviesDataFromJson(moviesJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        //        When Method DoInBackGround Finish OnPostExecute Running In Main Method and Show To User
        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            progressBar.setVisibility(View.GONE);
            if (movies != null) {
                Movies = movies;
                GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 2);
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(new ForcastRecycleAdapter(movies, mRecyclerView, MainActivity.this));

            }
        }

        //        Get Data From Json convert Data from Json Object To Array Of Object
        private ArrayList<Movie> getMoviesDataFromJson(String moviesJsonString) throws JSONException {
            final String TAG_RESULTS = "results";
            JSONObject moviesJson = new JSONObject(moviesJsonString);
            JSONArray resultsArray = moviesJson.getJSONArray(TAG_RESULTS);

            ArrayList<Movie> movies = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject movieInfo = resultsArray.getJSONObject(i);
                Movie movieItem = new Movie(movieInfo);
                movies.add(movieItem);
            }
            return movies;
        }

        //        Build URl To Use It In Start Connection In DoInBackGround
        private URL getApiUrl() throws MalformedURLException {
            final String TMDB_BASE_URL = getString(R.string.api_url) + sortBy;
            final String API_KEY_PARAM = "api_key";
            Uri builtUri;
            builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, mApiKey)
                    .build();
            return new URL(builtUri.toString());

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem action_sort_by_popularity = menu.findItem(R.id.action_sort_by_popularity);
        MenuItem action_sort_by_rating = menu.findItem(R.id.action_sort_by_rating);
        if (mSortBy.contains(POPULARITY)) {
            if (!action_sort_by_popularity.isChecked()) {
                action_sort_by_popularity.setChecked(true);
            }
        } else if (mSortBy.contains(RATING)) {
            if (!action_sort_by_rating.isChecked()) {
                action_sort_by_rating.setChecked(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        MovieAsyncTask movieAsyncTask;
        switch (id) {
            case R.id.action_sort_by_rating:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                if (isNetworkConnected()){
                mSortBy = RATING;
                movieAsyncTask = new MovieAsyncTask(apiKey, RATING);
                movieAsyncTask.execute();}
                return true;
            case R.id.action_sort_by_popularity:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                if (isNetworkConnected()){
                mSortBy = POPULARITY;
                movieAsyncTask = new MovieAsyncTask(apiKey, POPULARITY);
                movieAsyncTask.execute();}
                return true;
            case R.id.action_favorit:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }

//                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        progressBar.setVisibility(View.VISIBLE);

        return new CursorLoader(this, Contract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS, null, null,Contract.MovieEntry.title_rw);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ArrayList<Movie> movies = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movie movie = new Movie(cursor);
                movies.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }
        progressBar.setVisibility(View.INVISIBLE);
        mRecyclerView.setAdapter(new ForcastRecycleAdapter(movies, mRecyclerView, MainActivity.this));
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

}

