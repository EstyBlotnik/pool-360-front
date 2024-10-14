package myandroid.app.hhobzic.a360pool.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssueResponse {
    @SerializedName("sensor_name")
    @Expose
    private String sensorName;
    @SerializedName("sensor_value")
    @Expose
    private String sensorValue;
    @SerializedName("currentDateTime")
    @Expose
    private String currentDateTime;
    @SerializedName("issue_Title")
    @Expose
    private String issue_Title;

    public String getIssue_Title() {
        return issue_Title;
    }

    public void setIssue_Title(String issue_Title) {
        this.issue_Title = issue_Title;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(String sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(String currentDateTime) {
        this.currentDateTime = currentDateTime;
    }
}
