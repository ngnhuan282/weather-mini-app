package com.example.btqt02.api;

import com.example.btqt02.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    // API lấy thời tiết dựa trên tọa độ
    // Tự động lấy vị trí hiện tại
    @GET("onecall")
    Call<WeatherResponse> getFullWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("exclude") String exclude, // Loại bỏ phần dữ liệu không cần thiết
            @Query("appid") String apiKey,    // API Key
            @Query("units") String units      // "metric" để lấy độ C, đáp ứng yêu cầu
    );
}