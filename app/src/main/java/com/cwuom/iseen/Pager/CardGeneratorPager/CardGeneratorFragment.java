package com.cwuom.iseen.Pager.CardGeneratorPager;

import static android.content.Context.MODE_PRIVATE;
import static com.cwuom.iseen.Util.UtilMethod.copyToClipboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cwuom.iseen.QzoneActivity;
import com.cwuom.iseen.R;
import com.cwuom.iseen.Util.UtilMethod;
import com.cwuom.iseen.databinding.FragmentCardGeneratorBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Objects;

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

public class CardGeneratorFragment extends Fragment {
    FragmentCardGeneratorBinding binding;

    private String uin = "";
    private String p_skey = "";
    private String skey = "";
    private final String BASE_URL = "https://ark.cwuom.love";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCardGeneratorBinding.inflate(getLayoutInflater());
        initMethod();

        return binding.getRoot();
    }

    private void initMethod(){
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        hideAllInputs();
        String[] cardTypes = {"图转卡(imagetextbot)", "文转卡(Embed)", "分享类型卡片(News)", "小程序卡片(Miniapp)", "按钮卡片(eventshare.lua)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_dropdown_item, cardTypes);
        binding.autoCompleteTextView.setAdapter(adapter);

        binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            binding.btnGenerate.setEnabled(true);
            binding.btnGenerate.setBackgroundColor(Color.parseColor("#474F7A"));
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
            }



            switch (cardType) {
                case "图转卡(imagetextbot)":
                    endpoint = "/get_card";
                    formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                    formBodyBuilder.add("subtitle", Objects.requireNonNull(binding.cardSubtitle.getText()).toString());
                    formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                    formBodyBuilder.add("cover", Objects.requireNonNull(binding.cardCover.getText()).toString());
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
                    formBodyBuilder.add("desc", Objects.requireNonNull(binding.cardDesc.getText()).toString());
                    formBodyBuilder.add("preview", Objects.requireNonNull(binding.cardPreview.getText()).toString());
                    formBodyBuilder.add("tag", Objects.requireNonNull(binding.cardTag.getText()).toString());
                    formBodyBuilder.add("tagIcon", Objects.requireNonNull(binding.cardTagIcon.getText()).toString());
                    formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                    formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                    formBodyBuilder.add("uin", uin);
                    formBodyBuilder.add("skey", skey);
                    formBodyBuilder.add("pSkey", p_skey);
                    break;
                case "小程序卡片(Miniapp)":
                    endpoint = "/get_miniapp_card";
                    formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                    formBodyBuilder.add("jumpUrl", Objects.requireNonNull(binding.cardJumpUrl.getText()).toString());
                    formBodyBuilder.add("preview", Objects.requireNonNull(binding.cardPreview.getText()).toString());
                    formBodyBuilder.add("tag", Objects.requireNonNull(binding.cardTag.getText()).toString());
                    formBodyBuilder.add("tagIcon", Objects.requireNonNull(binding.cardTagIcon.getText()).toString());
                    formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                    formBodyBuilder.add("uin", uin);
                    formBodyBuilder.add("skey", skey);
                    formBodyBuilder.add("pSkey", p_skey);
                    break;
                case "按钮卡片(eventshare.lua)":
                    endpoint = "/get_btn_card";
                    formBodyBuilder.add("prompt", Objects.requireNonNull(binding.cardPrompt.getText()).toString());
                    formBodyBuilder.add("title", Objects.requireNonNull(binding.cardTitle.getText()).toString());
                    formBodyBuilder.add("preview", Objects.requireNonNull(binding.cardPreview.getText()).toString());
                    formBodyBuilder.add("jump", Objects.requireNonNull(binding.cardJumpUrl.getText()).toString());
                    formBodyBuilder.add("jumpButton", Objects.requireNonNull(binding.cardJumpButton.getText()).toString());
                    formBodyBuilder.add("buttonTitle", Objects.requireNonNull(binding.cardButtonTitle.getText()).toString());
                    formBodyBuilder.add("tag", Objects.requireNonNull(binding.cardTag.getText()).toString());
                    formBodyBuilder.add("uin", uin);
                    formBodyBuilder.add("skey", skey);
                    formBodyBuilder.add("pSkey", p_skey);
                    break;
            }

            if (!Objects.requireNonNull(endpoint).isEmpty()) {
                sendPostRequest(BASE_URL + endpoint, formBodyBuilder.build());
            }
        });

        binding.btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), QzoneActivity.class)));
    }


    private void setCardTypeVisibility(int position) {
        // 先设定所有输入框为不可见
        hideAllInputs();

        // 根据选择的卡片类型显示对应的输入框
        switch (position) {
            case 0: // 大图ARK卡片
                binding.textFieldCardTitle.setVisibility(View.VISIBLE);
                binding.textFieldCardPrompt.setVisibility(View.VISIBLE);
                binding.textFieldCardSubtitle.setVisibility(View.VISIBLE);
                binding.textFieldCardCover.setVisibility(View.VISIBLE);
                break;
            case 1: // Embed卡片
                binding.textFieldCardTitle.setVisibility(View.VISIBLE);
                binding.textFieldCardPrompt.setVisibility(View.VISIBLE);
                binding.textFieldCardThumbnailUrl.setVisibility(View.VISIBLE);
                binding.textFieldCardF1.setVisibility(View.VISIBLE);
                binding.textFieldCardF2.setVisibility(View.VISIBLE);
                binding.textFieldCardF3.setVisibility(View.VISIBLE);
                binding.textFieldCardF4.setVisibility(View.VISIBLE);
                binding.textFieldCardF5.setVisibility(View.VISIBLE);
                break;
            case 2: // 分享类型ARK卡片
                binding.textFieldCardDesc.setVisibility(View.VISIBLE);
                binding.textFieldCardPreview.setVisibility(View.VISIBLE);
                binding.textFieldCardTag.setVisibility(View.VISIBLE);
                binding.textFieldCardTagIcon.setVisibility(View.VISIBLE);
                binding.textFieldCardTitle.setVisibility(View.VISIBLE);
                binding.textFieldCardPrompt.setVisibility(View.VISIBLE);
                binding.textFieldUin.setVisibility(View.VISIBLE);
                binding.textFieldSkey.setVisibility(View.VISIBLE);
                binding.textFieldPSkey.setVisibility(View.VISIBLE);
                break;
            case 3: // 小程序ARK卡片
                binding.textFieldCardPrompt.setVisibility(View.VISIBLE);
                binding.textFieldCardJumpUrl.setVisibility(View.VISIBLE);
                binding.textFieldCardPreview.setVisibility(View.VISIBLE);
                binding.textFieldCardTag.setVisibility(View.VISIBLE);
                binding.textFieldCardTagIcon.setVisibility(View.VISIBLE);
                binding.textFieldCardTitle.setVisibility(View.VISIBLE);
                binding.textFieldUin.setVisibility(View.VISIBLE);
                binding.textFieldSkey.setVisibility(View.VISIBLE);
                binding.textFieldPSkey.setVisibility(View.VISIBLE);
                break;
            case 4: // 按钮卡片
                binding.textFieldCardPrompt.setVisibility(View.VISIBLE);
                binding.textFieldCardTitle.setVisibility(View.VISIBLE);
                binding.textFieldCardPreview.setVisibility(View.VISIBLE);
                binding.textFieldCardJumpButton.setVisibility(View.VISIBLE);
                binding.textFieldCardButtonTitle.setVisibility(View.VISIBLE);
                binding.textFieldCardTag.setVisibility(View.VISIBLE);
                binding.textFieldCardJumpUrl.setVisibility(View.VISIBLE);
                binding.textFieldUin.setVisibility(View.VISIBLE);
                binding.textFieldSkey.setVisibility(View.VISIBLE);
                binding.textFieldPSkey.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void sendPostRequest(String url, RequestBody body) {
        Snackbar snackbar = UtilMethod.ShowLoadingSnackbar("正在提交数据...", requireActivity().getCurrentFocus());

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("出了点问题！")
                            .setMessage("请求失败: " + e.getMessage())
                            .setPositiveButton("好", null)
                            .show();

                    snackbar.dismiss();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                String responseData = Objects.requireNonNull(response.body()).string();
                requireActivity().runOnUiThread(() -> {
                    copyToClipboard(responseData, requireActivity());
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("卡片数据已复制到您的剪贴板")
                            .setMessage(responseData)
                            .setPositiveButton("好", null)
                            .show();

                    snackbar.dismiss();
                });
            }
        });
    }

    private void hideAllInputs() {
        // Set visibility of all TextInputLayouts to gone
        binding.textFieldCardTitle.setVisibility(View.GONE);
        binding.textFieldCardSubtitle.setVisibility(View.GONE);
        binding.textFieldCardPrompt.setVisibility(View.GONE);
        binding.textFieldCardCover.setVisibility(View.GONE);
        binding.textFieldCardDesc.setVisibility(View.GONE);
        binding.textFieldCardPreview.setVisibility(View.GONE);
        binding.textFieldCardTag.setVisibility(View.GONE);
        binding.textFieldCardTagIcon.setVisibility(View.GONE);
        binding.textFieldCardThumbnailUrl.setVisibility(View.GONE);
        binding.textFieldCardJumpUrl.setVisibility(View.GONE);
        binding.textFieldCardJumpButton.setVisibility(View.GONE);
        binding.textFieldCardButtonTitle.setVisibility(View.GONE);
        binding.textFieldUin.setVisibility(View.GONE);
        binding.textFieldSkey.setVisibility(View.GONE);
        binding.textFieldPSkey.setVisibility(View.GONE);
        binding.textFieldCardF1.setVisibility(View.GONE);
        binding.textFieldCardF2.setVisibility(View.GONE);
        binding.textFieldCardF3.setVisibility(View.GONE);
        binding.textFieldCardF4.setVisibility(View.GONE);
        binding.textFieldCardF5.setVisibility(View.GONE);
    }
}