package com.example.btqt02.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("timezone")
    private String timezone;

    @SerializedName("current")
    private CurrentWeather current;

    @SerializedName("hourly")
    private List<HourlyWeather> hourlyForecast;

    @SerializedName("daily")
    private List<DailyWeather> dailyForecast;

    // Lớp chứa thời tiết hiện tại
    public static class CurrentWeather {
        @SerializedName("temp")
        public double temp;

        @SerializedName("feels_like")   // ← ĐÃ THÊM
        public double feelsLike;

        @SerializedName("humidity")
        public int humidity;

        @SerializedName("wind_speed")
        public double windSpeed;

        @SerializedName("weather")
        public List<WeatherCondition> weather;

        // Getters
        public double getTemp() { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public List<WeatherCondition> getWeather() { return weather; }
    }

    // Getters
    public CurrentWeather getCurrent() { return current; }
    public List<HourlyWeather> getHourlyForecast() { return hourlyForecast; }
    public List<DailyWeather> getDailyForecast() { return dailyForecast; }
}