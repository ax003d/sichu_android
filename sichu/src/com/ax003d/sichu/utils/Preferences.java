package com.ax003d.sichu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ax003d.sichu.R;

public class Preferences {

	public static boolean DEBUG = false;
	public static String SERVER;

	public static void setLoginInfo(Context context, String token,
			String refresh_token, long expire, long uid, String username,
			String avatar, String email) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		preferences.edit().putString("token", token)
				.putString("refresh_token", refresh_token)
				.putLong("expire", expire).putLong("uid", uid)
				.putString("username", username).putString("avatar", avatar)
				.putString("email", email).commit();
	}

	public static String getUserName(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("username", "");
	}

	public static String getAvatar(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("avatar", "");
	}

	public static void clearLoginInfo(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		preferences.edit().clear().commit();
	}

	public static void setRemember(Context context, String username,
			String password) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("username", username)
				.putString("password", password).commit();
	}

	public static long getUserID(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				"uid", -1);
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

	public static void setSyncID(Context context, String category, int id) {
		Log.d("sync_id", category + " " + id);
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putLong("sync_id_" + category, id).commit();
	}

	public static long getSyncID(Context context, String category) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				"sync_id_" + category, 0);
	}

	public static void storeWeiboUser(Context context, long uid,
			String screenName, String profileImageUrl) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		preferences.edit().putLong("wb_uid", uid)
				.putString("wb_screen_name", screenName)
				.putString("wb_profile_image_url", profileImageUrl).commit();

	}

	public static void clearWeiboUser(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		preferences.edit().remove("wb_uid").remove("wb_screen_name")
				.remove("wb_profile_image_url").commit();
	}

	public static String getWeiboScreenName(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("wb_screen_name", null);
	}

	public static long getWeiboUID(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				"wb_uid", -1);
	}

	public static String getEmail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("email", "");
	}

	public static void setEmail(String email, Context context) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("email", email).commit();
	}
}
