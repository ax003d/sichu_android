package com.ax003d.sichu.fragments;

import org.holoeverywhere.preference.PreferenceFragment;

import android.os.Bundle;

import com.ax003d.sichu.R;

public class AccountFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);
    }
}
