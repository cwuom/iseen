package com.cwuom.iseen.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.room.Room;

import com.cwuom.iseen.InitDataBase.InitCardDataBase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

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

    public static void showDialog(String title, String content, Context context){
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("好", null)
                .show();
    }
}
