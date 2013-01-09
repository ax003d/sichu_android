package com.ax003d.sichu.utils;

import java.io.IOException;

import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

public class WeiboAuthDialogListener implements WeiboAuthListener {

	private Activity mActivity;
	private long uid;

	public WeiboAuthDialogListener(Activity activity) {
		this.mActivity = activity;
	}
	
    @Override
    public void onComplete(Bundle values) {
    	uid = Long.parseLong(values.getString("uid"));
        String token = values.getString("access_token");
        String expires_in = values.getString("expires_in");
        Oauth2AccessToken accessToken = new Oauth2AccessToken(token, expires_in);
        if (accessToken.isSessionValid()) {
			AccessTokenKeeper.keepAccessToken(mActivity, accessToken);
			UsersAPI users = new UsersAPI(accessToken);
			users.show(uid, new UsersShowListener());        	
        }
    }

    @Override
    public void onError(WeiboDialogError e) {
        Toast.makeText(mActivity,
                "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCancel() {
        Toast.makeText(mActivity, "Auth cancel",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWeiboException(WeiboException e) {
        Toast.makeText(mActivity,
                "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                .show();
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
			WeiboUtils.storeUsersInfo(mActivity, response);
			if (Utils.isLogin(mActivity)) {
				// bind weibo
			} else {
				// login by weibo
			}
		}
	} // UsersShowListener    
}