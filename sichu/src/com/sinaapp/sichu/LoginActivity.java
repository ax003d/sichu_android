package com.sinaapp.sichu;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.sinaapp.sichu.api.ISichuAPI;
import com.sinaapp.sichu.api.SichuAPI;
import com.sinaapp.sichu.utils.Preferences;
import com.sinaapp.sichu.utils.Utils;

public class LoginActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {

	private boolean remember = false;
	private ISichuAPI api_client;

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

		Button btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(this);

		CheckBox chk_remember = (CheckBox) findViewById(R.id.chk_remember);
		chk_remember.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
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
	}

	private class LoginTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				JSONObject ret = api_client.account_login(params[0], params[1],
						null);
				if (ret.has("token")) {
					Preferences.setLoginInfo(getApplicationContext(),
							ret.getString("token"),
							ret.getString("refresh_token"),
							ret.getLong("expire"),
							ret.getLong("uid"));
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
				Toast.makeText(getApplicationContext(), "Login failed!",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.chk_remember) {
			if (isChecked) {
				remember = true;
			} else {
				remember = false;
				Preferences.setRemember(getApplicationContext(), "", "");
			}
		}
	}
}