package com.ax003d.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.ax003d.sichu.BindEmailActivity;
import com.ax003d.sichu.LoginActivity;
import com.ax003d.sichu.MainActivity;
import com.ax003d.sichu.R;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.events.PlatformAuthorizeEvent;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;
import com.squareup.otto.Subscribe;
import com.umeng.fb.UMFeedbackService;

public class AccountFragment extends PreferenceFragment implements
		OnPreferenceClickListener, OnPreferenceChangeListener {

	private static AccountFragment instance;
	private MainActivity mActivity;
	private String screenName;
	public ISichuAPI api_client;
	private Preference pref_key_weibo;
	private String email;
	private Preference pref_key_email;

	public static AccountFragment getInstance() {
		if (AccountFragment.instance == null) {
			AccountFragment.instance = new AccountFragment();
		}
		return AccountFragment.instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		api_client = SichuAPI.getInstance(mActivity);
		addPreferencesFromResource(R.xml.preferences);

		getPreferenceScreen().getPreference(0).setTitle(
				getString(R.string.pref_title) + " v"
						+ Utils.getPackagetInfo(mActivity).versionName);

		pref_key_weibo = findPreference("pref_key_weibo");
		pref_key_email = findPreference("pref_key_email");
		Preference pref_key_logout = findPreference("pref_key_logout");
		Preference pref_key_account = findPreference("pref_key_account");
		Preference pref_key_feedback = findPreference("pref_key_feedback");
		setScreenName();
		pref_key_logout.setOnPreferenceClickListener(this);
		pref_key_weibo.setOnPreferenceClickListener(this);
		pref_key_email.setOnPreferenceClickListener(this);
		pref_key_account.setTitle(Preferences.getUserName(mActivity));
		pref_key_feedback.setOnPreferenceClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.getBus().register(this);
		setScreenName();
	}

	public void setScreenName() {
		screenName = Preferences.getWeiboScreenName(mActivity);
		if (screenName != null) {
			pref_key_weibo.setTitle(screenName);
		} else {
			pref_key_weibo.setTitle(R.string.hint_bind_weibo);
		}

		email = Preferences.getEmail(mActivity);
		if (TextUtils.isEmpty(email)) {
			pref_key_email.setTitle(R.string.hint_bind_email);
		} else {
			pref_key_email.setTitle(email);
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("pref_key_logout")) {
			Preferences.clearLoginInfo(mActivity);
			mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
			mActivity.finish();
		} else if (preference.getKey().equals("pref_key_weibo")) {
			if (screenName == null) {
				mActivity.bindWeibo();
			} else {
				// unbind weibo
				Builder builder = new AlertDialog.Builder(mActivity);
				builder.setCancelable(false);
				builder.setTitle(R.string.title_unbind_weibo);
				builder.setMessage(R.string.msg_unbind_weibo);
				builder.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								new UnbindWeiboTask().execute();
							}
						});
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.create().show();
			}
		} else if (preference.getKey().equals("pref_key_feedback")) {
			UMFeedbackService.openUmengFeedbackSDK(mActivity);
		} else if (preference.getKey().equals("pref_key_email")) {
			startActivity(new Intent(mActivity, BindEmailActivity.class));
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

	private class UnbindWeiboTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActivity
							.setSupportProgressBarIndeterminateVisibility(true);
				}
			});
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				JSONObject ret = api_client.account_unbind_weibo(null);
				if (ret.has("status")) {
					return true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mActivity
							.setSupportProgressBarIndeterminateVisibility(false);
				}
			});
			if (!result) {
				Toast.makeText(mActivity, "Unbind weibo failed!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			Preferences.clearWeiboUser(mActivity);
			setScreenName();
		}
	}

	private class BindWeiboTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActivity
							.setSupportProgressBarIndeterminateVisibility(true);
				}
			});
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean bind_ok = false;
			try {
				String id = params[0];
				String name = params[1];
				String icon = params[2];
				String token = params[3];
				String expiresTime = params[4];
				JSONObject ret = api_client.account_bind_weibo(id, name, icon,
						token, expiresTime, null);
				if (ret.has("status")) {
					bind_ok = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return bind_ok;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mActivity
							.setSupportProgressBarIndeterminateVisibility(false);
					setScreenName();
				}
			});
		}
	}

	@Subscribe
	public void platformAuthorized(PlatformAuthorizeEvent event) {
		if (!event.getStatus()) {
			Toast.makeText(mActivity, "Bind weibo failed!", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Preferences.storeWeiboUser(mActivity, Long.parseLong(event.getId()),
				event.getName(), event.getIcon());
		new BindWeiboTask().execute(event.getId(), event.getName(),
				event.getIcon(), event.getToken(), event.getExpiresTime() + "");
		mActivity.checkFollow();
	}
}
