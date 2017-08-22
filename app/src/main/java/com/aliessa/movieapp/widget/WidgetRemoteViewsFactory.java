package com.aliessa.movieapp.widget;

/**
 * Created by Ali Essa on 6/11/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.aliessa.movieapp.R;
import com.aliessa.movieapp.model.Movie;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.aliessa.movieapp.MainActivity.Movies;


public class WidgetRemoteViewsFactory implements RemoteViewsFactory {

    private Context mContext;

    public WidgetRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {
        new GetOnlinedata();
    }

    @Override
    public void onDataSetChanged() {
        new GetOnlinedata();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return Movies.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_trailer);
        try {
            rv.setImageViewBitmap(R.id.imageViewThumbnail, BitmapFactory.decodeStream(new URL(Movies.get(position).getImage()).openConnection().getInputStream()));
        } catch (IOException e) {
            e.getStackTrace();
        }
        rv.setTextViewText(R.id.textViewName, Movies.get(position).getTitle());

        Intent intent = new Intent();
        intent.putExtra("item", position);
        rv.setOnClickFillInIntent(R.id.imageViewThumbnail, intent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }



    public class GetOnlinedata implements LoaderManager.LoaderCallbacks<ArrayList<Movie>>{

        @Override
        public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<ArrayList<Movie>>(mContext) {
                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public ArrayList<Movie> loadInBackground() {
                    System.out.println("ArrayList<Movie> loadInBackground()");
                    HttpURLConnection urlConnection = null;
                    BufferedReader reader = null;

                    try {
                        Uri builtUri = Uri.parse("https://api.themoviedb.org/3/movie/POPULARITY")
                                .buildUpon()
                                .build();

                        URL url = new URL(builtUri.toString());

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            return null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }
                        if (buffer.length() == 0) {
                            return null;
                        }
                        JSONArray bakingJArray = new JSONArray(buffer.toString());
                        Movies = new ArrayList<>();
                        for (int i = 0; i < bakingJArray.length(); i++) {
                            System.out.println();
                            Movies.add(new Movie(bakingJArray.getJSONObject(i)));
                            Log.e("name: ", Movies.get(i).getTitle());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }

                    return Movies;

                }
            };

        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {

        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Movie>> loader) {

        }
    }
}
