package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 18/1/19.
 */

public class WiegandSettingsInfo {

    private String isHexToDecEnabled;
    private String isSiteCodeEnabled;
    private String cardNoType;

    public String getIsHexToDecEnabled() {
        return isHexToDecEnabled;
    }

    public void setIsHexToDecEnabled(String isHexToDecEnabled) {
        this.isHexToDecEnabled = isHexToDecEnabled;
    }

    public String getIsSiteCodeEnabled() {
        return isSiteCodeEnabled;
    }

    public void setIsSiteCodeEnabled(String isSiteCodeEnabled) {
        this.isSiteCodeEnabled = isSiteCodeEnabled;
    }

    public String getCardNoType() {
        return cardNoType;
    }

    public void setCardNoType(String cardNoType) {
        this.cardNoType = cardNoType;
    }
}
