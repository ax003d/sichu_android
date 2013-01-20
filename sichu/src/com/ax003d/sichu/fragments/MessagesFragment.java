package com.ax003d.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.MenuItem;
import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.MessageListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.Book.Books;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.models.BookBorrowReq.BookBorrowReqs;
import com.ax003d.sichu.models.BookOwn.BookOwns;
import com.ax003d.sichu.models.User.Users;
import com.ax003d.sichu.utils.Preferences;

public class MessagesFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final int BOOKBORROWREQ_LOADER = 5;
	private static MessagesFragment instance;
	private static String[] bookborrowreqProjection = new String[] {
			BookBorrowReqs.TABLE_NAME + "." + BookBorrowReqs.GUID,
			BookBorrowReqs.DATETIME, BookBorrowReqs.REQUESTERID,
			BookBorrowReqs.BOOKOWNID, BookBorrowReqs.PLANED_RETURN_DATE,
			BookBorrowReqs.TABLE_NAME + "." + BookBorrowReqs.REMARK,
			BookBorrowReqs.TABLE_NAME + "." + BookBorrowReqs.STATUS,
			BookOwns.TABLE_NAME + "." + BookOwns.OWNERID,
			Users.USERNAME, Users.AVATAR, Books.TITLE, Books.COVER };

	public static MessagesFragment getInstance() {
		if (MessagesFragment.instance == null) {
			MessagesFragment.instance = new MessagesFragment();
		}
		return MessagesFragment.instance;
	}

	private ISichuAPI api_client;
	private SlidingActivity activity;
	private long userID;
	private MessageListAdapter adapter;
	private ListView lst_msg;
	public boolean requery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		api_client = SichuAPI.getInstance(getActivity());
		activity = (SlidingActivity) getActivity();
		userID = Preferences.getUserID(activity);
		adapter = new MessageListAdapter(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_messages, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_msg = (ListView) activity.findViewById(R.id.lst_msg);
		lst_msg.setAdapter(adapter);
		activity.getSupportLoaderManager().initLoader(BOOKBORROWREQ_LOADER,
				null, this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_sync:
			requery = false;
			new GetBookBorrowReqTask().execute();
			break;
		}		

		return super.onOptionsItemSelected(item);
	}	

	private class GetBookBorrowReqTask extends
			AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 0) {
					ret = api_client.bookborrowrequest(null, null);
				} else {
					ret = api_client.bookborrowrequest(params[0], null);
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
			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBookBorrowRequests;
				try {
					jBookBorrowRequests = result.getJSONArray("objects");
					for (int i = 0; i < jBookBorrowRequests.length(); i++) {
						BookBorrowReq req = new BookBorrowReq(
								jBookBorrowRequests.getJSONObject(i));
						if (req.save(contentResolver) != null) {
							requery = true;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (requery) {
				activity.getSupportLoaderManager().restartLoader(
						BOOKBORROWREQ_LOADER, null, MessagesFragment.this);
				requery = false;
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBookBorrowReqTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}

	} // GetBookBorrowReqTask

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == BOOKBORROWREQ_LOADER) {
			return new CursorLoader(activity, Uri.withAppendedPath(
					BookBorrowReqs.CONTENT_URI, "user/" + userID),
					bookborrowreqProjection, null, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.clearBookBorrowReq();

		if (!data.moveToFirst()) {
			return;
		}

		do {
			adapter.addBookBorrowReq(new BookBorrowReq(data));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}
}
