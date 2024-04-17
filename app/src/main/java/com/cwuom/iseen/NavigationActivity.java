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
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        UtilMethod.setTheme(this);
        UtilMethod.switchLanguage(preferences.getString("language", "zh"), this);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(ACTION_ACTIVITY_CREATED);
        sendBroadcast(intent);

        initOrRecoverFragments(savedInstanceState);
        setupBottomNavigation();
        configureStatusBarAndNavigationBar(preferences);
        registerUiChangeReceiver();
    }

    private void configureStatusBarAndNavigationBar(SharedPreferences preferences) {
        if (preferences.getBoolean("hide_status_bar_below", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            configureStatusBarBelow();
        }
        transparentNavigationBar(getWindow());
    }

    private void registerUiChangeReceiver() {
        uiChangeReceiver = new UiChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_UI_CHANGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(uiChangeReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        }
    }

    private void initOrRecoverFragments(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            cardGeneratorFragment = new CardGeneratorFragment();
            profileFragment = new ProfileFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.navFragment, homeFragment, HomeFragment.class.getSimpleName())
                    .hide(cardGeneratorFragment)
                    .add(R.id.navFragment, cardGeneratorFragment, CardGeneratorFragment.class.getSimpleName())
                    .hide(profileFragment)
                    .add(R.id.navFragment, profileFragment, ProfileFragment.class.getSimpleName())
                    .commit();
            currentFragment = homeFragment;
        } else {
            recoverFragments(fragmentManager);
        }
    }

    private void recoverFragments(FragmentManager fragmentManager) {
        homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName());
        cardGeneratorFragment = (CardGeneratorFragment) fragmentManager.findFragmentByTag(CardGeneratorFragment.class.getSimpleName());
        profileFragment = (ProfileFragment) fragmentManager.findFragmentByTag(ProfileFragment.class.getSimpleName());

        if (fragmentManager.getPrimaryNavigationFragment() != null) {
            currentFragment = fragmentManager.getPrimaryNavigationFragment();
        } else {
            currentFragment = homeFragment;
            fragmentManager.beginTransaction().show(currentFragment).commit();
        }
    }


    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                changeFragment(homeFragment);
                return true;
            } else if (id == R.id.menu_card_generator) {
                changeFragment(cardGeneratorFragment);
                return true;
            } else if (id == R.id.menu_profile) {
                changeFragment(profileFragment);
                return true;
            } else {
                return false;
            }
        });
    }


    private void changeFragment(Fragment newFragment) {
        if (newFragment != currentFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.fragment_enter,
                    R.anim.fragment_exit,
                    R.anim.fragment_enter,
                    R.anim.fragment_exit
            );

            fragmentTransaction.hide(currentFragment);
            fragmentTransaction.show(newFragment);
            fragmentTransaction.setPrimaryNavigationFragment(newFragment);
            fragmentTransaction.commit();
            currentFragment = newFragment;
        }
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
        systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        window.setNavigationBarColor(Color.TRANSPARENT);
        systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiChangeReceiver != null) {
            unregisterReceiver(uiChangeReceiver);
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
