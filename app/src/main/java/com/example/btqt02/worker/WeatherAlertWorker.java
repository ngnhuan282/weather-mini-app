package com.example.btqt02.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.btqt02.BuildConfig;
import com.example.btqt02.api.RetrofitClient;
import com.example.btqt02.api.WeatherApiService;
import com.example.btqt02.model.WeatherResponse;
import com.example.btqt02.model.WeatherResponse.CurrentWeather;
import com.example.btqt02.utils.NotificationUtils;
import com.example.btqt02.utils.Prefs;

import java.io.IOException;

import retrofit2.Response;

public class WeatherAlertWorker extends Worker {

    private static final int NOTIFICATION_ID = 1001;

    public WeatherAlertWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!Prefs.isAlertsEnabled(context)) {
            return Result.success();
        }

        if (!Prefs.hasLastLocation(context)) {
            return Result.success();
        }

        double lat = Prefs.getLastLat(context, 0);
        double lon = Prefs.getLastLon(context, 0);
        if (lat == 0 && lon == 0) {
            return Result.success();
        }

        String apiKey = BuildConfig.WEATHER_API_KEY;
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            return Result.success();
        }

        String units = Prefs.getUnits(context);

        WeatherApiService apiService = RetrofitClient.getApiService();

        try {
            Response<WeatherResponse> response = apiService
                    .getFullWeather(lat, lon, "minutely", apiKey, units)
                    .execute();

            if (!response.isSuccessful() || response.body() == null) {
                return Result.retry();
            }

            CurrentWeather current = response.body().getCurrent();
            if (current == null || current.getWeather() == null || current.getWeather().isEmpty()) {
                return Result.success();
            }

            maybeNotify(context, current, units);
            return Result.success();

        } catch (IOException e) {
            return Result.retry();
        } catch (Exception e) {
            return Result.success();
        }
    }

    private void maybeNotify(Context context, CurrentWeather current, String units) {
        double tempValue = current.getTemp();

        // Compare thresholds in Celsius to keep the same logic as MainActivity.
        double tempC = Prefs.UNITS_IMPERIAL.equals(units) ? (tempValue - 32.0) * 5.0 / 9.0 : tempValue;

        String mainCondition = current.getWeather().get(0).getMainStatus() != null
                ? current.getWeather().get(0).getMainStatus().toLowerCase()
                : "";

        String description = current.getWeather().get(0).getDescription() != null
                ? current.getWeather().get(0).getDescription().toLowerCase()
                : "";

        String tempLabel = Math.round(tempValue) + (Prefs.UNITS_IMPERIAL.equals(units) ? "°F" : "°C");

        if (tempC > 35) {
            NotificationUtils.notifyWeatherAlert(
                    context,
                    "⚠️ Cảnh báo nóng bức!",
                    "Nhiệt độ hiện tại: " + tempLabel + ".\nHãy ở trong nhà và uống nhiều nước!",
                    NOTIFICATION_ID
            );
        } else if (tempC < 12) {
            NotificationUtils.notifyWeatherAlert(
                    context,
                    "❄️ Cảnh báo trời lạnh!",
                    "Nhiệt độ hiện tại: " + tempLabel + ".\nNhớ mặc ấm khi ra ngoài!",
                    NOTIFICATION_ID
            );
        } else if (mainCondition.contains("rain") || mainCondition.contains("thunderstorm") ||
                mainCondition.contains("storm") || mainCondition.contains("snow") ||
                description.contains("heavy rain") || description.contains("shower") ||
                description.contains("thunder")) {
            NotificationUtils.notifyWeatherAlert(
                    context,
                    "🌧️ Thời tiết xấu!",
                    "Hiện đang có " + current.getWeather().get(0).getDescription() +
                            ".\nHãy cẩn thận khi di chuyển và mang theo ô!",
                    NOTIFICATION_ID
            );
        }
    }
}
