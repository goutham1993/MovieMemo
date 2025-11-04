package com.entertainment.moviememo.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.entities.WatchlistItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportImportHelper {
    private static final String TAG = "ExportImportHelper";
    
    public static class ExportResult {
        public final boolean success;
        public final String filePath;
        public final String errorMessage;
        
        public ExportResult(boolean success, String filePath, String errorMessage) {
            this.success = success;
            this.filePath = filePath;
            this.errorMessage = errorMessage;
        }
    }
    
    public static class ImportResult {
        public final boolean success;
        public final int watchedCount;
        public final int watchlistCount;
        public final String errorMessage;
        
        public ImportResult(boolean success, int watchedCount, int watchlistCount, String errorMessage) {
            this.success = success;
            this.watchedCount = watchedCount;
            this.watchlistCount = watchlistCount;
            this.errorMessage = errorMessage;
        }
    }
    
    // Export to JSON
    public static ExportResult exportToJson(Context context) {
        try {
            List<WatchedEntry> watchedEntries = AppDatabase.getDatabase(context)
                    .movieDao().getAllWatchedSync();
            List<WatchlistItem> watchlistItems = AppDatabase.getDatabase(context)
                    .movieDao().getAllWatchlistSync();
            
            JSONObject exportData = new JSONObject();
            exportData.put("exportDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date()));
            exportData.put("version", "1.0");
            
            // Export watched entries
            JSONArray watchedArray = new JSONArray();
            for (WatchedEntry entry : watchedEntries) {
                JSONObject entryJson = new JSONObject();
                entryJson.put("id", entry.id);
                entryJson.put("title", entry.title);
                entryJson.put("rating", entry.rating != null ? entry.rating : JSONObject.NULL);
                entryJson.put("watchedDate", entry.watchedDate);
                entryJson.put("locationType", entry.locationType);
                entryJson.put("locationNotes", entry.locationNotes != null ? entry.locationNotes : JSONObject.NULL);
                entryJson.put("companions", entry.companions != null ? entry.companions : JSONObject.NULL);
                entryJson.put("spendCents", entry.spendCents != null ? entry.spendCents : JSONObject.NULL);
                entryJson.put("durationMin", entry.durationMin != null ? entry.durationMin : JSONObject.NULL);
                entryJson.put("timeOfDay", entry.timeOfDay);
                entryJson.put("genre", entry.genre != null ? entry.genre : JSONObject.NULL);
                entryJson.put("notes", entry.notes != null ? entry.notes : JSONObject.NULL);
                entryJson.put("posterUri", entry.posterUri != null ? entry.posterUri : JSONObject.NULL);
                entryJson.put("language", entry.language != null ? entry.language : JSONObject.NULL);
                entryJson.put("theaterName", entry.theaterName != null ? entry.theaterName : JSONObject.NULL);
                entryJson.put("city", entry.city != null ? entry.city : JSONObject.NULL);
                entryJson.put("streamingPlatform", entry.streamingPlatform != null ? entry.streamingPlatform : JSONObject.NULL);
                watchedArray.put(entryJson);
            }
            exportData.put("watchedEntries", watchedArray);
            
            // Export watchlist items
            JSONArray watchlistArray = new JSONArray();
            for (WatchlistItem item : watchlistItems) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("id", item.id);
                itemJson.put("title", item.title);
                itemJson.put("notes", item.notes != null ? item.notes : JSONObject.NULL);
                itemJson.put("priority", item.priority != null ? item.priority : JSONObject.NULL);
                itemJson.put("createdAt", item.createdAt != null ? item.createdAt : JSONObject.NULL);
                itemJson.put("targetDate", item.targetDate != null ? item.targetDate : JSONObject.NULL);
                itemJson.put("language", item.language != null ? item.language : JSONObject.NULL);
                itemJson.put("whereToWatch", item.whereToWatch != null ? item.whereToWatch : JSONObject.NULL);
                itemJson.put("releaseDate", item.releaseDate != null ? item.releaseDate : JSONObject.NULL);
                watchlistArray.put(itemJson);
            }
            exportData.put("watchlistItems", watchlistArray);
            
            // Write to file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "moviememo_export_" + timestamp + ".json";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File exportFile = new File(downloadsDir, fileName);
            
            try (FileOutputStream fos = new FileOutputStream(exportFile)) {
                fos.write(exportData.toString(2).getBytes(StandardCharsets.UTF_8));
            }
            
            return new ExportResult(true, exportFile.getAbsolutePath(), null);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting to JSON", e);
            return new ExportResult(false, null, e.getMessage());
        }
    }
    
    // Export to CSV
    public static ExportResult exportToCsv(Context context) {
        try {
            List<WatchedEntry> watchedEntries = AppDatabase.getDatabase(context)
                    .movieDao().getAllWatchedSync();
            List<WatchlistItem> watchlistItems = AppDatabase.getDatabase(context)
                    .movieDao().getAllWatchlistSync();
            
            StringBuilder csvContent = new StringBuilder();
            
            // CSV Header
            csvContent.append("Type,Title,Rating,Watched Date,Location Type,Location Notes,Companions,")
                    .append("Spend (Cents),Duration (Min),Time of Day,Genre,Notes,Poster URI,Language,")
                    .append("Theater Name,City,Streaming Platform,Priority,Created At,Target Date,")
                    .append("Where To Watch,Release Date\n");
            
            // Export watched entries
            for (WatchedEntry entry : watchedEntries) {
                csvContent.append("Watched,")
                        .append(escapeCsv(entry.title)).append(",")
                        .append(entry.rating != null ? entry.rating : "").append(",")
                        .append(escapeCsv(entry.watchedDate)).append(",")
                        .append(escapeCsv(entry.locationType)).append(",")
                        .append(escapeCsv(entry.locationNotes)).append(",")
                        .append(escapeCsv(entry.companions)).append(",")
                        .append(entry.spendCents != null ? entry.spendCents : "").append(",")
                        .append(entry.durationMin != null ? entry.durationMin : "").append(",")
                        .append(escapeCsv(entry.timeOfDay)).append(",")
                        .append(escapeCsv(entry.genre)).append(",")
                        .append(escapeCsv(entry.notes)).append(",")
                        .append(escapeCsv(entry.posterUri)).append(",")
                        .append(escapeCsv(entry.language)).append(",")
                        .append(escapeCsv(entry.theaterName)).append(",")
                        .append(escapeCsv(entry.city)).append(",")
                        .append(escapeCsv(entry.streamingPlatform)).append(",")
                        .append(",").append(",").append(",").append(",").append(",").append("\n");
            }
            
            // Export watchlist items
            for (WatchlistItem item : watchlistItems) {
                csvContent.append("Watchlist,")
                        .append(escapeCsv(item.title)).append(",") // Title
                        .append(",") // Rating (empty for watchlist)
                        .append(",") // Watched Date (empty for watchlist)
                        .append(",") // Location Type (empty for watchlist)
                        .append(",") // Location Notes (empty for watchlist)
                        .append(",") // Companions (empty for watchlist)
                        .append(",") // Spend (Cents) (empty for watchlist)
                        .append(",") // Duration (Min) (empty for watchlist)
                        .append(",") // Time of Day (empty for watchlist)
                        .append(",") // Genre (empty for watchlist)
                        .append(escapeCsv(item.notes)).append(",") // Notes
                        .append(",") // Poster URI (empty for watchlist)
                        .append(escapeCsv(item.language)).append(",") // Language
                        .append(",") // Theater Name (empty for watchlist)
                        .append(",") // City (empty for watchlist)
                        .append(",") // Streaming Platform (empty for watchlist)
                        .append(item.priority != null ? item.priority : "").append(",") // Priority
                        .append(item.createdAt != null ? item.createdAt : "").append(",") // Created At
                        .append(item.targetDate != null ? item.targetDate : "").append(",") // Target Date
                        .append(escapeCsv(item.whereToWatch)).append(",") // Where To Watch
                        .append(item.releaseDate != null ? item.releaseDate : "").append("\n"); // Release Date
            }
            
            // Write to file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "moviememo_export_" + timestamp + ".csv";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File exportFile = new File(downloadsDir, fileName);
            
            try (FileOutputStream fos = new FileOutputStream(exportFile)) {
                fos.write(csvContent.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            return new ExportResult(true, exportFile.getAbsolutePath(), null);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting to CSV", e);
            return new ExportResult(false, null, e.getMessage());
        }
    }
    
    // Import from JSON
    public static ImportResult importFromJson(Context context, String filePath) {
        try {
            File importFile = new File(filePath);
            if (!importFile.exists()) {
                return new ImportResult(false, 0, 0, "File not found");
            }
            
            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(importFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }
            
            JSONObject exportData = new JSONObject(jsonContent.toString());
            
            List<WatchedEntry> watchedEntries = new ArrayList<>();
            JSONArray watchedArray = exportData.getJSONArray("watchedEntries");
            for (int i = 0; i < watchedArray.length(); i++) {
                JSONObject entryJson = watchedArray.getJSONObject(i);
                WatchedEntry entry = new WatchedEntry(
                        entryJson.getString("title"),
                        entryJson.getString("watchedDate"),
                        entryJson.getString("locationType"),
                        entryJson.getString("timeOfDay")
                );
                entry.id = entryJson.getLong("id");
                entry.rating = entryJson.isNull("rating") ? null : entryJson.getInt("rating");
                entry.locationNotes = entryJson.isNull("locationNotes") ? null : entryJson.getString("locationNotes");
                entry.companions = entryJson.isNull("companions") ? null : entryJson.getString("companions");
                entry.spendCents = entryJson.isNull("spendCents") ? null : entryJson.getInt("spendCents");
                entry.durationMin = entryJson.isNull("durationMin") ? null : entryJson.getInt("durationMin");
                entry.genre = entryJson.isNull("genre") ? null : entryJson.getString("genre");
                entry.notes = entryJson.isNull("notes") ? null : entryJson.getString("notes");
                entry.posterUri = entryJson.isNull("posterUri") ? null : entryJson.getString("posterUri");
                entry.language = entryJson.isNull("language") ? null : entryJson.getString("language");
                entry.theaterName = entryJson.isNull("theaterName") ? null : entryJson.getString("theaterName");
                entry.city = entryJson.isNull("city") ? null : entryJson.getString("city");
                entry.streamingPlatform = entryJson.isNull("streamingPlatform") ? null : entryJson.getString("streamingPlatform");
                watchedEntries.add(entry);
            }
            
            List<WatchlistItem> watchlistItems = new ArrayList<>();
            JSONArray watchlistArray = exportData.getJSONArray("watchlistItems");
            for (int i = 0; i < watchlistArray.length(); i++) {
                JSONObject itemJson = watchlistArray.getJSONObject(i);
                WatchlistItem item = new WatchlistItem(itemJson.getString("title"));
                item.id = itemJson.getLong("id");
                item.notes = itemJson.isNull("notes") ? null : itemJson.getString("notes");
                item.priority = itemJson.isNull("priority") ? null : itemJson.getInt("priority");
                item.createdAt = itemJson.isNull("createdAt") ? null : itemJson.getLong("createdAt");
                item.targetDate = itemJson.isNull("targetDate") ? null : itemJson.getLong("targetDate");
                item.language = itemJson.isNull("language") ? null : itemJson.getString("language");
                item.whereToWatch = itemJson.isNull("whereToWatch") ? null : itemJson.getString("whereToWatch");
                item.releaseDate = itemJson.isNull("releaseDate") ? null : itemJson.getLong("releaseDate");
                watchlistItems.add(item);
            }
            
            // Insert into database
            AppDatabase.getDatabase(context).movieDao().insertWatchedBulk(watchedEntries);
            AppDatabase.getDatabase(context).movieDao().insertWatchlistBulk(watchlistItems);
            
            return new ImportResult(true, watchedEntries.size(), watchlistItems.size(), null);
        } catch (Exception e) {
            Log.e(TAG, "Error importing from JSON", e);
            return new ImportResult(false, 0, 0, e.getMessage());
        }
    }
    
    // Import from CSV (simplified - only basic fields)
    public static ImportResult importFromCsv(Context context, String filePath) {
        try {
            File importFile = new File(filePath);
            if (!importFile.exists()) {
                return new ImportResult(false, 0, 0, "File not found");
            }
            
            List<WatchedEntry> watchedEntries = new ArrayList<>();
            List<WatchlistItem> watchlistItems = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(importFile), StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // Skip header
                
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    
                    String[] values = parseCsvLine(line);
                    if (values.length < 2) continue;
                    
                    String type = values[0].trim();
                    if ("Watched".equals(type)) {
                        if (values.length >= 4) {
                            WatchedEntry entry = new WatchedEntry(
                                    unescapeCsv(values[1]), // Title
                                    values[3], // Watched Date
                                    values[4], // Location Type
                                    values.length > 9 && !values[9].isEmpty() ? values[9] : "MORNING" // Time of Day
                            );
                            entry.rating = values.length > 2 && !values[2].isEmpty() ? Integer.parseInt(values[2]) : null;
                            entry.locationNotes = values.length > 5 && !values[5].isEmpty() ? unescapeCsv(values[5]) : null;
                            entry.companions = values.length > 6 && !values[6].isEmpty() ? unescapeCsv(values[6]) : null;
                            entry.spendCents = values.length > 7 && !values[7].isEmpty() ? Integer.parseInt(values[7]) : null;
                            entry.durationMin = values.length > 8 && !values[8].isEmpty() ? Integer.parseInt(values[8]) : null;
                            entry.genre = values.length > 10 && !values[10].isEmpty() ? unescapeCsv(values[10]) : null;
                            entry.notes = values.length > 11 && !values[11].isEmpty() ? unescapeCsv(values[11]) : null;
                            entry.posterUri = values.length > 12 && !values[12].isEmpty() ? unescapeCsv(values[12]) : null;
                            entry.language = values.length > 13 && !values[13].isEmpty() ? unescapeCsv(values[13]) : null;
                            entry.theaterName = values.length > 14 && !values[14].isEmpty() ? unescapeCsv(values[14]) : null;
                            entry.city = values.length > 15 && !values[15].isEmpty() ? unescapeCsv(values[15]) : null;
                            entry.streamingPlatform = values.length > 16 && !values[16].isEmpty() ? unescapeCsv(values[16]) : null;
                            watchedEntries.add(entry);
                        }
                    } else if ("Watchlist".equals(type)) {
                        if (values.length >= 2) {
                            WatchlistItem item = new WatchlistItem(unescapeCsv(values[1])); // Title
                            item.notes = values.length > 11 && !values[11].isEmpty() ? unescapeCsv(values[11]) : null;
                            item.language = values.length > 13 && !values[13].isEmpty() ? unescapeCsv(values[13]) : null;
                            item.priority = values.length > 17 && !values[17].isEmpty() ? Integer.parseInt(values[17]) : null;
                            item.createdAt = values.length > 18 && !values[18].isEmpty() ? Long.parseLong(values[18]) : System.currentTimeMillis();
                            item.targetDate = values.length > 19 && !values[19].isEmpty() ? Long.parseLong(values[19]) : null;
                            item.whereToWatch = values.length > 20 && !values[20].isEmpty() ? unescapeCsv(values[20]) : null;
                            item.releaseDate = values.length > 21 && !values[21].isEmpty() ? Long.parseLong(values[21]) : null;
                            watchlistItems.add(item);
                        }
                    }
                }
            }
            
            // Insert into database
            if (!watchedEntries.isEmpty()) {
                AppDatabase.getDatabase(context).movieDao().insertWatchedBulk(watchedEntries);
            }
            if (!watchlistItems.isEmpty()) {
                AppDatabase.getDatabase(context).movieDao().insertWatchlistBulk(watchlistItems);
            }
            
            return new ImportResult(true, watchedEntries.size(), watchlistItems.size(), null);
        } catch (Exception e) {
            Log.e(TAG, "Error importing from CSV", e);
            return new ImportResult(false, 0, 0, e.getMessage());
        }
    }
    
    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private static String unescapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }
    
    private static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }
}

