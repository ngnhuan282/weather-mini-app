package com.example.btqt02.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btqt02.R;
import com.example.btqt02.utils.Prefs;
import com.example.btqt02.utils.WeatherAlertScheduler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchUnits;
    private SwitchMaterial switchAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        switchUnits = findViewById(R.id.switchUnits);
        switchAlerts = findViewById(R.id.switchAlerts);

        String units = Prefs.getUnits(this);
        switchUnits.setChecked(Prefs.UNITS_IMPERIAL.equals(units));

        boolean alertsEnabled = Prefs.isAlertsEnabled(this);
        switchAlerts.setChecked(alertsEnabled);

        switchUnits.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.setUnits(this, isChecked ? Prefs.UNITS_IMPERIAL : Prefs.UNITS_METRIC);
        });

        switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.setAlertsEnabled(this, isChecked);
            WeatherAlertScheduler.syncWithPrefs(this);
        });

        // Ensure scheduler matches current state.
        WeatherAlertScheduler.syncWithPrefs(this);
    }
}
