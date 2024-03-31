package com.cwuom.iseen.InitDataBase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cwuom.iseen.Dao.UserDao;
import com.cwuom.iseen.Entity.EntityUser;

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


@Database(entities = {EntityUser.class}, version = 1, exportSchema = false)
public abstract class InitUserDataBase extends RoomDatabase {
    public abstract UserDao userDao();
}
