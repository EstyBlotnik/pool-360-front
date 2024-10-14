package myandroid.app.hhobzic.a360pool.classes;

public class Issue {
    private String currentDateTime;
    private String issue_Title;
    private String sensor_name;
    private String sensor_value;

    // Getters and setters
    public String getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(String currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public String getIssue_Title() {
        return issue_Title;
    }

    public void setIssue_Title(String issue_Title) {
        this.issue_Title = issue_Title;
    }

    public String getSensor_name() {
        return sensor_name;
    }

    public void setSensor_name(String sensor_name) {
        this.sensor_name = sensor_name;
    }

    public String getSensor_value() {
        return sensor_value;
    }

    public void setSensor_value(String sensor_value) {
        this.sensor_value = sensor_value;
    }
}
