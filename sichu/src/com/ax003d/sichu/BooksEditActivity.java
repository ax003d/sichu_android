package com.ax003d.sichu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.actionbarsherlock.view.Window;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.Book;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BooksEditActivity extends Activity implements OnClickListener {
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private BookOwn mBookOwn;
	private ISichuAPI api_client;
	private Spinner spin_status;
	private EditText edit_remark;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_booksedit);

		api_client = SichuAPI.getInstance(getApplicationContext());
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);

		findViewById(R.id.btn_douban).setOnClickListener(this);
		findViewById(R.id.btn_save).setOnClickListener(this);

		ImageView img_cover = (ImageView) findViewById(R.id.img_cover);
		TextView txt_title = (TextView) findViewById(R.id.txt_title);
		TextView txt_author = (TextView) findViewById(R.id.txt_author);
		TextView txt_isbn = (TextView) findViewById(R.id.txt_isbn);
		spin_status = (Spinner) findViewById(R.id.spin_status);
		edit_remark = (EditText) findViewById(R.id.edit_remark);

		Bundle extras = getIntent().getExtras();
		mBookOwn = extras.getParcelable("bookown");
		Book book = mBookOwn.getBook();
		img_loader.displayImage(book.getCover().replace("lpic", "spic"),
				img_cover, options);
		txt_title.setText(book.getTitle());
		txt_author.setText(book.getAuthor());
		txt_isbn.setText(book.getISBN());
		spin_status.setSelection(mBookOwn.getStatus() - 1);
		edit_remark.setText(mBookOwn.getRemark());
		
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_douban:
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://book.douban.com/subject/"
							+ mBookOwn.getBook().getDoubanID() + "/"));
			startActivity(intent);
			break;
		case R.id.btn_save:
			new EditBookOwnTask().execute(mBookOwn.getGuid() + "",
					(spin_status.getSelectedItemPosition() + 1) + "",
					edit_remark.getText().toString());
			break;

		default:
			break;
		}
	}

	private class EditBookOwnTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			JSONObject ret;
			try {
				ret = api_client.bookownEdit(params[0], params[1], params[2],
						null);
				if (ret.has("status")) {
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
			setSupportProgressBarIndeterminateVisibility(false);
			if (result) {
				mBookOwn.setStatus((spin_status.getSelectedItemPosition() + 1) + "");
				mBookOwn.setRemark(edit_remark.getText().toString());
				mBookOwn.update(getContentResolver());
				Toast.makeText(BooksEditActivity.this,
						R.string.msg_edit_bookown, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(BooksEditActivity.this,
						R.string.err_edit_bookown, Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
}
