package com.cwuom.iseen.Entity;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "t_cards")
public class EntityCard {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    long cardID;

    @ColumnInfo(name = "card_title")
    String cardTitle;
    @ColumnInfo(name = "card_subtitle")
    String cardSubtitle;
    @ColumnInfo(name = "card_url")
    String cardUrl;
    @ColumnInfo(name = "card_data")
    String cardData;
    @ColumnInfo(name = "card_listener_url")
    String cardListenerUrl;
    @ColumnInfo(name = "card_create_time")
    String cardCreateTime;
    @ColumnInfo(name = "card_note")
    String cardNote;

    public EntityCard(String cardTitle, String cardSubtitle, String cardUrl, String cardData, String cardListenerUrl, String cardCreateTime, String cardNote) {
        this.cardTitle = cardTitle;
        this.cardSubtitle = cardSubtitle;
        this.cardUrl = cardUrl;
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

    public String getCardUrl() {
        return cardUrl;
    }

    public void setCardUrl(String cardUrl) {
        this.cardUrl = cardUrl;
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
