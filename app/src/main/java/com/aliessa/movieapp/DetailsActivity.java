package com.aliessa.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.aliessa.movieapp.data.Contract;
import com.aliessa.movieapp.adapters.TrailerAdapter;
import com.aliessa.movieapp.model.Movie;
import com.aliessa.movieapp.model.Review;
import com.aliessa.movieapp.model.Trailer;
import com.squareup.picasso.Picasso;
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

import static android.view.View.VISIBLE;


public class DetailsActivity extends AppCompatActivity {

    public static final String MOVIE_OBJECT = "movie";
    @BindView(R.id.textViewName) TextView textViewName;
    @BindView(R.id.textViewRelease) TextView textViewRelease;
    @BindView(R.id.textViewOverView) TextView textViewOverView;
    @BindView(R.id.textViewRating) TextView textViewRating;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.imageViewPoster) ImageView imageViewPoster;
    @BindView(R.id.imageViewBack) ImageView imageViewBack;
    @BindView(R.id.reviewsRecyclerView) RecyclerView reviewsRecyclerView;
    @BindView(R.id.reviews) LinearLayout linearLayout;
    @BindView(R.id.favrt) Button favourite;
    ShareActionProvider mShareActionProvider;
    private String apiKey;
    boolean check;
    Movie movie;
    Trailer trailerItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        //Call Intent And Use Serializable To Get Data
        Intent intent = getIntent();
        movie = (Movie) intent.getSerializableExtra(MOVIE_OBJECT);
        favourite = (Button) findViewById(R.id.favrt);
        apiKey = getResources().getString(R.string.api_key);
//       Call Method setDetails To Set Data
        setDetails(movie);
        getReviewsFromTMDb(String.valueOf(movie.getId()));
        getTrailersFromTMDb(String.valueOf(movie.getId()));
        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check) {
                    ContentValues values =new ContentValues();
                    values.put(Contract.MovieEntry.id_rw, movie.getId());
                    values.put(Contract.MovieEntry.title_rw, movie.getTitle());
                    values.put(Contract.MovieEntry.image, movie.getImage());
                    values.put(Contract.MovieEntry.image2, movie.getImage2());
                    values.put(Contract.MovieEntry.overview_rw, movie.getOverview());
                    values.put(Contract.MovieEntry.rate_rw, movie.getRating());
                    values.put(Contract.MovieEntry.data_name, movie.getDate());
                    DetailsActivity.this.getContentResolver().insert(Contract.MovieEntry.CONTENT_URI,values);
                    check=true;
                    Toast.makeText(DetailsActivity.this, getString(R.string.add_fav), Toast.LENGTH_SHORT).show();
                } else {
                    DetailsActivity.this.getContentResolver().delete(
                            Contract.MovieEntry.CONTENT_URI,
                            Contract.MovieEntry.id_rw + " = ?",
                            new String[]{movie.getId()});
                    check=false;
                    Toast.makeText(DetailsActivity.this, getString(R.string.remove_fav), Toast.LENGTH_SHORT).show();
                }

                selectfavorite();
            }
        });

    }

    private void setDetails(Movie movie) {
        textViewName.setText(movie.getTitle());
        textViewRelease.setText(movie.getDate());
        textViewOverView.setText(movie.getOverview());
        textViewRating.setText(getString(R.string.rated) + movie.getRating() + getString(R.string.rated_max));

        Picasso.with(this).load(getString(R.string.Url_Img) + movie.getImage())
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.poster_placeholder)
                .into(imageViewPoster);
        Picasso.with(this)
                .load(getString(R.string.Url_Img) + movie.getImage2())
                .placeholder(R.drawable.trailer_thumbnail_placeholder)
                .error(R.drawable.trailer_thumbnail_placeholder)
                .into(imageViewBack);

    }
    public static int isFavorited(Context context, String id) {
        Cursor cursor = context.getContentResolver().query(
                Contract.MovieEntry.CONTENT_URI,
                null,   // projection
                Contract.MovieEntry.id_rw + " = ?", // selection
                new String[] { id },   // selectionArgs
                null    // sort order
        );
        if (cursor!=null){
        int numRows = cursor.getCount();
        cursor.close();
        return numRows;}
        return 0;
    }

    public void selectfavorite() {

        if (isFavorited(this,movie.getId())==1){
            favourite.setBackgroundResource(R.drawable.loved);
        check=true;}
        else {
            favourite.setBackgroundResource(R.drawable.disloved);
            check=false;
        }

    }

    @Override
    protected void onStart() {
        selectfavorite();
        super.onStart();
    }

    private void getReviewsFromTMDb(String movieID) {
        // Execute ReviewsAsyncTask
        ReviewsAsyncTask reviewsAsyncTask = new ReviewsAsyncTask(apiKey, movieID);
        reviewsAsyncTask.execute();
    }

    private void getTrailersFromTMDb(String movieID) {
        // Execute TrailerAsyncTask
        TrailerAsyncTask trailerAsyncTask = new TrailerAsyncTask(apiKey, movieID);
        trailerAsyncTask.execute();

    }

    /*

     ReviewsAsyncTask to get data from Reviews api

      */
    private class ReviewsAsyncTask extends AsyncTask<String, Void, ArrayList<Review>> {

        private final String LOG_TAG = ReviewsAsyncTask.class.getCanonicalName();
        private final String mApiKey;
        private String movieID;

        /**
         * {@link java.lang.reflect.Constructor}
         *
         * @param movieID movieID.
         * @param mApiKey TMDb API key.
         */
        private ReviewsAsyncTask(String mApiKey, String movieID) {
            this.mApiKey = mApiKey;
            this.movieID = movieID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(VISIBLE);
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String reviewsJsonStr = null;
            try {
                URL url = getApiUrl();

                // Start connecting to get JSON
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Adds '\n' at last line if not already there.
                    // This supposedly makes it easier to debug.
                    builder.append(line).append("");
                }
                if (builder.length() == 0) {
                    return null;
                }
                reviewsJsonStr = builder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "error", e);
                return null;
            } finally {
                // Tidy up: release url connection and buffered reader
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "close", e);
                    }
                }
            }
            try {
                return getReviewsDataFromJson(reviewsJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        private URL getApiUrl() throws MalformedURLException {
            final String TMDB_BASE_URL = getString(R.string.api_url) + movieID + "/reviews";
            final String API_KEY_PARAM = "api_key";
            Uri builtUri;
            builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, mApiKey)
                    .build();
            return new URL(builtUri.toString());

        }

        private ArrayList<Review> getReviewsDataFromJson(String reviewsJsonStr) throws JSONException {
            final String TAG_RESULTS = "results";

            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray resultsArray = reviewsJson.getJSONArray(TAG_RESULTS);

            ArrayList<Review> reviews = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject reviewInfo = resultsArray.getJSONObject(i);
                //
                Review reviewItem = new Review(reviewInfo);
                reviews.add(reviewItem);
            }
            return reviews;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            progressBar.setVisibility(View.GONE);
            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    Review review = reviews.get(i);
                    View reviewItem = getLayoutInflater().inflate(R.layout.list_item_review, null);
                    linearLayout.addView(reviewItem);
                    TextView textViewAuthor = (TextView) reviewItem.findViewById(R.id.textViewAuthor);
                    TextView textViewReview = (TextView) reviewItem.findViewById(R.id.textViewReview);

                    textViewAuthor.setText(review.getAuthor());
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        textViewReview.setText(Html.fromHtml(review.getContent(), Html.FROM_HTML_OPTION_USE_CSS_COLORS));
                    } else {
                        //noinspection deprecation
                        textViewReview.setText(Html.fromHtml(review.getContent()));
                    }
                }
            }
        }
    }
     /*

     TrailerAsyncTask to get data from Trailer api

      */
     private class TrailerAsyncTask extends AsyncTask<String, Void, ArrayList<Trailer>> {

         private final String LOG_TAG = TrailerAsyncTask.class.getCanonicalName();
         private final String mApiKey;
         private String movieID;

         /**
          * {@link java.lang.reflect.Constructor}
          *
          * @param movieID movieID.
          * @param mApiKey TMDb API key.
          */
         private TrailerAsyncTask(String mApiKey, String movieID) {
             this.mApiKey = mApiKey;
             this.movieID = movieID;
         }

         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressBar.setVisibility(VISIBLE);
         }

         @Override
         protected ArrayList<Trailer> doInBackground(String... params) {

             HttpURLConnection urlConnection = null;
             BufferedReader reader = null;
             String trailersJsonStr = null;
             try {
                 URL url = getApiUrl();

                 // Start connecting to get JSON
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.connect();
                 InputStream inputStream = urlConnection.getInputStream();
                 StringBuilder builder = new StringBuilder();
                 if (inputStream == null) {
                     return null;
                 }
                 reader = new BufferedReader(new InputStreamReader(inputStream));
                 String line;
                 while ((line = reader.readLine()) != null) {
                     // Adds '\n' at last line if not already there.
                     // This supposedly makes it easier to debug.
                     builder.append(line).append("");
                 }
                 if (builder.length() == 0) {
                     return null;
                 }
                 trailersJsonStr = builder.toString();
             } catch (IOException e) {
                 Log.e(LOG_TAG, "error", e);
                 return null;
             } finally {
                 // Tidy up: release url connection and buffered reader
                 if (urlConnection != null) {
                     urlConnection.disconnect();
                 }
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (final IOException e) {
                         Log.e(LOG_TAG, "close", e);
                     }
                 }
             }
             try {
                 return getTrailersDataFromJson(trailersJsonStr);
             } catch (JSONException e) {
                 e.printStackTrace();
             }

             return null;
         }

         /**
          * Extracts data from the JSON object and returns an Array of movie objects.
          *
          * @param trailersJsonStr JSON string to be traversed
          * @return ArrayList of Trailer objects
          * @throws JSONException throws JSONException
          */
         private ArrayList<Trailer> getTrailersDataFromJson(String trailersJsonStr) throws JSONException {
             final String TAG_RESULTS = getString(R.string.results);

             JSONObject trailersJson = new JSONObject(trailersJsonStr);
             JSONArray resultsArray = trailersJson.getJSONArray(TAG_RESULTS);

             ArrayList<Trailer> trailers = new ArrayList<>();
             for (int i = 0; i < resultsArray.length(); i++) {
                 JSONObject trailersInfo = resultsArray.getJSONObject(i);

                 trailerItem = new Trailer(trailersInfo);
                 trailers.add(trailerItem);
             }
             return trailers;
         }

         /**
          * Creates and returns an URL.
          *
          * @return URL formatted with parameters for the API
          * @throws MalformedURLException throws MalformedURLException
          */
         private URL getApiUrl() throws MalformedURLException {
             final String TMDB_BASE_URL = getString(R.string.api_url) + movieID + "/videos";
             final String API_KEY_PARAM = "api_key";
             Uri builtUri;
             builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                     .appendQueryParameter(API_KEY_PARAM, mApiKey)
                     .build();
             return new URL(builtUri.toString());

         }

         @Override
         protected void onPostExecute(ArrayList<Trailer> trailers) {
             progressBar.setVisibility(View.GONE);
             if (trailers != null) {
                 //setting recyclerView layout and adapter.
                 StaggeredGridLayoutManager layoutManager1 = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                 layoutManager1.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
                 reviewsRecyclerView.setLayoutManager(layoutManager1);
                 reviewsRecyclerView.setHasFixedSize(false);
                 reviewsRecyclerView.setItemAnimator(new DefaultItemAnimator());

                 reviewsRecyclerView.setAdapter(new TrailerAdapter(trailers, reviewsRecyclerView, DetailsActivity.this));
             }
         }
     }
    private Intent createShareMovie() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            //noinspection deprecation
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.app_name) + ": " + movie.getTitle() + " " +
                "http://www.youtube.com/watch?v=" + trailerItem.getKey());
        return shareIntent;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (movie != null) {
            getMenuInflater().inflate(R.menu.details, menu);

            MenuItem action_share = menu.findItem(R.id.action_share);

            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_share);

            if (trailerItem != null) {
                mShareActionProvider.setShareIntent(createShareMovie());
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

}
