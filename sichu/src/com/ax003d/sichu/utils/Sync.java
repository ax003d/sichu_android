package com.ax003d.sichu.utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.LinearLayout;

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.models.BookOwn.BookOwns;
import com.ax003d.sichu.models.Follow.Follows;

public class Sync {

	private Context mContext;
	private ISichuAPI api_client;
	private long userID;

	public Sync(Context context) {
		mContext = context;
		api_client = SichuAPI.getInstance(mContext);
		userID = Preferences.getUserID(mContext);
	}

	public void start_sync_task(String category) {
		new SyncTask().execute(category);
	}

	private class SyncTask extends AsyncTask<String, LinearLayout, JSONObject> {

		private String mCategory = null;

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				if (params.length == 1) {
					mCategory = params[0];
					return api_client.oplog(null, mCategory, null);
				} else {
					mCategory = params[1];
					return api_client.oplog(params[0], null, null);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = mContext.getContentResolver();
				try {
					JSONArray jOplogs = result.getJSONArray("objects");
					for (int i = 0; i < jOplogs.length(); i++) {
						JSONObject log = jOplogs.getJSONObject(i);
						switch (log.getInt("opcode")) {
						case 1:
							add_object(contentResolver, log);
							break;
						case 2:
							update_object(contentResolver, log);
							break;
						case 3:
							delete_object(contentResolver, log);
							break;
						default:
							break;
						} // endswitch
					} // endfor
					if (mCategory.equals(BookOwn.CATEGORY)) {
						contentResolver.notifyChange(Uri.withAppendedPath(
								BookOwns.CONTENT_URI, "owner/" + userID), null);
					} else if (mCategory.equals(Follow.CATEGORY)) {
						contentResolver.notifyChange(Uri.withAppendedPath(
								Follows.CONTENT_URI, "user/" + userID), null);
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			} // endif

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new SyncTask().execute(next, mCategory);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} // endif
		} // onPostExecute

		private void delete_object(ContentResolver contentResolver,
				JSONObject log) throws JSONException {
			if (mCategory.equals(BookOwn.CATEGORY)) {
				JSONObject ret = new JSONObject(log.getString("data"));
				contentResolver.delete(
						Uri.withAppendedPath(BookOwns.CONTENT_URI, "/guid/"
								+ ret.getInt("id")), null, null);
			} else if (mCategory.equals(Follow.CATEGORY)) {
				JSONObject ret = new JSONObject(log.getString("data"));
				contentResolver.delete(
						Uri.withAppendedPath(Follows.CONTENT_URI, "/guid/"
								+ ret.getInt("id")), null, null);
			}
			Preferences.setSyncTime(mContext, mCategory,
					log.getLong("timestamp"));
		}

		private void update_object(ContentResolver contentResolver,
				JSONObject log) throws JSONException {
			if (mCategory.equals(BookOwn.CATEGORY)) {
				BookOwn own = new BookOwn(new JSONObject(log.getString("data")));
				if (own != null) {
					own.update(contentResolver);
				}
			} else if (mCategory.equals(Follow.CATEGORY)) {
				Follow follow = new Follow(
						new JSONObject(log.getString("data")));
				if (follow != null) {
					follow.update(contentResolver);
				}
			}
			Preferences.setSyncTime(mContext, mCategory,
					log.getLong("timestamp"));
		}

		private void add_object(ContentResolver contentResolver, JSONObject log)
				throws JSONException {
			if (mCategory.equals(BookOwn.CATEGORY)) {
				BookOwn own = new BookOwn(new JSONObject(log.getString("data")));
				if (own != null) {
					own.save(contentResolver);
				}
			} else if (mCategory.equals(Follow.CATEGORY)) {
				Follow follow = new Follow(
						new JSONObject(log.getString("data")));
				if (follow != null) {
					follow.save(contentResolver);
				}
			}
			Preferences.setSyncTime(mContext, mCategory,
					log.getLong("timestamp"));
		}
	} // SyncTask
}
