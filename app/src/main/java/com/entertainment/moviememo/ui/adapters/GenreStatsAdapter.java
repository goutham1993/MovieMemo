package com.entertainment.moviememo.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.KeyCount;

import java.util.List;

public class GenreStatsAdapter extends RecyclerView.Adapter<GenreStatsAdapter.GenreStatsViewHolder> {

    private List<KeyCount> genres;

    public GenreStatsAdapter() {
        this.genres = new java.util.ArrayList<>();
    }

    public void updateGenres(List<KeyCount> newGenres) {
        this.genres = newGenres;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GenreStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genre_chip, parent, false);
        return new GenreStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreStatsViewHolder holder, int position) {
        KeyCount genre = genres.get(position);
        holder.bind(genre);
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    static class GenreStatsViewHolder extends RecyclerView.ViewHolder {
        private final TextView textGenre;
        private final TextView textCount;

        public GenreStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            textGenre = itemView.findViewById(R.id.text_genre);
            textCount = itemView.findViewById(R.id.text_count);
        }

        public void bind(KeyCount genre) {
            textGenre.setText(genre.category);
            textCount.setText(String.valueOf(genre.cnt));
        }
    }
}
