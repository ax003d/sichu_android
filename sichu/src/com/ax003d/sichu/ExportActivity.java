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

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.utils.Utils;

public class ExportActivity extends Activity {

	private View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_export:
				String email = et_email.getText().toString();
				if (TextUtils.isEmpty(email) || !Utils.isEmailValid(email)) {
					Toast.makeText(ExportActivity.this, "Email not valid!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				new ExportTask().execute(email);
				break;
			}
		}
	};

	private ISichuAPI api_client;

	private EditText et_email;

	private TextView tv_success;

	private TextView tv_failed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);

		et_email = (EditText) findViewById(R.id.et_email);
		tv_success = (TextView) findViewById(R.id.tv_success);
		tv_failed = (TextView) findViewById(R.id.tv_failed);
		findViewById(R.id.btn_export).setOnClickListener(onClickListener);

		api_client = SichuAPI.getInstance(this);
	}

	private class ExportTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				JSONObject resp = api_client.bookownExport(params[0], null);
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
			if (result) {
				tv_success.setVisibility(View.VISIBLE);
				tv_failed.setVisibility(View.GONE);
			} else {
				tv_success.setVisibility(View.GONE);
				tv_failed.setVisibility(View.VISIBLE);
			}
		}
	}
}
