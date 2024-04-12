package com.cwuom.iseen.Util;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.cwuom.iseen.InitDataBase.InitCardDataBase;
import com.cwuom.iseen.InitDataBase.InitUserDataBase;
import com.cwuom.iseen.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

/*
 * This software is provided for educational purposes only and should not be used for commercial or illegal activities.
 * Please respect the original author's work by retaining their information intact.
 * If you make modifications to this code, you can repackage it accordingly.
 *
 * Original Author: cwuom
 * Date: 2024.3.1
 *
 * Instructions:
 * 1. Make necessary modifications.
 * 2. Rebuild the app.
 * 3. Retain this header.
 *
 * Thank you!
 */

/**
 * 工具类
 * ----------------------
 * @author cwuom
 * 下列源码支持变动后重新打包，在变动不大的情况下，请尽量保留作者的信息！
 * 请勿用于商业用途和非法用途。
 * ----------------------
 * */

public class UtilMethod {

    public static InitCardDataBase baseRoomDatabase;
    public static InitUserDataBase baseUserRoomDatabase;

    public static InitCardDataBase getInstance(Context context) {
        if (baseRoomDatabase == null) {
            baseRoomDatabase = Room.databaseBuilder(context, InitCardDataBase.class,
                    "history_database.db").allowMainThreadQueries().build();
        }
        return baseRoomDatabase;
    }

    public static InitUserDataBase getInstance_user(Context context) {
        if (baseUserRoomDatabase == null) {
            baseUserRoomDatabase = Room.databaseBuilder(context, InitUserDataBase.class, "bilibili_user_database.db").allowMainThreadQueries().build();
        }
        return baseUserRoomDatabase;
    }


    public static AlertDialog showDialog(String title, String content, Context context){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("好", null);
        return materialAlertDialogBuilder.show();
    }

    /**
     展示Snackbar
     @param info 需要展示的信息
     */
    @SuppressLint("RestrictedApi")
    public static void ShowSnackbar(String info, Activity activity, View view){  // 注: 无障碍模式会导致Snackbar无动画
        Snackbar snackbar;
        View rootView = activity.getWindow().getDecorView();
        View coordinatorLayout = rootView.findViewById(android.R.id.content);
        snackbar = Snackbar.make(coordinatorLayout, "", com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarView = (Snackbar.SnackbarLayout) snackbar.getView();
        ViewGroup.LayoutParams layoutParams = snackbarView.getLayoutParams();
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height);
        fl.gravity = Gravity.BOTTOM;
        snackbarView.setLayoutParams(fl);
        @SuppressLint("InflateParams") View inflate = LayoutInflater.from(snackbar.getView().getContext()).inflate(R.layout.layout_snackbar_view, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView text = inflate.findViewById(R.id.tv_snackbar);
        text.setText(info);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView text2 = inflate.findViewById(R.id.tv_act);
        text2.setText("好");
        text2.setOnClickListener(v -> snackbar.dismiss());
        snackbarView.addView(inflate);
        if (view != null){
            snackbar.setAnchorView(view);
        }
        snackbar.show();
    }

    @SuppressLint("RestrictedApi")
    public static Snackbar ShowLoadingSnackbar(String info ,View coordinatorLayout, View view){  // 注: 无障碍模式会导致Snackbar无动画
        Snackbar snackbar;
        snackbar = Snackbar.make(coordinatorLayout, "", BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarView = (Snackbar.SnackbarLayout) snackbar.getView();
        ViewGroup.LayoutParams layoutParams = snackbarView.getLayoutParams();
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height);
        fl.gravity = Gravity.BOTTOM;
        snackbarView.setLayoutParams(fl);
        @SuppressLint("InflateParams") View inflate = LayoutInflater.from(snackbar.getView().getContext()).inflate(R.layout.layout_snackbar_loading_view, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView text = inflate.findViewById(R.id.tv_snackbar);
        text.setText(info);
        snackbarView.addView(inflate);
        snackbar.setDuration(20000);
        if (view != null){
            snackbar.setAnchorView(view);
        }
        snackbar.show();
        return snackbar;
    }

    public static String replaceLastNewline(String text, String replacement) {
        int lastNewlineIndex = text.lastIndexOf("\n");
        if (lastNewlineIndex >= 0) {
            String part1 = text.substring(0, lastNewlineIndex);
            String part2 = text.substring(lastNewlineIndex + 1);
            return part1 + replacement + part2;
        }
        return text;
    }

    public static String timeToFormat(long time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    /**
     * 复制内容到剪贴板
     * @param text 需要复制的内容
     */
    public static void copyToClipboard(String text, Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", text);
        cm.setPrimaryClip(mClipData);
    }

    public static float getPingDelay(String api) {
        try {
            URL url = new URL(api);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            long startTime = System.currentTimeMillis();
            connection.connect();
            connection.getResponseCode();
            long endTime = System.currentTimeMillis();

            long delay = endTime - startTime;
            Log.i(TAG, "Server latency: " + delay + " ms");
            return delay;

        } catch (IOException e) {
            return -1;
        }
    }

    public static void setTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = preferences.getString("theme_color", "CLASSIC");

        switch (theme) {
            case "CLASSIC":
                context.setTheme(R.style.Base_Theme_Iseen);
                break;
            case "SAKURA":
                context.setTheme(R.style.Theme_Iseen_Sakura);
                break;
            case "MATERIAL_BLUE":
                context.setTheme(R.style.Theme_Iseen_Blue);
                break;
        }
    }

    public static String getSignatureAPI(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("signature_server_address", "https://ark.cwuom.love/");
    }
    public static String getAuthAPI(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("authentication_server_address", "https://api.cwuom.love/");
    }


    public static void switchLanguage(String language, Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        configuration.setLocale(locale);
        context.getResources().updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static String[] parseURLComponents(String url) {
        String host = "";
        String type = "";
        try {
            URL Url = new URL(url);
            host = Url.getHost();
            type = Url.toURI().getScheme();
        } catch (Exception ignored) {}
        return new String[] {host, type};
    }
    public static void recreate_fragment(Context context){
        Intent intent = new Intent("ui_change");
        context.sendBroadcast(intent);
    }


}
