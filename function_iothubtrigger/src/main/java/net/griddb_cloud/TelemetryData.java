package net.griddb_cloud;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.toshiba.mwcloud.gs.RowKey;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryData {
    @RowKey
    Date ts;
    private double temperature;
    private int humidity;
    private double pressure;
    @JsonProperty("data_point_id")
    private int data_point_id;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public int getDataPointId() {
        return data_point_id;
    }

    public void setDataPointId(int data_point_id) {
        this.data_point_id = data_point_id;
    }

    @Override
    public String toString() {
        return "TelemetryData {" +
                "dataPointId=" + data_point_id +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                '}';
    }

}
