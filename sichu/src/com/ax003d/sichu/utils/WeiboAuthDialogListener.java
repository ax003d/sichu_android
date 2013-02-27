package com.ax003d.sichu.utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ax003d.sichu.MainActivity;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

public class WeiboAuthDialogListener implements WeiboAuthListener {

	private Activity mActivity;
	private long uid;
	private ISichuAPI api_client;
	private Oauth2AccessToken accessToken;

	public WeiboAuthDialogListener(Activity activity) {
		this.mActivity = activity;
		this.api_client = SichuAPI.getInstance(mActivity);
	}

	@Override
	public void onComplete(Bundle values) {
		uid = Long.parseLong(values.getString("uid"));
		String token = values.getString("access_token");
		String expires_in = values.getString("expires_in");
		accessToken = new Oauth2AccessToken(token, expires_in);
		if (accessToken.isSessionValid()) {
			AccessTokenKeeper.keepAccessToken(mActivity, accessToken);
			new Thread(new Runnable() {
				@Override
				public void run() {
					WeiboUtils.checkIsFollower(mActivity, uid);
				}
			}).start();
			UsersAPI users = new UsersAPI(accessToken);
			users.show(uid, new UsersShowListener());
		}
	}

	@Override
	public void onError(WeiboDialogError e) {
		Log.d("weibo", "error", e);
	}

	@Override
	public void onCancel() {
		Log.d("weibo", "cancel");
	}

	@Override
	public void onWeiboException(WeiboException e) {
		Log.d("weibo", "exception", e);
	}

	private class UsersShowListener implements RequestListener {
		@Override
		public void onIOException(IOException e) {
			Log.d("Weibo", "users show error", e);
		}

		@Override
		public void onError(WeiboException e) {
			Log.d("Weibo", "users show error", e);
		}

		@Override
		public void onComplete(String response) {
			JSONObject json;
			String screenName;
			String profileImageUrl;
			try {
				// store weibo user
				json = new JSONObject(response);
				screenName = json.getString("screen_name");
				profileImageUrl = json.getString("profile_image_url");
				Preferences.storeWeiboUser(mActivity, uid, screenName,
						profileImageUrl);
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}

			JSONObject ret;
			try {
				if (Utils.isLogin(mActivity)) {
					// bind weibo
					ret = api_client.account_bind_weibo(uid + "", screenName,
							profileImageUrl, accessToken.getToken(),
							accessToken.getExpiresTime() + "", null);
					if (ret.has("status")) {
						// bind ok
						MainActivity activity = (MainActivity) mActivity;
						activity.sendMessage();
					}
				} else {
					// login by weibo
					ret = api_client.account_login_by_weibo(uid + "",
							screenName, profileImageUrl,
							accessToken.getToken(),
							accessToken.getExpiresTime() + "", null);
					if (ret.has("token")) {
						Preferences.setLoginInfo(mActivity,
								ret.getString("token"),
								ret.getString("refresh_token"),
								ret.getLong("expire"), ret.getLong("uid"),
								ret.getString("username"),
								ret.getString("avatar"));
						Intent intent = new Intent(mActivity,
								MainActivity.class);
						intent.putExtra("ask_following", true);
						mActivity.startActivity(intent);
						mActivity.finish();
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	} // UsersShowListener
}