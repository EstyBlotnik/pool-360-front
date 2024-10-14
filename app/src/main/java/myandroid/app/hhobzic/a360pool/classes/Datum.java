package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {
    @SerializedName("confidence")
    @Expose
    private Double confidence;
    @SerializedName("xmax")
    @Expose
    private Double xmax;
    @SerializedName("xmin")
    @Expose
    private Double xmin;
    @SerializedName("ymax")
    @Expose
    private Double ymax;
    @SerializedName("ymin")
    @Expose
    private Double ymin;

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Double getXmax() {
        return xmax;
    }

    public void setXmax(Double xmax) {
        this.xmax = xmax;
    }

    public Double getXmin() {
        return xmin;
    }

    public void setXmin(Double xmin) {
        this.xmin = xmin;
    }

    public Double getYmax() {
        return ymax;
    }

    public void setYmax(Double ymax) {
        this.ymax = ymax;
    }

    public Double getYmin() {
        return ymin;
    }

    public void setYmin(Double ymin) {
        this.ymin = ymin;
    }
}

