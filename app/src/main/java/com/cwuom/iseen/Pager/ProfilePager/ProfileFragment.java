package com.cwuom.iseen.Pager.ProfilePager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.cwuom.iseen.Dao.UserDao;
import com.cwuom.iseen.Entity.EntityUser;
import com.cwuom.iseen.InitDataBase.InitUserDataBase;
import com.cwuom.iseen.LoginActivity;
import com.cwuom.iseen.R;
import com.cwuom.iseen.SettingsActivity;
import com.cwuom.iseen.Util.API.Ark.ArkAPIReq;
import com.cwuom.iseen.Util.API.Bili.BiliBiliAPIReq;
import com.cwuom.iseen.Util.UtilMethod;
import com.cwuom.iseen.View.CircleCropTransform;
import com.cwuom.iseen.databinding.FragmentProfileBinding;
import com.kongzue.dialogx.dialogs.MessageDialog;

import org.json.JSONObject;

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

public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;
    InitUserDataBase initUserDataBase;
    UserDao userDao;
    String userUID = "载入中..";
    String userName = "载入中..";
    String userImageUrl;
    String userRegTime = "0";
    String userSign = "载入中..";
    String userCoins;
    String userBirthday = "0";
    String ark_coins;
    int userFollows;
    String userCookies;
    boolean isLogin;
    boolean profile_img_load_flag = false;
    boolean profile_info_load_flag = false;

    static final int HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN = 0;
    static final int HANDLER_MESSAGE_UPDATE_USERINFO = 1;
    static final int HANDLER_MESSAGE_UPDATE_USERINFO_LOADING = 2;

    private final Runnable updateUserInfoRunnable = new Runnable() {
        @Override
        public void run() {
            fetchAndUpdateUserInfo();
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());


        if (savedInstanceState != null) {
            userName = savedInstanceState.getString("UserName", userName);
            userRegTime = savedInstanceState.getString("UserRegTime", userRegTime);
            userUID = savedInstanceState.getString("UserUID", userUID);
            userSign = savedInstanceState.getString("UserSign", userSign);
            userBirthday = savedInstanceState.getString("UserBirthday", userBirthday);
            ark_coins = savedInstanceState.getString("UserCoins", ark_coins);
            userImageUrl = savedInstanceState.getString("UserImageUrl", userImageUrl);
            profile_img_load_flag = savedInstanceState.getBoolean("ProfileImgLoadFlag", profile_img_load_flag);
            updateProfileViews();
            try {
                updatePermission(Integer.parseInt(ark_coins));
            } catch (NumberFormatException e) {
                binding.tvPermission.setText(getString(R.string.permission_info, "普通用户"));
            }

        }

        binding.btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        binding.btnSetting.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));

        initMethod();

        binding.btnLogout.setOnClickListener(v -> MessageDialog.show("确定登出此账户么？", "登出后，您随时可以重新登录。", "确认登出").setOkButton((baseDialog, v1) -> {
            profile_img_load_flag = false;
            profile_info_load_flag = false;
            userDao.deleteLoginUser();
            binding.profileImg.setImageResource(R.drawable.ghost);
            handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN);
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            return false;
        }).setCancelButton("手滑了", (dialog, v12) -> false));

        return binding.getRoot();
    }

    private void initMethod() {
        initUserDataBase = UtilMethod.getInstance_user(getContext());
        userDao = initUserDataBase.userDao();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        boolean hidden_warning = preferences.getBoolean("hidden_warning", false);
        if (hidden_warning){
            binding.loginWarningCard.setVisibility(View.GONE);
            binding.tips.setVisibility(View.GONE);
        }

        binding.classicsHeader.setEnableLastTime(false);
        binding.refreshLayout.setOnRefreshListener(refreshlayout -> new Thread(() -> {
            // Get user info, and commit them to the database
            try {
                Log.e("UID", userUID);
                String userinfo = Objects.requireNonNull(BiliBiliAPIReq.getCardByMid(userUID));
                JSONObject jsonObject_userinfo = new JSONObject(userinfo).optJSONObject("card");

                userUID = Objects.requireNonNull(jsonObject_userinfo).optString("mid");
                userName = jsonObject_userinfo.optString("name");
                userImageUrl = jsonObject_userinfo.optString("face");
                userRegTime = UtilMethod.timeToFormat(jsonObject_userinfo.optLong("regtime") * 1000L);
                userSign = jsonObject_userinfo.optString("sign");
                userCoins = jsonObject_userinfo.optString("coins");
                userBirthday = jsonObject_userinfo.optString("birthday");
                userFollows = jsonObject_userinfo.optInt("friend");
                ark_coins = Objects.requireNonNull(ArkAPIReq.getArkCoinsByMid(userUID, getActivity()));
                userDao.deleteLoginUser();

                userDao.insertUser(new EntityUser(userUID, userName, userImageUrl, userRegTime, userSign, userCoins, userBirthday, ark_coins, userFollows, userCookies, true));
                refreshlayout.finishRefresh(true);
                profile_img_load_flag = false;
                handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO);
            } catch (Exception e){
                refreshlayout.finishRefresh(false);
            }
        }).start());
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        if (msg.what == HANDLER_MESSAGE_UPDATE_USERINFO) {
           updateUserInfo();
        }
        if (msg.what == HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN) {
            updateUserInfoNotLogin();
        }

        if (msg.what == HANDLER_MESSAGE_UPDATE_USERINFO_LOADING) {
            updateUserInfoLoading();
        }
        return true;
    });

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("UserName", userName);
        outState.putString("UserRegTime", userRegTime);
        outState.putString("UserUID", userUID);
        outState.putString("UserSign", userSign);
        outState.putString("UserBirthday", userBirthday);
        outState.putString("UserCoins", ark_coins);
        outState.putString("UserImageUrl", userImageUrl);
        outState.putBoolean("ProfileImgLoadFlag", profile_img_load_flag);
    }

    private void updateUserInfo() {
        if (isAdded()) {
            binding.tvUsername.setText(getString(R.string.username, userName));
            binding.tvRegtime.setText(getString(R.string.create_time, userRegTime));
            binding.tvUid.setText(getString(R.string.uid, userUID));
            binding.tvSign.setText(getString(R.string.info, userSign));
            binding.tvBirthday.setText(getString(R.string.birthday, userBirthday));
            binding.tvCoins.setText(getString(R.string.coins, ark_coins));
            try {
                updatePermission(Integer.parseInt(ark_coins));
            } catch (NumberFormatException e) {
                binding.tvPermission.setText(getString(R.string.permission_info, "普通用户"));
            }


            if (!profile_img_load_flag) {
                if (isAdded()) {
                    Glide.with(requireActivity())
                            .load(userImageUrl)
                            .transform(new CircleCropTransform(1, Color.WHITE)) // 使用白色边框宽度为2dp的圆形变换
                            .into(binding.profileImg);
                    profile_img_load_flag = true;
                }
            }
        }
    }

    private void updatePermission(int coins){
        if (coins == 0) {
            binding.tvPermission.setText(getString(R.string.permission_info, "普通用户"));
        }
        else if (coins > 0 && coins < 500) {
            binding.tvPermission.setText(getString(R.string.permission_info, "高级用户"));
        }
        else if (coins > 500 && coins < 1000) {
            binding.tvPermission.setText(getString(R.string.permission_info, "白金用户"));
        }
        else if (coins > 1000 && coins < 5000) {
            binding.tvPermission.setText(getString(R.string.permission_info, "至尊用户"));
        }
        else if (coins > 5000 && coins < 1000000) {
            binding.tvPermission.setText(getString(R.string.permission_info, "超级用户"));
        }
        else if (coins > 1000000) {
            binding.tvPermission.setText(getString(R.string.permission_info, "永久用户"));
        }
    }

    private void updateUserInfoNotLogin(){
        if (isAdded()) {
            binding.tvUsername.setText(getString(R.string.username, "用户未登录"));
            binding.tvRegtime.setText(getString(R.string.create_time, "-"));
            binding.tvUid.setText(getString(R.string.uid, "-"));
            binding.tvSign.setText(getString(R.string.info, "-"));
            binding.tvBirthday.setText(getString(R.string.birthday, "-"));
            binding.tvCoins.setText(getString(R.string.coins, "0"));
            binding.tvPermission.setText(getString(R.string.permission_info, "普通用户"));
        }
    }

    private void updateUserInfoLoading(){
        if (isAdded()) {
            binding.tvUsername.setText(getString(R.string.username, userName));
            binding.tvRegtime.setText(getString(R.string.create_time, userRegTime));
            binding.tvUid.setText(getString(R.string.uid, userUID));
            binding.tvSign.setText(getString(R.string.info, userSign));
            binding.tvBirthday.setText(getString(R.string.birthday, userBirthday));
            binding.tvCoins.setText(getString(R.string.coins, "载入中.."));
            binding.tvPermission.setText(getString(R.string.permission_info, "载入中.."));

            if (!profile_img_load_flag) {
                Glide.with(requireActivity())
                        .load(userImageUrl)
                        .transform(new CircleCropTransform(1, Color.WHITE)) // 使用白色边框宽度为2dp的圆形变换
                        .into(binding.profileImg);
                profile_img_load_flag = true;
            }
        }
    }


    private void updateProfileViews() {
        binding.tvUsername.setText(getString(R.string.username, userName));
        binding.tvRegtime.setText(getString(R.string.create_time, userRegTime));
        binding.tvUid.setText(getString(R.string.uid, userUID));
        binding.tvSign.setText(getString(R.string.info, userSign));
        binding.tvBirthday.setText(getString(R.string.birthday, userBirthday));
        binding.tvCoins.setText(getString(R.string.coins, ark_coins));

        if (profile_img_load_flag) {
            Glide.with(requireActivity())
                    .load(userImageUrl)
                    .transform(new CircleCropTransform(1, Color.WHITE)) // 使用白色边框宽度为2dp的圆形变换
                    .into(binding.profileImg);
        }
    }

    private void fetchAndUpdateUserInfo() {
        EntityUser entityUser = userDao.getUserByUserIsLogin(true);
        if (entityUser != null) {
            updateUserInfoFromEntity(entityUser);
            if (!profile_info_load_flag) {
                new Thread(() -> {
                    try {
                        String arkCoins = ArkAPIReq.getArkCoinsByMid(userUID, getActivity());
                        requireActivity().runOnUiThread(() -> {
                            ark_coins = arkCoins;
                            profile_info_load_flag = true;
                            handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO);
                        });
                    } catch (Exception ignored) {}
                }).start();
            } else {
                handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO);
            }
        } else {
            handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN);
        }
    }

    private void updateUserInfoFromEntity(EntityUser user) {
        userUID = user.getUserUID();
        userName = user.getUserName();
        userImageUrl = user.getUserImageUrl();
        userRegTime = user.getUserRegTime();
        userSign = user.getUserSign();
        userCoins = user.getUserCoins();
        userBirthday = user.getUserBirthday();
        userFollows = user.getUserFollows();
        userCookies = user.getUserCookies();
        isLogin = user.isLoginUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(updateUserInfoRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateUserInfoRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}