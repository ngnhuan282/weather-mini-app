package com.example.btqt02.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private Prefs() {}

    private static final String PREFS_NAME = "weather_prefs";

    public static final String UNITS_METRIC = "metric";
    public static final String UNITS_IMPERIAL = "imperial";

    private static final String KEY_UNITS = "units";
    private static final String KEY_ALERTS_ENABLED = "alerts_enabled";
    private static final String KEY_LAST_LAT = "last_lat";
    private static final String KEY_LAST_LON = "last_lon";

    private static SharedPreferences sp(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getUnits(Context context) {
        return sp(context).getString(KEY_UNITS, UNITS_METRIC);
    }

    public static void setUnits(Context context, String units) {
        sp(context).edit().putString(KEY_UNITS, units).apply();
    }

    public static boolean isAlertsEnabled(Context context) {
        return sp(context).getBoolean(KEY_ALERTS_ENABLED, true);
    }

    public static void setAlertsEnabled(Context context, boolean enabled) {
        sp(context).edit().putBoolean(KEY_ALERTS_ENABLED, enabled).apply();
    }

    public static void setLastLocation(Context context, double lat, double lon) {
        sp(context).edit()
                .putLong(KEY_LAST_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LAST_LON, Double.doubleToRawLongBits(lon))
                .apply();
    }

    public static boolean hasLastLocation(Context context) {
        return sp(context).contains(KEY_LAST_LAT) && sp(context).contains(KEY_LAST_LON);
    }

    public static double getLastLat(Context context, double fallback) {
        if (!sp(context).contains(KEY_LAST_LAT)) return fallback;
        return Double.longBitsToDouble(sp(context).getLong(KEY_LAST_LAT, Double.doubleToRawLongBits(fallback)));
    }

    public static double getLastLon(Context context, double fallback) {
        if (!sp(context).contains(KEY_LAST_LON)) return fallback;
        return Double.longBitsToDouble(sp(context).getLong(KEY_LAST_LON, Double.doubleToRawLongBits(fallback)));
    }
}
