package com.entertainment.moviememo.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Utility class to slow down swipe gestures in ViewPager2 by reducing fling velocity.
 * Since ViewPager2 is final, we use composition instead of inheritance.
 */
public class SlowSwipeViewPager2 {
    
    private static final float FLING_VELOCITY_MULTIPLIER = 0.4f; // Reduce fling velocity by 60%
    
    /**
     * Applies slow swipe behavior to a ViewPager2 by modifying its internal RecyclerView's fling listener.
     * 
     * @param viewPager2 The ViewPager2 instance to apply slow swipe behavior to
     */
    public static void applySlowSwipe(ViewPager2 viewPager2) {
        if (viewPager2 == null) {
            return;
        }
        
        // Access the internal RecyclerView and override its fling behavior
        viewPager2.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = getRecyclerView(viewPager2);
                if (recyclerView != null) {
                    // Store the original fling listener if it exists
                    RecyclerView.OnFlingListener originalFlingListener = recyclerView.getOnFlingListener();
                    
                    // Set a custom fling listener that reduces velocity
                    recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
                        @Override
                        public boolean onFling(int velocityX, int velocityY) {
                            // Reduce the fling velocity to slow down swipes
                            int reducedVelocityX = (int) (velocityX * FLING_VELOCITY_MULTIPLIER);
                            int reducedVelocityY = (int) (velocityY * FLING_VELOCITY_MULTIPLIER);
                            
                            // If there was an original listener, call it with reduced velocity
                            if (originalFlingListener != null) {
                                return originalFlingListener.onFling(reducedVelocityX, reducedVelocityY);
                            }
                            
                            // Use the RecyclerView's default fling behavior with reduced velocity
                            // Note: onFling is a protected method, so we use reflection as fallback
                            try {
                                java.lang.reflect.Method method = RecyclerView.class.getDeclaredMethod(
                                    "fling", int.class, int.class
                                );
                                method.setAccessible(true);
                                return (boolean) method.invoke(recyclerView, reducedVelocityX, reducedVelocityY);
                            } catch (Exception e) {
                                // If reflection fails, the reduced velocity in the listener should still help
                                return false;
                            }
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Get the internal RecyclerView used by ViewPager2.
     */
    private static RecyclerView getRecyclerView(ViewPager2 viewPager2) {
        for (int i = 0; i < viewPager2.getChildCount(); i++) {
            View child = viewPager2.getChildAt(i);
            if (child instanceof RecyclerView) {
                return (RecyclerView) child;
            }
        }
        return null;
    }
}

