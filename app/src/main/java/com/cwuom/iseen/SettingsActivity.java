package com.cwuom.iseen;

import static com.cwuom.iseen.Util.UtilMethod.getPingDelay;
import static com.cwuom.iseen.Util.UtilMethod.parseURLComponents;
import static com.cwuom.iseen.Util.UtilMethod.switchLanguage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cwuom.iseen.Util.UtilMethod;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import rikka.preference.SimpleMenuPreference;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UtilMethod.setTheme(this);
        this.getTheme().applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference use_default_api = findPreference("use_default_api_settings");
            EditTextPreference api_signature = findPreference("signature_server_address");
            EditTextPreference api_auth = findPreference("authentication_server_address");
            Preference check = findPreference("check_api");
            SimpleMenuPreference language = findPreference("language");
            Preference bilibili = findPreference("bilibili");
            Preference join_qq = findPreference("join_qq");
            Preference theme = findPreference("theme_color");
            Preference github = findPreference("github");

            assert use_default_api != null;
            if (Objects.requireNonNull(use_default_api.getSharedPreferences()).getBoolean("use_default_api_settings", true)){
                Objects.requireNonNull(api_signature).setText("https://ark.cwuom.love/");
                Objects.requireNonNull(api_auth).setText("https://api.lyhc.top/");
                Objects.requireNonNull(api_signature).setEnabled(false);
                Objects.requireNonNull(api_auth).setEnabled(false);
            }
            use_default_api.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((boolean) newValue){
                    Objects.requireNonNull(api_signature).setText("https://ark.cwuom.love/");
                    Objects.requireNonNull(api_auth).setText("https://api.lyhc.top/");
                    Objects.requireNonNull(api_signature).setEnabled(false);
                    Objects.requireNonNull(api_auth).setEnabled(false);
                } else {
                    Objects.requireNonNull(api_signature).setEnabled(true);
                    Objects.requireNonNull(api_auth).setEnabled(true);
                }

                return true;
            });
            Objects.requireNonNull(check).setOnPreferenceClickListener(preference -> {
                assert api_signature != null;
                new Thread(() -> {
                    String _api_signature = Objects.requireNonNull(parseURLComponents(api_signature.getText())[0]);
                    float ping = getPingDelay(api_signature.getText());

                    String _api_auth = parseURLComponents(Objects.requireNonNull(api_auth).getText())[0];
                    float ping_auth = getPingDelay(api_auth.getText());
                    try {
                        InetAddress address = InetAddress.getByName(_api_signature);
                        String ip = address.getHostAddress();
                        InetAddress address_auth = InetAddress.getByName(_api_auth);
                        String ip_auth = address_auth.getHostAddress();
                        new Handler(Looper.getMainLooper()).post(() -> UtilMethod.showDialog("诊断结果", _api_signature + " --> " +ip + "\n响应延迟: " + ping + "ms" + "\n\n"
                                + _api_auth + "-->" + ip_auth + "\n响应延迟: " + ping_auth + "ms", getActivity()));

                    } catch (UnknownHostException e) {
                        new Handler(Looper.getMainLooper()).post(() -> UtilMethod.showDialog("诊断结果", e.toString(), getActivity()));
                    }
                }).start();

                return false;
            });

            assert theme != null;
            theme.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals("CLASSIC")) {
                    requireActivity().setTheme(R.style.Base_Theme_Iseen);
                } else if (newValue.equals("SAKURA")) {
                    requireActivity().setTheme(R.style.Theme_Iseen_Sakura);
                } else if (newValue.equals("MATERIAL_BLUE")) {
                    requireActivity().setTheme(R.style.Theme_Iseen_Blue);
                }
                Intent intent = new Intent("ui_change");
                requireActivity().sendBroadcast(intent);

                requireActivity().recreate();
                return true;
            });
            assert github != null;
            github.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cwuom/iseen"));
                startActivity(browserIntent);
                return false;
            });

            Preference join_telegram = findPreference("join_telegram");
            assert join_telegram != null;
            join_telegram.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/cwuoms_group"));
                startActivity(browserIntent);
                return false;
            });

            assert join_qq != null;
            join_qq.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=UD5xNmXt0Otz0OrvpXCaKnSd04BDf0rm&authKey=40ctuZ7TZLHzf1LBJZ29Nqvu%2F55gAnvqJ%2FB7s8oJvWsM7AA07%2BXIF8J2GKctM4hD&noverify=0&group_code=923071208"));
                startActivity(browserIntent);
                return false;
            });

            assert bilibili != null;
            bilibili.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://space.bilibili.com/473400804"));
                startActivity(browserIntent);
                return false;
            });

            assert language != null;
            language.setOnPreferenceChangeListener((preference, newValue) -> {
                switchLanguage((String) newValue, requireActivity());
                Intent intent = new Intent("ui_change");
                requireActivity().sendBroadcast(intent);
                requireActivity().recreate();
                return true;
            });
        }

    }
}