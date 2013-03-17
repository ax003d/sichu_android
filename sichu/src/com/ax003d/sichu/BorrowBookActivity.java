package com.ax003d.sichu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.DatePicker;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.Book;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BorrowBookActivity extends Activity implements OnClickListener {
	private BookOwn mBookOwn;
	private ISichuAPI api_client;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private DatePicker dp_return_date;
	private EditText edit_remark;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_borrow_book);

		api_client = SichuAPI.getInstance(getApplicationContext());
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);

		findViewById(R.id.btn_douban).setOnClickListener(this);
		findViewById(R.id.btn_send).setOnClickListener(this);
		ImageView img_cover = (ImageView) findViewById(R.id.img_cover);
		TextView txt_title = (TextView) findViewById(R.id.txt_title);
		TextView txt_author = (TextView) findViewById(R.id.txt_author);
		TextView txt_isbn = (TextView) findViewById(R.id.txt_isbn);
		TextView txt_owner = (TextView) findViewById(R.id.txt_owner);
		TextView txt_status = (TextView) findViewById(R.id.txt_status);
		edit_remark = (EditText) findViewById(R.id.edit_remark);
		dp_return_date = (DatePicker) findViewById(R.id.dp_return_date);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}

		mBookOwn = extras.getParcelable("bookown");
		Book book = mBookOwn.getBook();
		img_loader.displayImage(book.getCover().replace("lpic", "spic"),
				img_cover, options);
		txt_title.setText(book.getTitle());
		txt_author.setText(book.getAuthor());
		txt_isbn.setText(book.getISBN());
		txt_owner.setText(mBookOwn.getOwner().getUsername());
		txt_status
				.setText(getResources().getStringArray(R.array.bookown_status)[mBookOwn
						.getStatus() - 1]);
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
		case R.id.btn_send:
			int year = dp_return_date.getYear();
			int month = dp_return_date.getMonth() + 1;
			int day = dp_return_date.getDayOfMonth();
			String planed_return_date = String.format("%04d-%02d-%02d", year,
					month, day);
			String remark = edit_remark.getText().toString();
			Log.d("borrow", planed_return_date + " remark");
			new SendBookBorrowRequestTask().execute(planed_return_date, remark);
			break;

		default:
			break;
		}
	}

	private class SendBookBorrowRequestTask extends
			AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				JSONObject ret = api_client.bookborrow__add(mBookOwn.getGuid()
						+ "", params[0], params[1], null);
				return ret.has("status");
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
			Toast.makeText(
					BorrowBookActivity.this,
					result ? "Borrow Request sent!"
							: "Send borrow request failed!", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
