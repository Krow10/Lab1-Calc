package com.example.lab1_calc;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class CalcSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}