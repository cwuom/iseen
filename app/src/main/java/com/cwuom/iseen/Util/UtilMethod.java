package com.cwuom.iseen.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;
import androidx.room.TransactionExecutor;

import com.cwuom.iseen.InitDataBase.InitCardDataBase;
import com.cwuom.iseen.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

    public static InitCardDataBase getInstance(Context context) {
        if (baseRoomDatabase == null) {
            baseRoomDatabase = Room.databaseBuilder(context, InitCardDataBase.class,
                    "history_database.db").allowMainThreadQueries().build();
        }
        return baseRoomDatabase;
    }

    public static String inputStreamToString(InputStream inputStream) {
        StringBuilder buffer = new StringBuilder();
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
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
    public static void ShowSnackbar(String info, Activity activity){  // 注: 无障碍模式会导致Snackbar无动画
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
        snackbar.show();
    }

    @SuppressLint("RestrictedApi")
    public static Snackbar ShowLoadingSnackbar(String info ,View coordinatorLayout){  // 注: 无障碍模式会导致Snackbar无动画
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

}
