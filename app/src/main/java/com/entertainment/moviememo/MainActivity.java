package com.entertainment.moviememo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.entertainment.moviememo.databinding.ActivityMainBinding;
import com.entertainment.moviememo.ui.watched.WatchedListFragment;
import com.entertainment.moviememo.ui.watched.AddWatchedFragment;
import com.entertainment.moviememo.ui.watchlist.WatchlistFragment;
import com.entertainment.moviememo.ui.watchlist.AddWatchlistFragment;
import com.entertainment.moviememo.ui.stats.StatsFragment;
import com.entertainment.moviememo.ui.settings.SettingsFragment;
import com.entertainment.moviememo.ui.about.AboutFragment;
import com.entertainment.moviememo.ui.SlowSwipeViewPager2;
import com.entertainment.moviememo.utils.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    
    private ActivityMainBinding binding;
    private ViewPagerAdapter viewPagerAdapter;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private ViewPager2 viewPager;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);
//        tabLayout = findViewById(R.id.tabLayout);
//        viewPager = findViewById(R.id.v);
        fab = findViewById(R.id.fab);

        setSupportActionBar(binding.toolbar);

        // Request notification permission if needed (Android 13+)
        requestNotificationPermission();
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this);
        
        // Reschedule notifications on app startup based on user-selected dates
        NotificationHelper.rescheduleAllNotifications(this);
        
        // Handle notification click navigation
        handleNotificationIntent();
        
        setupViewPager();
        setupFab();
        setupMenu();
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Reschedule notifications now that permission is granted
                NotificationHelper.rescheduleAllNotifications(this);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent();
    }

    private void handleNotificationIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("navigate_to_watchlist", false)) {
            // Navigate to watchlist (position 1)
            binding.viewPager.setCurrentItem(1, true);
            binding.bottomNavigation.setSelectedItemId(R.id.nav_watchlist);
            updateToolbarTitle(1);
        }
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);
        
        // Apply slow swipe behavior
        SlowSwipeViewPager2.applySlowSwipe(binding.viewPager);
        
        // Enable user input for swiping
        binding.viewPager.setUserInputEnabled(true);
        
        // Set offscreen page limit to prevent memory issues
        binding.viewPager.setOffscreenPageLimit(1);

        // Set initial selected item
        binding.bottomNavigation.setSelectedItemId(R.id.nav_watched);
        updateToolbarTitle(0); // Set initial title to "MovieMemo"

        // Sync BottomNavigationView with ViewPager2
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_watched) {
                binding.viewPager.setCurrentItem(0, true);
                updateToolbarTitle(0);
                return true;
            } else if (itemId == R.id.nav_watchlist) {
                binding.viewPager.setCurrentItem(1, true);
                updateToolbarTitle(1);
                return true;
            } else if (itemId == R.id.nav_stats) {
                binding.viewPager.setCurrentItem(2, true);
                updateToolbarTitle(2);
                return true;
            }
            return false;
        });

        // Sync ViewPager2 with BottomNavigationView
        binding.viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateToolbarTitle(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_watched);
                        binding.fab.setImageResource(android.R.drawable.ic_input_add);
                        binding.fab.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_watchlist);
                        binding.fab.setImageResource(android.R.drawable.ic_input_add);
                        binding.fab.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_stats);
                        binding.fab.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    private void setupFab() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentTab = binding.viewPager.getCurrentItem();
                switch (currentTab) {
                    case 0:
                        // Add watched movie
                        showAddWatchedFragment();
                        break;
                    case 1:
                        // Add to watchlist
                        showAddWatchlistFragment();
                        break;
                    case 2:
                        // Stats - show settings for data management
                        showSettingsFragment();
                        break;
                }
            }
        });
        
        // Update FAB icon based on current page
        // This is handled in setupViewPager's OnPageChangeCallback
    }

    private void showAddWatchedFragment() {
        AddWatchedFragment fragment = new AddWatchedFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAddWatchlistFragment() {
        AddWatchlistFragment fragment = new AddWatchlistFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateToolbarTitle(int position) {
        switch (position) {
            case 0:
                // Watched tab - show "MovieMemo"
                binding.toolbar.setTitle(getString(R.string.app_name));
                break;
            case 1:
                // Watchlist tab
                binding.toolbar.setTitle("Watchlist");
                break;
            case 2:
                // Stats tab
                binding.toolbar.setTitle("Stats");
                break;
            default:
                binding.toolbar.setTitle(getString(R.string.app_name));
                break;
        }
    }

    private void setupMenu() {
        // Set overflow icon tint to match theme
//        if (binding.toolbar.getOverflowIcon() != null) {
//            // Use theme-aware color that adapts to light/dark mode
//            android.content.res.TypedArray a = getTheme().obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorOnPrimary});
//            int tintColor = a.getColor(0, 0);
//            a.recycle();
//            binding.toolbar.getOverflowIcon().setTint(tintColor);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Ensure menu items have proper text color for visibility
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                // Get theme-aware color for icons
                android.content.res.TypedArray a = getTheme().obtainStyledAttributes(
                    new int[]{com.google.android.material.R.attr.colorOnSurface});
                int iconColor = a.getColor(0, 0xFF000000);
                a.recycle();
                item.getIcon().setTint(iconColor);
            }
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            showSettingsFragment();
            return true;
        } else if (id == R.id.action_about) {
            showAboutFragment();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAboutFragment() {
        AboutFragment fragment = new AboutFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        
        public ViewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new WatchedListFragment();
                case 1:
                    return new WatchlistFragment();
                case 2:
                    return new StatsFragment();
                default:
                    return new WatchedListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}