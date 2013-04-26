package com.ax003d.sichu;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookBorrow;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;
import com.ax003d.sichu.utils.WeiboAuthDialogListener;
import com.ax003d.sichu.utils.WeiboUtils;
import com.weibo.sdk.android.sso.SsoHandler;

public class LoginActivity extends Activity implements OnClickListener {

	private boolean remember = false;
	private ISichuAPI api_client;
	SsoHandler mSsoHandler;
	private ProgressDialog mDialog;
	private Handler weiboErrorHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			mSsoHandler = new SsoHandler(LoginActivity.this,
					WeiboUtils.getWeiboInstance());
			mSsoHandler.authorize(new WeiboAuthDialogListener(this));
			break;

		default:
			break;
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
						Preferences.setSyncID(LoginActivity.this, BookOwn.CATEGORY, 0);
						Preferences.setSyncID(LoginActivity.this, BookBorrow.CATEGORY, 0);
						Preferences.setSyncID(LoginActivity.this, BookBorrowReq.CATEGORY, 0);
						Preferences.setSyncID(LoginActivity.this, Follow.CATEGORY, 0);
					}
					Preferences.setLoginInfo(getApplicationContext(),
							ret.getString("token"),
							ret.getString("refresh_token"),
							ret.getLong("expire"), ret.getLong("uid"),
							ret.getString("username"), ret.getString("avatar"));
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
			if (mDialog == null) {
				mDialog = Utils.createLoginDialog(this);			
			}
			if (resultCode == RESULT_OK) {
				mDialog.show();
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
			Toast.makeText(this, R.string.err_login_by_weibo, Toast.LENGTH_SHORT).show();
		}
	}
}