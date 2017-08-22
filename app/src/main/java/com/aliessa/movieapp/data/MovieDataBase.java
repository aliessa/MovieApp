package com.aliessa.movieapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.aliessa.movieapp.model.Movie;

import java.util.ArrayList;


/**
 * Created by Ali Essa on 4/26/2017
 */

public class MovieDataBase extends SQLiteOpenHelper {

     String log = MovieDataBase.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(log, "constractor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + Contract.MovieEntry.table_name + " (" +
                Contract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Contract.MovieEntry.id_rw + " INTEGER NOT NULL, " +
                Contract.MovieEntry.title_rw + " TEXT NOT NULL, " +
                Contract.MovieEntry.image + " TEXT, " +
                Contract.MovieEntry.image2 + " TEXT, " +
                Contract.MovieEntry.overview_rw + " TEXT, " +
                Contract.MovieEntry.rate_rw + " INTEGER, " +
                Contract.MovieEntry.data_name + " TEXT);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contract.MovieEntry.data_name);
        onCreate(db);
    }

}
