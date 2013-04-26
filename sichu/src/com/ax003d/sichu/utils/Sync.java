package com.ax003d.sichu.utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.LinearLayout;

import com.ax003d.sichu.R;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookBorrow;
import com.ax003d.sichu.models.BookBorrow.BookBorrows;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.models.BookBorrowReq.BookBorrowReqs;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.BookOwn.BookOwns;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.models.Follow.Follows;

public class Sync {

	private SlidingActivity mActivity;
	private ISichuAPI api_client;
	private long userID;
	private int mAddNum = 0;
	private int mUpdateNum = 0;
	private int mDeleteNum = 0;

	public Sync(SlidingActivity activity) {
		mActivity = activity;
		api_client = SichuAPI.getInstance(mActivity);
		userID = Preferences.getUserID(mActivity);
	}

	public void start_sync_task(String category) {
		mAddNum = 0;
		mUpdateNum = 0;
		mDeleteNum = 0;
		new SyncTask().execute(category);
	}

	public void set_sync_id(String category) {
		new GetSyncIDTask().execute(category);
	}

	private class GetSyncIDTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				JSONObject oplog__latest = api_client.oplog__latest(params[0],
						null);
				if (oplog__latest.has("id")) {
					Preferences.setSyncID(mActivity, BookBorrow.CATEGORY,
							oplog__latest.getInt("id"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	} // GetSyncIDTask

	private class SyncTask extends AsyncTask<String, LinearLayout, JSONObject> {

		private String mCategory = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mActivity.setSupportProgressBarIndeterminateVisibility(true);
		}

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
				ContentResolver contentResolver = mActivity
						.getContentResolver();
				try {
					JSONArray jOplogs = result.getJSONArray("objects");
					for (int i = 0; i < jOplogs.length(); i++) {
						JSONObject log = jOplogs.getJSONObject(i);
						switch (log.getInt("opcode")) {
						case 1:
							mAddNum++;
							add_object(contentResolver, log);
							break;
						case 2:
							mUpdateNum++;
							update_object(contentResolver, log);
							break;
						case 3:
							mDeleteNum++;
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
						contentResolver.notifyChange(Uri.withAppendedPath(
								Follows.CONTENT_URI, "following/" + userID),
								null);
					} else if (mCategory.equals(BookBorrowReq.CATEGORY)) {
						contentResolver.notifyChange(Uri.withAppendedPath(
								BookBorrowReqs.CONTENT_URI, "user/" + userID),
								null);
					} else if (mCategory.equals(BookBorrow.CATEGORY)) {
						contentResolver.notifyChange(Uri.withAppendedPath(
								BookBorrows.CONTENT_URI, "owner/" + userID),
								null);
						contentResolver.notifyChange(Uri.withAppendedPath(
								BookBorrows.CONTENT_URI, "borrower/" + userID),
								null);
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			} // endif
			mActivity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new SyncTask().execute(next, mCategory);
					} else {
						if (mAddNum + mUpdateNum + mDeleteNum > 0) {
							Toast.makeText(
									mActivity,
									String.format(mActivity
											.getString(R.string.msg_sync_ok),
											mAddNum, mUpdateNum, mDeleteNum),
									Toast.LENGTH_SHORT).show();
						}
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
			} else if (mCategory.equals(BookBorrowReq.CATEGORY)) {
				JSONObject ret = new JSONObject(log.getString("data"));
				contentResolver.delete(
						Uri.withAppendedPath(BookBorrowReqs.CONTENT_URI,
								"/guid/" + ret.getInt("id")), null, null);
			} else if (mCategory.equals(BookBorrow.CATEGORY)) {
				JSONObject ret = new JSONObject(log.getString("data"));
				contentResolver.delete(
						Uri.withAppendedPath(BookBorrows.CONTENT_URI, "/guid/"
								+ ret.getInt("id")), null, null);
			}
			Preferences.setSyncID(mActivity, mCategory, log.getInt("id"));
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
			} else if (mCategory.equals(BookBorrowReq.CATEGORY)) {
				BookBorrowReq req = new BookBorrowReq(new JSONObject(
						log.getString("data")));
				if (req != null) {
					req.update(contentResolver);
				}
			} else if (mCategory.equals(BookBorrow.CATEGORY)) {
				BookBorrow borrow = new BookBorrow(new JSONObject(
						log.getString("data")));
				if (borrow != null) {
					borrow.update(contentResolver);
				}
			}
			Preferences.setSyncID(mActivity, mCategory, log.getInt("id"));
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
			} else if (mCategory.equals(BookBorrowReq.CATEGORY)) {
				BookBorrowReq req = new BookBorrowReq(new JSONObject(
						log.getString("data")));
				if (req != null) {
					req.save(contentResolver);
				}
			} else if (mCategory.equals(BookBorrow.CATEGORY)) {
				BookBorrow borrow = new BookBorrow(new JSONObject(
						log.getString("data")));
				if (borrow != null) {
					borrow.save(contentResolver);
				}
			}
			Preferences.setSyncID(mActivity, mCategory, log.getInt("id"));
		}
	} // SyncTask
}
