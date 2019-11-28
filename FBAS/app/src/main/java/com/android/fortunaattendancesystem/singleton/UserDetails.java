package com.android.fortunaattendancesystem.singleton;

/**
 * Created by fortuna on 11/4/17.
 */
public class UserDetails {

    private int loginId = -1;
    private String name = "";
    private String role = "";
    private byte[] photo = null;

    private static UserDetails mInstance = null;

    public static UserDetails getInstance() {
        if (mInstance == null) {
            mInstance = new UserDetails();
            mInstance.reset();
        }
        return mInstance;
    }

    public void reset() {
        loginId = -1;
        name = "";
        role = "";
        photo = null;
    }

    public int getLoginId() {
        return loginId;
    }

    public void setLoginId(int loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
