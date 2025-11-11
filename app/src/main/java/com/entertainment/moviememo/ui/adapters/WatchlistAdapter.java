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
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.enums.WhereToWatch;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WatchlistAdapter extends ListAdapter<WatchlistItem, WatchlistAdapter.WatchlistViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WatchlistItem item);
        void onItemLongClick(WatchlistItem item);
    }

    public WatchlistAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WatchlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_watchlist_entry, parent, false);
        return new WatchlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistViewHolder holder, int position) {
        WatchlistItem item = getItem(position);
        holder.bind(item);
    }

    class WatchlistViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textNotes;
        private TextView textLanguage;
        private TextView textWhereToWatch;
        private TextView textReleaseDate;
        private TextView textTargetDate;

        public WatchlistViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_movie_title);
            textNotes = itemView.findViewById(R.id.text_notes);
            textLanguage = itemView.findViewById(R.id.text_language);
            textWhereToWatch = itemView.findViewById(R.id.text_where_to_watch);
            textReleaseDate = itemView.findViewById(R.id.text_release_date);
            textTargetDate = itemView.findViewById(R.id.text_target_date);

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

        public void bind(WatchlistItem item) {
            textTitle.setText(item.title);

            // Notes
            if (item.notes != null && !item.notes.isEmpty()) {
                textNotes.setText(item.notes);
                textNotes.setVisibility(View.VISIBLE);
            } else {
                textNotes.setVisibility(View.GONE);
            }

            // Language
            if (item.language != null && !item.language.isEmpty()) {
                try {
                    com.entertainment.moviememo.data.enums.Language language = com.entertainment.moviememo.data.enums.Language.fromCode(item.language);
                    String languageDisplay = "🌐 " + language.getDisplayName();
                    textLanguage.setText(languageDisplay);
                    textLanguage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    textLanguage.setVisibility(View.GONE);
                }
            } else {
                textLanguage.setVisibility(View.GONE);
            }

            // Where to watch
            if (item.whereToWatch != null && !item.whereToWatch.isEmpty()) {
                try {
                    WhereToWatch whereToWatch = WhereToWatch.valueOf(item.whereToWatch);
                    String whereToWatchDisplay = "📍 " + whereToWatch.getDisplayName();
                    textWhereToWatch.setText(whereToWatchDisplay);
                    textWhereToWatch.setVisibility(View.VISIBLE);
                } catch (IllegalArgumentException e) {
                    textWhereToWatch.setVisibility(View.GONE);
                }
            } else {
                textWhereToWatch.setVisibility(View.GONE);
            }

            // Release date (available for all options)
            if (item.releaseDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date(item.releaseDate));
                textReleaseDate.setText("🎬 Release: " + formattedDate);
                textReleaseDate.setVisibility(View.VISIBLE);
            } else {
                textReleaseDate.setVisibility(View.GONE);
            }

            // Target date
            if (item.targetDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date(item.targetDate));
                textTargetDate.setText("Target: " + formattedDate);
                textTargetDate.setVisibility(View.VISIBLE);
            } else {
                textTargetDate.setVisibility(View.GONE);
            }
        }


    }

    private static final DiffUtil.ItemCallback<WatchlistItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<WatchlistItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull WatchlistItem oldItem, @NonNull WatchlistItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull WatchlistItem oldItem, @NonNull WatchlistItem newItem) {
            return oldItem.equals(newItem);
        }
    };
}