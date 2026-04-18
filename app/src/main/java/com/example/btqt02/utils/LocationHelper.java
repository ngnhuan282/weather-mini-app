package com.example.btqt02.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    public interface OnLocationResultListener {
        void onResult(double lat, double lon);
        void onFailure(String message);
    }

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(OnLocationResultListener listener) {
        // Lấy vị trí mới nhất
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            listener.onResult(location.getLatitude(), location.getLongitude());
                        } else {
                            listener.onFailure("Không lấy được vị trí. Hãy thử lại.");
                        }
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Lỗi lấy vị trí: " + e.getMessage()));
    }
}