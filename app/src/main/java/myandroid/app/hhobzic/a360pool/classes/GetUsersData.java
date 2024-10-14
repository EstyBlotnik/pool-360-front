package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetUsersData {
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("fullName")
    @Expose
    private String fullName;
    @SerializedName("profileImage")
    @Expose
    private String profileImage;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("waterLevel")
    @Expose
    private float waterLevel;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("safetyAlert")
    @Expose
    private Boolean safetyAlert;
    @SerializedName("qualityAlert")
    @Expose
    private Boolean qualityAlert;

    public Boolean getSafetyAlert() {
        return safetyAlert;
    }

    public void setSafetyAlert(Boolean safetyAlert) {
        this.safetyAlert = safetyAlert;
    }

    public Boolean getQualityAlert() {
        return qualityAlert;
    }

    public void setQualityAlert(Boolean qualityAlert) {
        this.qualityAlert = qualityAlert;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
