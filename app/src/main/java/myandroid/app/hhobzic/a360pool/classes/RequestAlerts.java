package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestAlerts {
    @SerializedName("email")
    @Expose
    private String uid;
    @SerializedName("safetyAlert")
    @Expose
    private Boolean safetyAlert;
    @SerializedName("qualityAlert")
    @Expose
    private Boolean qualityAlert;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

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

}
