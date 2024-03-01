package com.cwuom.iseen;

import static com.cwuom.iseen.Util.UtilMethod.inputStreamToString;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cwuom.iseen.Adapter.CardHistoryAdapter;
import com.cwuom.iseen.Dao.CardDao;
import com.cwuom.iseen.Entity.EntityCard;
import com.cwuom.iseen.Entity.EntityCardHistory;
import com.cwuom.iseen.InitDataBase.InitCardDataBase;
import com.cwuom.iseen.Util.NeverCrash;
import com.cwuom.iseen.Util.NotificationUtil;
import com.cwuom.iseen.Util.UtilMethod;
import com.cwuom.iseen.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kongzue.dialogx.BuildConfig;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.InputDialog;
import com.kongzue.dialogx.interfaces.OnBindView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
 * @author cwuom
 * 下列源码支持变动后重新打包，在变动不大的情况下，请尽量保留作者的信息！
 * 请勿用于商业用途和非法用途。
 * */

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;  // 视图绑定
    private CardDao cardDao;  // 与数据库进行交互
    private final int HANDLER_MESSAGE_SHOW_CARD_DATA = 1;  // 当签名接口请求完成后，弹出卡片代码
    private final int HANDLER_MESSAGE_TIMEOUT = -1;  // 监听超时时，后台弹出通知
    private final int HANDLER_MESSAGE_CONNECTION_ERR = -2;  // 监听连接错误时，后台弹出通知
    private final int HANDLER_MESSAGE_SERVER_REPLY = 2;  // 2FA代码发送到服务器且服务器回应时，弹出反馈窗口
    private final int HANDLER_MESSAGE_THREAD_STOP = 3;  // 监听线程被终止时，弹出提示窗口
    private String card_data = "";  // 卡片代码

    private String server;  // 服务器地址
    private String title;  // 卡片标题
    private String subtitle;  // 卡片副标题
    private String yx;  // 卡片外显
    private String id;  // 卡片标识
    private String php_filename;  // PHP文件名/地址
    private String data_dir;  // 监听数据存放目录
    private String req_url;  // 图片签名地址
    private String note;  // 卡片备注
    private Boolean switch_followID = Boolean.TRUE;  // 子标题是否跟随标识
    private Boolean switch_hidePhp = Boolean.TRUE;  // 是否隐藏php地址（可以把它隐藏成沙雕图）
    private Boolean switch_hide_dangerous_input = Boolean.FALSE;  // 是否隐藏关键输入信息
    private Boolean switch_show_background = Boolean.TRUE;  // 是否显示背景图片
    private SharedPreferences sharedPreferences;  // SharedPreferences，存放配置
    private SharedPreferences.Editor editor;  // 编辑存放的配置
    private String hidePath;  // 隐匿路径
    private String res_2fa_callback;  // 输入2FA代码后，服务器响应的信息
    private NotificationManager notificationManager;  // 通知管理器
    private String channelId;  // 渠道ID
    int nID = 0;  // 通知ID
    private Boolean isDismiss = true;  // 防止重复弹出dialog窗口
    private final ArrayList<Thread> listener_threads = new ArrayList<>();  // 监听器线程列表
    private final ArrayList<String> listener_url = new ArrayList<>();  // 监听地址列表
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;  // 图片选择器 (旧android也许不兼容，Android13支持)
    private String image_uri;  // 背景图uri地址
    private String API = "http://api.mrgnb.cn/API/qq_ark37.php?url=";

    private final Handler handler = new CustomHandler(this);  // 创建handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());  // 布局绑定
        setContentView(binding.getRoot());

        initMethod();
    }

    private void initMethod(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        configRestore();

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        editor.putString("image_uri", String.valueOf(uriToUri(uri, MainActivity.this))).apply();
                        binding.background.setImageURI(uri);
                        ShowSnackbar("背景图已更新！");
                    } else {
                        ShowSnackbar("没有选择合适的媒体，维持原态");
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        // 全局崩溃拦截(懒得用try盖住了)
        NeverCrash.getInstance()
                .setDebugMode(BuildConfig.DEBUG)
                .setMainCrashHandler((t, e) -> UtilMethod.showDialog("发生了意想不到的问题..", "已尝试在主线程拦截崩溃，您需要将此截图反馈给开发者。\n报错详情: "+e.toString(), this))
                .setUncaughtCrashHandler((t, e) -> UtilMethod.showDialog("发生了意想不到的问题..", "已尝试在子线程拦截崩溃，您需要将此截图反馈给开发者。\n报错详情: "+e.toString(), this))
                .register(this.getApplication());

        // 判断通知权限状态，若无权限则弹出窗口帮助用户授权
        if (!NotificationUtil.isNotifyEnabled(this) && sharedPreferences.getBoolean("notify_permission_remind", true)){
            new MaterialAlertDialogBuilder(this)
                    .setTitle("程序功能受限")
                    .setMessage("未开启通知权限，将无法使用卡片监听功能！但能保证基础功能正常使用。")
                    .setNeutralButton("忽略", null)
                    .setNegativeButton("不再提示", (dialog, which) -> editor.putBoolean("notify_permission_remind", false).apply())
                    .setPositiveButton("去开启", (dialog, which) -> {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(localIntent);
                    })
                    .show();

        }

        // 弹出免责声明
        if (!sharedPreferences.getBoolean("is_read", false)){
            new MaterialAlertDialogBuilder(this)
                    .setTitle("声明")
                    .setMessage("iseen 软件免责声明\n" +
                            "\n" +
                            "在使用 iseen 软件（以下简称“软件”）之前，请仔细阅读并理解以下条款：\n" +
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

        InitCardDataBase initCardDataBase = UtilMethod.getInstance(this);
        cardDao = initCardDataBase.cardDao();


        // 设置随机卡片标识
        binding.btnRandom.setOnClickListener(v -> {
            String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
            binding.id.setText(uuid);
        });

        // 签名并生成卡片
        binding.btnGenerate.setOnClickListener(v -> {
            updateInputInfo();
            String url = server+php_filename+"?id="+id+".png";
            if (binding.switchHidePhp.isChecked()){
                url = server+hidePath+"/"+id+".png";
            }
            req_url = API+url+"&title="+title+"&subtitle="+subtitle+"&yx="+yx;
            String cardListenerUrl = server+data_dir+"/"+id+".png.txt";
            if (binding.switchHidePhp.isChecked()){  // 开启了隐匿模式，查询地址会发生变化
                cardListenerUrl = server+data_dir+"/"+id+".txt";
            }
            if (!JudgmentListenerRepetition(cardListenerUrl)){ // 判断是否重复
                ShowSnackbar("正在提交数据并签名，请稍等。");
                getCardData(req_url);  // 向API发送签名请求
            } else {
                ShowSnackbar("相同标识的卡片已经存在，请更换标识。");
            }
        });

        // 保存卡片配置
        binding.btnSave.setOnClickListener(v -> {
            updateInputInfo();
            editor.putString("server", server);
            editor.putString("title", title);
            editor.putString("subtitle", subtitle);
            editor.putString("yx", yx);
            editor.putString("id", id);
            editor.putString("php_filename", php_filename);
            editor.putString("data_dir", data_dir);
            editor.putString("note", note);
            editor.putString("hidePath", hidePath);

            editor.apply();
            ShowSnackbar("配置已保存！");
        });

        // 恢复卡片配置
        binding.btnRestore.setOnClickListener(v -> {
            configRestore();
            ShowSnackbar("配置已恢复！");
        });

        // 监听卡片标识的变化
        binding.id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.switchFollowId.isChecked()){
                    binding.subtitle.setText(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 复制查询地址
        binding.btnGetUrl.setOnClickListener(v -> {
            updateInputInfo();
            copyToClipboard(server+data_dir+"/"+id+".png.txt");
            ShowSnackbar("已将查询链接放入剪贴板。");
        });

        // 平台跳转
        binding.devCard.setOnClickListener(v -> new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("对此有点兴趣吗？")
                .setMessage("点击下方按钮，跳转对应平台。")
                .setNegativeButton("Telegram", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/cwuoms_group"));
                    startActivity(browserIntent);
                })
                .setPositiveButton("QQ", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=UD5xNmXt0Otz0OrvpXCaKnSd04BDf0rm&authKey=40ctuZ7TZLHzf1LBJZ29Nqvu%2F55gAnvqJ%2FB7s8oJvWsM7AA07%2BXIF8J2GKctM4hD&noverify=0&group_code=923071208"));
                    startActivity(browserIntent);
                })
                .setNeutralButton("关闭", null)
                .show());

        // 长按跳转到B站
        binding.devCard.setOnLongClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://space.bilibili.com/473400804"));
            startActivity(browserIntent);
            return true;
        });

        binding.warningCard.setOnClickListener(v -> new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("戳我没用啦~")
                .setMessage("这只是让你阅读的，当然非常感谢你能成功注意到这个卡片。")
                .setPositiveButton("好！", null)
                .show());

        binding.warningCard.setOnLongClickListener(v -> true);  // 没什么用的监听

        binding.btnHistory.setOnClickListener(v -> showHistoryManager());  // 弹出历史管理

        binding.switchFollowId.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switch_followID", isChecked);
            editor.apply();
            if (isChecked){
                ShowSnackbar("子标题将随着标识一同改变");
            }else {
                ShowSnackbar("子标题与标识已独立");
            }
        });
        binding.switchHidePhp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switch_hidePhp", false);
            editor.apply();
            if (isChecked){
                new MaterialAlertDialogBuilder(this)
                        .setTitle("并不是所有服务器都支持这个特性！")
                        .setMessage("开启后，程序生成的卡片代码将不包含自己的php地址，这可以一定程度的防止他人抓包。需要他工作的前提是服务端需要在nginx规则里设置如下代码，如\nlocation ~* ^/special/kp/(.*)\\.png$ {\n" +
                                "    rewrite ^/special/kp/(.*)\\.png$ /kp.php?id=$1 last;\n" +
                                "}\n否则卡片代码将无法正常工作以满足最初的需求。若您已经做好配置，开启后可在按钮下方键入伪装路径(如special/kp/)")

                        .setNeutralButton("关闭此选项", (dialog, which) -> binding.switchHidePhp.setChecked(false))
                        .setPositiveButton("仍要开启", (dialog, which) -> {
                            editor.putBoolean("switch_hidePhp", true);
                            editor.apply();
                            binding.textFieldHidePath.setVisibility(View.VISIBLE);
                        })
                        .show();
            } else {
                binding.textFieldHidePath.setVisibility(View.GONE);
                ShowSnackbar("隐匿模式已关闭");
            }
        });

        binding.switchHideEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                hideDangerousInput();
                ShowSnackbar("关键输入数据已被隐去");
            } else {
                binding.server.setTransformationMethod(null);
                binding.php.setTransformationMethod(null);
                binding.dataDirectory.setTransformationMethod(null);
            }

            editor.putBoolean("switch_hide_edit", isChecked);
            editor.apply();
        });


        binding.switchBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("showBackground", isChecked);
            editor.apply();
            if (isChecked){
                binding.background.setVisibility(View.VISIBLE);
                binding.btnBackgroundSetting.setVisibility(View.VISIBLE);
            } else {
                binding.background.setVisibility(View.GONE);
                binding.btnBackgroundSetting.setVisibility(View.GONE);
                ShowSnackbar("不再显示背景了..");
            }
        });

        binding.btnBackgroundSetting.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.bg_setting_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.item_use_default_background){
                    editor.remove("image_uri").apply();
                    binding.background.setImageResource(R.drawable.neri);
                    ShowSnackbar("背景已设置为默认");
                }
                if (id == R.id.item_choose_background){
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
                return false;
            });
            popupMenu.show();
        });
    }

    /**
     * 将临时uri转换为永久uri
     * @param uri 临时uri地址
     * @param context 上下文
     * @return 永久uri地址
     */
    public static URI uriToUri(Uri uri, Context context) {
        File file = null;
        if (uri == null) return file.toURI();
        //android10以上转换
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_FILE)) {
            file = new File(Objects.requireNonNull(uri.getPath()));
        } else if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assert file != null;
        return file.toURI();
    }

    /**
     * 隐藏关键输入信息
     */
    void hideDangerousInput(){
        binding.server.setTransformationMethod(new PasswordTransformationMethod());
        binding.php.setTransformationMethod(new PasswordTransformationMethod());
        binding.dataDirectory.setTransformationMethod(new PasswordTransformationMethod());
    }

    /**
     * 显示卡片历史管理器
     */
    void showHistoryManager(){
        FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.layout_history) {
            @Override
            public void onBind(FullScreenDialog dialog, View v) {
                v.findViewById(R.id.btn_go_back).setOnClickListener(v1 -> dialog.dismiss());
                TextView tv_no_card = v.findViewById(R.id.no_card);
                RecyclerView rv_card_list = v.findViewById(R.id.card_list);
                TextView tv_count = v.findViewById(R.id.number_of_cards);
                Button btn_del_all = v.findViewById(R.id.btn_del_all);
                List<EntityCard> localCard = getLocalCardHistory();
                if (localCard != null && localCard.size() > 0){
                    tv_count.setText(getString(R.string.number_of_cards, Objects.requireNonNull(localCard).size()+""));
                    ArrayList<EntityCardHistory> list = entityCardToEntityCardHistory(localCard);
                    CardHistoryAdapter cardHistoryAdapter = new CardHistoryAdapter(list, MainActivity.this, count -> {
                        if (count > 0) {
                            tv_count.setText(getString(R.string.number_of_cards, count + ""));
                        } else {
                            tv_count.setText("");
                            tv_no_card.setVisibility(View.VISIBLE);
                        }

                    }, (action, entityCard, entityCardHistory, pos) -> doAction(action, entityCard, entityCardHistory, pos, rv_card_list));
                    rv_card_list.setAdapter(cardHistoryAdapter);
                    rv_card_list.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false){
                        @Override
                        public boolean canScrollVertically() {
                            return false; // 禁用滑动，防止与ScrollView冲突。
                        }
                    });
                } else {
                    tv_count.setText("");
                    tv_no_card.setVisibility(View.VISIBLE);
                }

                v.findViewById(R.id.btn_del_all_local).setOnClickListener(v15 -> new MaterialAlertDialogBuilder(v15.getContext())
                        .setTitle("确认删除所有卡片么？")
                        .setMessage("删除所有卡片后在本地不可恢复，但服务器上的文件不会一并清除。您仍有权限可通过指定网页链接对其进行访问！")
                        .setNeutralButton("手滑了..", null)
                        .setPositiveButton("确认删除", (dialog1, which) -> {
                            cardDao.deleteAll();
                            tv_count.setText("");
                            rv_card_list.setAdapter(null);
                            tv_no_card.setVisibility(View.VISIBLE);
                        })
                        .show());

                btn_del_all.setOnClickListener(v14 -> new InputDialog("您需要先通过2FA验证", "默认您已长按此按钮并填入对应的验证地址。操作前先要确认您是服务器管理员，在下方键入验证代码才能进行下一步操作。如果您的访客，非常抱歉，您无权更改服务器数据！", "验证", "取消")
                        .setCancelable(false)
                        .setOkButton((baseDialog, v12, code) -> {
                            String url = sharedPreferences.getString("auth_server", "");
                            if (!url.equals("")){
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
                                UtilMethod.showDialog("无法建立连接", "验证服务器地址未配置！", MainActivity.this);
                            }

                            return false;
                        })
                        .show());

                btn_del_all.setOnLongClickListener(v13 -> {
                    new InputDialog("键入您的验证服务器地址", "请先输入验证服务器地址(格式: https://example.com/del.php?verify=)，点击‘好’后自动保存。请严格按照格式要求填写，错误的填写也许会造成闪退等预期之外的问题。", "好", "取消")
                            .setCancelable(false)
                            .setOkButton((baseDialog, v131, server) -> {
                                editor.putString("auth_server", server);
                                editor.apply();
                                return false;
                            })
                            .show();
                    return true;
                });
            }
        }).show();
    }

    /**
     * 列表点击事件处理
     * BottomMenu.show(new String[]{"复制卡片代码", "跳转到监听地址", "后台自动监听"})
     */
    void doAction(int action, EntityCard entityCard, EntityCardHistory entityCardHistory, int pos, RecyclerView rv_card_list){
        switch (action){
            case 0: // 复制卡片代码
                copyToClipboard(entityCard.getCardData());
                break;
            case 1: // 跳转到监听地址
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entityCard.getCardListenerUrl()));
                startActivity(browserIntent);
                break;
            case 2: // 后台自动监听
                if (isDismiss){
                    isDismiss = false;
                    if (listener_url.contains(entityCard.getCardListenerUrl())){
                        new MaterialAlertDialogBuilder(MainActivity.this)
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
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("真的希望监听这个卡片么？")
                                .setMessage("监听后，但会造成额外的资源占用；当网络环境处于不稳定状态时，不建议开启。程序将每隔30秒向监听地址发送一次请求，以确认已读状态... 您需要给予一些权限来推送更新，否则这个功能对您来说是无效的。")
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

        }
    }

    /**
     * 处理卡片监听事件
     * @param card 卡片实体类
     * */
    void cardListener(EntityCard card){
        Thread thread = new Thread(() -> {
            final String[] temp = {""};
            while (!Thread.currentThread().isInterrupted()){
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(card.getCardListenerUrl())
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        if (e instanceof SocketTimeoutException){
                            push_listen_res("监听超时", "监听地址连接超时，请检查网络设置...", 1);
                        }
                        if (e instanceof ConnectException){
                            push_listen_res("连接错误", "监听地址无法返回正确数据，请检查网络设置...", 2);
                        }
                    }
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String res = Objects.requireNonNull(response.body()).string();
                        if (!res.equals(temp[0])){
                            push_listen_res(card.getCardNote(), res.replace(temp[0], ""), 0);
                        }
                        temp[0] = res;
                    }
                });
                try {
                    throwInMethod();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
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
        Thread.sleep(30000);
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
            if (cardHeadTitle.equals("")){
                cardHeadTitle = "No title";
            }
            String cardHeadSubtitle = card.getCardSubtitle();
            if (cardHeadSubtitle.equals("")){
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
        if (allCard.size() > 0){
            return allCard;
        }
        return null;
    }

    /**
     判断卡片监听地址是否重复
     @param url
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
     获取卡片代码，需先请求API
     * @param req_url
     */
    void getCardData(String req_url){
        new Thread(() -> {
            try {
                URL url = new URL(req_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    card_data = inputStreamToString(inputStream);
                    handler.sendEmptyMessage(HANDLER_MESSAGE_SHOW_CARD_DATA);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     读取EditText等数据赋值到全局变量中
     */
    void updateInputInfo(){
        server = Objects.requireNonNull(binding.server.getText()).toString();
        title = Objects.requireNonNull(binding.title.getText()).toString();
        subtitle = Objects.requireNonNull(binding.subtitle.getText()).toString();
        yx = Objects.requireNonNull(binding.yx.getText()).toString();
        id = Objects.requireNonNull(binding.id.getText()).toString();
        php_filename = Objects.requireNonNull(binding.php.getText()).toString();
        data_dir = Objects.requireNonNull(binding.dataDirectory.getText()).toString();
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
        binding.server.setText(server);
        binding.title.setText(title);
        binding.subtitle.setText(subtitle);
        binding.yx.setText(yx);
        binding.id.setText(id);
        binding.php.setText(php_filename);
        binding.dataDirectory.setText(data_dir);
        binding.note.setText(note);
        binding.hidePath.setText(hidePath);
        binding.switchFollowId.setChecked(switch_followID);
        binding.switchHidePhp.setChecked(switch_hidePhp);
        binding.switchBackground.setChecked(switch_show_background);
        if (switch_hidePhp){
            binding.textFieldHidePath.setVisibility(View.VISIBLE);
        }
        binding.switchHideEdit.setChecked(switch_hide_dangerous_input);
        if (switch_hide_dangerous_input){
            hideDangerousInput();
        }

        if (image_uri != null && !image_uri.equals("")){
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
        server = sharedPreferences.getString("server","");
        title = sharedPreferences.getString("title","");
        subtitle = sharedPreferences.getString("subtitle","");
        yx = sharedPreferences.getString("yx","");
        id = sharedPreferences.getString("id","");
        php_filename = sharedPreferences.getString("php_filename","");
        data_dir = sharedPreferences.getString("data_dir","");
        note = sharedPreferences.getString("note","");
        hidePath =  sharedPreferences.getString("hidePath","");
        switch_followID = sharedPreferences.getBoolean("switch_followID",true);
        switch_hidePhp = sharedPreferences.getBoolean("switch_hidePhp",false);
        switch_hide_dangerous_input = sharedPreferences.getBoolean("switch_hide_edit",false);
        switch_show_background = sharedPreferences.getBoolean("showBackground",true);
        image_uri = sharedPreferences.getString("image_uri",null);
    }


    /**
     展示Snackbar
     @param info 需要展示的信息
     */
    @SuppressLint("RestrictedApi")
    void ShowSnackbar(String info){  // 注: 无障碍模式会导致Snackbar无动画
        Snackbar snackbar;
        View rootView = MainActivity.this.getWindow().getDecorView();
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

    /**
     * 复制内容到剪贴板
     * @param text 需要复制的内容
     */
    void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", text);
        cm.setPrimaryClip(mClipData);
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
     使用Handler方便在子线程更新UI
     */
    @SuppressLint("HandlerLeak")
    private class CustomHandler extends Handler {
        // 弱引用持有HandlerActivity , GC 回收时会被回收掉
        private final WeakReference<MainActivity> weakReference;
        public CustomHandler(MainActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            weakReference.get();
            super.handleMessage(msg);
            if (msg.what == HANDLER_MESSAGE_SHOW_CARD_DATA) {
                copyToClipboard(card_data);

//                MessageDialog.show("卡片数据已复制到您的剪贴板", card_data, "确定");
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("卡片数据已复制到您的剪贴板")
                        .setMessage(card_data)
                        .setPositiveButton("好", null)
                        .show();

                String cardData = card_data;
                String cardListenerUrl = server+data_dir+"/"+id+".png.txt";
                if (binding.switchHidePhp.isChecked()){
                    cardListenerUrl = server+data_dir+"/"+id+".txt";
                }
                cardDao.insertCard(new EntityCard(title, subtitle, req_url, cardData, cardListenerUrl, getDate(), note));
            }

            if (msg.what == HANDLER_MESSAGE_TIMEOUT) {
                UtilMethod.showDialog("请求超时！", "无法与服务器建立连接，请检查网络后重试！", MainActivity.this);
            }
            if (msg.what == HANDLER_MESSAGE_CONNECTION_ERR) {
                UtilMethod.showDialog("请求连接异常！", "无法正确读取通讯数据，请检查网络后重试！", MainActivity.this);
            }
            if (msg.what == HANDLER_MESSAGE_SERVER_REPLY) {
                UtilMethod.showDialog("服务器响应了您的请求", res_2fa_callback, MainActivity.this);
            }
            if (msg.what == HANDLER_MESSAGE_THREAD_STOP) {
                UtilMethod.showDialog("监听线程已终止", "如果您没有手动关闭监听器出现此弹窗，请反馈。如果您手动关闭监听器，请忽略此弹窗。", MainActivity.this);
            }
        }
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
                 notification = new Notification.Builder(MainActivity.this, channelId)
                         .setContentTitle(title)
                         .setContentText(subtitle)
                         .setWhen(System.currentTimeMillis())
                         .setSmallIcon(R.drawable.iseen)
                         .build();

                 notificationManager.notify(nID, notification);
                 break;
             case 1:  // 监听器连接超时
                 notification = new Notification.Builder(MainActivity.this, channelId)
                         .setContentTitle(title)
                         .setContentText(subtitle)
                         .setWhen(System.currentTimeMillis())
                         .setSmallIcon(R.drawable.iseen)
                         .build();

                 notificationManager.notify(-1, notification);
                 break;
             case 2:  // 监听器无法返回正确数据
                 notification = new Notification.Builder(MainActivity.this, channelId)
                         .setContentTitle(title)
                         .setContentText(subtitle)
                         .setWhen(System.currentTimeMillis())
                         .setSmallIcon(R.drawable.iseen)
                         .build();

                 notificationManager.notify(-2, notification);
                 break;

         }
     }



}