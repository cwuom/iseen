package com.cwuom.iseen;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.cwuom.iseen.Util.UtilMethod;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.MessageDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QZoneActivity extends AppCompatActivity {
    String qq = "";
    String password = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        DialogX.init(this);

        WebView mWvLogin = findViewById(R.id.wv_login);
        mWvLogin.getSettings().setJavaScriptEnabled(true);
        mWvLogin.setWebViewClient(new LoginWebViewClient());
        mWvLogin.loadUrl("https://i.qq.com/");
        mWvLogin.getSettings().setAllowUniversalAccessFromFileURLs(true);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        qq = preferences.getString("qq", "");
        password = preferences.getString("password", "");
    }

    public class LoginWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();

            view.loadUrl(url);
            if (url.startsWith("wtloginmqq://ptlogin/qlogin")) {
                UtilMethod.showDialog("暂不支持！", "暂不支持一键登录，请尝试密码登录！", QZoneActivity.this);
                view.loadUrl("https://i.qq.com/");
            }
            if (url.startsWith("jsbridge://ui/webviewCanScroll")) {
                UtilMethod.showDialog("你在点啥呢", "你在点啥呢？？", QZoneActivity.this);
                view.loadUrl("https://i.qq.com/");
            }
            if (url.startsWith("https://ssl.zc.qq.com/phone/index.html?from=pt")) {
                UtilMethod.showDialog("你在点啥呢", "403了吧，这次就不帮你重定向了！", QZoneActivity.this);
            }

            if (url.startsWith("https://h5.qzone.qq.com/")) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                onLoginSuccess(cookies);
            }
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (url.contains("ui.ptlogin2.qq.com")) {
                String js = "(function() {" +
                        "var inputU = document.getElementById('u');" +
                        "var inputP = document.getElementById('p');" +
                        "if (inputU && inputP) {" +
                        "   inputU.value = '" + qq + "';" +
                        "   inputP.value = '" + password + "';" +
                        "}" +
                        "})()";
                view.evaluateJavascript(js, null);
            }
        }

    }


    void onLoginSuccess(String cookies){
        Log.e("cookies", cookies);
        String uin = extractValue(cookies, "uin");
        String p_skey = extractValue(cookies, "p_skey");
        String skey = extractValue(cookies, "skey");
        assert uin != null;
        if (uin.isEmpty()){
            uin = extractValue(cookies, "p_uin");
        }

        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("config", MODE_PRIVATE);
        sharedPreferences.edit().putString("uin", uin).apply();
        sharedPreferences.edit().putString("p_skey", p_skey).apply();
        sharedPreferences.edit().putString("skey", skey).apply();
        MessageDialog.show("授权成功", "已经成功链接到您的QQ空间，点击确认后返回到上级页面。", "确定").setOkButton((baseDialog, v) -> {
            finish();
            return false;
        });
    }

    public static String extractValue(String text, String key) {
        String REGEX = key + "=([^;]*)";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }
}