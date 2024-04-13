package com.cwuom.iseen.Pager.CardGeneratorPager;

import static android.content.Context.MODE_PRIVATE;
import static com.cwuom.iseen.Util.UtilMethod.ShowLoadingSnackbar;
import static com.cwuom.iseen.Util.UtilMethod.ShowSnackbar;
import static com.cwuom.iseen.Util.UtilMethod.copyToClipboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.cwuom.iseen.NavigationActivity;
import com.cwuom.iseen.QZoneActivity;
import com.cwuom.iseen.R;
import com.cwuom.iseen.Util.API.Ark.ArkAPIReq;
import com.cwuom.iseen.Util.API.Ark.ArkApiCallback;
import com.cwuom.iseen.databinding.FragmentCardGeneratorBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
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

        return binding.getRoot();
    }

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
        String[] cardTypes = {"图转卡(imagetextbot)", "文转卡(Embed)", "分享类型卡片(News)", "小程序卡片(Miniapp)", "按钮卡片(eventshare.lua)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_dropdown_item, cardTypes);
        binding.autoCompleteTextView.setAdapter(adapter);

        binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            binding.btnGenerate.setEnabled(true);
            binding.viewCut.setVisibility(View.VISIBLE);
            setCardTypeVisibility(position);
        });

        binding.btnGenerate.setOnClickListener(v -> {
            String cardType = binding.autoCompleteTextView.getText().toString();

            String endpoint = "";
            FormBody.Builder formBodyBuilder = new FormBody.Builder();

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
                    }
                    if (preview.isEmpty()){
                        preview = "https://api.lyhc.top/bot/a.jpg";
                        binding.cardPreview.setText(preview);
                        Toast.makeText(requireActivity(), "怎么能没有图片呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
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
                    }
                    if (preview.isEmpty()){
                        preview = "https://api.lyhc.top/bot/a.jpg";
                        binding.cardPreview.setText(preview);
                        Toast.makeText(requireActivity(), "怎么能没有图片呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
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
                    if (tagIcon.isEmpty()){
                        tagIcon = "https://tianquan.gtimg.cn/chatBg/item/53693/newPreview2.png";
                        binding.cardTagIcon.setText(tagIcon);
                        Toast.makeText(requireActivity(), "小图标不能为空哦... 已经填充为默认啦！", Toast.LENGTH_LONG).show();
                    }
                    if (preview.isEmpty()){
                        preview = "https://tianquan.gtimg.cn/chatBg/item/53693/newPreview2.png";
                        binding.cardPreview.setText(preview);
                        Toast.makeText(requireActivity(), "怎么能没有图片呀.. 已经填充为默认啦！", Toast.LENGTH_LONG).show();
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
    }


    private void makeRequired(TextInputLayout input) {
        input.setVisibility(View.VISIBLE);
        String originalHint = originalHints.get(input);
        input.setHint("* " + originalHint); // Mark as required
    }

    private void makeOptional(TextInputLayout input) {
        input.setVisibility(View.VISIBLE);
        String originalHint = originalHints.get(input);
        input.setHint(originalHint); // Mark as optional
    }
    private void hideAllInputs() {
        for (Map.Entry<View, String> entry : originalHints.entrySet()) {
            View input = entry.getKey();
            input.setVisibility(View.GONE);
            ((TextInputLayout) input).setHint(entry.getValue());
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
}