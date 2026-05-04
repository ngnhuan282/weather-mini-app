package com.example.btqt02.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btqt02.R;
import com.example.btqt02.adapter.DailyAdapter;
import com.example.btqt02.adapter.HourlyAdapter;
import com.example.btqt02.adapter.SuggestionAdapter;
import com.example.btqt02.api.RetrofitClient;
import com.example.btqt02.api.WeatherApiService;
import com.example.btqt02.model.WeatherResponse;
import com.example.btqt02.model.WeatherResponse.CurrentWeather;
import com.example.btqt02.utils.LocationHelper;
import com.example.btqt02.utils.NotificationUtils;
import com.example.btqt02.utils.Prefs;
import com.example.btqt02.utils.WeatherAlertScheduler;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHourly, rvDaily, rvSuggestions;
    private TextView tvTemperature, tvCity, tvCondition;
    private BottomNavigationView bottomNav;
    private SearchView searchView;
    private View layoutSuggestion;
    private SuggestionAdapter suggestionAdapter;
    private PlacesClient placesClient;

    private View cardHumidity, cardWind, cardFeelsLike;
    private TextView tvStatHumidityValue, tvStatWindValue, tvStatFeelsValue;

    private LocationHelper locationHelper;
    private String API_KEY;
    private double currentLat = 10.7583, currentLon = 106.6806;
    private static final int NOTIFICATION_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loadApiKey();
        initPlaces();
        initViews();
        setupSearchLogic();
        setupNavigation();

        NotificationUtils.ensureChannel(this);
        WeatherAlertScheduler.syncWithPrefs(this);

        locationHelper = new LocationHelper(this);

        // Kiểm tra quyền và load dữ liệu lần đầu
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
    }

    // --- TÍNH NĂNG CẬP NHẬT KHI QUAY LẠI TỪ SETTINGS ---
    @Override
    protected void onResume() {
        super.onResume();
        // Tự động fetch lại data với đơn vị mới (C hoặc F) khi quay lại màn hình chính
        fetchWeatherData(currentLat, currentLon);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    private void loadApiKey() {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            API_KEY = appInfo.metaData.getString("com.example.btqt02.WEATHER_API_KEY");
        } catch (Exception ignored) {}
    }

    private void initPlaces() {
        if (!Places.isInitialized()) Places.initialize(getApplicationContext(), API_KEY);
        placesClient = Places.createClient(this);
    }

    private void initViews() {
        rvHourly = findViewById(R.id.rvHourly);
        rvDaily = findViewById(R.id.rvDaily);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCity = findViewById(R.id.tvCity);
        tvCondition = findViewById(R.id.tvCondition);
        bottomNav = findViewById(R.id.bottomNav);
        searchView = findViewById(R.id.searchView);
        layoutSuggestion = findViewById(R.id.layoutSuggestion);

        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));

        suggestionAdapter = new SuggestionAdapter(this::fetchPlaceDetails);
        rvSuggestions.setAdapter(suggestionAdapter);

        cardHumidity = findViewById(R.id.cardHumidity);
        tvStatHumidityValue = cardHumidity.findViewById(R.id.tvStatValue);
        cardWind = findViewById(R.id.cardWind);
        tvStatWindValue = cardWind.findViewById(R.id.tvStatValue);
        cardFeelsLike = findViewById(R.id.cardFeelsLike);
        tvStatFeelsValue = cardFeelsLike.findViewById(R.id.tvStatValue);
    }

    private void setupSearchLogic() {
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            layoutSuggestion.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        layoutSuggestion.setOnClickListener(v -> {
            getCurrentLocation();
            searchView.clearFocus();
            rvSuggestions.setVisibility(View.GONE);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) getSuggestions(newText);
                else rvSuggestions.setVisibility(View.GONE);
                return true;
            }
        });
    }

    private void getSuggestions(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setCountry("VN").setQuery(query).build();
        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            suggestionAdapter.setSuggestions(response.getAutocompletePredictions());
            rvSuggestions.setVisibility(View.VISIBLE);
        });
    }

    private void fetchPlaceDetails(AutocompletePrediction prediction) {
        List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.builder(prediction.getPlaceId(), fields).build();
        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            if (place.getLatLng() != null) {
                currentLat = place.getLatLng().latitude;
                currentLon = place.getLatLng().longitude;
                tvCity.setText(place.getName());
                fetchWeatherData(currentLat, currentLon);
                Prefs.setLastLocation(this, currentLat, currentLon);
                rvSuggestions.setVisibility(View.GONE);
                searchView.clearFocus();
            }
        });
    }

    private void getCurrentLocation() {
        locationHelper.getLastLocation(new LocationHelper.OnLocationResultListener() {
            @Override
            public void onResult(double lat, double lon) {
                currentLat = lat; currentLon = lon;
                Prefs.setLastLocation(MainActivity.this, lat, lon);
                updateCityName(lat, lon);
                fetchWeatherData(lat, lon);
            }
            @Override
            public void onFailure(String message) { fetchWeatherData(currentLat, currentLon); }
        });
    }

    private void updateCityName(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (!addresses.isEmpty()) tvCity.setText(addresses.get(0).getLocality());
        } catch (IOException ignored) {}
    }

    private void fetchWeatherData(double lat, double lon) {
        WeatherApiService api = RetrofitClient.getApiService();
        // Lấy đơn vị từ Prefs để gửi lên Server
        String units = Prefs.getUnits(this);
        api.getFullWeather(lat, lon, "minutely", API_KEY, units).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) updateUI(response.body());
            }
            @Override public void onFailure(Call<WeatherResponse> call, Throwable t) {}
        });
    }

    private void updateUI(WeatherResponse weather) {
        CurrentWeather current = weather.getCurrent();
        String units = Prefs.getUnits(this);
        String symbol = units.equals("imperial") ? "°F" : "°C";

        tvTemperature.setText(Math.round(current.getTemp()) + symbol);
        tvCondition.setText(current.getWeather().get(0).getMainStatus());
        tvStatHumidityValue.setText(current.getHumidity() + "%");
        tvStatFeelsValue.setText(Math.round(current.getFeelsLike()) + symbol);

        double wind = units.equals("imperial") ? current.getWindSpeed() : current.getWindSpeed() * 3.6;
        tvStatWindValue.setText(Math.round(wind) + (units.equals("imperial") ? " mph" : " km/h"));

        rvHourly.setAdapter(new HourlyAdapter(weather.getHourlyForecast(), symbol));
        rvDaily.setAdapter(new DailyAdapter(weather.getDailyForecast(), symbol));

        checkAndSendWeatherAlert(current);
    }

    private void checkAndSendWeatherAlert(CurrentWeather current) {
        String units = Prefs.getUnits(this);
        boolean imperial = units.equals("imperial");
        double tempC = imperial ? (current.getTemp() - 32.0) * 5.0 / 9.0 : current.getTemp();
        String main = current.getWeather().get(0).getMainStatus().toLowerCase();

        if (tempC > 35) {
            NotificationUtils.notifyWeatherAlert(this, "⚠️ Cảnh báo nóng!", "Nhiệt độ rất cao, hãy uống đủ nước.", NOTIFICATION_ID);
        } else if (tempC < 12) {
            NotificationUtils.notifyWeatherAlert(this, "❄️ Cảnh báo lạnh!", "Trời đang khá lạnh, hãy mặc ấm.", NOTIFICATION_ID);
        } else if (main.contains("rain") || main.contains("storm")) {
            NotificationUtils.notifyWeatherAlert(this, "🌧️ Thời tiết xấu!", "Có thể có mưa, hãy mang theo ô.", NOTIFICATION_ID);
        }
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("lat", currentLat); intent.putExtra("lon", currentLon);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return id == R.id.nav_home;
        });
    }
}