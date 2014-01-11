package com.ax003d.sichu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.Window;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;

public class BindEmailActivity extends Activity {

	private EditText et_email;
	private TextView tv_success;
	private TextView tv_failed;

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_verify:
				String email = et_email.getText().toString();
				if (TextUtils.isEmpty(email) || !Utils.isEmailValid(email)) {
					Toast.makeText(BindEmailActivity.this, "Email not valid!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				new VerifyTask().execute(email);
				break;

			default:
				break;
			}
		}
	};

	private ISichuAPI api_client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_bind_email);
		
		et_email = (EditText) findViewById(R.id.et_email);
		et_email.setText(Preferences.getEmail(this));
		tv_success = (TextView) findViewById(R.id.tv_success);
		tv_failed = (TextView) findViewById(R.id.tv_failed);
		findViewById(R.id.btn_verify).setOnClickListener(onClickListener);

		api_client = SichuAPI.getInstance(this);
	}

	private class VerifyTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				JSONObject resp = api_client
						.accountEmailVerify(params[0], null);
				if (resp.has("OK")) {
					return true;
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
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setSupportProgressBarIndeterminateVisibility(false);
			if (result) {
				tv_success.setVisibility(View.VISIBLE);
				tv_failed.setVisibility(View.GONE);
				Preferences.setEmail(et_email.getText().toString(), BindEmailActivity.this);
			} else {
				tv_success.setVisibility(View.GONE);
				tv_failed.setVisibility(View.VISIBLE);
			}
		}
	}
}
