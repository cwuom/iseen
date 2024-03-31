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
 * 数据库实体类
 * ----------------------
 * @author cwuom
 * 下列源码支持变动后重新打包，在变动不大的情况下，请尽量保留作者的信息！
 * 请勿用于商业用途和非法用途。
 * ----------------------
 * */

@Entity(tableName = "t_cards")
public class EntityCard {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    long cardID;

    @ColumnInfo(name = "card_title")
    String cardTitle;
    @ColumnInfo(name = "card_subtitle")
    String cardSubtitle;
    @ColumnInfo(name = "card_filename")
    String cardFilename;
    @ColumnInfo(name = "card_data")
    String cardData;
    @ColumnInfo(name = "card_listener_url")
    String cardListenerUrl;
    @ColumnInfo(name = "card_create_time")
    String cardCreateTime;
    @ColumnInfo(name = "card_note")
    String cardNote;

    public EntityCard(String cardTitle, String cardSubtitle, String cardFilename, String cardData, String cardListenerUrl, String cardCreateTime, String cardNote) {
        this.cardTitle = cardTitle;
        this.cardSubtitle = cardSubtitle;
        this.cardFilename = cardFilename;
        this.cardData = cardData;
        this.cardListenerUrl = cardListenerUrl;
        this.cardCreateTime = cardCreateTime;
        this.cardNote = cardNote;
    }

    public long getCardID() {
        return cardID;
    }

    public void setCardID(long cardID) {
        this.cardID = cardID;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getCardSubtitle() {
        return cardSubtitle;
    }

    public void setCardSubtitle(String cardSubtitle) {
        this.cardSubtitle = cardSubtitle;
    }

    public String getCardFilename() {
        return cardFilename;
    }

    public void setCardFilename(String cardFilename) {
        this.cardFilename = cardFilename;
    }

    public String getCardData() {
        return cardData;
    }

    public void setCardData(String cardData) {
        this.cardData = cardData;
    }

    public String getCardListenerUrl() {
        return cardListenerUrl;
    }

    public void setCardListenerUrl(String cardListenerUrl) {
        this.cardListenerUrl = cardListenerUrl;
    }

    public String getCardCreateTime() {
        return cardCreateTime;
    }

    public void setCardCreateTime(String cardCreateTime) {
        this.cardCreateTime = cardCreateTime;
    }

    public String getCardNote() {
        return cardNote;
    }

    public void setCardNote(String cardNote) {
        this.cardNote = cardNote;
    }
}
