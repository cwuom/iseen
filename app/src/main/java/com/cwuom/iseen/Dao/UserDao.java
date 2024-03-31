package com.cwuom.iseen.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cwuom.iseen.Entity.EntityUser;

import java.util.List;

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
 * 数据库操作接口(User)
 * ----------------------
 * @author cwuom
 * 下列源码支持变动后重新打包，在变动不大的情况下，请尽量保留作者的信息！
 * 请勿用于商业用途和非法用途。
 * ----------------------
 * */

@Dao
public interface UserDao {
    @Insert
    void insertUser(EntityUser entityUser);

    @Query("SELECT * FROM t_user")
    List<EntityUser> getAllUser();

    @Query("SELECT * FROM t_user WHERE user_uid = :userUID")
    EntityUser getUserByUserUID(long userUID);

    @Query("SELECT * FROM t_user WHERE is_login_user = :isLoginUser")
    EntityUser getUserByUserIsLogin(boolean isLoginUser);

    @Query("DELETE FROM t_user")
    void deleteAll();

    @Update
    void updateUser(EntityUser user);

    @Delete
    void deleteUser(EntityUser user);

    @Query("DELETE FROM t_user WHERE is_login_user = 1")
    void deleteLoginUser();
}
