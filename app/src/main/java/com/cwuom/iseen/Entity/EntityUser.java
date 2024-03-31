package com.cwuom.iseen.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

@Entity(tableName = "t_user")
public class EntityUser {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    long userID;

    @ColumnInfo(name = "user_uid")
    String userUID;
    @ColumnInfo(name = "user_name")
    String userName;
    @ColumnInfo(name = "user_image_url")
    String userImageUrl;
    @ColumnInfo(name = "user_reg_time")
    String userRegTime;
    @ColumnInfo(name = "user_sign")
    String userSign;
    @ColumnInfo(name = "user_coins")
    String userCoins;
    @ColumnInfo(name = "user_birthday")
    String userBirthday;
    @ColumnInfo(name = "user_ark_coins")
    String userArkCoins;
    @ColumnInfo(name = "user_follows")
    int userFollows;
    @ColumnInfo(name = "user_cookies")
    String userCookies;
    @ColumnInfo(name = "is_login_user")
    boolean isLoginUser;

    public EntityUser(String userUID, String userName, String userImageUrl, String userRegTime,
                      String userSign, String userCoins, String userBirthday, String userArkCoins,
                      int userFollows, String userCookies, boolean isLoginUser) {
        this.userUID = userUID;
        this.userName = userName;
        this.userImageUrl = userImageUrl;
        this.userRegTime = userRegTime;
        this.userSign = userSign;
        this.userCoins = userCoins;
        this.userBirthday = userBirthday;
        this.userArkCoins = userArkCoins;
        this.userFollows = userFollows;
        this.userCookies = userCookies;
        this.isLoginUser = isLoginUser;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserRegTime() {
        return userRegTime;
    }

    public void setUserRegTime(String userRegTime) {
        this.userRegTime = userRegTime;
    }

    public String getUserSign() {
        return userSign;
    }

    public void setUserSign(String userSign) {
        this.userSign = userSign;
    }

    public String getUserCoins() {
        return userCoins;
    }

    public void setUserCoins(String userCoins) {
        this.userCoins = userCoins;
    }

    public String getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(String userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getUserArkCoins() {
        return userArkCoins;
    }

    public void setUserArkCoins(String userArkCoins) {
        this.userArkCoins = userArkCoins;
    }

    public int getUserFollows() {
        return userFollows;
    }

    public void setUserFollows(int userFollows) {
        this.userFollows = userFollows;
    }

    public String getUserCookies() {
        return userCookies;
    }

    public void setUserCookies(String userCookies) {
        this.userCookies = userCookies;
    }

    public boolean isLoginUser() {
        return isLoginUser;
    }

    public void setLoginUser(boolean loginUser) {
        isLoginUser = loginUser;
    }

    @Override
    public String toString() {
        return "EntityUser{" +
                "userID=" + userID +
                ", userUID=" + userUID +
                ", userName='" + userName + '\'' +
                ", userImageUrl='" + userImageUrl + '\'' +
                ", userRegTime='" + userRegTime + '\'' +
                ", userSign='" + userSign + '\'' +
                ", userCoins='" + userCoins + '\'' +
                ", userBirthday=" + userBirthday +
                ", userArkCoins=" + userArkCoins +
                ", userFollows=" + userFollows +
                ", userCookies='" + userCookies + '\'' +
                ", isLoginUser=" + isLoginUser +
                '}';
    }
}


