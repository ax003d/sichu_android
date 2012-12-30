package com.sinaapp.sichu.fragments;

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
import android.view.View;
import android.view.ViewGroup;

import com.sinaapp.sichu.R;
import com.sinaapp.sichu.adapters.BookLoanedListAdapter;
import com.sinaapp.sichu.api.ISichuAPI;
import com.sinaapp.sichu.api.SichuAPI;
import com.sinaapp.sichu.models.BookBorrow;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.models.BookOwn.BookOwns;
import com.sinaapp.sichu.utils.Preferences;

public class LoanedBooksFragment extends Fragment {
	private static LoanedBooksFragment instance;

	public static LoanedBooksFragment getInstance() {
		if (LoanedBooksFragment.instance == null) {
			return new LoanedBooksFragment();
		}
		return LoanedBooksFragment.instance;
	}

	private BookLoanedListAdapter adapter;
	private SlidingActivity activity;
	private ISichuAPI api_client;
	private long userID;
	private ListView lst_books_loaned;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		activity = (SlidingActivity) getActivity();
		adapter = new BookLoanedListAdapter(activity);
		api_client = SichuAPI.getInstance(activity);
		userID = Preferences.getUserID(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_loanedbooks, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_books_loaned = (ListView) getActivity().findViewById(
				R.id.lst_books_loaned);
		lst_books_loaned.setAdapter(adapter);
		activity.setSupportProgressBarIndeterminateVisibility(false);
		new GetBooksLoanedTask().execute();
//		if (Preferences.getSyncTime(activity.getApplicationContext()) == 0) {
//		} else {
//		}
	}

	private class GetBooksLoanedTask extends
			AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 0) {
					ret = api_client.bookborrow(null, null);
				} else {
					ret = api_client.bookborrow(params[0], null);
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
		protected void onPreExecute() {
			super.onPreExecute();
			activity.setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			Cursor cursor = null;
			BookOwn own = null;
			
			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBooksLoaned;
				try {
					jBooksLoaned = result.getJSONArray("objects");
					for (int i = 0; i < jBooksLoaned.length(); i++) {
						// get bookown by ownership
						BookBorrow borrow = new BookBorrow(
								jBooksLoaned.getJSONObject(i));
						cursor = contentResolver.query(Uri.withAppendedPath(
								BookOwns.CONTENT_URI, "owner/" + userID),
								null, BookOwns.TABLE_NAME + "." +  BookOwns.GUID
										+ " = " + borrow.getBookOwnID(), null,
								null);
						if ( cursor.moveToNext() ) {
							own = new BookOwn(cursor);
						} else {
							// to-do: get bookown from server
						}
						borrow.setBookOwn(own);
						adapter.addBookBorrow(borrow);
					}
					adapter.notifyDataSetChanged();
					Preferences.setSyncTime(activity.getApplicationContext());
					activity.setSupportProgressBarIndeterminateVisibility(false);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBooksLoanedTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} // onPostExecute
	} // GetBooksLoanedTask
}
