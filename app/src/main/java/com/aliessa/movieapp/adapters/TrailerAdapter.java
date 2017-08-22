package com.aliessa.movieapp.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.aliessa.movieapp.DetailsActivity;
import com.aliessa.movieapp.R;
import com.aliessa.movieapp.model.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ali Essa on 4/28/2017
 */

public class TrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private DetailsActivity activity;
    private ArrayList<Trailer> trailers;

    public TrailerAdapter(ArrayList<Trailer> trailers, RecyclerView recyclerView, DetailsActivity activity) {
        inflater = activity.getLayoutInflater();
        this.recyclerView = recyclerView;
        this.activity = activity;
        this.trailers = trailers;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder;

        //inflate your layout and pass it to view holder
        view = inflater.inflate(R.layout.list_item_trailer, parent, false);
        holder = new ItemHolder(view);

        return holder;    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.width = (int) (recyclerView.getMeasuredWidth() * 0.75);
        layoutParams.setFullSpan(false);

        ItemHolder itemHolder = (ItemHolder) holder;
        itemHolder.setDetails(trailers.get(position));
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }
    private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewTitle;
        ImageView imageViewThumbnail;
        Trailer trailer;

        private ItemHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.textViewName);
            imageViewThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);

            itemView.setOnClickListener(this);
        }

        private void setDetails(Trailer trailer) {
            this.trailer = trailer;
            textViewTitle.setText(trailer.getName());

            String yt_thumbnail_url = "http://img.youtube.com/vi/" + trailer.getKey() + "/0.jpg";
            Picasso.with(activity).load(yt_thumbnail_url).into(imageViewThumbnail);
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                activity.startActivity(intent);
            }
        }
    }
}
