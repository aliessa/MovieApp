package com.aliessa.movieapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ali Essa on 4/26/2017
 */

public class Contract {

    public static final String CONTENT_AUTHORITY = "com.aliessa.movieapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String data_name = "Movie_data";
        public static final String table_name = "movie";
        public static final int version = 2;
        public static final String id_rw = "id";
        public static final String title_rw = "title";
        public static final String release_date_rw = "date";
        public static final String overview_rw = "overview";
        public static final String image = "image";
        public static final String image2 = "image2";
        public static final String rate_rw = "rate";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }
}