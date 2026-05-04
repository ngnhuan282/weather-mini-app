package com.example.btqt02.ui;

import android.os.Bundle;
import android.widget.TextView;
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
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TileOverlay currentOverlay;
    private double lat, lon;
    private TextView tvMapLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tvMapLabel = findViewById(R.id.tvMapLabel);
        lat = getIntent().getDoubleExtra("lat", 10.7583);
        lon = getIntent().getDoubleExtra("lon", 106.6806);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // LINK CÁC NÚT BẤM
        findViewById(R.id.btnTemp).setOnClickListener(v -> updateLayer("temp", "Nhiệt độ"));
        findViewById(R.id.btnClouds).setOnClickListener(v -> updateLayer("clouds", "Mây"));
        findViewById(R.id.btnRain).setOnClickListener(v -> updateLayer("precipitation", "Lượng mưa"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng pos = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10f));
        mMap.addMarker(new MarkerOptions().position(pos).title("Vị trí đã chọn"));

        updateLayer("temp", "Nhiệt độ"); // Mặc định hiện nhiệt độ
    }

    private void updateLayer(String type, String label) {
        if (mMap == null) return;

        // XÓA LỚP CŨ (Tránh lag và lỗi referer)[cite: 1]
        if (currentOverlay != null) {
            currentOverlay.remove();
        }

        if (tvMapLabel != null) tvMapLabel.setText("Bản đồ " + label);

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int z) {
                // SỬ DỤNG LAYER CHUẨN (Bỏ đuôi _new nếu dùng key Free
                String url = String.format(Locale.US, "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=%s",
                        type, z, x, y, BuildConfig.WEATHER_API_KEY);
                try { return new URL(url); } catch (MalformedURLException e) { return null; }
            }
        };

        currentOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider)
                .transparency(0.3f));
    }
}