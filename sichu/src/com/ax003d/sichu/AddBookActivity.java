package com.ax003d.sichu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
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
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;

public class AddBookActivity extends Activity {

	private ISichuAPI api_client;

	private EditText et_isbn;
	private OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btn_add) {
				Utils.hideKeyboard(AddBookActivity.this,
						et_isbn.getWindowToken());
				String isbn = et_isbn.getText().toString();
				if (TextUtils.isEmpty(isbn)) {
					Toast.makeText(AddBookActivity.this,
							R.string.err_empty_isbn, Toast.LENGTH_SHORT).show();
					return;
				}
				new AddBookOwnTask().execute(isbn);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_add_book);

		api_client = SichuAPI.getInstance(this);
		et_isbn = (EditText) findViewById(R.id.et_isbn);

		findViewById(R.id.btn_add).setOnClickListener(onClick);
	}

	private class AddBookOwnTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 1) {
					ret = api_client.bookownAdd(params[0], null, null, null);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			setSupportProgressBarIndeterminateVisibility(false);
			if (result != null && result.has("id")) {
				BookOwn own = new BookOwn(result);
				own.save(getContentResolver());
				et_isbn.setText("");
				Toast.makeText(AddBookActivity.this, R.string.success_add_book,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(AddBookActivity.this, R.string.failed_add_book,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
