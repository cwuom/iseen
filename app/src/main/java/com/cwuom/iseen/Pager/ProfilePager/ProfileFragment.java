package com.cwuom.iseen.Pager.ProfilePager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import com.bumptech.glide.Glide;
import com.cwuom.iseen.Dao.UserDao;
import com.cwuom.iseen.Entity.EntityUser;
import com.cwuom.iseen.InitDataBase.InitUserDataBase;
import com.cwuom.iseen.LoginActivity;
import com.cwuom.iseen.R;
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
    boolean profile_img_load_flag;
    boolean profile_info_load_flag = false;

    static final int HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN = 0;
    static final int HANDLER_MESSAGE_UPDATE_USERINFO = 1;
    static final int HANDLER_MESSAGE_UPDATE_USERINFO_LOADING = 2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(getLayoutInflater());

        binding.btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));

        initMethod();

        binding.btnLogout.setOnClickListener(v -> MessageDialog.show("确定登出此账户么？", "登出后，您随时可以重新登录。", "确认登出").setOkButton((baseDialog, v1) -> {
            userDao.deleteLoginUser();
            binding.profileImg.setImageResource(R.drawable.ghost);
            handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN);
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            return false;
        }).setCancelButton("手滑了", (dialog, v12) -> false));

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void initMethod() {
        initUserDataBase = UtilMethod.getInstance_user(getContext());
        userDao = initUserDataBase.userDao();

        new Thread(() -> {
            while (true){
                EntityUser entityUser = userDao.getUserByUserIsLogin(true);
                if (entityUser != null){
                    userUID = entityUser.getUserUID();
                    userName = entityUser.getUserName();
                    userImageUrl = entityUser.getUserImageUrl();
                    userRegTime = entityUser.getUserRegTime();
                    userSign = entityUser.getUserSign();
                    userCoins = entityUser.getUserCoins();
                    userBirthday = entityUser.getUserBirthday();
                    userFollows = entityUser.getUserFollows();
                    userCookies = entityUser.getUserCookies();
                    isLogin = entityUser.isLoginUser();
                    handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO_LOADING);
                    if (!profile_info_load_flag){
                        ark_coins = Objects.requireNonNull(ArkAPIReq.getArkCoinsByMid(userUID));
                        profile_info_load_flag = true;
                    }

                    handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO);
                }else {
                    handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

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
                ark_coins = Objects.requireNonNull(ArkAPIReq.getArkCoinsByMid(userUID));
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

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == HANDLER_MESSAGE_UPDATE_USERINFO) {
                binding.tvUsername.setText(getString(R.string.username, userName));
                binding.tvRegtime.setText(getString(R.string.create_time, userRegTime));
                binding.tvUid.setText(getString(R.string.uid, userUID));
                binding.tvSign.setText(getString(R.string.info, userSign));
                binding.tvBirthday.setText(getString(R.string.birthday, userBirthday));
                binding.tvCoins.setText(getString(R.string.coins, ark_coins));
                binding.tvPermission.setText(getString(R.string.permission_info, "普通用户"));
                if (Integer.parseInt(ark_coins) > 0){
                    binding.tvPermission.setText(getString(R.string.permission_info, "高级用户"));
                }

                if (!profile_img_load_flag){
                    Glide.with(requireActivity())
                            .load(userImageUrl)
                            .transform(new CircleCropTransform(1, Color.WHITE)) // 使用白色边框宽度为2dp的圆形变换
                            .into(binding.profileImg);
                    profile_img_load_flag = true;
                }

            }
            if (msg.what == HANDLER_MESSAGE_UPDATE_USERINFO_NOTLOGIN) {
                binding.tvUsername.setText(getString(R.string.username, "用户未登录"));
                binding.tvRegtime.setText(getString(R.string.create_time, "-"));
                binding.tvUid.setText(getString(R.string.uid, "-"));
                binding.tvSign.setText(getString(R.string.info, "-"));
                binding.tvBirthday.setText(getString(R.string.birthday, "-"));
                binding.tvCoins.setText(getString(R.string.coins, "0"));
                binding.tvPermission.setText(getString(R.string.permission_info, "普通用户"));
            }

            if (msg.what == HANDLER_MESSAGE_UPDATE_USERINFO_LOADING) {
                binding.tvUsername.setText(getString(R.string.username, userName));
                binding.tvRegtime.setText(getString(R.string.create_time, userRegTime));
                binding.tvUid.setText(getString(R.string.uid, userUID));
                binding.tvSign.setText(getString(R.string.info, userSign));
                binding.tvBirthday.setText(getString(R.string.birthday, userBirthday));
                binding.tvCoins.setText(getString(R.string.coins, "载入中.."));
                binding.tvPermission.setText(getString(R.string.permission_info, "载入中.."));

                if (!profile_img_load_flag){
                    Glide.with(requireActivity())
                            .load(userImageUrl)
                            .transform(new CircleCropTransform(1, Color.WHITE)) // 使用白色边框宽度为2dp的圆形变换
                            .into(binding.profileImg);
                    profile_img_load_flag = true;
                }

            }
            return true;
        }
    });




}