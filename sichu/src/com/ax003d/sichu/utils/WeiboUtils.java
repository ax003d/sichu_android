package com.ax003d.sichu.utils;

import java.io.IOException;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.widget.CheckBox;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

import com.ax003d.sichu.R;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.net.RequestListener;

public class WeiboUtils {
	private static final String CONSUMER_KEY = "1157643996";
	private static final String REDIRECT_URL = "http://sichu.sinaapp.com/callback/weibo/authorize/";
	private static long MICABINET_UID = 2749010082L;
	private static Weibo g_weibo;
	private static boolean isFollower;

	public static Weibo getWeiboInstance() {
		if (g_weibo == null) {
			g_weibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);
		}
		return g_weibo;
	}
	
	public static boolean isFollower() {
		return WeiboUtils.isFollower;
	}	

	public static void checkIsFollower(final Context context, final long uid) {
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (accessToken == null) {
			return;
		}
		FriendshipsAPI friendships = new FriendshipsAPI(accessToken);
		friendships.show(uid, MICABINET_UID, new RequestListener() {

			@Override
			public void onIOException(IOException arg0) {
			}

			@Override
			public void onError(WeiboException arg0) {
			}

			@Override
			public void onComplete(String response) {
				try {
					JSONObject resp = new JSONObject(response);
					WeiboUtils.isFollower = resp.getJSONObject("target")
							.getBoolean("followed_by");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	} // checkIsFollower
	
	public static void askFollowing(Context context) {
		Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.following, null);
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.chk_following);
		builder.setTitle(R.string.title_following);
		builder.setView(view);
		builder.setCancelable(false);
		final Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!checkBox.isChecked()) {
					return;
				} 
				FriendshipsAPI friendships = new FriendshipsAPI(accessToken);
				friendships.create(MICABINET_UID, null, new RequestListener() {
					@Override
					public void onIOException(IOException arg0) {
					}
					
					@Override
					public void onError(WeiboException arg0) {
					}
					
					@Override
					public void onComplete(String arg0) {	
					}
				});
			}
		};
		builder.setPositiveButton(android.R.string.ok, onClickListener);
		builder.create().show();
	}	
}
