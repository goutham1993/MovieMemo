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
import com.entertainment.moviememo.data.enums.Language;
import com.entertainment.moviememo.data.enums.LocationType;
import com.entertainment.moviememo.data.enums.TimeOfDay;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        private TextView textSpendUnderDuration;
        private TextView textCompanions;
        private TextView textLanguage;
        private TextView textNotes;
        private TextView textTheaterInfo;
        private TextView textStreamingPlatform;

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
            textSpendUnderDuration = itemView.findViewById(R.id.text_spend_under_duration);
            textCompanions = itemView.findViewById(R.id.text_companions);
            textLanguage = itemView.findViewById(R.id.text_language);
            textNotes = itemView.findViewById(R.id.text_notes);
            textTheaterInfo = itemView.findViewById(R.id.text_theater_info);
            textStreamingPlatform = itemView.findViewById(R.id.text_streaming_platform);

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

            textWatchedDate.setText("📅 " + formatDateWithDay(entry.watchedDate));

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
                String locationText = getLocationEmoji(locationType) + " " + locationType.getDisplayName();
                chipLocation.setText(locationText);
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

            // Spend text under duration
            if (entry.spendCents != null && entry.spendCents > 0) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                String spendText = currencyFormat.format(entry.spendCents / 100.0);
                // Replace $ with 💰 emoji
                String emojiSpendText = "💰 " + spendText.replace("$", "");
                textSpendUnderDuration.setText(emojiSpendText);
                textSpendUnderDuration.setVisibility(View.VISIBLE);
            } else {
                textSpendUnderDuration.setVisibility(View.GONE);
            }

            // Hide the old spend text in metadata section
            textSpend.setVisibility(View.GONE);

            // Companions
            if (entry.companions != null && !entry.companions.isEmpty()) {
                textCompanions.setText("👥 with " + entry.companions);
                textCompanions.setVisibility(View.VISIBLE);
            } else {
                textCompanions.setVisibility(View.GONE);
            }

            // Language
            if (entry.language != null && !entry.language.isEmpty()) {
                try {
                    Language language = Language.fromCode(entry.language);
                    String languageDisplay = "🌐 in " + language.getDisplayName();
                    textLanguage.setText(languageDisplay);
                    textLanguage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    textLanguage.setVisibility(View.GONE);
                }
            } else {
                textLanguage.setVisibility(View.GONE);
            }

            // Theater info and streaming platform (only for THEATER/HOME location)
            boolean theaterInfoVisible = false;
            boolean streamingPlatformVisible = false;
            try {
                LocationType locationType = LocationType.valueOf(entry.locationType);
                if (locationType == LocationType.THEATER && entry.theaterName != null && !entry.theaterName.isEmpty()) {
                    StringBuilder theaterInfo = new StringBuilder("📍 at ");
                    theaterInfo.append(entry.theaterName);
                    if (entry.city != null && !entry.city.isEmpty()) {
                        theaterInfo.append(", ").append(entry.city);
                    }
                    textTheaterInfo.setText(theaterInfo.toString());
                    textTheaterInfo.setVisibility(View.VISIBLE);
                    theaterInfoVisible = true;
                    textStreamingPlatform.setVisibility(View.GONE);
                } else if (locationType == LocationType.HOME && entry.streamingPlatform != null && !entry.streamingPlatform.isEmpty()) {
                    // Streaming platform (only for HOME location)
                    textStreamingPlatform.setText("📺 Watched on " + entry.streamingPlatform);
                    textStreamingPlatform.setVisibility(View.VISIBLE);
                    streamingPlatformVisible = true;
                    textTheaterInfo.setVisibility(View.GONE);
                } else {
                    textTheaterInfo.setVisibility(View.GONE);
                    textStreamingPlatform.setVisibility(View.GONE);
                }
            } catch (IllegalArgumentException e) {
                textTheaterInfo.setVisibility(View.GONE);
                textStreamingPlatform.setVisibility(View.GONE);
            }

            // Notes - position it after whichever field is visible
            if (entry.notes != null && !entry.notes.isEmpty()) {
                textNotes.setText("💭 " + entry.notes);
                textNotes.setVisibility(View.VISIBLE);
                // Position notes below whichever info is visible
                android.view.ViewGroup.LayoutParams params = textNotes.getLayoutParams();
                if (params instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams = 
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) params;
                    if (theaterInfoVisible) {
                        constraintParams.topToBottom = R.id.text_theater_info;
                    } else if (streamingPlatformVisible) {
                        constraintParams.topToBottom = R.id.text_streaming_platform;
                    } else {
                        constraintParams.topToBottom = R.id.text_language;
                    }
                    textNotes.setLayoutParams(constraintParams);
                }
            } else {
                textNotes.setVisibility(View.GONE);
            }
        }

        private String getLocationEmoji(LocationType locationType) {
            switch (locationType) {
                case HOME:
                    return "🏠";
                case THEATER:
                    return "🎭";
                case FRIENDS_HOME:
                    return "👥";
                case OTHER:
                    return "📍";
                default:
                    return "📍";
            }
        }

    }

    private String formatDateWithDay(String dateString) {
        try {
            // Parse the date string (assuming format like "2024-12-15")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            
            if (date != null) {
                // Format as "Monday, Dec 15, 2024"
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            // If parsing fails, return the original date string
            e.printStackTrace();
        }
        
        // Fallback to original format if parsing fails
        return "Watched on " + dateString;
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