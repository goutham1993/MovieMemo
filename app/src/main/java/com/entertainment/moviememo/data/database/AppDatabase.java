package com.entertainment.moviememo.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.entertainment.moviememo.data.dao.MovieDao;
import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.entities.Genre;

@Database(
    entities = {WatchedEntry.class, WatchlistItem.class, Genre.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract MovieDao movieDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "movie_memo_database"
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Seed default genres on first creation
                            seedDefaultGenres(context);
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    private static void seedDefaultGenres(Context context) {
        // This will be handled by the repository
        // We'll create a background thread to insert default genres
        new Thread(() -> {
            AppDatabase db = getDatabase(context);
            String[] defaultGenres = {
                "Action", "Drama", "Comedy", "Thriller", "Romance", 
                "Sci-Fi", "Fantasy", "Animation", "Documentary", 
                "Horror", "Mystery", "Crime", "Family"
            };
            
            for (String genreName : defaultGenres) {
                Genre genre = new Genre(genreName);
                db.movieDao().addGenre(genre);
            }
        }).start();
    }
}
