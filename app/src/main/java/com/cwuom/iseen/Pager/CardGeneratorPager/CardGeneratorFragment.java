package com.cwuom.iseen.Pager.CardGeneratorPager;

import static android.content.Context.MODE_PRIVATE;
import static com.cwuom.iseen.Util.UtilMethod.ShowLoadingSnackbar;
import static com.cwuom.iseen.Util.UtilMethod.ShowSnackbar;
import static com.cwuom.iseen.Util.UtilMethod.copyToClipboard;
import static com.cwuom.iseen.Util.UtilMethod.parseURLComponents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.cwuom.iseen.NavigationActivity;
import com.cwuom.iseen.QZoneActivity;
import com.cwuom.iseen.R;
import com.cwuom.iseen.Util.API.Ark.ArkAPIReq;
import com.cwuom.iseen.Util.API.Ark.ArkApiCallback;
import com.cwuom.iseen.Util.Constants;
import com.cwuom.iseen.databinding.FragmentCardGeneratorBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.FormBody;


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

public class CardGeneratorFragment extends Fragment {
    FragmentCardGeneratorBinding binding;

    private String uin = "";
    private String p_skey = "";
    private String skey = "";
    BottomNavigationView bottomNavigationView;

    private static final String PREFS_NAME = "CardGeneratorPrefs";
    private static final String KEY_CARD_TITLE = "cardTitle";
    private static final String KEY_CARD_SUBTITLE = "cardSubtitle";
    private static final String KEY_CARD_PROMPT = "cardPrompt";
    private static final String KEY_CARD_COVER = "cardCover";
    private static final String KEY_CARD_DESC = "cardDesc";
    private static final String KEY_CARD_PREVIEW = "cardPreview";
    private static final String KEY_CARD_TAG = "cardTag";
    private static final String KEY_CARD_TAG_ICON = "cardTagIcon";
    private static final String KEY_CARD_THUMBNAIL_URL = "cardThumbnailUrl";
    private static final String KEY_CARD_JUMP_URL = "cardJumpUrl";
    private static final String KEY_CARD_JUMP_BUTTON = "cardJumpButton";
    private static final String KEY_CARD_BUTTON_TITLE = "cardButtonTitle";
    private static final String KEY_CARD_F1 = "cardF1";
    private static final String KEY_CARD_F2 = "cardF2";
    private static final String KEY_CARD_F3 = "cardF3";
    private static final String KEY_CARD_F4 = "cardF4";
    private static final String KEY_CARD_F5 = "cardF5";
    private final HashMap<View, String> originalHints = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCardGeneratorBinding.inflate(getLayoutInflater());
        storeOriginalHints();
        initMethod();
        if (savedInstanceState != null) {
            initAdapter();
        }

        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Constants.ACTION_ACTIVITY_CREATED);
        requireActivity().registerReceiver(activityCreatedReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(activityCreatedReceiver);
    }

    private final BroadcastReceiver activityCreatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_ACTIVITY_CREATED.equals(intent.getAction())) {
                // Activity 被创建或重建时调用 initAdapter()
                initAdapter();
            }
        }
    };




    @SuppressLint("SetTextI18n")
    private void initMethod(){
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        boolean hidden_warning = preferences.getBoolean("hidden_warning", false);
        if (hidden_warning){
            binding.warningCard.setVisibility(View.GONE);
        }

        if(getActivity() instanceof NavigationActivity) {
            bottomNavigationView = ((NavigationActivity) getActivity()).getBottomNavigationView();
        }

        hideAllInputs();
        initAdapter();
        initializeInputVisibility();

        for (TextInputLayout inputLayout : getAllInputs()) {
            setupEditTextListener(inputLayout);
        }

        binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            binding.btnGenerate.setEnabled(true);
            binding.viewCut.setVisibility(View.VISIBLE);
            setCardTypeVisibility(position);
        });

        binding.btnGenerate.setOnClickListener(v -> {
            String cardType = binding.autoCompleteTextView.getText().toString();
            uin = Objects.requireNonNull(binding.uin.getText()).toString();
            skey = Objects.requireNonNull(binding.skey.getText()).toString();
            p_skey = Objects.requireNonNull(binding.pSkey.getText()).toString();

            if (uin.isEmpty() || skey.isEmpty() || p_skey.isEmpty()){
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("config", MODE_PRIVATE);
                uin = sharedPreferences.getString("uin", "");
                skey = sharedPreferences.getString("skey", "");
                p_skey = sharedPreferences.getString("p_skey", "");
                binding.uin.setText(uin);
                binding.skey.setText(skey);
                binding.pSkey.setText(p_skey);
                if (uin.isEmpty()){
                    binding.uin.setText("o"+preferences.getString("qq", ""));
                }
            }

            sendRequestByCardType(cardType);


        });

        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), QZoneActivity.class));
            binding.uin.setText("");
            binding.skey.setText("");
            binding.pSkey.setText("");
        });

        binding.btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_CARD_TITLE, Objects.requireNonNull(binding.cardTitle.getText()).toString());
            editor.putString(KEY_CARD_SUBTITLE, Objects.requireNonNull(binding.cardSubtitle.getText()).toString());
            editor.putString(KEY_CARD_PROMPT, Objects.requireNonNull(binding.cardPrompt.getText()).toString());
            editor.putString(KEY_CARD_COVER, Objects.requireNonNull(binding.cardCover.getText()).toString());
            editor.putString(KEY_CARD_DESC, Objects.requireNonNull(binding.cardDesc.getText()).toString());
            editor.putString(KEY_CARD_PREVIEW, Objects.requireNonNull(binding.cardPreview.getText()).toString());
            editor.putString(KEY_CARD_TAG, Objects.requireNonNull(binding.cardTag.getText()).toString());
            editor.putString(KEY_CARD_TAG_ICON, Objects.requireNonNull(binding.cardTagIcon.getText()).toString());
            editor.putString(KEY_CARD_THUMBNAIL_URL, Objects.requireNonNull(binding.cardThumbnailUrl.getText()).toString());
            editor.putString(KEY_CARD_JUMP_URL, Objects.requireNonNull(binding.cardJumpUrl.getText()).toString());
            editor.putString(KEY_CARD_JUMP_BUTTON, Objects.requireNonNull(binding.cardJumpButton.getText()).toString());
            editor.putString(KEY_CARD_BUTTON_TITLE, Objects.requireNonNull(binding.cardButtonTitle.getText()).toString());
            editor.putString(KEY_CARD_F1, Objects.requireNonNull(binding.cardF1.getText()).toString());
            editor.putString(KEY_CARD_F2, Objects.requireNonNull(binding.cardF2.getText()).toString());
            editor.putString(KEY_CARD_F3, Objects.requireNonNull(binding.cardF3.getText()).toString());
            editor.putString(KEY_CARD_F4, Objects.requireNonNull(binding.cardF4.getText()).toString());
            editor.putString(KEY_CARD_F5, Objects.requireNonNull(binding.cardF5.getText()).toString());

            editor.apply();
            ShowSnackbar("已保存当前模板", requireActivity(), bottomNavigationView);
        });

        binding.btnRestore.setOnClickListener(v -> {
            binding.cardTitle.setText(prefs.getString(KEY_CARD_TITLE, ""));
            binding.cardSubtitle.setText(prefs.getString(KEY_CARD_SUBTITLE, ""));
            binding.cardPrompt.setText(prefs.getString(KEY_CARD_PROMPT, ""));
            binding.cardCover.setText(prefs.getString(KEY_CARD_COVER, ""));
            binding.cardDesc.setText(prefs.getString(KEY_CARD_DESC, ""));
            binding.cardPreview.setText(prefs.getString(KEY_CARD_PREVIEW, ""));
            binding.cardTag.setText(prefs.getString(KEY_CARD_TAG, ""));
            binding.cardTagIcon.setText(prefs.getString(KEY_CARD_TAG_ICON, ""));
            binding.cardThumbnailUrl.setText(prefs.getString(KEY_CARD_THUMBNAIL_URL, ""));
            binding.cardJumpUrl.setText(prefs.getString(KEY_CARD_JUMP_URL, ""));
            binding.cardJumpButton.setText(prefs.getString(KEY_CARD_JUMP_BUTTON, ""));
            binding.cardButtonTitle.setText(prefs.getString(KEY_CARD_BUTTON_TITLE, ""));
            binding.cardF1.setText(prefs.getString(KEY_CARD_F1, ""));
            binding.cardF2.setText(prefs.getString(KEY_CARD_F2, ""));
            binding.cardF3.setText(prefs.getString(KEY_CARD_F3, ""));
            binding.cardF4.setText(prefs.getString(KEY_CARD_F4, ""));
            binding.cardF5.setText(prefs.getString(KEY_CARD_F5, ""));
            ShowSnackbar("模板已恢复", requireActivity(), bottomNavigationView);
        });
    }

    private void sendRequestByCardType(String cardType) {
        String endpoint = "";
        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        String preview = Objects.requireNonNull(binding.cardPreview.getText()).toString();
        String tagIcon = Objects.requireNonNull(binding.cardTagIcon.getText()).toString();
        String jumpUrl = Objects.requireNonNull(binding.cardJumpUrl.getText()).toString();
        String title = Objects.requireNonNull(binding.cardTitle.getText()).toString();
        String cover = Objects.requireNonNull(binding.cardCover.getText()).toString();

        switch (cardType) {
            case "图转卡(imagetextbot)":
                if (cover.isEmpty()){
                    cover = "https://api.lyhc.top/bot/a.jpg";
                    binding.cardCover.setText(cover);
                    Toast.makeText(requireActivity(), "怎么能没有预览图呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                }
                endpoint = "/get_card";
                formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                formBodyBuilder.add("subtitle", Objects.requireNonNull(binding.cardSubtitle.getText()).toString());
                formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                formBodyBuilder.add("cover", cover);
                break;
            case "文转卡(Embed)":
                endpoint = "/get_card_embed";
                formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                formBodyBuilder.add("thumbnailUrl", Objects.requireNonNull(binding.cardThumbnailUrl.getText()).toString());
                formBodyBuilder.add("f1", Objects.requireNonNull(binding.cardF1.getText()).toString());
                formBodyBuilder.add("f2", Objects.requireNonNull(binding.cardF2.getText()).toString());
                formBodyBuilder.add("f3", Objects.requireNonNull(binding.cardF3.getText()).toString());
                formBodyBuilder.add("f4", Objects.requireNonNull(binding.cardF4.getText()).toString());
                formBodyBuilder.add("f5", Objects.requireNonNull(binding.cardF5.getText()).toString());
                break;
            case "分享类型卡片(News)":
                endpoint = "/get_card_news";
                if (tagIcon.isEmpty()){
                    tagIcon = "https://api.lyhc.top/bot/a.jpg";
                    binding.cardTagIcon.setText(tagIcon);
                    Toast.makeText(requireActivity(), "小图标不能为空哦... 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                } else{
                    if (Objects.equals(parseURLComponents(tagIcon)[0], "tianquan.gtimg.cn")){
                        tagIcon = "https://api.lyhc.top/bot/a.jpg";
                        binding.cardPreview.setText(tagIcon);
                        Toast.makeText(requireActivity(), "这个预览图的域名不受支持呢.. 已经为你填充默认预览图", Toast.LENGTH_LONG).show();
                    }
                }
                if (preview.isEmpty()){
                    preview = "https://api.lyhc.top/bot/a.jpg";
                    binding.cardPreview.setText(preview);
                    Toast.makeText(requireActivity(), "怎么能没有图片呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                } else{
                    if (Objects.equals(parseURLComponents(preview)[0], "tianquan.gtimg.cn")){
                        tagIcon = "https://api.lyhc.top/bot/a.jpg";
                        binding.cardPreview.setText(tagIcon);
                        Toast.makeText(requireActivity(), "这个预览图的域名不受支持呢.. 已经为你填充默认预览图", Toast.LENGTH_LONG).show();
                    }
                }
                formBodyBuilder.add("desc", Objects.requireNonNull(binding.cardDesc.getText()).toString());
                formBodyBuilder.add("preview", Objects.requireNonNull(binding.cardPreview.getText()).toString());
                formBodyBuilder.add("tag", Objects.requireNonNull(binding.cardTag.getText()).toString());
                formBodyBuilder.add("tagIcon", tagIcon);
                formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                formBodyBuilder.add("uin", uin);
                formBodyBuilder.add("skey", skey);
                formBodyBuilder.add("pSkey", p_skey);
                break;
            case "小程序卡片(Miniapp)":
                endpoint = "/get_miniapp_card";
                if (tagIcon.isEmpty()){
                    tagIcon = "https://api.lyhc.top/bot/a.jpg";
                    binding.cardTagIcon.setText(tagIcon);
                    Toast.makeText(requireActivity(), "小图标不能为空哦... 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                } else{
                    if (Objects.equals(parseURLComponents(tagIcon)[0], "tianquan.gtimg.cn")){
                        tagIcon = "https://api.lyhc.top/bot/a.jpg";
                        binding.cardPreview.setText(tagIcon);
                        Toast.makeText(requireActivity(), "这个预览图的域名不受支持呢.. 已经为你填充默认预览图", Toast.LENGTH_LONG).show();
                    }
                }
                if (preview.isEmpty()){
                    preview = "https://api.lyhc.top/bot/a.jpg";
                    binding.cardPreview.setText(preview);
                    Toast.makeText(requireActivity(), "怎么能没有图片呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                } else{
                    if (Objects.equals(parseURLComponents(preview)[0], "tianquan.gtimg.cn")){
                        tagIcon = "https://api.lyhc.top/bot/a.jpg";
                        binding.cardPreview.setText(tagIcon);
                        Toast.makeText(requireActivity(), "这个预览图的域名不受支持呢.. 已经为你填充默认预览图", Toast.LENGTH_LONG).show();
                    }
                }


                formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                formBodyBuilder.add("jumpUrl", jumpUrl);
                formBodyBuilder.add("preview", Objects.requireNonNull(binding.cardPreview.getText()).toString());
                formBodyBuilder.add("tag", Objects.requireNonNull(binding.cardTag.getText()).toString());
                formBodyBuilder.add("tagIcon", tagIcon);
                formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                formBodyBuilder.add("uin", uin);
                formBodyBuilder.add("skey", skey);
                formBodyBuilder.add("pSkey", p_skey);
                break;
            case "按钮卡片(eventshare.lua)":
                endpoint = "/get_btn_card";
                if (title.isEmpty()){
                    title = "嘿嘿! 这里是标题";
                    binding.cardTitle.setText(title);
                    Toast.makeText(requireActivity(), "你忘记填标题啦！已经为你填好了哦", Toast.LENGTH_LONG).show();
                }
                if (jumpUrl.isEmpty()){
                    jumpUrl = "https://cwuom.love";
                    binding.cardJumpUrl.setText(jumpUrl);
                    Toast.makeText(requireActivity(), "总感觉哪里空空的，原来是因为你没有填跳转链接！已经填好了哦~", Toast.LENGTH_SHORT).show();
                }

                if (preview.isEmpty()){
                    preview = "https://tianquan.gtimg.cn/chatBg/item/53693/newPreview2.png";
                    binding.cardPreview.setText(preview);
                    Toast.makeText(requireActivity(), "怎么能没有图片呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                } else{
                    if (!Objects.equals(parseURLComponents(preview)[0], "tianquan.gtimg.cn")){
                        preview = "https://tianquan.gtimg.cn/chatBg/item/53693/newPreview2.png";
                        binding.cardPreview.setText(preview);
                        Toast.makeText(requireActivity(), "这个预览图的域名不受支持呢.. 你需要在QQ主题商店抓取背景图再填入，已经为你填充默认预览图", Toast.LENGTH_LONG).show();
                    }
                }
                formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                formBodyBuilder.add("title", title);
                formBodyBuilder.add("preview", preview);
                formBodyBuilder.add("jump", jumpUrl);
                formBodyBuilder.add("jumpButton", Objects.requireNonNull(binding.cardJumpButton.getText()).toString());
                formBodyBuilder.add("buttonTitle", Objects.requireNonNull(binding.cardButtonTitle.getText()).toString());
                formBodyBuilder.add("tag", Objects.requireNonNull(binding.cardTag.getText()).toString());
                formBodyBuilder.add("uin", uin);
                formBodyBuilder.add("skey", skey);
                formBodyBuilder.add("pSkey", p_skey);
                break;
        }

        if (!Objects.requireNonNull(endpoint).isEmpty()) {
            Snackbar snackbar = ShowLoadingSnackbar("请求已发送，正在等待服务器响应..", binding.getRoot(), bottomNavigationView);
            ArkAPIReq.sendSignaturePostRequest(endpoint, formBodyBuilder.build(), getActivity(), new ArkApiCallback() {
                @Override
                public void onSuccess(String result) {
                    requireActivity().runOnUiThread(() -> {
                        copyToClipboard(result, requireActivity());
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setTitle("卡片数据已复制到您的剪贴板")
                                .setMessage(result)
                                .setPositiveButton("好", null)
                                .show();

                        snackbar.dismiss();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setTitle("出了点问题！")
                                .setMessage("请求失败: " + e.getMessage())
                                .setPositiveButton("好", null)
                                .show();

                        snackbar.dismiss();
                    });
                }
            });
        }
    }


    private void setCardTypeVisibility(int position) {
        hideAllInputs(); // 先隐藏所有输入
        switch (position) {
            case 0: // 大图ARK卡片
                makeOptional(binding.textFieldCardTitle);
                makeOptional(binding.textFieldCardPrompt);
                makeOptional(binding.textFieldCardSubtitle);
                makeRequired(binding.textFieldCardCover);
                break;
            case 1: // Embed卡片
                makeOptional(binding.textFieldCardTitle);
                makeOptional(binding.textFieldCardPrompt);
                makeOptional(binding.textFieldCardThumbnailUrl);
                makeOptional(binding.textFieldCardF1);
                makeOptional(binding.textFieldCardF2);
                makeOptional(binding.textFieldCardF3);
                makeOptional(binding.textFieldCardF4);
                makeOptional(binding.textFieldCardF5);
                break;
            case 2: // 分享类型卡片 (News)
                makeOptional(binding.textFieldCardDesc);
                makeRequired(binding.textFieldCardPreview);
                makeOptional(binding.textFieldCardTag);
                makeRequired(binding.textFieldCardTagIcon);
                makeOptional(binding.textFieldCardTitle);
                makeOptional(binding.textFieldCardPrompt);
                makeRequired(binding.textFieldUin);
                makeRequired(binding.textFieldSkey);
                makeRequired(binding.textFieldPSkey);
                break;
            case 3: // 小程序卡片 (Miniapp)
                makeOptional(binding.textFieldCardPrompt);
                makeOptional(binding.textFieldCardJumpUrl);
                makeRequired(binding.textFieldCardPreview);
                makeOptional(binding.textFieldCardTag);
                makeRequired(binding.textFieldCardTagIcon);
                makeOptional(binding.textFieldCardTitle);
                makeRequired(binding.textFieldUin);
                makeRequired(binding.textFieldSkey);
                makeRequired(binding.textFieldPSkey);
                break;
            case 4: // 按钮卡片 (eventshare.lua)
                makeRequired(binding.textFieldCardPrompt);
                makeRequired(binding.textFieldCardTitle);
                makeRequired(binding.textFieldCardPreview);
                makeRequired(binding.textFieldCardJumpUrl);
                makeOptional(binding.textFieldCardJumpButton);
                makeOptional(binding.textFieldCardButtonTitle);
                makeOptional(binding.textFieldCardTag);
                makeRequired(binding.textFieldUin);
                makeRequired(binding.textFieldSkey);
                makeRequired(binding.textFieldPSkey);
                break;
        }

        applyAnimationsWithOverlap();
    }


    private void makeRequired(TextInputLayout input) {
        input.setVisibility(View.VISIBLE);
        String originalHint = originalHints.get(input);
        input.setHint("* " + originalHint); // Mark as required

        animateViewHeight(input);
    }

    private void makeOptional(TextInputLayout input) {
        input.setVisibility(View.VISIBLE);
        String originalHint = originalHints.get(input);
        input.setHint(originalHint); // Mark as optional

        animateViewHeight(input);
    }

    private void hideAllInputs() {
        for (Map.Entry<View, String> entry : originalHints.entrySet()) {
            View input = entry.getKey();
            input.setVisibility(View.GONE);
            ((TextInputLayout) input).setHint(entry.getValue());

            ViewGroup.LayoutParams params = input.getLayoutParams();
            params.height = 0;
            input.setLayoutParams(params);
        }
    }


    private void storeOriginalHints() {
        originalHints.put(binding.textFieldCardTitle, Objects.requireNonNull(binding.textFieldCardTitle.getHint()).toString());
        originalHints.put(binding.textFieldCardPrompt, Objects.requireNonNull(binding.textFieldCardPrompt.getHint()).toString());
        originalHints.put(binding.textFieldCardSubtitle, Objects.requireNonNull(binding.textFieldCardSubtitle.getHint()).toString());
        originalHints.put(binding.textFieldCardCover, Objects.requireNonNull(binding.textFieldCardCover.getHint()).toString());
        originalHints.put(binding.textFieldCardDesc, Objects.requireNonNull(binding.textFieldCardDesc.getHint()).toString());
        originalHints.put(binding.textFieldCardPreview, Objects.requireNonNull(binding.textFieldCardPreview.getHint()).toString());
        originalHints.put(binding.textFieldCardTag, Objects.requireNonNull(binding.textFieldCardTag.getHint()).toString());
        originalHints.put(binding.textFieldCardTagIcon, Objects.requireNonNull(binding.textFieldCardTagIcon.getHint()).toString());
        originalHints.put(binding.textFieldCardThumbnailUrl, Objects.requireNonNull(binding.textFieldCardThumbnailUrl.getHint()).toString());
        originalHints.put(binding.textFieldCardJumpUrl, Objects.requireNonNull(binding.textFieldCardJumpUrl.getHint()).toString());
        originalHints.put(binding.textFieldCardJumpButton, Objects.requireNonNull(binding.textFieldCardJumpButton.getHint()).toString());
        originalHints.put(binding.textFieldCardButtonTitle, Objects.requireNonNull(binding.textFieldCardButtonTitle.getHint()).toString());
        originalHints.put(binding.textFieldCardF1, Objects.requireNonNull(binding.textFieldCardF1.getHint()).toString());
        originalHints.put(binding.textFieldCardF2, Objects.requireNonNull(binding.textFieldCardF2.getHint()).toString());
        originalHints.put(binding.textFieldCardF3, Objects.requireNonNull(binding.textFieldCardF3.getHint()).toString());
        originalHints.put(binding.textFieldCardF4, Objects.requireNonNull(binding.textFieldCardF4.getHint()).toString());
        originalHints.put(binding.textFieldCardF5, Objects.requireNonNull(binding.textFieldCardF5.getHint()).toString());
        originalHints.put(binding.textFieldUin, Objects.requireNonNull(binding.textFieldUin.getHint()).toString());
        originalHints.put(binding.textFieldSkey, Objects.requireNonNull(binding.textFieldSkey.getHint()).toString());
        originalHints.put(binding.textFieldPSkey, Objects.requireNonNull(binding.textFieldPSkey.getHint()).toString());
    }
    private void applyAnimationsWithOverlap() {
        long animationDuration = 300;  // 每个动画的持续时间
        long delayIncrement = 90;      // 动画之间的延迟增量
        long initialDelay = 50;        // 第一个动画的初始延迟

        for (TextInputLayout input : getAllInputs()) {
            if (input.getVisibility() == View.VISIBLE) {
                animateInput(input, animationDuration, initialDelay);
                initialDelay += delayIncrement;
            }
        }
    }

    private void animateInput(View input, long duration, long delay) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(input, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(input, "scaleX", 0.85f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(input, "scaleY", 0.85f, 1f);

        fadeIn.setDuration(duration);
        scaleX.setDuration(duration);
        scaleY.setDuration(duration);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, scaleX, scaleY);
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }

    private void animateViewHeight(final View view) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int startHeight = view.getHeight();
        int endHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束后将高度设置为WRAP_CONTENT
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(layoutParams);
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private void setupEditTextListener(TextInputLayout textInputLayout) {
        EditText editText = textInputLayout.getEditText();
        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 触发重新测量和布局
                    textInputLayout.requestLayout();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }



    private void initializeInputVisibility() {
        // 设置所有控件为完全透明并缩放
        for (TextInputLayout input : getAllInputs()) {
            input.setAlpha(0f);
            input.setScaleX(0.85f);
            input.setScaleY(0.85f);
        }
    }

    private List<TextInputLayout> getAllInputs() {
        return Arrays.asList(
                binding.textFieldCardTitle, binding.textFieldCardSubtitle, binding.textFieldCardPrompt,
                binding.textFieldCardCover, binding.textFieldCardDesc, binding.textFieldCardPreview,
                binding.textFieldCardTag, binding.textFieldCardTagIcon, binding.textFieldCardThumbnailUrl,
                binding.textFieldCardJumpUrl, binding.textFieldCardJumpButton, binding.textFieldCardButtonTitle,
                binding.textFieldCardF1, binding.textFieldCardF2, binding.textFieldCardF3,
                binding.textFieldCardF4, binding.textFieldCardF5, binding.textFieldUin,
                binding.textFieldSkey, binding.textFieldPSkey
        );
    }

    void initAdapter(){
        binding.autoCompleteTextView.setText("", false);
        String[] cardTypes = {"图转卡(imagetextbot)", "文转卡(Embed)", "分享类型卡片(News)", "小程序卡片(Miniapp)", "按钮卡片(eventshare.lua)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_dropdown_item, cardTypes);
        binding.autoCompleteTextView.setAdapter(adapter);
    }
}