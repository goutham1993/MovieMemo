package com.entertainment.moviememo.utils;

import android.content.Context;
import android.util.Log;

import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.entities.Genre;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportImportUtils {
    
    private static final String TAG = "ExportImportUtils";
    private static final String EXPORT_DIR = "MovieMemo_Exports";
    private static final String EXPORT_FILE_PREFIX = "MovieMemo_";
    
    public static String exportToJson(Context context, List<WatchedEntry> watchedEntries, 
                                   List<WatchlistItem> watchlistItems, List<Genre> genres) {
        try {
            JSONObject exportData = new JSONObject();
            
            // Add metadata
            exportData.put("exportDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            exportData.put("version", "1.0");
            exportData.put("appName", "MovieMemo");
            
            // Export watched entries
            JSONArray watchedArray = new JSONArray();
            for (WatchedEntry entry : watchedEntries) {
                JSONObject entryObj = new JSONObject();
                entryObj.put("id", entry.id);
                entryObj.put("title", entry.title);
                entryObj.put("rating", entry.rating != null ? entry.rating : JSONObject.NULL);
                entryObj.put("watchedDate", entry.watchedDate);
                entryObj.put("locationType", entry.locationType);
                entryObj.put("locationNotes", entry.locationNotes != null ? entry.locationNotes : JSONObject.NULL);
                entryObj.put("companions", entry.companions != null ? entry.companions : JSONObject.NULL);
                entryObj.put("spendCents", entry.spendCents != null ? entry.spendCents : JSONObject.NULL);
                entryObj.put("durationMin", entry.durationMin != null ? entry.durationMin : JSONObject.NULL);
                entryObj.put("timeOfDay", entry.timeOfDay);
                entryObj.put("genre", entry.genre != null ? entry.genre : JSONObject.NULL);
                entryObj.put("notes", entry.notes != null ? entry.notes : JSONObject.NULL);
                entryObj.put("posterUri", entry.posterUri != null ? entry.posterUri : JSONObject.NULL);
                watchedArray.put(entryObj);
            }
            exportData.put("watchedEntries", watchedArray);
            
            // Export watchlist items
            JSONArray watchlistArray = new JSONArray();
            for (WatchlistItem item : watchlistItems) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("id", item.id);
                itemObj.put("title", item.title);
                itemObj.put("notes", item.notes != null ? item.notes : JSONObject.NULL);
                itemObj.put("priority", item.priority != null ? item.priority : JSONObject.NULL);
                itemObj.put("createdAt", item.createdAt != null ? item.createdAt : JSONObject.NULL);
                itemObj.put("targetDate", item.targetDate != null ? item.targetDate : JSONObject.NULL);
                watchlistArray.put(itemObj);
            }
            exportData.put("watchlistItems", watchlistArray);
            
            // Export genres
            JSONArray genresArray = new JSONArray();
            for (Genre genre : genres) {
                JSONObject genreObj = new JSONObject();
                genreObj.put("name", genre.name);
                genreObj.put("createdAt", genre.createdAt != null ? genre.createdAt : JSONObject.NULL);
                genresArray.put(genreObj);
            }
            exportData.put("genres", genresArray);
            
            // Save to file
            String fileName = EXPORT_FILE_PREFIX + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json";
            File exportDir = new File(context.getExternalFilesDir(null), EXPORT_DIR);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File exportFile = new File(exportDir, fileName);
            FileOutputStream fos = new FileOutputStream(exportFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(exportData.toString(2)); // Pretty print
            writer.close();
            fos.close();
            
            Log.d(TAG, "Export successful: " + exportFile.getAbsolutePath());
            return exportFile.getAbsolutePath();
            
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Export failed", e);
            return null;
        }
    }
    
    public static ImportResult importFromJson(Context context, String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            fis.close();
            
            JSONObject importData = new JSONObject(jsonString.toString());
            
            ImportResult result = new ImportResult();
            
            // Import watched entries
            if (importData.has("watchedEntries")) {
                JSONArray watchedArray = importData.getJSONArray("watchedEntries");
                for (int i = 0; i < watchedArray.length(); i++) {
                    JSONObject entryObj = watchedArray.getJSONObject(i);
                    WatchedEntry entry = new WatchedEntry(
                        entryObj.getString("title"),
                        entryObj.getString("watchedDate"),
                        entryObj.getString("locationType"),
                        entryObj.getString("timeOfDay")
                    );
                    
                    if (!entryObj.isNull("rating")) {
                        entry.rating = entryObj.getInt("rating");
                    }
                    if (!entryObj.isNull("locationNotes")) {
                        entry.locationNotes = entryObj.getString("locationNotes");
                    }
                    if (!entryObj.isNull("companions")) {
                        entry.companions = entryObj.getString("companions");
                    }
                    if (!entryObj.isNull("spendCents")) {
                        entry.spendCents = entryObj.getInt("spendCents");
                    }
                    if (!entryObj.isNull("durationMin")) {
                        entry.durationMin = entryObj.getInt("durationMin");
                    }
                    if (!entryObj.isNull("genre")) {
                        entry.genre = entryObj.getString("genre");
                    }
                    if (!entryObj.isNull("notes")) {
                        entry.notes = entryObj.getString("notes");
                    }
                    if (!entryObj.isNull("posterUri")) {
                        entry.posterUri = entryObj.getString("posterUri");
                    }
                    
                    result.watchedEntries.add(entry);
                }
            }
            
            // Import watchlist items
            if (importData.has("watchlistItems")) {
                JSONArray watchlistArray = importData.getJSONArray("watchlistItems");
                for (int i = 0; i < watchlistArray.length(); i++) {
                    JSONObject itemObj = watchlistArray.getJSONObject(i);
                    WatchlistItem item = new WatchlistItem(itemObj.getString("title"));
                    
                    if (!itemObj.isNull("notes")) {
                        item.notes = itemObj.getString("notes");
                    }
                    if (!itemObj.isNull("priority")) {
                        item.priority = itemObj.getInt("priority");
                    }
                    if (!itemObj.isNull("createdAt")) {
                        item.createdAt = itemObj.getLong("createdAt");
                    }
                    if (!itemObj.isNull("targetDate")) {
                        item.targetDate = itemObj.getLong("targetDate");
                    }
                    
                    result.watchlistItems.add(item);
                }
            }
            
            // Import genres
            if (importData.has("genres")) {
                JSONArray genresArray = importData.getJSONArray("genres");
                for (int i = 0; i < genresArray.length(); i++) {
                    JSONObject genreObj = genresArray.getJSONObject(i);
                    Genre genre = new Genre(genreObj.getString("name"));
                    
                    if (!genreObj.isNull("createdAt")) {
                        genre.createdAt = genreObj.getLong("createdAt");
                    }
                    
                    result.genres.add(genre);
                }
            }
            
            Log.d(TAG, "Import successful: " + result.watchedEntries.size() + " watched, " + 
                  result.watchlistItems.size() + " watchlist, " + result.genres.size() + " genres");
            return result;
            
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Import failed", e);
            return null;
        }
    }
    
    public static class ImportResult {
        public java.util.List<WatchedEntry> watchedEntries = new java.util.ArrayList<>();
        public java.util.List<WatchlistItem> watchlistItems = new java.util.ArrayList<>();
        public java.util.List<Genre> genres = new java.util.ArrayList<>();
    }
}
