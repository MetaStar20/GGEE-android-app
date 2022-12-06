package com.example.ggee_vs.Preferences;


import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    public static SharedPreferences get(final Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static String getStringPref(final Context context, String pref, String def) {
        SharedPreferences prefs = Prefs.get(context);
        String val = prefs.getString(pref, def);

        if (val == null || val.equals("") || val.equals("null"))
            return def;
        else
            return val;
    }

    public static void putStringPref(final Context context, String pref, String val) {
        SharedPreferences prefs = Prefs.get(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(pref, val);
        editor.apply();
    }

    public static int getIntPref(final Context context, String pref, int def) {
        SharedPreferences prefs = Prefs.get(context);
        return prefs.getInt(pref, def);
    }

    public static void putIntPref(final Context context, String pref, int val) {
        SharedPreferences prefs = Prefs.get(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(pref, val);
        editor.apply();
    }

    public static boolean getBooleanPref(final Context context, String pref, boolean def) {
        SharedPreferences prefs = Prefs.get(context);
        return prefs.getBoolean(pref, def);
    }

    public static void putBooleanPref(final Context context, String pref, boolean val) {
        SharedPreferences prefs = Prefs.get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(pref, val);
        editor.apply();
    }

}
