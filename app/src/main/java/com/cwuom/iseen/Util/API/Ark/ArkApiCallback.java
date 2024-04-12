package com.cwuom.iseen.Util.API.Ark;

public interface ArkApiCallback {
    void onSuccess(String result);
    void onFailure(Exception e);
}