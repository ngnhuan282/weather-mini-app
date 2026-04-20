package com.example.btqt02.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btqt02.R;
import com.example.btqt02.adapter.DailyAdapter;
import com.example.btqt02.adapter.HourlyAdapter;
import com.example.btqt02.api.RetrofitClient;
import com.example.btqt02.api.WeatherApiService;
import com.example.btqt02.model.WeatherResponse;
import com.example.btqt02.model.WeatherResponse.CurrentWeather;
import com.example.btqt02.utils.LocationHelper;
import com.example.btqt02.utils.NotificationUtils;
import com.example.btqt02.utils.Prefs;
import com.example.btqt02.utils.WeatherAlertScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHourly, rvDaily;
    private TextView tvTemperature, tvCity, tvCondition;
    private BottomNavigationView bottomNav;

    // Các thành phần trong 3 Card Stats
    private View cardHumidity, cardWind, cardFeelsLike;
    private TextView tvStatHumidityTitle, tvStatHumidityValue;
    private TextView tvStatWindTitle, tvStatWindValue;
    private TextView tvStatFeelsTitle, tvStatFeelsValue;
    private ImageView ivStatHumidity, ivStatWind, ivStatFeels;

    private LocationHelper locationHelper;
    private String API_KEY;

    // Thêm 2 biến này để lưu vị trí hiện tại
    private double currentLat = 10.7583;
    private double currentLon = 106.6806;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2002;
    private static final int NOTIFICATION_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loadApiKey();
        initViews();
        setupNavigation();
        NotificationUtils.ensureChannel(this);

        // Keep background alerts in sync with user settings
        WeatherAlertScheduler.syncWithPrefs(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left, 0, 0, 0);
            return insets;
        });

        locationHelper = new LocationHelper(this);
        checkLocationPermission();
    }

    // ================== HIỂN THỊ THÔNG BÁO ==================
    private void showWeatherNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }
        NotificationUtils.notifyWeatherAlert(this, title, message, NOTIFICATION_ID);
    }

    private void loadApiKey() {
        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            API_KEY = appInfo.metaData.getString("com.example.btqt02.WEATHER_API_KEY");
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nạp API Key!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        rvHourly = findViewById(R.id.rvHourly);
        rvDaily = findViewById(R.id.rvDaily);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCity = findViewById(R.id.tvCity);
        tvCondition = findViewById(R.id.tvCondition);
        bottomNav = findViewById(R.id.bottomNav);

        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        cardHumidity = findViewById(R.id.cardHumidity);
        tvStatHumidityTitle = cardHumidity.findViewById(R.id.tvStatTitle);
        tvStatHumidityValue = cardHumidity.findViewById(R.id.tvStatValue);
        ivStatHumidity = cardHumidity.findViewById(R.id.ivStatIcon);

        cardWind = findViewById(R.id.cardWind);
        tvStatWindTitle = cardWind.findViewById(R.id.tvStatTitle);
        tvStatWindValue = cardWind.findViewById(R.id.tvStatValue);
        ivStatWind = cardWind.findViewById(R.id.ivStatIcon);

        cardFeelsLike = findViewById(R.id.cardFeelsLike);
        tvStatFeelsTitle = cardFeelsLike.findViewById(R.id.tvStatTitle);
        tvStatFeelsValue = cardFeelsLike.findViewById(R.id.tvStatValue);
        ivStatFeels = cardFeelsLike.findViewById(R.id.ivStatIcon);
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                getCurrentLocation();
                return true;
            } else if (itemId == R.id.nav_map) {
                // Truyền tọa độ thực tế sang MapActivity
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("lat", currentLat);
                intent.putExtra("lon", currentLon);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Toast.makeText(this, "Tính năng thông báo đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        locationHelper.getLastLocation(new LocationHelper.OnLocationResultListener() {
            @Override
            public void onResult(double lat, double lon) {
                currentLat = lat;     // Lưu vị trí thực tế
                currentLon = lon;
                Prefs.setLastLocation(MainActivity.this, lat, lon);
                updateCityName(lat, lon);
                fetchWeatherData(lat, lon);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                fetchWeatherData(currentLat, currentLon);
            }
        });
    }

    private void updateCityName(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                if (cityName == null) cityName = addresses.get(0).getAdminArea();
                tvCity.setText(cityName != null ? cityName : "TP. Hồ Chí Minh");
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvCity.setText("Unknown Location");
        }
    }

    private void fetchWeatherData(double lat, double lon) {
        if (API_KEY == null || API_KEY.isEmpty()) return;

        WeatherApiService apiService = RetrofitClient.getApiService();
        String units = Prefs.getUnits(this);
        apiService.getFullWeather(lat, lon, "minutely", API_KEY, units)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateUI(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(WeatherResponse weather) {
        CurrentWeather current = weather.getCurrent();

        String units = Prefs.getUnits(this);
        boolean imperial = Prefs.UNITS_IMPERIAL.equals(units);

        tvTemperature.setText(Math.round(current.getTemp()) + (imperial ? "°F" : "°C"));
        tvCondition.setText(current.getWeather().get(0).getMainStatus());

        tvStatHumidityTitle.setText("HUMIDITY");
        tvStatHumidityValue.setText(current.getHumidity() + "%");
        ivStatHumidity.setImageResource(R.drawable.ic_humidity);

        tvStatWindTitle.setText("WIND");
        if (imperial) {
            tvStatWindValue.setText(Math.round(current.getWindSpeed()) + " mph");
        } else {
            double windKmh = current.getWindSpeed() * 3.6;
            tvStatWindValue.setText(Math.round(windKmh) + " km/h");
        }
        ivStatWind.setImageResource(R.drawable.ic_wind);

        tvStatFeelsTitle.setText("FEELS LIKE");
        tvStatFeelsValue.setText(Math.round(current.getFeelsLike()) + (imperial ? "°F" : "°C"));
        ivStatFeels.setImageResource(R.drawable.ic_temperature);

        String unitSymbol = imperial ? "°F" : "°C";
        rvHourly.setAdapter(new HourlyAdapter(weather.getHourlyForecast(), unitSymbol));
        rvDaily.setAdapter(new DailyAdapter(weather.getDailyForecast(), unitSymbol));

        checkAndSendWeatherAlert(current);
    }

    private void checkAndSendWeatherAlert(CurrentWeather current) {
        String units = Prefs.getUnits(this);
        boolean imperial = Prefs.UNITS_IMPERIAL.equals(units);

        double tempValue = current.getTemp();
        double tempC = imperial ? (tempValue - 32.0) * 5.0 / 9.0 : tempValue;
        String mainCondition = current.getWeather().get(0).getMainStatus().toLowerCase();
        String description = current.getWeather().get(0).getDescription().toLowerCase();

        String tempLabel = Math.round(tempValue) + (imperial ? "°F" : "°C");

        if (tempC > 35) {
            showWeatherNotification("⚠️ Cảnh báo nóng bức!",
                "Nhiệt độ hiện tại: " + tempLabel + ".\nHãy ở trong nhà và uống nhiều nước!");
        } else if (tempC < 12) {
            showWeatherNotification("❄️ Cảnh báo trời lạnh!",
                "Nhiệt độ hiện tại: " + tempLabel + ".\nNhớ mặc ấm khi ra ngoài!");
        } else if (mainCondition.contains("rain") || mainCondition.contains("thunderstorm") ||
                mainCondition.contains("storm") || mainCondition.contains("snow") ||
                description.contains("heavy rain") || description.contains("shower") ||
                description.contains("thunder")) {
            showWeatherNotification("🌧️ Thời tiết xấu!",
                    "Hiện đang có " + current.getWeather().get(0).getDescription() +
                            ".\nHãy cẩn thận khi di chuyển và mang theo ô!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);

        // Refresh UI when user changes units in Settings
        fetchWeatherData(currentLat, currentLon);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                fetchWeatherData(currentLat, currentLon);
            }
        }
    }
}