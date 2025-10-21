package com.entertainment.moviememo;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.entertainment.moviememo.databinding.ActivityMainBinding;
import com.entertainment.moviememo.ui.watched.WatchedListFragment;
import com.entertainment.moviememo.ui.watched.AddWatchedFragment;
import com.entertainment.moviememo.ui.watchlist.WatchlistFragment;
import com.entertainment.moviememo.ui.watchlist.AddWatchlistFragment;
import com.entertainment.moviememo.ui.stats.StatsFragment;
import com.entertainment.moviememo.ui.settings.SettingsFragment;
import com.entertainment.moviememo.ui.about.AboutFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPagerAdapter viewPagerAdapter;
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
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

        setupViewPager();
        setupFab();
        setupMenu();
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);
        
        // Enable user input for swiping
        binding.viewPager.setUserInputEnabled(true);
        
        // Set offscreen page limit to prevent memory issues
        binding.viewPager.setOffscreenPageLimit(1);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("🎬 Watched");
                            break;
                        case 1:
                            tab.setText("🎫 Watchlist");
                            break;
                        case 2:
                            tab.setText("📊 Stats");
                            break;
                    }
                }).attach();
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
        
        // Update FAB icon based on current tab
        binding.viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.fab.setImageResource(android.R.drawable.ic_input_add);
                        break;
                    case 1:
                        binding.fab.setImageResource(android.R.drawable.ic_input_add);
                        break;
                    case 2:
                        binding.fab.setImageResource(android.R.drawable.ic_menu_manage);
                        break;
                }
            }
        });
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

    private void setupMenu() {
        // Set overflow icon tint to match theme
        if (binding.toolbar.getOverflowIcon() != null) {
            // Use theme-aware color that adapts to light/dark mode
            android.content.res.TypedArray a = getTheme().obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorOnPrimary});
            int tintColor = a.getColor(0, 0);
            a.recycle();
            binding.toolbar.getOverflowIcon().setTint(tintColor);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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