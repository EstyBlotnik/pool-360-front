package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SensorData {
    @SerializedName("pH_value")
    @Expose
    private Double pHValue;
    @SerializedName("temperature")
    @Expose
    private Double temperature;
    @SerializedName("turbidity")
    @Expose
    private Double turbidity;
    @SerializedName("water_level")
    @Expose
    private Double waterLevel;

    public Double getpHValue() {
        return pHValue;
    }

    public void setpHValue(Double pHValue) {
        this.pHValue = pHValue;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTurbidity() {
        return turbidity;
    }

    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }

    public Double getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(Double waterLevel) {
        this.waterLevel = waterLevel;
    }
}
