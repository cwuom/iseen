package com.cwuom.iseen.Util.API.Ark;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cwuom.iseen.Dao.UserDao;
import com.cwuom.iseen.Entity.EntityUser;
import com.cwuom.iseen.InitDataBase.InitUserDataBase;
import com.cwuom.iseen.Util.UtilMethod;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

public class ArkAPIReq {
    public static String API_getArkCoinsByMid = "https://api.cwuom.love/coins_query.php?mid=";
    public static final String API_signature = "https://ark.cwuom.love";

    public static String getArkCoinsByMid(String UID){
        try {
            URL apiUrl = new URL(API_getArkCoinsByMid+UID);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();
            connection.disconnect();
            return result.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void getArkListenerReturn(String url, Context context, boolean permission, ArkApiCallback callback) {
        InitUserDataBase initUserDataBase = UtilMethod.getInstance_user(context.getApplicationContext());
        UserDao userDao = initUserDataBase.userDao();

        String userCookies = "no cookies";
        if (!userDao.getAllUser().isEmpty()) {
            EntityUser entityUser = userDao.getUserByUserIsLogin(true);
            userCookies = entityUser.getUserCookies();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("cookies", userCookies)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        callback.onSuccess(res);
                    } else {
                        callback.onFailure(new IOException("Unexpected code " + response));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                } finally {
                    if (response.body() != null) {
                        response.body().close(); // Ensure closing the response body
                    }
                }
            }
        });
    }

    public static void sendSignaturePostRequest(String endpoint, RequestBody body, ArkApiCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request;
        if (body != null){
             request = new Request.Builder()
                    .url(API_signature + endpoint)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(API_signature + endpoint)
                    .get()
                    .build();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        callback.onSuccess(res);
                    } else {
                        callback.onFailure(new IOException("Unexpected code " + response));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }

            }
        });
    }
}
