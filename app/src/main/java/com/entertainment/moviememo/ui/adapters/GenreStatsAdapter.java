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
        private final TextView textEmoji;
        private final TextView textGenre;
        private final TextView textCount;

        public GenreStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            textEmoji = itemView.findViewById(R.id.text_emoji);
            textGenre = itemView.findViewById(R.id.text_genre);
            textCount = itemView.findViewById(R.id.text_count);
        }

        public void bind(KeyCount genre) {
            textEmoji.setText(getEmojiForCategory(genre.category));
            textGenre.setText(genre.category);
            textCount.setText(String.valueOf(genre.cnt));
        }
        
        private String getEmojiForCategory(String category) {
            if (category == null) return "📊";
            
            // Genre emojis
            if (category.toLowerCase().contains("action")) return "💥";
            if (category.toLowerCase().contains("comedy")) return "😂";
            if (category.toLowerCase().contains("drama")) return "🎭";
            if (category.toLowerCase().contains("horror")) return "👻";
            if (category.toLowerCase().contains("romance")) return "💕";
            if (category.toLowerCase().contains("thriller")) return "🔪";
            if (category.toLowerCase().contains("sci-fi") || category.toLowerCase().contains("sci fi")) return "🚀";
            if (category.toLowerCase().contains("fantasy")) return "🧙";
            if (category.toLowerCase().contains("family")) return "👨‍👩‍👧‍👦";
            if (category.toLowerCase().contains("animation")) return "🎨";
            if (category.toLowerCase().contains("documentary")) return "📹";
            if (category.toLowerCase().contains("crime")) return "🔍";
            if (category.toLowerCase().contains("mystery")) return "🕵️";
            if (category.toLowerCase().contains("adventure")) return "🗺️";
            if (category.toLowerCase().contains("western")) return "🤠";
            if (category.toLowerCase().contains("musical")) return "🎵";
            if (category.toLowerCase().contains("war")) return "⚔️";
            if (category.toLowerCase().contains("biography")) return "📖";
            if (category.toLowerCase().contains("sport")) return "⚽";
            if (category.toLowerCase().contains("superhero")) return "🦸";
            
            // Location emojis
            if (category.toLowerCase().contains("theater")) return "🎬";
            if (category.toLowerCase().contains("home")) return "🏠";
            if (category.toLowerCase().contains("friends")) return "👥";
            if (category.toLowerCase().contains("other")) return "📍";
            
            // Time emojis
            if (category.toLowerCase().contains("morning")) return "🌅";
            if (category.toLowerCase().contains("afternoon")) return "☀️";
            if (category.toLowerCase().contains("evening")) return "🌆";
            if (category.toLowerCase().contains("night")) return "🌙";
            
            // Language emojis
            if (category.toLowerCase().contains("english")) return "🇺🇸";
            if (category.toLowerCase().contains("telugu") || category.toLowerCase().contains("తెలుగు")) return "🇮🇳";
            if (category.toLowerCase().contains("hindi")) return "🇮🇳";
            if (category.toLowerCase().contains("spanish")) return "🇪🇸";
            if (category.toLowerCase().contains("french")) return "🇫🇷";
            if (category.toLowerCase().contains("german")) return "🇩🇪";
            if (category.toLowerCase().contains("japanese")) return "🇯🇵";
            if (category.toLowerCase().contains("korean")) return "🇰🇷";
            if (category.toLowerCase().contains("chinese")) return "🇨🇳";
            
            // Default emoji
            return "📊";
        }
    }
}
