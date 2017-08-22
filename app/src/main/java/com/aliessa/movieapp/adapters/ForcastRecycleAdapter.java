package com.aliessa.movieapp.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.aliessa.movieapp.MainActivity;
import com.aliessa.movieapp.DetailsActivity;
import com.aliessa.movieapp.model.Movie;
import com.aliessa.movieapp.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

/**
 * Created by Ali issa on 4/12/2017
 */
//Adapter RecyclerView
public class ForcastRecycleAdapter extends RecyclerView.Adapter<ForcastRecycleAdapter.ForcastAdapter> {


    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private MainActivity activity;
    private ArrayList<Movie> movies;


    public ForcastRecycleAdapter(ArrayList<Movie> movies,RecyclerView recyclerView,MainActivity activity){


        inflater =activity.getLayoutInflater();
        this.recyclerView = recyclerView;
        this.activity =activity;
        this.movies=movies;
    }

    class ForcastAdapter extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textViewTitle;
        ImageView imageViewPoster;
        Movie movie;

        public ForcastAdapter(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.textViewName);
            imageViewPoster = (ImageView) itemView.findViewById(R.id.imageViewPoster);
            itemView.setOnClickListener(this);
        }
        private void setDetails(Movie details){
            this.movie =details;
            textViewTitle.setText(details.getTitle());
            Picasso.with(activity).load("http://image.tmdb.org/t/p/w185/"+movie.getImage()).into(imageViewPoster);
        }

        @Override
        public void onClick(View v) {

            if (v == itemView) {
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE_OBJECT, movie);
                activity.startActivity(intent);
            }


        }
    }

    @Override
    public ForcastAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        ForcastAdapter forcastAdapter;
        view =inflater.inflate(R.layout.number_list_item,parent,false);
        forcastAdapter = new ForcastAdapter(view);
        return forcastAdapter;
    }

    @Override
    public void onBindViewHolder(ForcastAdapter holder, int position) {

        ForcastAdapter forcastAdapter =holder;
        forcastAdapter.setDetails(movies.get(position));

    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

}
