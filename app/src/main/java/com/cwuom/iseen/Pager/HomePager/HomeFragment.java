package com.cwuom.iseen.Pager.HomePager;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.cwuom.iseen.Util.UtilMethod.ShowLoadingSnackbar;
import static com.cwuom.iseen.Util.UtilMethod.ShowSnackbar;
import static com.cwuom.iseen.Util.UtilMethod.copyToClipboard;
import static com.cwuom.iseen.Util.UtilMethod.getAuthAPI;
import static com.cwuom.iseen.Util.UtilMethod.replaceLastNewline;
import static com.kongzue.dialogx.impl.ActivityLifecycleImpl.getApplicationContext;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cwuom.iseen.Adapter.CardHistoryAdapter;
import com.cwuom.iseen.Dao.CardDao;
import com.cwuom.iseen.Entity.EntityCallbackContent;
import com.cwuom.iseen.Entity.EntityCard;
import com.cwuom.iseen.Entity.EntityCardHistory;
import com.cwuom.iseen.InitDataBase.InitCardDataBase;
import com.cwuom.iseen.NavigationActivity;
import com.cwuom.iseen.R;
import com.cwuom.iseen.Util.API.Ark.ArkAPIReq;
import com.cwuom.iseen.Util.API.Ark.ArkApiCallback;
import com.cwuom.iseen.Util.NeverCrash;
import com.cwuom.iseen.Util.NotificationUtil;
import com.cwuom.iseen.Util.UtilMethod;
import com.cwuom.iseen.databinding.FragmentHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.kongzue.dialogx.BuildConfig;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.InputDialog;
import com.kongzue.dialogx.interfaces.OnBindView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    private CardDao cardDao;  // 与数据库进行交互
    private final int HANDLER_MESSAGE_SHOW_CARD_DATA = 1;  // 当签名接口请求完成后，弹出卡片代码
    private final int HANDLER_MESSAGE_TIMEOUT = -1;  // 监听超时时，后台弹出通知
    private final int HANDLER_MESSAGE_CONNECTION_ERR = -2;  // 监听连接错误时，后台弹出通知
    private final int HANDLER_MESSAGE_SERVER_REPLY = 2;  // 2FA代码发送到服务器且服务器回应时，弹出反馈窗口
    private final int HANDLER_MESSAGE_THREAD_STOP = 3;  // 监听线程被终止时，弹出提示窗口
    private final int HANDLER_MESSAGE_QUERY_IP_SUCCESS = 4;  // 成功获取到IP详情时
    private final int HANDLER_MESSAGE_QUERY_IP_FAILED = 5;  // IP详情获取失败时
    private String card_data = "";  // 卡片代码

    private String title;  // 卡片标题
    private String subtitle;  // 卡片副标题
    private String yx;  // 卡片外显
    private String id;  // 卡片标识
    private String custom_image_url;  // 自定义图片地址
    private String php_filename;  // PHP文件名/地址
    private String note;  // 卡片备注
    private Boolean switch_followID = Boolean.TRUE;  // 子标题是否跟随标识
    private Boolean switch_hidePhp = Boolean.TRUE;  // 是否隐藏php地址（可以把它隐藏成沙雕图）
    private Boolean switch_auto_update_uuid = Boolean.TRUE;  // 子标题是否跟随标识
    private Boolean switch_show_background = Boolean.TRUE;  // 是否显示背景图片
    private Boolean switch_MaskConnectionErr = Boolean.FALSE;  // 是否屏蔽连接错误的通知
    private Boolean switch_MaskTimeout = Boolean.FALSE;  // 是否屏蔽请求超时的通知

    private SharedPreferences sharedPreferences;  // SharedPreferences，存放配置
    private SharedPreferences.Editor editor;  // 编辑存放的配置
    private String hidePath;  // 隐匿路径
    private String res_2fa_callback;  // 输入2FA代码后，服务器响应的信息
    private String getIPQueryRes_callback;
    private String queryIP;
    private NotificationManager notificationManager;  // 通知管理器
    private String channelId;  // 渠道ID
    int nID = 0;  // 通知ID
    private Boolean isDismiss = true;  // 防止重复弹出dialog窗口
    private final ArrayList<Thread> listener_threads = new ArrayList<>();  // 监听器线程列表
    private final ArrayList<String> listener_url = new ArrayList<>();  // 监听地址列表
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;  // 图片选择器 (旧android也许不兼容，Android13支持)
    private String image_uri;  // 背景图uri地址
    private final String API_GET_RES_endpoint = "/get.php?filename=%s";   // API地址设置(获取结果)
    private Snackbar snackbar = null;  // 加载snackbar
    private int req_interval = 30000;
    private boolean skipHiddenModeTips = false;
    BottomNavigationView bottomNavigationView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        initMethod();
        initListeners();

        return binding.getRoot();
    }

    private void initListeners() {
        binding.btnRandom.setOnClickListener(v -> setRandomUuid());
        binding.btnGenerate.setOnClickListener(v -> generateCard());
        binding.btnSave.setOnClickListener(v -> saveConfig());
        binding.btnRestore.setOnClickListener(v -> restoreConfig());
        binding.btnHistory.setOnClickListener(v -> showHistoryManager());
        binding.btnDefaultConfiguration.setOnClickListener(v -> fillDefaultConfiguration());
        binding.id.addTextChangedListener(onIDChange());
        binding.warningCard.setOnClickListener(v -> showWarningDialog());
        binding.warningCard.setOnLongClickListener(v -> true);  // 没什么用的监听
        binding.switchFollowId.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean("switch_followID", isChecked).apply());
        binding.switchHidePhp.setOnCheckedChangeListener((buttonView, isChecked) -> onSwitchHidePhpUrl(isChecked));
        binding.switchAutoUpdateUuid.setOnCheckedChangeListener((buttonView, isChecked) -> onSwitchAutoUpdateUUID(isChecked));
        binding.switchBackground.setOnCheckedChangeListener((buttonView, isChecked) -> onSwitchBackground(isChecked));
        binding.btnBackgroundSetting.setOnClickListener(this::backgroundSetting);
    }



    @SuppressLint("SetTextI18n")
    private void initMethod(){
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        sharedPreferences = requireActivity().getSharedPreferences("config", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        configRestore();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        boolean thin_mode = preferences.getBoolean("thin_mode", false);
        boolean hidden_warning = preferences.getBoolean("hidden_warning", false);
        if (thin_mode){
            binding.hidePath.setText("special/kp/");
            switch_hidePhp = Boolean.TRUE;
            skipHiddenModeTips = true;
            binding.switchHidePhp.setChecked(true);
            binding.switchAutoUpdateUuid.setChecked(true);
            editor.putBoolean("switch_hidePhp", true).apply();
            save_config();
            binding.settingCard.setVisibility(View.GONE);
        }
        if (hidden_warning){
            binding.warningCard.setVisibility(View.GONE);
        }

        if(getActivity() instanceof NavigationActivity) {
            bottomNavigationView = ((NavigationActivity) getActivity()).getBottomNavigationView();
        }

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        editor.putString("image_uri", String.valueOf(uriToUri(uri, requireActivity()))).apply();
                        binding.background.setImageURI(uri);
                        ShowSnackbar("背景图已更新！", requireActivity(), bottomNavigationView);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        // 全局崩溃拦截(懒得用try盖住了)
        NeverCrash.getInstance()
                .setDebugMode(BuildConfig.DEBUG)
                .setMainCrashHandler((t, e) -> UtilMethod.showDialog("发生了意想不到的问题..", "已尝试在主线程拦截崩溃，您需要将此截图反馈给开发者。\n报错详情: "+e.toString(), requireActivity()))
                .setUncaughtCrashHandler((t, e) -> UtilMethod.showDialog("发生了意想不到的问题..", "已尝试在子线程拦截崩溃，您需要将此截图反馈给开发者。\n报错详情: "+e.toString(), requireActivity()))
                .register(requireActivity().getApplication());

        // 判断通知权限状态，若无权限则弹出窗口帮助用户授权
        if (!NotificationUtil.isNotifyEnabled(requireActivity()) && sharedPreferences.getBoolean("notify_permission_remind", true)){
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("程序功能受限")
                    .setMessage("未开启通知权限，将无法使用卡片监听功能！但能保证基础功能正常使用。")
                    .setNeutralButton("忽略", null)
                    .setNegativeButton("不再提示", (dialog, which) -> editor.putBoolean("notify_permission_remind", false).apply())
                    .setPositiveButton("去开启", (dialog, which) -> {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", requireActivity().getPackageName(), null));
                        startActivity(localIntent);
                    })
                    .show();

        }

        // 弹出免责声明
        if (!sharedPreferences.getBoolean("is_read", false)){
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("免责声明")
                    .setMessage("在使用 iseen 软件（以下简称“软件”）之前，请仔细阅读并理解以下条款：\n" +
                            "\n" +
                            "API使用: 软件调用了第三方API，这些API的使用仅限于软件内部，与API的作者无关。我们对这些API的任何问题或故障不承担任何责任。\n" +
                            "用户责任: 使用软件的用户需对自己的行为负责，包括但不限于数据输入、输出和操作。软件作者不对因使用软件导致的任何直接或间接损失承担责任。\n" +
                            "知识产权: 所有通过此软件获取的信息和内容都受到版权、商标和其他相关法律的保护。未经授权的复制、分发或使用可能违反法律。\n" +
                            "免责声明: 我们尽力确保软件的正常运作，但无法保证其永久可用或完全无误。因此，您应对任何由于软件中断、延迟、错误、故障或其他问题导致的损失承担责任。\n" +
                            "隐私政策: 我们尊重用户的隐私权，不会收集、使用或披露您的个人信息，除非法律要求或经您明确同意。\n" +
                            "\n" +
                            "请注意，本免责声明可能随时更新，请定期查看最新版本。通过使用 iseen 软件，您同意遵守上述条款和条件。")
                    .setNegativeButton("关闭", null)
                    .setNeutralButton("不再提示", (dialog, which) -> editor.putBoolean("is_read", true).apply())
                    .show();

        }

        // 创建通知渠道
        Context context = getApplicationContext();
        channelId = "卡片监听";
        notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId, "卡片监听", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        InitCardDataBase initCardDataBase = UtilMethod.getInstance(requireActivity());
        cardDao = initCardDataBase.cardDao();
    }


    // ----------------------------------

    @SuppressLint("SetTextI18n")
    private void fillDefaultConfiguration(){
        binding.hidePath.setText("special/kp/");
        switch_hidePhp = Boolean.TRUE;
        skipHiddenModeTips = true;
        binding.switchHidePhp.setChecked(true);
        editor.putBoolean("switch_hidePhp", true).apply();
        save_config();

        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        binding.id.setText(uuid);
        ShowSnackbar("默认服务器配置已填充~", requireActivity(), bottomNavigationView);
        skipHiddenModeTips = false;
    }

    private void onSwitchHidePhpUrl(boolean isChecked){
        editor.putBoolean("switch_hidePhp", false);
        editor.apply();
        if (isChecked && !skipHiddenModeTips){
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("并不是所有服务器都支持这个特性！")
                    .setMessage("开启后，程序生成的卡片代码将不包含自己的php地址，这可以一定程度的防止他人抓包。需要他工作的前提是服务端需要在nginx规则里设置如下代码，如\nlocation ~* ^/special/kp/(.*)\\.png$ {\n" +
                            "    rewrite ^/special/kp/(.*)\\.png$ /kp.php?id=$1 last;\n" +
                            "}\n否则卡片代码将无法正常工作以满足最初的需求。若您已经做好配置，开启后可在按钮下方键入伪装路径(如special/kp/)")

                    .setNeutralButton("关闭此选项", (dialog, which) -> binding.switchHidePhp.setChecked(false))
                    .setPositiveButton("仍要开启", (dialog, which) -> {
                        editor.putBoolean("switch_hidePhp", true).apply();
                        binding.textFieldHidePath.setVisibility(View.VISIBLE);
                    })
                    .show();
        } else {
            binding.textFieldHidePath.setVisibility(View.GONE);
        }

        if (skipHiddenModeTips){
            binding.textFieldHidePath.setVisibility(View.VISIBLE);
        }
    }

    private void onSwitchBackground(boolean isChecked){
        editor.putBoolean("showBackground", isChecked).apply();
        if (isChecked){
            binding.background.setVisibility(View.VISIBLE);
            binding.btnBackgroundSetting.setVisibility(View.VISIBLE);
        } else {
            binding.background.setVisibility(View.GONE);
            binding.btnBackgroundSetting.setVisibility(View.GONE);
        }
    }

    private void onSwitchAutoUpdateUUID(boolean isChecked){
        editor.putBoolean("switch_auto_update_uuid", isChecked).apply();
        switch_auto_update_uuid = isChecked;
    }

    private TextWatcher onIDChange(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.switchFollowId.isChecked()) {
                    binding.subtitle.setText(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void restoreConfig() {
        configRestore();
        ShowSnackbar("配置已恢复！", requireActivity(), bottomNavigationView);
    }

    private void saveConfig() {
        save_config();
        ShowSnackbar("配置已保存！", requireActivity(), bottomNavigationView);
    }

    private void setRandomUuid(){
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        binding.id.setText(uuid);
    }

    private void generateCard() {
        if (switch_auto_update_uuid){
            String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
            binding.id.setText(uuid);
        }

        updateInputInfo();
        String url = UtilMethod.getAuthAPI(getActivity())+php_filename+"?id="+id+".png";
        if (binding.switchHidePhp.isChecked()){
            url = UtilMethod.getAuthAPI(getActivity())+hidePath+id+".png";
        }
        // API地址设置
        String API_endpoint = "/get_card?title=%s&subtitle=%s&prompt=%s&cover=%s";
        //    private String data_dir;  // 监听数据存放目录
        // 图片签名地址
        String endpoint = String.format(API_endpoint, title, subtitle, yx, url);
        String img = Objects.requireNonNull(binding.customImage.getText()).toString();
        if (!img.isEmpty()){
            endpoint += "/" + Base64.encodeToString(img.getBytes(), Base64.NO_WRAP);
        }

        String cardListenerUrl = getAuthAPI(getActivity()) + String.format(API_GET_RES_endpoint, id+".png.txt");
        if (binding.switchHidePhp.isChecked()){  // 开启了隐匿模式，查询地址会发生变化
            cardListenerUrl = getAuthAPI(getActivity()) + String.format(API_GET_RES_endpoint, id+".txt");
        }
        if (!JudgmentListenerRepetition(cardListenerUrl)){ // 判断是否重复
            binding.btnGenerate.setLoading(true);
            ArkAPIReq.sendSignaturePostRequest(endpoint, null, getApplicationContext(), new ArkApiCallback() {
                @Override
                public void onSuccess(String result) {
                    card_data = result;
                    handler.sendEmptyMessage(HANDLER_MESSAGE_SHOW_CARD_DATA);
                }

                @Override
                public void onFailure(Exception e) {
                    requireActivity().runOnUiThread(() -> new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("出了点问题！")
                            .setMessage("请求失败: " + e.getMessage())
                            .setPositiveButton("好", null)
                            .show());
                    requireActivity().runOnUiThread(() -> binding.btnGenerate.setLoading(false));
                }
            });
        } else {
            ShowSnackbar("相同标识的卡片已经存在，请更换标识。", requireActivity(), bottomNavigationView);
        }
    }

    private void backgroundSetting(View v){
        PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.bg_setting_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.item_use_default_background){
                editor.remove("image_uri").apply();
                binding.background.setImageResource(R.drawable.neri);
            }
            if (id == R.id.item_choose_background){
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
            return false;
        });
        popupMenu.show();
    }

    private void showWarningDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle("戳我没用啦~")
                .setMessage("这只是让你阅读的，当然非常感谢你能成功注意到这个卡片。")
                .setPositiveButton("好！", null)
                .show();
    }

    // ----------------------------------

    /**
     * 将临时uri转换为永久uri
     * @param uri 临时uri地址
     * @param context 上下文
     * @return 永久uri地址
     */
    public static URI uriToUri(Uri uri, Context context) {
        File file = null;
        if (uri == null) return null;

        // android10以上转换
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_FILE)) {
            file = new File(Objects.requireNonNull(uri.getPath()));
        } else if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            // 把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

            try {
                InputStream is;
                is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                assert is != null;
                FileUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException ignored) {
            }
        }
        assert file != null;
        return file.toURI();
    }

    void save_config(){
        updateInputInfo();
        editor.putString("title", title);
        editor.putString("subtitle", subtitle);
        editor.putString("yx", yx);
        editor.putString("id", id);
        editor.putString("php_filename", php_filename);
        editor.putString("custom_image", custom_image_url);
        editor.putString("note", note);
        editor.putString("hidePath", hidePath);

        editor.apply();
    }

    /**
     * 显示卡片历史管理器
     */
    void showHistoryManager(){
        FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.layout_history) {
            @Override
            public void onBind(FullScreenDialog dialog, View v) {
                setupHistoryManagerUI(dialog, v);
            }
        }).show();
    }

    void setupHistoryManagerUI(FullScreenDialog dialog, View v){
        v.findViewById(R.id.btn_go_back).setOnClickListener(v1 -> dialog.dismiss());
        TextView tv_no_card = v.findViewById(R.id.no_card);
        RecyclerView rv_card_list = v.findViewById(R.id.card_list);
        TextView tv_count = v.findViewById(R.id.number_of_cards);
        TextView tv_more_options = v.findViewById(R.id.more_options);
        List<EntityCard> localCard = getLocalCardHistory();
        if (localCard != null && !localCard.isEmpty()){
            tv_count.setText(getString(R.string.number_of_cards, Objects.requireNonNull(localCard).size()+""));
            ArrayList<EntityCardHistory> list = entityCardToEntityCardHistory(localCard);
            CardHistoryAdapter cardHistoryAdapter = new CardHistoryAdapter(list, requireActivity(), count -> {
                if (count > 0) {
                    tv_count.setText(getString(R.string.number_of_cards, count + ""));
                } else {
                    tv_count.setText("");
                    tv_no_card.setVisibility(View.VISIBLE);
                }

            }, (action, entityCard, entityCardHistory, pos) -> doAction(action, entityCard, entityCardHistory, pos, rv_card_list));
            rv_card_list.setAdapter(cardHistoryAdapter);
            rv_card_list.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false){
                @Override
                public boolean canScrollVertically() {
                    return false; // 禁用滑动，防止与ScrollView冲突。
                }
            });
        } else {
            tv_count.setText("");
            tv_no_card.setVisibility(View.VISIBLE);
        }

        tv_more_options.setOnClickListener(v14 -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), v14);
            popupMenu.getMenuInflater().inflate(R.menu.history_manage_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.item_set_2fa_verify_url){
                    new InputDialog("键入您的验证服务器地址", "请先输入验证服务器地址(格式: https://example.com/del.php?verify=)，点击‘好’后自动保存。请严格按照格式要求填写，错误的填写也许会造成闪退等预期之外的问题。", "好", "取消")
                            .setCancelable(false)
                            .setOkButton((baseDialog, v131, server) -> {
                                editor.putString("auth_server", server);
                                editor.apply();
                                return false;
                            })
                            .show();
                }
                if (id == R.id.item_del_all_on_server){
                    onDelAllOnServer();
                }
                if (id == R.id.item_del_all_on_local){
                    new MaterialAlertDialogBuilder(v14.getContext())
                            .setTitle("确认删除所有卡片么？")
                            .setMessage("删除所有卡片后在本地不可恢复，但服务器上的文件不会一并清除。您仍有权限可通过指定网页链接对其进行访问！")
                            .setNeutralButton("手滑了..", null)
                            .setPositiveButton("确认删除", (dialog1, which) -> {
                                cardDao.deleteAll();
                                tv_count.setText("");
                                rv_card_list.setAdapter(null);
                                tv_no_card.setVisibility(View.VISIBLE);
                            })
                            .show();
                }
                if (id == R.id.item_push_setup){
                    onPushSetup();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    void onDelAllOnServer(){
        new InputDialog("您需要先通过2FA验证", "默认您已填入对应的验证地址。操作前先要确认您是服务器管理员，在下方键入验证代码才能进行下一步操作。如果您的访客，非常抱歉，您无权更改服务器数据！", "验证", "取消")
            .setCancelable(false)
            .setOkButton((baseDialog, v12, code) -> {
                String url = sharedPreferences.getString("auth_server", "");
                if (!url.isEmpty()){
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url+code)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            if (e instanceof SocketTimeoutException){
                                handler.sendEmptyMessage(HANDLER_MESSAGE_TIMEOUT);
                            }
                            if (e instanceof ConnectException){
                                handler.sendEmptyMessage(HANDLER_MESSAGE_CONNECTION_ERR);
                            }
                        }
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            res_2fa_callback = Objects.requireNonNull(response.body()).string();
                            handler.sendEmptyMessage(HANDLER_MESSAGE_SERVER_REPLY);
                        }
                    });
                } else {
                    UtilMethod.showDialog("无法建立连接", "验证服务器地址未配置！", requireActivity());
                }

                return false;
            })
            .show();
    }
    void onPushSetup(){
        BottomDialog.show("", "",
            new OnBindView<BottomDialog>(R.layout.layout_notification_settings) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onBind(BottomDialog dialog12, View v14) {
                    EditText mEtInterval = v14.findViewById(R.id.interval);
                    Button mBtnSave = v14.findViewById(R.id.btn_save_and_apply);
                    MaterialSwitch mSwitchMaskTimeout = v14.findViewById(R.id.switch_mask_request_timeout_notification);
                    MaterialSwitch mSwitchMaskConnectionErr = v14.findViewById(R.id.switch_mask_request_err_notification);
                    mEtInterval.setText((req_interval / 1000) + "");
                    mSwitchMaskTimeout.setChecked(switch_MaskTimeout);
                    mSwitchMaskConnectionErr.setChecked(switch_MaskConnectionErr);
                    mBtnSave.setOnClickListener(v13 -> {
                        try {
                            req_interval = Integer.parseInt(String.valueOf(mEtInterval.getText())) * 1000;
                            editor.putInt("req_interval", req_interval).apply();
                        } catch (NumberFormatException e) {
                            UtilMethod.showDialog("无法处理输入的数据", "请输入一个整数，单位秒。", requireActivity());
                        }
                    });

                    mSwitchMaskConnectionErr.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        switch_MaskConnectionErr = isChecked;
                        editor.putBoolean("switchMaskConnectionErr", isChecked).apply();
                    });

                    mSwitchMaskTimeout.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        switch_MaskTimeout = isChecked;
                        editor.putBoolean("switchMaskTimeout", isChecked).apply();
                    });
                }
            });
    }

    /**
     * 列表点击事件处理
     * BottomMenu.show(new String[]{"复制卡片代码", "跳转到监听地址", "后台自动监听"})
     */
    void doAction(int action, EntityCard entityCard, EntityCardHistory entityCardHistory, int pos, RecyclerView rv_card_list){
        switch (action){
            case 0: // 复制卡片代码
                copyToClipboard(entityCard.getCardData(), requireActivity());
                break;
            case 1: // 跳转到监听地址
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entityCard.getCardListenerUrl()));
                startActivity(browserIntent);
                break;
            case 2: // 后台自动监听
                if (isDismiss){
                    isDismiss = false;
                    if (listener_url.contains(entityCard.getCardListenerUrl())){
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setTitle("同标识的监听器已被设置")
                                .setMessage("相同标识的监听器所获数据相同，无需多次设置监听，这会浪费部分资源。您可选择关闭此监听器来重新设置！")
                                .setOnDismissListener(dialog -> isDismiss = true)
                                .setNeutralButton("保持原态", null)
                                .setPositiveButton("取消监听", (dialog, which) -> {
                                    String content = entityCardHistory.getCardHeadTitle();
                                    String sub = content.substring(0, content.length() - " (Listening)".length());
                                    entityCardHistory.setCardHeadTitle(sub);
                                    int index = listener_url.indexOf(entityCard.getCardListenerUrl());
                                    listener_url.remove(index);
                                    Thread thread = listener_threads.get(index);
                                    thread.interrupt();
                                    listener_threads.remove(index);
                                    Objects.requireNonNull(rv_card_list.getAdapter()).notifyItemChanged(pos);
                                }).show();
                    } else {
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setTitle("真的希望监听这个卡片么？")
                                .setMessage("监听后，会造成额外的资源占用；当网络环境处于不稳定状态时，不建议开启。程序将每隔"+(req_interval / 1000)+"秒向监听地址发送一次请求，以确认已读状态... 您需要给予一些权限来推送更新，否则这个功能对您来说是无效的。")
                                .setOnDismissListener(dialog -> isDismiss = true)
                                .setNeutralButton("算了", null)
                                .setPositiveButton("开始监听", (dialog, which) -> {
                                    cardListener(entityCard);
                                    listener_url.add(entityCard.getCardListenerUrl());
                                    String sub =  entityCardHistory.getCardHeadTitle() + " (Listening)";
                                    entityCardHistory.setCardHeadTitle(sub);
                                    Objects.requireNonNull(rv_card_list.getAdapter()).notifyItemChanged(pos);
                                }).show();
                    }
                }
                break;

            case -1:  // click 'Get'
                snackbar = ShowLoadingSnackbar("请求已发送，正在等待服务器响应..", requireActivity().getCurrentFocus(), null);
                break;
            case -2:  // do dismiss
                if (snackbar != null) snackbar.dismiss();
                break;
            case -3:
                copyToClipboard(entityCardHistory.getCardContentCallback(), requireActivity());
                break;
            case -4:
                String contents = replaceLastNewline(entityCardHistory.getCardContentCallback(), "");
                ArrayList<EntityCallbackContent> list = new ArrayList<>();
                for (String content : contents.split("\n\n")){
                    String[] res = content.split("\n");
                    for (int i = 0; i < res.length; i++) {
                        if (res[i].startsWith("## ")) {
                            res[i] = res[i].substring(3);
                        } else if (res[i].startsWith("- ")) {
                            res[i] = res[i].substring(2);
                        }
                    }
                    if (res.length == 4){
                        list.add(new EntityCallbackContent(res[0], res[1], res[2], res[3]));
                    }
                }
                String[] ips = new String[list.size()];
                for (EntityCallbackContent content : list){
                    ips[list.indexOf(content)] = content.getIp() + " | " + content.getRegion();
                }
                BottomMenu.show(ips)
                        .setOnMenuItemClickListener((dialog, text, index) -> {
                            EntityCallbackContent entity = list.get(index);
                            BottomMenu.show(new String[] {"点击下方信息进行复制", entity.getTime(), entity.getIp(), entity.getRegion(), entity.getUserAgent(), "-----------", "使用其它API查询此IP"})
                                    .setOnMenuItemClickListener((dialog1, text1, index1) -> {
                                        clickToCopy(String.valueOf(text1), entity);
                                        return true;
                                    });
                            return true;
                        });
                break;


        }
    }

    void clickToCopy(String text, EntityCallbackContent entity){
        if (Objects.equals(text, entity.getIp())){
            copyToClipboard(entity.getIp(), requireActivity());
        }
        if (Objects.equals(text, entity.getTime())){
            copyToClipboard(entity.getTime(), requireActivity());
        }
        if (Objects.equals(text, entity.getRegion())){
            copyToClipboard(entity.getRegion(), requireActivity());
        }
        if (Objects.equals(text, entity.getUserAgent())){
            copyToClipboard(entity.getUserAgent(), requireActivity());
        }
        if (Objects.equals(text, "使用其它API查询此IP")){
            getIPQueryRes(entity.getIp());
        }
    }

    void startListener(EntityCard card, String[] temp){
        ArkAPIReq.getArkListenerReturn(
                card.getCardListenerUrl(),
                getApplicationContext(),
                true,
                new ArkApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        String contents = replaceLastNewline(result, "");
                        ArrayList<EntityCallbackContent> list = new ArrayList<>();
                        for (String content : contents.split("\n\n")){
                            String[] res = content.split("\n");
                            for (int i = 0; i < res.length; i++) {
                                if (res[i].startsWith("## ")) {
                                    res[i] = res[i].substring(3);
                                } else if (res[i].startsWith("- ")) {
                                    res[i] = res[i].substring(2);
                                }
                            }
                            if (res.length == 4){
                                list.add(new EntityCallbackContent(res[0], res[1], res[2], res[3]));
                            }
                        }
                        result = "";
                        for (EntityCallbackContent content : list){
                            result = content.getIp() + " | " + content.getRegion() + "\n";
                        }

                        if (!result.equals(temp[0])){
                            push_listen_res(card.getCardNote(), result.replace(temp[0], ""), 0);
                        }
                        temp[0] = result;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (e instanceof SocketTimeoutException){
                                push_listen_res("监听超时", "监听地址连接超时，请检查网络设置...", 1);
                            }
                            if (e instanceof ConnectException){
                                push_listen_res("连接错误", "监听地址无法返回正确数据，请检查网络设置...", 2);
                            }
                        });
                    }
                }
        );
    }

    /**
     * 处理卡片监听事件
     * @param card 卡片实体类
     * */
    void cardListener(EntityCard card){
        Thread thread = new Thread(() -> {
            final String[] temp = {""};
            while (!Thread.currentThread().isInterrupted()){
                startListener(card, temp);
                try {
                    throwInMethod();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            handler.sendEmptyMessage(HANDLER_MESSAGE_THREAD_STOP);

        });
        listener_threads.add(thread);
        thread.start();
    }

    /**
     * 监听间隔设置
     * （防isInterrupted自动改变）
     */
    private void throwInMethod() throws InterruptedException {
        Thread.sleep(req_interval);
    }

    /**
     * 实体卡片类列表转换为卡片历史列表
     */
    private ArrayList<EntityCardHistory> entityCardToEntityCardHistory(List<EntityCard> list) {
        ArrayList<EntityCardHistory> cards = new ArrayList<>();
        for (EntityCard card : list){
            EntityCardHistory entityCardHistory = new EntityCardHistory();
            long cardId = card.getCardID();
            String cardHeadTitle = card.getCardTitle();
            if (cardHeadTitle.isEmpty()){
                cardHeadTitle = "No title";
            }
            String cardHeadSubtitle = card.getCardSubtitle();
            if (cardHeadSubtitle.isEmpty()){
                cardHeadSubtitle = "No subtitle";
            }
            String cardContentTitle = getString(R.string.item_card_title);
            String cardContentCallback = getString(R.string.item_card_content);
            String cardCreateTime = card.getCardCreateTime();
            String cardRemark = card.getCardNote();
            entityCardHistory.setCardHeadTitle(cardHeadTitle);
            if (listener_url.contains(card.getCardListenerUrl())){
                entityCardHistory.setCardHeadTitle(cardHeadTitle + " (Listening)");
            }
            entityCardHistory.setCardHeadSubtitle(cardHeadSubtitle);
            entityCardHistory.setCardContentTitle(cardContentTitle);
            entityCardHistory.setCardContentCallback(cardContentCallback);
            entityCardHistory.setCardCreateTime(cardCreateTime);
            entityCardHistory.setHistoryCardId(cardId);
            entityCardHistory.setCardNote(cardRemark);
            cards.add(entityCardHistory);
        }

        return cards;
    }

    /**
     * 获取本地卡片历史记录
     @return 卡片历史列表
     */
    private List<EntityCard> getLocalCardHistory(){
        List<EntityCard> allCard = cardDao.getAllCard();
        if (!allCard.isEmpty()){
            return allCard;
        }
        return null;
    }

    /**
     判断卡片监听地址是否重复
     @return 是否重复
     */
    boolean JudgmentListenerRepetition(String url){
        List<EntityCard> allCard = cardDao.getAllCard();
        for (EntityCard card : allCard){
            if (Objects.equals(card.getCardListenerUrl(), url)){
                return true;
            }
        }
        return false;
    }

    /**
     读取EditText等数据赋值到全局变量中
     */
    void updateInputInfo(){
        title = Objects.requireNonNull(binding.title.getText()).toString();
        subtitle = Objects.requireNonNull(binding.subtitle.getText()).toString();
        yx = Objects.requireNonNull(binding.yx.getText()).toString();
        custom_image_url = Objects.requireNonNull(binding.customImage.getText()).toString();
        id = Objects.requireNonNull(binding.id.getText()).toString();
        note = Objects.requireNonNull(binding.note.getText()).toString();
        hidePath = Objects.requireNonNull(binding.hidePath.getText()).toString();
        if (Objects.equals(note, "") || note == null){
            note = id;
        }
    }

    /**
     复原先前保存的配置
     */
    void configRestore(){
        read_config();
        binding.title.setText(title);
        binding.subtitle.setText(subtitle);
        binding.yx.setText(yx);
        binding.id.setText(id);
        binding.note.setText(note);
        binding.hidePath.setText(hidePath);
        binding.switchFollowId.setChecked(switch_followID);
        binding.switchHidePhp.setChecked(switch_hidePhp);
        binding.switchAutoUpdateUuid.setChecked(switch_auto_update_uuid);
        binding.switchBackground.setChecked(switch_show_background);
        if (switch_hidePhp){
            binding.textFieldHidePath.setVisibility(View.VISIBLE);
        }
        if (image_uri != null && !image_uri.isEmpty()){
            binding.background.setImageURI(Uri.parse(image_uri));
        }
        if (!switch_show_background){
            binding.background.setVisibility(View.GONE);
            binding.btnBackgroundSetting.setVisibility(View.GONE);
        }
    }

    /**
     读取以往保存的数据并赋值到全局变量中
     */
    void read_config(){
        title = sharedPreferences.getString("title","");
        subtitle = sharedPreferences.getString("subtitle","");
        yx = sharedPreferences.getString("yx","");
        custom_image_url = sharedPreferences.getString("custom_image","");
        id = sharedPreferences.getString("id","");
        php_filename = sharedPreferences.getString("php_filename","");
        note = sharedPreferences.getString("note","");
        hidePath =  sharedPreferences.getString("hidePath","");
        switch_followID = sharedPreferences.getBoolean("switch_followID",false);
        switch_hidePhp = sharedPreferences.getBoolean("switch_hidePhp",false);
        switch_auto_update_uuid = sharedPreferences.getBoolean("switch_auto_update_uuid",true);
        switch_show_background = sharedPreferences.getBoolean("showBackground",false);
        image_uri = sharedPreferences.getString("image_uri",null);
        req_interval = sharedPreferences.getInt("req_interval",30000);
        if (req_interval / 1000 == 0){
            req_interval = 30000;
        }
        switch_MaskTimeout = sharedPreferences.getBoolean("switchMaskTimeout",false);
        switch_MaskConnectionErr = sharedPreferences.getBoolean("switchMaskConnectionErr", false);
    }

    /**
     * 获取当前时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    String getDate(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    /**
     * 此方法用于向用户推送通知(当探针监听器内容或状态发生更改时)
     * 取决于通知的类型。
     * @param title 通知标题
     * @param subtitle 通知副标题
     * @param type 通知类型 (0: 监听器内容发生改变，1/2:监听器连接超时/无法返回正确数据)
     */
    void push_listen_res(String title, String subtitle, int type) {
        Notification notification;
        // 根据通知的类型，将创建不同的通知
        switch (type) {
            case 0:  // 监听器内容发生改变
                nID += 1;
                notification = new Notification.Builder(requireActivity(), channelId)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.iseen)
                        .build();

                notificationManager.notify(nID, notification);
                break;
            case 1:  // 监听器连接超时
                notification = new Notification.Builder(requireActivity(), channelId)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.iseen)
                        .build();

                if (!switch_MaskTimeout){
                    notificationManager.notify(-1, notification);
                }
                break;
            case 2:  // 监听器无法返回正确数据
                notification = new Notification.Builder(requireActivity(), channelId)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.iseen)
                        .build();

                if (!switch_MaskConnectionErr){
                    notificationManager.notify(-2, notification);
                }
                break;

        }
    }

    void getIPQueryRes(String ip){
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("ip", ip)
                .build();
        Request request = new Request.Builder().url("https://api.wetools.com/tool/ip").post(requestBody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.sendEmptyMessage(HANDLER_MESSAGE_QUERY_IP_FAILED);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                queryIP = ip;
                getIPQueryRes_callback = Objects.requireNonNull(response.body()).string();
                handler.sendEmptyMessage(HANDLER_MESSAGE_QUERY_IP_SUCCESS);
            }
        });
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == HANDLER_MESSAGE_SHOW_CARD_DATA) {
                copyToClipboard(card_data, requireActivity());
                new MaterialAlertDialogBuilder(requireActivity())
                        .setTitle("卡片数据已复制到您的剪贴板")
                        .setMessage(card_data)
                        .setPositiveButton("好", null)
                        .show();

                binding.btnGenerate.setLoading(false);

                String cardData = card_data;
                String filename = id+".png.txt";
                String cardListenerUrl = getAuthAPI(getActivity()) + String.format(API_GET_RES_endpoint, id+".png.txt");
                if (binding.switchHidePhp.isChecked() && Objects.requireNonNull(binding.customImage.getText()).toString().isEmpty()){  // 开启了隐匿模式，查询地址会发生变化
                    cardListenerUrl = getAuthAPI(getActivity()) + String.format(API_GET_RES_endpoint, id+".txt");
                }
                cardDao.insertCard(new EntityCard(title, subtitle, filename, cardData, cardListenerUrl, getDate(), note));
            }

            if (msg.what == HANDLER_MESSAGE_TIMEOUT) {
                UtilMethod.showDialog("请求超时！", "无法与服务器建立连接，请检查网络后重试！", requireActivity());
            }
            if (msg.what == HANDLER_MESSAGE_CONNECTION_ERR) {
                UtilMethod.showDialog("请求连接异常！", "无法正确读取通讯数据，请检查网络后重试！", requireActivity());
            }
            if (msg.what == HANDLER_MESSAGE_SERVER_REPLY) {
                UtilMethod.showDialog("服务器响应了您的请求", res_2fa_callback, requireActivity());
            }
            if (msg.what == HANDLER_MESSAGE_THREAD_STOP) {
                UtilMethod.showDialog("监听线程已终止", "如果您没有手动关闭监听器出现此弹窗，请反馈。如果您手动关闭监听器，请忽略此弹窗。", requireActivity());
            }
            if (msg.what == HANDLER_MESSAGE_QUERY_IP_SUCCESS) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(getIPQueryRes_callback).getJSONObject("data");
                    String location = jsonObject.getString("location");
                    String postalCode = jsonObject.getJSONObject("detail").getString("邮政编码");
                    String longitude = jsonObject.getJSONObject("detail").getString("经度");
                    String latitude = jsonObject.getJSONObject("detail").getString("纬度");
                    String altitude = jsonObject.getJSONObject("detail").getString("海拔");
                    String timezone = jsonObject.getJSONObject("detail").getString("时区");
                    String operator = jsonObject.getJSONObject("detail").getString("运营商");
                    String isProxy = jsonObject.getJSONObject("detail").getString("是否代理");
                    String proxyType = jsonObject.getJSONObject("detail").getString("代理类型");
                    String type = jsonObject.getJSONObject("detail").getString("应用场景");
                    String res = "IP 归属地: "+location+"\n邮政编码: "+postalCode+"\n经度: "+longitude+"\n纬度: "+latitude+"\n海拔: "+altitude+"\n时区: "+timezone+"\n运营商: "+operator+"\n是否代理: "+isProxy+"\n代理类型: "+proxyType+"\n应用场景: "+type;
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(queryIP+"的查询结果")
                            .setMessage(res)
                            .setNegativeButton("复制结果", (dialog, which) -> copyToClipboard(res, requireActivity()))
                            .setPositiveButton("好", null).show();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
            if (msg.what == HANDLER_MESSAGE_QUERY_IP_FAILED) {
                UtilMethod.showDialog("在获取IP详情时发生错误", "请检查网络设置..", requireActivity());
            }
            return true;
        }
    });
}