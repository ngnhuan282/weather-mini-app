package com.example.btqt02.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btqt02.BuildConfig;
import com.example.btqt02.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TileOverlay currentTileOverlay;
    private TextView tvMapLabel;

    private final String OWM_API_KEY = BuildConfig.WEATHER_API_KEY;

    private double currentLat = 10.7583;
    private double currentLon = 106.6806;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tvMapLabel = findViewById(R.id.tvMapLabel);

        // Nhận tọa độ thực tế từ MainActivity
        if (getIntent() != null) {
            currentLat = getIntent().getDoubleExtra("lat", 10.7583);
            currentLon = getIntent().getDoubleExtra("lon", 106.6806);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnTemp).setOnClickListener(v -> updateWeatherLayer("temp_new", "Bản đồ Nhiệt độ"));
        findViewById(R.id.btnClouds).setOnClickListener(v -> updateWeatherLayer("clouds_new", "Bản đồ Mây che phủ"));
        findViewById(R.id.btnRain).setOnClickListener(v -> updateWeatherLayer("precipitation_new", "Bản đồ Lượng mưa"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        LatLng userLocation = new LatLng(currentLat, currentLon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10.0f));

        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Vị trí của bạn"));

        updateWeatherLayer("temp_new", "Bản đồ Nhiệt độ");
    }

    private void updateWeatherLayer(String type, String label) {
        if (mMap == null) return;

        if (currentTileOverlay != null) {
            currentTileOverlay.remove();
        }

        tvMapLabel.setText(label);

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String urlString = String.format(
                        "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=%s",
                        type, zoom, x, y, OWM_API_KEY);
                try {
                    return new URL(urlString);
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        };

        float transparency = type.equals("precipitation_new") ? 0.78f : 0.65f;

        currentTileOverlay = mMap.addTileOverlay(
                new TileOverlayOptions()
                        .tileProvider(tileProvider)
                        .transparency(transparency)
        );
    }
}