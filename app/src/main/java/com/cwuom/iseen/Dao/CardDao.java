package com.cwuom.iseen.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cwuom.iseen.Entity.EntityCard;

import java.util.List;

@Dao
public interface CardDao {
    @Insert
    void insertCard(EntityCard entityCard);

    @Query("SELECT * FROM t_cards")
    List<EntityCard> getAllCard();

    @Query("SELECT * FROM t_cards WHERE card_id = :cardID")
    EntityCard getCardByID(long cardID);

    @Query("DELETE FROM t_cards")
    void deleteAll();

    @Update
    void updateCard(EntityCard card);

    @Delete
    void deleteCard(EntityCard card);
    @Query("DELETE FROM t_cards WHERE card_id = :id")
    void deleteCardById(long id);
}
