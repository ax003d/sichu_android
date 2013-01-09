package com.ax003d.sichu.fragments;

import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;

import com.ax003d.sichu.R;
import com.ax003d.sichu.utils.Preferences;

public class AccountFragment extends PreferenceFragment implements
		OnPreferenceClickListener, OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// Preference pref_key_weibo = findPreference("pref_key_weibo");
		Preference pref_key_logout = findPreference("pref_key_logout");
		// pref_key_weibo.setTitle("Weibo: ax003d");
		pref_key_logout.setOnPreferenceClickListener(this);
		// pref_key_weibo.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("pref_key_logout")) {
			Preferences.clearLoginInfo(getActivity());
			getActivity().finish();
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("pref_key_weibo")) {
			Toast.makeText(getActivity(), "Bind", Toast.LENGTH_SHORT).show();
		}
		return false;
	}
}
