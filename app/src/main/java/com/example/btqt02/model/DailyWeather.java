package com.example.btqt02.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DailyWeather {
    @SerializedName("dt")
    private long timestamp;

    @SerializedName("temp")
    private TemperatureRange tempRange; // Nhiệt độ Max/Min

    @SerializedName("humidity")
    private int humidity; // Độ ẩm

    @SerializedName("pop")
    private double rainChance; // Xác suất mưa (0.0 đến 1.0)

    @SerializedName("weather")
    private List<WeatherCondition> weatherDescriptions;

    // Inner class để lấy nhiệt độ Max và Min của ngày
    public static class TemperatureRange {
        @SerializedName("min")
        public double minTemp;
        @SerializedName("max")
        public double maxTemp;
    }

    // Getters
    public long getTimestamp() { return timestamp; }
    public TemperatureRange getTempRange() { return tempRange; }
    public int getHumidity() { return humidity; }
    public double getRainChance() { return rainChance; }
    public List<WeatherCondition> getWeatherDescriptions() { return weatherDescriptions; }
}
