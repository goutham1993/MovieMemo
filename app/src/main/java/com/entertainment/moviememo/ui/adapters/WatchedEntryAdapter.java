package com.entertainment.moviememo.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.enums.LocationType;
import com.entertainment.moviememo.data.enums.TimeOfDay;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class WatchedEntryAdapter extends ListAdapter<WatchedEntry, WatchedEntryAdapter.WatchedEntryViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WatchedEntry entry);
        void onItemLongClick(WatchedEntry entry);
    }

    public WatchedEntryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WatchedEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_watched_entry, parent, false);
        return new WatchedEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchedEntryViewHolder holder, int position) {
        WatchedEntry entry = getItem(position);
        holder.bind(entry);
    }

    class WatchedEntryViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textRating;
        private TextView textWatchedDate;
        private Chip chipGenre;
        private Chip chipLocation;
        private Chip chipTime;
        private TextView textSpend;
        private TextView textDuration;
        private TextView textCompanions;
        private TextView textNotes;

        public WatchedEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_movie_title);
            textRating = itemView.findViewById(R.id.text_rating);
            textWatchedDate = itemView.findViewById(R.id.text_watched_date);
            chipGenre = itemView.findViewById(R.id.chip_genre);
            chipLocation = itemView.findViewById(R.id.chip_location);
            chipTime = itemView.findViewById(R.id.chip_time);
            textSpend = itemView.findViewById(R.id.text_spend);
            textDuration = itemView.findViewById(R.id.text_duration);
            textCompanions = itemView.findViewById(R.id.text_companions);
            textNotes = itemView.findViewById(R.id.text_notes);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(getItem(position));
                        return true;
                    }
                }
                return false;
            });
        }

        public void bind(WatchedEntry entry) {
            textTitle.setText(entry.title);
            
            if (entry.rating != null) {
                textRating.setText("⭐ " + String.format(Locale.getDefault(), "%d/10", entry.rating));
                textRating.setVisibility(View.VISIBLE);
            } else {
                textRating.setVisibility(View.GONE);
            }

            textWatchedDate.setText("📅 Watched on " + entry.watchedDate);

            // Genre chip
            if (entry.genre != null && !entry.genre.isEmpty()) {
                chipGenre.setText(entry.genre);
                chipGenre.setVisibility(View.VISIBLE);
            } else {
                chipGenre.setVisibility(View.GONE);
            }

            // Location chip
            try {
                LocationType locationType = LocationType.valueOf(entry.locationType);
                chipLocation.setText(locationType.name().replace("_", " "));
                chipLocation.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                chipLocation.setVisibility(View.GONE);
            }

            // Time chip
            try {
                TimeOfDay timeOfDay = TimeOfDay.valueOf(entry.timeOfDay);
                chipTime.setText(timeOfDay.name());
                chipTime.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                chipTime.setVisibility(View.GONE);
            }

            // Duration
            if (entry.durationMin != null && entry.durationMin > 0) {
                textDuration.setText("⏱️ " + entry.durationMin + " min");
                textDuration.setVisibility(View.VISIBLE);
            } else {
                textDuration.setVisibility(View.GONE);
            }

            // Spend text
            if (entry.spendCents != null && entry.spendCents > 0) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                String spendText = currencyFormat.format(entry.spendCents / 100.0);
                textSpend.setText("💰 " + spendText);
                textSpend.setVisibility(View.VISIBLE);
            } else {
                textSpend.setVisibility(View.GONE);
            }

            // Companions
            if (entry.companions != null && !entry.companions.isEmpty()) {
                textCompanions.setText("👥 with " + entry.companions);
                textCompanions.setVisibility(View.VISIBLE);
            } else {
                textCompanions.setVisibility(View.GONE);
            }

            // Notes
            if (entry.notes != null && !entry.notes.isEmpty()) {
                textNotes.setText(entry.notes);
                textNotes.setVisibility(View.VISIBLE);
            } else {
                textNotes.setVisibility(View.GONE);
            }
        }

    }

    private static final DiffUtil.ItemCallback<WatchedEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<WatchedEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull WatchedEntry oldItem, @NonNull WatchedEntry newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull WatchedEntry oldItem, @NonNull WatchedEntry newItem) {
            return oldItem.equals(newItem);
        }
    };
}