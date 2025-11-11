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
import com.entertainment.moviememo.data.entities.NotificationSettings;

@Database(
    entities = {WatchedEntry.class, WatchlistItem.class, Genre.class, NotificationSettings.class},
    version = 8,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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
    
    // Migration from version 1 to 2: Add language field to both tables
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add language column to watched_entries table
            database.execSQL("ALTER TABLE watched_entries ADD COLUMN language TEXT");
            
            // Add language column to watchlist_items table  
            database.execSQL("ALTER TABLE watchlist_items ADD COLUMN language TEXT");
            
            // Set default language to English for existing records
            database.execSQL("UPDATE watched_entries SET language = 'en' WHERE language IS NULL");
            database.execSQL("UPDATE watchlist_items SET language = 'en' WHERE language IS NULL");
        }
    };
    
    // Migration from version 2 to 3: Add theater fields to watched_entries table
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add theater_name and city columns to watched_entries table
            database.execSQL("ALTER TABLE watched_entries ADD COLUMN theaterName TEXT");
            database.execSQL("ALTER TABLE watched_entries ADD COLUMN city TEXT");
        }
    };
    
    // Migration from version 3 to 4: Add where to watch and release date fields to watchlist_items table
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add whereToWatch and releaseDate columns to watchlist_items table
            database.execSQL("ALTER TABLE watchlist_items ADD COLUMN whereToWatch TEXT");
            database.execSQL("ALTER TABLE watchlist_items ADD COLUMN releaseDate INTEGER");
            // Existing records will have NULL values, which is fine
        }
    };
    
    // Migration from version 4 to 5: Add streamingPlatform field to watched_entries table
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add streamingPlatform column to watched_entries table
            database.execSQL("ALTER TABLE watched_entries ADD COLUMN streamingPlatform TEXT");
            // Existing records will have NULL values, which is fine
        }
    };
    
    // Migration from version 5 to 6: Add notification_settings table
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create notification_settings table
            database.execSQL("CREATE TABLE IF NOT EXISTS notification_settings (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "selectedDates TEXT, " +
                    "notificationHour INTEGER, " +
                    "notificationMinute INTEGER)");
        }
    };
    
    // Migration from version 6 to 7: Rename selectedDates to selectedDays for day-of-week selection
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Rename selectedDates to selectedDays
            database.execSQL("ALTER TABLE notification_settings RENAME COLUMN selectedDates TO selectedDays");
        }
    };
    
    // Migration from version 7 to 8: Add streamingPlatform field to watchlist_items table
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add streamingPlatform column to watchlist_items table
            database.execSQL("ALTER TABLE watchlist_items ADD COLUMN streamingPlatform TEXT");
            // Existing records will have NULL values, which is fine
        }
    };
}
