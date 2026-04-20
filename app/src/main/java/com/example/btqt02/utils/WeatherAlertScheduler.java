package com.example.btqt02.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.btqt02.worker.WeatherAlertWorker;

import java.util.concurrent.TimeUnit;

public final class WeatherAlertScheduler {
    private WeatherAlertScheduler() {}

    public static final String UNIQUE_WORK_NAME = "weather_alert_worker";

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeatherAlertWorker.class,
                1,
                TimeUnit.HOURS
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
    }

    public static void syncWithPrefs(Context context) {
        if (Prefs.isAlertsEnabled(context)) {
            schedule(context);
        } else {
            cancel(context);
        }
    }
}
