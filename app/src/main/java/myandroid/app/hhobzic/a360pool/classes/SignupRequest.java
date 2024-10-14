package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignupRequest {
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("fullName")
    @Expose
    private String fullName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("profileImage")
    @Expose
    private String image;
    @SerializedName("waterLevel")
    @Expose
    private float waterLevel;
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

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    public SignupRequest(String userName, String fullName, String email, String password, String image, float waterLevel, Boolean safetyAlert, Boolean qualityAlert) {
        this.userName = userName;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.image = image;
        this.waterLevel = waterLevel;
        this.safetyAlert = safetyAlert;
        this.qualityAlert = qualityAlert;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
