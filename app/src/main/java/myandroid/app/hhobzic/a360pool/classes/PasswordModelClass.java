package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PasswordModelClass {

    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("new_password")
    @Expose
    private String newPassword;
    @SerializedName("message")
    @Expose
    private String message;

    public PasswordModelClass(String uid, String newPassword) {
        this.uid = uid;
        this.newPassword = newPassword;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
