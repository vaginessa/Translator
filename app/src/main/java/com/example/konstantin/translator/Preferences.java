package com.example.konstantin.translator;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class Preferences{
    private SharedPreferences preferences;
    private Context context;

    public Preferences(Context context)
    {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveLang(String currentLangFrom,
                         String currentLangFromShort,
                         String currentLangTo,
                         String currentLangToShort)
    {
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString("currentLangFrom", currentLangFrom);
        ed.putString("currentLangFromShort", currentLangFromShort);
        ed.putString("currentLangTo", currentLangTo);
        ed.putString("currentLangToShort", currentLangToShort);
        ed.apply();
    }

    public Map<String, String> loadLang()
    {
        Map<String, String> result = new HashMap<>();
        result.put("currentLangFrom", preferences.getString("currentLangFrom", "Русский"));
        result.put("currentLangFromShort", preferences.getString("currentLangFromShort", "ru"));
        result.put("currentLangTo", preferences.getString("currentLangTo", "Английский"));
        result.put("currentLangToShort", preferences.getString("currentLangToShort", "en"));
        return result;
    }
}
