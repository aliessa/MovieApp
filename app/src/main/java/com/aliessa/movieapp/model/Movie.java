package com.aliessa.movieapp.model;

import android.database.Cursor;

import com.aliessa.movieapp.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Ali issa on 4/12/2017
 */
//use Serializable To Transfer Data & Movie is Model Of Data To use It AS Object
public class Movie implements Serializable {
    private  String id;
    private String title; // original_title
    private String image; // poster_path
    private String image2; //movie_details_image
    private String overview; // over_view_Movie
    private float rating; // vote_average
    private String date; // release_date
    public Movie (){}

//    Method Set Data
    public Movie(JSONObject movie) throws JSONException{
        this.id=movie.optString("id");
        this.title = movie.optString("original_title");
        this.image = movie.optString("poster_path");
        this.image2 = movie.optString("backdrop_path");
        this.overview = movie.optString("overview");
        this.rating = movie.optInt("vote_average");
        this.date = movie.optString("release_date");
    }
    public Movie(Cursor cursor) {
        this.id = cursor.getString(MainActivity.COL_MOVIE_ID);
        this.title = cursor.getString(MainActivity.COL_TITLE);
        this.image = cursor.getString(MainActivity.COL_IMAGE);
        this.image2 = cursor.getString(MainActivity.COL_IMAGE2);
        this.overview = cursor.getString(MainActivity.COL_OVERVIEW);
        this.rating = cursor.getInt(MainActivity.COL_RATING);
        this.date = cursor.getString(MainActivity.COL_DATE);
    }

//    Get Data
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getImage2() {
        return image2;
    }

    public String getOverview() {
        return overview;
    }

    public float getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
