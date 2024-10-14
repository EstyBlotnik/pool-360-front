package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestWaterLevel {

    @SerializedName("waterLevel")
    @Expose
    private String waterLevel;
    @SerializedName("email")
    @Expose
    private String uid;

    public RequestWaterLevel(String waterLevel, String uid) {
        this.waterLevel = waterLevel;
        this.uid = uid;
    }

    public String getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(String waterLevel) {
        this.waterLevel = waterLevel;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
