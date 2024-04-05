package com.cwuom.iseen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;

import com.cwuom.iseen.Dao.UserDao;
import com.cwuom.iseen.Entity.EntityUser;
import com.cwuom.iseen.InitDataBase.InitUserDataBase;
import com.cwuom.iseen.Util.API.Ark.ArkAPIReq;
import com.cwuom.iseen.Util.UtilMethod;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.style.IOSStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

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

public class LoginActivity extends AppCompatActivity {
    InitUserDataBase initUserDataBase;
    UserDao userDao;
    WaitDialog loadingDialog = WaitDialog.build();

    private final Handler handler = new CustomHandler(this);
    final int HANDLER_MESSAGE_SHOW_LOADING_DIALOG = 1;
    final int HANDLER_MESSAGE_DISMISS_LOADING_DIALOG = 2;
    final int HANDLER_MESSAGE_WENT_WRONG = 3;
    String error_message = "";
    boolean flag = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        DialogX.init(this);
        initMethod();

        WebView mWvLogin = findViewById(R.id.wv_login);
        mWvLogin.getSettings().setJavaScriptEnabled(true);
        mWvLogin.setWebViewClient(new LoginWebViewClient());
        mWvLogin.loadUrl("https://passport.bilibili.com/login");
    }

    private void initMethod() {
        initUserDataBase = UtilMethod.getInstance_user(getApplicationContext());
        userDao = initUserDataBase.userDao();
    }

    public class LoginWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            view.loadUrl(url);
            if (url.contains("m.bilibili.com")) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                onLoginSuccess(cookies);
            }
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

    }

    void onLoginSuccess(String cookies){
        // delete login user
        userDao.deleteLoginUser();

        new Thread(new Runnable(){
            @Override
            public void run() {
                // Do show
                handler.sendEmptyMessage(HANDLER_MESSAGE_SHOW_LOADING_DIALOG);

                // Get user info, and commit them to the database
                try {
                    StringBuilder sb = getStringBuilder();

                    String userinfo = String.valueOf(sb);
                    Log.e("cookies", cookies);
                    JSONObject jsonObject_userinfo = new JSONObject(userinfo).optJSONObject("card");
                    String userUID = Objects.requireNonNull(jsonObject_userinfo).optString("mid");
                    String userName = jsonObject_userinfo.optString("name");
                    String userImageUrl = jsonObject_userinfo.optString("face");
                    String userRegTime = UtilMethod.timeToFormat(jsonObject_userinfo.optLong("regtime") * 1000L);
                    String userSign = jsonObject_userinfo.optString("sign");
                    String userCoins = jsonObject_userinfo.optString("coins");
                    String userBirthday = jsonObject_userinfo.optString("birthday");
                    int userFollows = jsonObject_userinfo.optInt("friend");
                    String ark_coins = ArkAPIReq.getArkCoinsByMid(userUID);

                    userDao.insertUser(new EntityUser(userUID, userName, userImageUrl, userRegTime, userSign, userCoins, userBirthday, ark_coins, userFollows, cookies, true));
                } catch (JSONException | IOException e){
                    error_message = e.toString();  // get the error message
                    handler.sendEmptyMessage(HANDLER_MESSAGE_DISMISS_LOADING_DIALOG);
                    handler.sendEmptyMessage(HANDLER_MESSAGE_WENT_WRONG);  // show error dialog
                } catch (Exception ignored){
                }

                // Do dismiss
                handler.sendEmptyMessage(HANDLER_MESSAGE_DISMISS_LOADING_DIALOG);
            }

            @NonNull
            private StringBuilder getStringBuilder() throws IOException {
                String urlPath = "https://account.bilibili.com/api/member/getCardByMid?";
                URL url = new URL(urlPath);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.setDoInput(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb;
            }
        }).start();

        if (!flag){
            MessageDialog.show("登录成功", "已经成功链接到您的第三方账户，点击确认后返回到上级页面。", "确定").setOkButton((baseDialog, v) -> {
                finish();
                return false;
            });
            flag = true;
        }

    }


    @SuppressLint("HandlerLeak")
    private class CustomHandler extends Handler {
        private final WeakReference<LoginActivity> weakReference;
        public CustomHandler(LoginActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            weakReference.get();
            super.handleMessage(msg);
            if (msg.what == HANDLER_MESSAGE_DISMISS_LOADING_DIALOG) {
                loadingDialog = WaitDialog.build().setStyle(IOSStyle.style());
                loadingDialog.setMessageContent("获取用户信息...");
                loadingDialog.doDismiss();
                loadingDialog.show();
            }
            if (msg.what == HANDLER_MESSAGE_DISMISS_LOADING_DIALOG){
                loadingDialog.doDismiss();
            }
            if (msg.what == HANDLER_MESSAGE_WENT_WRONG){
                MessageDialog.build()
                        .setTheme(DialogX.THEME.DARK)
                        .setTitle("获取用户信息时出错！")
                        .setMessage(error_message)
                        .setOkButton("了解")
                        .show();
            }
        }
    }

}