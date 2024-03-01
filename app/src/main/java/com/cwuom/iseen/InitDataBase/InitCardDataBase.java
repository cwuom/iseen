package com.cwuom.iseen.InitDataBase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cwuom.iseen.Dao.CardDao;
import com.cwuom.iseen.Entity.EntityCard;

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
 * 数据库初始化
 * ----------------------
 * @author cwuom
 * 下列源码支持变动后重新打包，在变动不大的情况下，请尽量保留作者的信息！
 * 请勿用于商业用途和非法用途。
 * ----------------------
 * */

@Database(entities = {EntityCard.class}, version = 1, exportSchema = false)
public abstract class InitCardDataBase extends RoomDatabase {
    public abstract CardDao cardDao();
}
