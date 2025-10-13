package com.entertainment.moviememo.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.MonthCount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonthlyStatsAdapter extends RecyclerView.Adapter<MonthlyStatsAdapter.MonthlyStatsViewHolder> {

    private List<MonthCount> monthlyStats;

    public MonthlyStatsAdapter() {
        this.monthlyStats = new java.util.ArrayList<>();
    }

    public void updateMonthlyStats(List<MonthCount> newStats) {
        this.monthlyStats = newStats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MonthlyStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month_stat, parent, false);
        return new MonthlyStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyStatsViewHolder holder, int position) {
        MonthCount monthStat = monthlyStats.get(position);
        holder.bind(monthStat);
    }

    @Override
    public int getItemCount() {
        return monthlyStats.size();
    }

    static class MonthlyStatsViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMonth;
        private final TextView textCount;

        public MonthlyStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            textMonth = itemView.findViewById(R.id.text_month);
            textCount = itemView.findViewById(R.id.text_count);
        }

        public void bind(MonthCount monthStat) {
            // Format the month (e.g., "2024-12" -> "December 2024")
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                Date date = inputFormat.parse(monthStat.ym);
                if (date != null) {
                    textMonth.setText(outputFormat.format(date));
                } else {
                    textMonth.setText(monthStat.ym);
                }
            } catch (ParseException e) {
                textMonth.setText(monthStat.ym);
            }
            
            textCount.setText(String.valueOf(monthStat.cnt));
        }
    }
}
