package com.ax003d.sichu.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.weibo.sdk.android.Weibo;

public class WeiboUtils {
	private static final String CONSUMER_KEY = "1059222096";
	private static final String REDIRECT_URL = "http://sichu.sinaapp.com/callback/weibo/authorize/";
	private static Weibo g_weibo;
	
	public static Weibo getWeiboInstance() {
		if (g_weibo == null) {
			g_weibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);
		}
		return g_weibo;
	}
	
	public static void storeUsersInfo(Context context,
			String response) {
		try {
			JSONObject json = new JSONObject(response);
			if (json.has("screen_name")) {
				long uid = json.getLong("id");
				String screenName = json.getString("screen_name");
				String name = json.getString("name");
				String profileImageUrl = json.getString("profile_image_url");
				Preferences.storeWeiboUser(context, uid, response, name,
						screenName, profileImageUrl);
			}
		} catch (JSONException e) {
		}
	}	
}
