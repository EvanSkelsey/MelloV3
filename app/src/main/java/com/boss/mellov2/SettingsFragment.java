package com.boss.mellov2;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragmentCompat {

    //=============================================

    //=============================================


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

}