package com.cwuom.iseen.InitDataBase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cwuom.iseen.Dao.CardDao;
import com.cwuom.iseen.Entity.EntityCard;


@Database(entities = {EntityCard.class}, version = 1, exportSchema = false)
public abstract class InitCardDataBase extends RoomDatabase {
    public abstract CardDao cardDao();
}
