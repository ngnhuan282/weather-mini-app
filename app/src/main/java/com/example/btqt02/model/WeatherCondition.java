package com.example.btqt02.model;

import com.google.gson.annotations.SerializedName;

public class WeatherCondition {
    @SerializedName("main")
    private String mainStatus; // VD: Clear, Clouds, Rain

    @SerializedName("description")
    private String description; // VD: sunny, light rain

    @SerializedName("icon")
    private String iconCode; // Mã icon để tải hình ảnh từ API

    // Getters and Setters
    public String getMainStatus() { return mainStatus; }
    public String getDescription() { return description; }
    public String getIconCode() { return iconCode; }
}
