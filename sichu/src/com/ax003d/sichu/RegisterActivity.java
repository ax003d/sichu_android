package com.ax003d.sichu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookBorrow;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.utils.Preferences;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public class RegisterActivity extends Activity implements OnClickListener {
	private EditText edit_username;
	private EditText edit_email;
	private EditText edit_pwd_1;
	private EditText edit_pwd_2;
	private ISichuAPI api_client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		api_client = SichuAPI.getInstance(getApplicationContext());

		findViewById(R.id.btn_cancel).setOnClickListener(this);
		findViewById(R.id.btn_register).setOnClickListener(this);

		edit_username = (EditText) findViewById(R.id.edit_username);
		edit_email = (EditText) findViewById(R.id.edit_email);
		edit_pwd_1 = (EditText) findViewById(R.id.edit_pwd_1);
		edit_pwd_2 = (EditText) findViewById(R.id.edit_pwd_2);
	}

	@Override
	@SuppressLint("NewApi")
	public void onBackPressed() {
		onCancel();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			onCancel();
			break;
		case R.id.btn_register:
			onRegister();
			break;

		default:
			break;
		}
	}

	private void onRegister() {
		if (TextUtils.isEmpty(edit_username.getText())) {
			Toast.makeText(this, R.string.empty_username, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (TextUtils.isEmpty(edit_email.getText())) {
			Toast.makeText(this, R.string.empty_email, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (TextUtils.isEmpty(edit_pwd_1.getText())
				|| edit_pwd_1.getText().length() < 6) {
			Toast.makeText(this, R.string.err_password_too_short,
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (!edit_pwd_1.getText().toString()
				.equals(edit_pwd_2.getText().toString())) {
			Toast.makeText(this, R.string.err_password_not_consistence,
					Toast.LENGTH_SHORT).show();
			return;
		}

		new RegisterTask().execute(edit_username.getText().toString(),
				edit_email.getText().toString(), edit_pwd_1.getText()
						.toString());
	}

	private void onCancel() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private class RegisterTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			try {
				JSONObject ret = api_client.account_register(params[0],
						params[1], params[2], null);
				if (ret.has("token")) {
					long uid = ret.getLong("uid");
					if (Preferences.getUserID(RegisterActivity.this) != uid) {
						Preferences.setSyncTime(RegisterActivity.this,
								BookOwn.CATEGORY, 0);
						Preferences.setSyncTime(RegisterActivity.this,
								BookBorrow.CATEGORY, 0);
						Preferences.setSyncTime(RegisterActivity.this,
								BookBorrowReq.CATEGORY, 0);
						Preferences.setSyncTime(RegisterActivity.this,
								Follow.CATEGORY, 0);

					}
					Preferences.setLoginInfo(getApplicationContext(),
							ret.getString("token"),
							ret.getString("refresh_token"),
							ret.getLong("expire"), ret.getLong("uid"),
							ret.getString("username"), ret.getString("avatar"));
					return 0;
				} else if (ret.has("error_code")) {
					String error_code = ret.getString("error_code");
					if (error_code.equals("6502")) {
						return 1;
					} else if (error_code.equals("6503")) {
						return 2;
					}
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
			return 3;
		}

		@Override
		protected void onPostExecute(Integer ret) {
			super.onPostExecute(ret);
			if (ret == 0) {
				startActivity(new Intent(getApplicationContext(),
						MainActivity.class));
				RegisterActivity.this.finish();
			} else if (ret == 1) {
				Toast.makeText(getApplicationContext(),
						R.string.err_username_not_available, Toast.LENGTH_SHORT)
						.show();
			} else if (ret == 2) {
				Toast.makeText(getApplicationContext(),
						R.string.err_email_not_available, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.err_register,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
