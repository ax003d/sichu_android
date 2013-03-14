package com.ax003d.sichu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.ax003d.sichu.adapters.BookOwnListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.User;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FriendDetailActivity extends Activity {

	private User mFriend;
	private String mRemark;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private ListView lst_books;
	private ISichuAPI api_client;
	private BookOwnListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);

		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);
		api_client = SichuAPI.getInstance(this);
		adapter = new BookOwnListAdapter(this);
		lst_books = (ListView) findViewById(R.id.lst_books);
		lst_books.setAdapter(adapter);

		ImageView img_avatar = (ImageView) findViewById(R.id.img_avatar);
		TextView txt_username = (TextView) findViewById(R.id.txt_username);
		TextView txt_remark = (TextView) findViewById(R.id.txt_remark);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
		}
		mFriend = (User) extras.getParcelable("friend");
		mRemark = (String) extras.getString("remark");

		img_loader.displayImage(mFriend.getAvatar(), img_avatar, options);
		txt_username.setText(mFriend.getUsername());
		if (mRemark != null) {
			txt_remark.setText(" (" + mRemark + ")");
		} else {
			txt_remark.setText("");
		}

		new GetBookOwnTask().execute();
	}

	private class GetBookOwnTask extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 0) {
					ret = api_client.bookown(mFriend.getGuid() + "", false,
							null, null);
				} else {
					ret = api_client.bookown(null, false, params[0], null);
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
			if (result != null && result.has("objects")) {
				JSONArray jBookOwns;
				try {
					jBookOwns = result.getJSONArray("objects");
					for (int i = 0; i < jBookOwns.length(); i++) {
						BookOwn own = new BookOwn(jBookOwns.getJSONObject(i));
						adapter.addBookOwn(own);
					}
					adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBookOwnTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		} // onPostExecute
	} // GetBookOwnTask
}
