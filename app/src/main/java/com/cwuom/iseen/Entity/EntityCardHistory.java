package com.cwuom.iseen.Entity;

public class EntityCardHistory {
    long historyCardId;
    String cardHeadTitle;
    String cardHeadSubtitle;
    String cardContentTitle;
    String cardContentCallback;
    String cardCreateTime;
    String cardNote;


    public EntityCardHistory() {
    }

    public EntityCardHistory(String cardHeadTitle, String cardHeadSubtitle, String cardContentTitle, String cardContentCallback, String cardCreateTime, String cardNote) {
        this.cardHeadTitle = cardHeadTitle;
        this.cardHeadSubtitle = cardHeadSubtitle;
        this.cardContentTitle = cardContentTitle;
        this.cardContentCallback = cardContentCallback;
        this.cardCreateTime = cardCreateTime;
        this.cardNote = cardNote;
    }

    public long getHistoryCardId() {
        return historyCardId;
    }

    public void setHistoryCardId(long historyCardId) {
        this.historyCardId = historyCardId;
    }

    public String getCardHeadTitle() {
        return cardHeadTitle;
    }

    public void setCardHeadTitle(String cardHeadTitle) {
        this.cardHeadTitle = cardHeadTitle;
    }

    public String getCardHeadSubtitle() {
        return cardHeadSubtitle;
    }

    public void setCardHeadSubtitle(String cardHeadSubtitle) {
        this.cardHeadSubtitle = cardHeadSubtitle;
    }

    public String getCardContentTitle() {
        return cardContentTitle;
    }

    public void setCardContentTitle(String cardContentTitle) {
        this.cardContentTitle = cardContentTitle;
    }

    public String getCardContentCallback() {
        return cardContentCallback;
    }

    public void setCardContentCallback(String cardContentCallback) {
        this.cardContentCallback = cardContentCallback;
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

    @Override
    public String toString() {
        return "EntityCardHistory{" +
                "historyCardId=" + historyCardId +
                ", cardHeadTitle='" + cardHeadTitle + '\'' +
                ", cardHeadSubtitle='" + cardHeadSubtitle + '\'' +
                ", cardContentTitle='" + cardContentTitle + '\'' +
                ", cardContentCallback='" + cardContentCallback + '\'' +
                ", cardCreateTime='" + cardCreateTime + '\'' +
                ", cardNote='" + cardNote + '\'' +
                '}';
    }
}
