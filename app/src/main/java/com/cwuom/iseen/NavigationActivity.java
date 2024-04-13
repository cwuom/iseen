package com.cwuom.iseen;

import static com.cwuom.iseen.Util.Constants.ACTION_ACTIVITY_CREATED;
import static com.cwuom.iseen.Util.Constants.ACTION_UI_CHANGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.cwuom.iseen.Pager.CardGeneratorPager.CardGeneratorFragment;
import com.cwuom.iseen.Pager.HomePager.HomeFragment;
import com.cwuom.iseen.Pager.ProfilePager.ProfileFragment;
import com.cwuom.iseen.Util.UtilMethod;
import com.cwuom.iseen.databinding.ActivityNavigationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
 * This software is provided for educational purposes only and should not be used for commercial or illegal activities.
 * Please respect the original author's work by retaining their information intact.
 * If you make modifications to this code, you can repackage it accordingly.
 *
 * Original Author: cwuom
 * Date: 2024.3.31
 *
 * Instructions:
 * 1. Make necessary modifications.
 * 2. Rebuild the app.
 * 3. Retain this header.
 *
 * Thank you!
 */

public class NavigationActivity extends AppCompatActivity {
    ActivityNavigationBinding binding;
    Fragment currentFragment;

    HomeFragment homeFragment;
    CardGeneratorFragment cardGeneratorFragment;
    ProfileFragment profileFragment;
    UiChangeReceiver uiChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        UtilMethod.setTheme(this);
        UtilMethod.switchLanguage(preferences.getString("language", "zh"), this);
        super.onCreate(savedInstanceState);
        uiChangeReceiver = new UiChangeReceiver();
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(ACTION_ACTIVITY_CREATED);
        sendBroadcast(intent);

        if (savedInstanceState != null) {
            removeExistingFragments();
        }
        initOrRecoverFragments();

        setupBottomNavigation();

        boolean hide_status_bar_below = preferences.getBoolean("hide_status_bar_below", false);
        if (hide_status_bar_below && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            configureStatusBarBelow();
        }
        transparentNavigationBar(getWindow());
    }

    private void removeExistingFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Remove fragments if they exist
        if (homeFragment != null) {
            transaction.remove(homeFragment);
        }
        if (cardGeneratorFragment != null) {
            transaction.remove(cardGeneratorFragment);
        }
        if (profileFragment != null) {
            transaction.remove(profileFragment);
        }
        transaction.commitNow();
    }

    private void initOrRecoverFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName()) == null &&
                fragmentManager.findFragmentByTag(CardGeneratorFragment.class.getSimpleName()) == null &&
                fragmentManager.findFragmentByTag(ProfileFragment.class.getSimpleName()) == null) {
            initFragments();
        } else {
            recoverFragments();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UI_CHANGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(uiChangeReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uiChangeReceiver);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private void configureStatusBarBelow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);

        View decorView = window.getDecorView();
        decorView.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            int insetTop = windowInsets.getSystemWindowInsetTop();
            View mainContent = findViewById(R.id.navFragment);
            if (mainContent != null) {
                mainContent.setPadding(mainContent.getPaddingLeft(), insetTop, mainContent.getPaddingRight(), mainContent.getPaddingBottom());
            }
            return windowInsets.consumeSystemWindowInsets();
        });

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(flags);
    }

    public void transparentNavigationBar(Window window) {
        window.setNavigationBarContrastEnforced(false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
        systemUiVisibility = systemUiVisibility|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        window.setNavigationBarColor(Color.TRANSPARENT);

        systemUiVisibility = systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    private void recoverFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName());
        cardGeneratorFragment = (CardGeneratorFragment) fragmentManager.findFragmentByTag(CardGeneratorFragment.class.getSimpleName());
        profileFragment = (ProfileFragment) fragmentManager.findFragmentByTag(ProfileFragment.class.getSimpleName());

        if (profileFragment != null) {
            currentFragment = profileFragment;
        }else if (homeFragment != null) {
            currentFragment = homeFragment;
        } else if (cardGeneratorFragment != null) {
            currentFragment = cardGeneratorFragment;
        }
    }


    private void initFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        homeFragment = new HomeFragment();
        cardGeneratorFragment = new CardGeneratorFragment();
        profileFragment = new ProfileFragment();

        fragmentTransaction.add(R.id.navFragment, homeFragment, HomeFragment.class.getSimpleName());
        fragmentTransaction.add(R.id.navFragment, cardGeneratorFragment, CardGeneratorFragment.class.getSimpleName()).hide(cardGeneratorFragment);
        fragmentTransaction.add(R.id.navFragment, profileFragment, ProfileFragment.class.getSimpleName()).hide(profileFragment);

        fragmentTransaction.commit();

        currentFragment = homeFragment;
    }
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                changeFragment(currentFragment, homeFragment);
            } else if (id == R.id.menu_card_generator) {
                changeFragment(currentFragment, cardGeneratorFragment);
            } else if (id == R.id.menu_profile) {
                changeFragment(currentFragment, profileFragment);
            }
            return true;
        });
    }

    private void changeFragment(Fragment from, Fragment to) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(
                R.anim.fragment_enter,
                R.anim.fragment_exit
        );

        if (to != currentFragment) {
            fragmentTransaction.show(to);
            if (from != null && from.isAdded()) {
                fragmentTransaction.hide(from);
            }

            fragmentTransaction.commit();
            currentFragment = to;
        }
    }

    class UiChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            recreate();
        }
    }


    public BottomNavigationView getBottomNavigationView() {
        return binding.bottomNavigation;
    }

}