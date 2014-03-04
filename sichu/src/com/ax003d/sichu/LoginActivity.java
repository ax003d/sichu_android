package com.ax003d.sichu;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ProgressDialog;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookBorrow;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;

public class LoginActivity extends Activity implements OnClickListener {

	private boolean remember = false;
	private ISichuAPI api_client;
	private ProgressDialog mDialog;
	private Handler weiboErrorHandler;

	private PlatformActionListener paListener = new PlatformActionListener() {

		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			weiboErrorHandler.sendEmptyMessage(0);
		}

		@Override
		public void onComplete(Platform platform, int action,
				HashMap<String, Object> res) {
			if (action == Platform.ACTION_AUTHORIZING) {
				PlatformDb db = platform.getDb();
				String token = db.getToken();
				long expiresTime = db.getExpiresTime();
				String id = db.getUserId();
				String name = db.getUserName();
				String icon = db.getUserIcon();

				Preferences.storeWeiboUser(LoginActivity.this,
						Long.parseLong(id), name, icon);

				new LoginByWeiboTask().execute(id, name, icon, token,
						expiresTime + "");
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			// Do nothing
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ShareSDK.initSDK(this);
		setContentView(R.layout.activity_login);

		api_client = SichuAPI.getInstance(getApplicationContext());

		Date now = new Date();
		long expire = Preferences.getExpire(getApplicationContext()) * 1000;

		if (Preferences.getToken(getApplicationContext()) != null
				&& now.getTime() < expire) {
			startActivity(new Intent(getApplicationContext(),
					MainActivity.class));
			finish();
			return;
		}

		String username = Preferences.getRememberedUsername(getBaseContext());
		String password = Preferences.getRememberedPassword(getBaseContext());
		if (username.length() != 0 && password.length() != 0) {
			new LoginTask().execute(username, password);
		}

		EditText edit_username = (EditText) findViewById(R.id.edit_username);
		edit_username.setText(username);

		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_register).setOnClickListener(this);
		findViewById(R.id.btn_login_by_weibo).setOnClickListener(this);

		weiboErrorHandler = new WeiboErrorHandler(this);

		mDialog = Utils.createLoginDialog(LoginActivity.this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			if (v.getId() == R.id.btn_login) {
				if (!Utils.isNetworkAvailable(getBaseContext())) {
					Toast.makeText(getBaseContext(), R.string.hint_no_network,
							Toast.LENGTH_SHORT).show();
					return;
				}

				EditText edit_username = (EditText) findViewById(R.id.edit_username);
				EditText edit_password = (EditText) findViewById(R.id.edit_password);

				String username = edit_username.getText().toString();
				String password = edit_password.getText().toString();

				if (remember) {
					Preferences.setRemember(getApplicationContext(), username,
							password);
				}

				new LoginTask().execute(username, password);
			}
			break;
		case R.id.btn_register:
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.btn_login_by_weibo:
			Platform weibo = ShareSDK.getPlatform(this, SinaWeibo.NAME);
			weibo.setPlatformActionListener(paListener);
			weibo.authorize();
			break;

		default:
			break;
		}
	}

	private class LoginByWeiboTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mDialog.show();
				}
			});
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean login_ok = false;
			try {
				String id = params[0];
				String name = params[1];
				String icon = params[2];
				String token = params[3];
				String expiresTime = params[4];
				JSONObject ret = api_client.account_login_by_weibo(id, name,
						icon, token, expiresTime, null);
				if (ret.has("token")) {
					Preferences.setLoginInfo(LoginActivity.this,
							ret.getString("token"),
							ret.getString("refresh_token"),
							ret.getLong("expire"), ret.getLong("uid"),
							ret.getString("username"), ret.getString("avatar"),
							ret.getString("email"));
					login_ok = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return login_ok;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				sendMessage();
			} else {
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				intent.putExtra("ask_following", true);
				LoginActivity.this.startActivity(intent);
				LoginActivity.this.finish();
			}
		}
	}

	private class LoginTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				JSONObject ret = api_client.account_login(params[0], params[1],
						null);
				if (ret.has("token")) {
					long uid = ret.getLong("uid");
					if (Preferences.getUserID(LoginActivity.this) != uid) {
						Preferences.setSyncID(LoginActivity.this,
								BookOwn.CATEGORY, 0);
						Preferences.setSyncID(LoginActivity.this,
								BookBorrow.CATEGORY, 0);
						Preferences.setSyncID(LoginActivity.this,
								BookBorrowReq.CATEGORY, 0);
						Preferences.setSyncID(LoginActivity.this,
								Follow.CATEGORY, 0);
					}
					Preferences.setLoginInfo(getApplicationContext(),
							ret.getString("token"),
							ret.getString("refresh_token"),
							ret.getLong("expire"), ret.getLong("uid"),
							ret.getString("username"), ret.getString("avatar"),
							ret.getString("email"));
					return true;
				} else {
					Preferences.expireToken(getApplicationContext());
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
		protected void onPostExecute(Boolean ret) {
			super.onPostExecute(ret);
			if (ret) {
				startActivity(new Intent(getApplicationContext(),
						MainActivity.class));
				LoginActivity.this.finish();
			} else {
				Toast.makeText(getApplicationContext(), R.string.error_login,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static class WeiboErrorHandler extends Handler {
		private final WeakReference<LoginActivity> mActivity;

		public WeiboErrorHandler(LoginActivity activity) {
			mActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			LoginActivity activity = mActivity.get();
			if (activity != null) {
				activity.closeDialog();
			}
		}
	}

	public void sendMessage() {
		weiboErrorHandler.sendEmptyMessage(0);
	}

	public void closeDialog() {
		if (mDialog != null) {
			mDialog.cancel();
			Toast.makeText(this, R.string.err_login_by_weibo,
					Toast.LENGTH_SHORT).show();
		}
	}
}