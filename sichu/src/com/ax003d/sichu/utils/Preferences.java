package com.ax003d.sichu.utils;

import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ax003d.sichu.R;

public class Preferences {

	public static boolean DEBUG = false;
	public static String SERVER;

	public static void setLoginInfo(Context context, String token,
			String refresh_token, long expire, long uid) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		preferences.edit().putString("token", token)
				.putString("refresh_token", refresh_token)
				.putLong("expire", expire).putLong("uid", uid).commit();
	}

	public static void setRemember(Context context, String username,
			String password) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("username", username)
				.putString("password", password).commit();
	}

	public static long getUserID(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getLong("uid", -1);
	}
	
	public static String getRememberedUsername(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("username", "");
	}

	public static String getRememberedPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("password", "");
	}

	public static String getToken(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("token", null);
	}

	public static long getExpire(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				"expire", 0);
	}

	public static void expireToken(Context context) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putLong("expire", 0).commit();
	}

	public static String getServer(Context context) {
		if (Preferences.SERVER != null) {
			return Preferences.SERVER;
		}

		if (Preferences.DEBUG) {
			Preferences.SERVER = context.getString(R.string.debug_server);
		} else {
			Preferences.SERVER = context.getString(R.string.production_server);
		}

		return Preferences.SERVER;
	}

	public static void setSyncTime(Context context) {
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		int offset = tz.getOffset(now.getTime());
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putLong("sync_time", (now.getTime() - offset) / 1000).commit();
	}

	public static void setSyncTime(Context context, long timestamp) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putLong("sync_time", timestamp).commit();
	}

	public static long getSyncTime(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				"sync_time", 0);
	}
}
