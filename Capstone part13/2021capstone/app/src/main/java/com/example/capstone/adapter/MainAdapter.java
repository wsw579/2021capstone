package com.example.capstone.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.PostInfo;
import com.example.capstone.R;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.GalleryViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        GalleryViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public MainAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
        mDataset = myDataset;
        this.activity=activity;
    }

    @NonNull
    @Override
    public MainAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);

        final GalleryViewHolder galleryViewHolder = new GalleryViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return galleryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, int position) {

        CardView cardView =holder.cardView;
        TextView textView=cardView.findViewById(R.id.textView);
        textView.setText(mDataset.get(position).getTitle());
    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}