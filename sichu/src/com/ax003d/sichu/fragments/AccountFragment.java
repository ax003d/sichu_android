package com.ax003d.sichu.fragments;

import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ax003d.sichu.R;
import com.ax003d.sichu.utils.Preferences;

public class AccountFragment extends PreferenceFragment implements
		OnPreferenceClickListener, OnPreferenceChangeListener {

	private FragmentActivity mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		addPreferencesFromResource(R.xml.preferences);

		Preference pref_key_weibo = findPreference("pref_key_weibo");
		Preference pref_key_logout = findPreference("pref_key_logout");
		Preference pref_key_account = findPreference("pref_key_account");
		String screenName = Preferences.getWeiboScreenName(mActivity);
		if (screenName != null) {
			pref_key_weibo.setTitle(screenName);			
		}
		pref_key_logout.setOnPreferenceClickListener(this);
		// pref_key_weibo.setOnPreferenceChangeListener(this);
		pref_key_account.setTitle(Preferences.getUserName(mActivity));
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("pref_key_logout")) {
			Preferences.clearLoginInfo(mActivity);
			mActivity.finish();
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("pref_key_weibo")) {
			Toast.makeText(mActivity, "Bind", Toast.LENGTH_SHORT).show();
		}
		return false;
	}
}
