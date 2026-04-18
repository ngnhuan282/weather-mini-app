package com.example.btqt02.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HourlyWeather {
    @SerializedName("dt")
    private long timestamp; // Thời gian định dạng Unix

    @SerializedName("temp")
    private double temperature; // Nhiệt độ theo giờ [cite: 67]

    @SerializedName("weather")
    private List<WeatherCondition> weatherDescriptions;

    // Getters
    public long getTimestamp() { return timestamp; }
    public double getTemperature() { return temperature; }
    public List<WeatherCondition> getWeatherDescriptions() { return weatherDescriptions; }
}
