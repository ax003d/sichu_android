package com.sinaapp.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.widget.Toast;
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

import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.sinaapp.sichu.R;
import com.sinaapp.sichu.adapters.BookLoanedListAdapter;
import com.sinaapp.sichu.api.ISichuAPI;
import com.sinaapp.sichu.api.SichuAPI;
import com.sinaapp.sichu.models.Book.Books;
import com.sinaapp.sichu.models.BookBorrow;
import com.sinaapp.sichu.models.BookBorrow.BookBorrows;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.utils.Preferences;

public class BooksBorrowedFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	// Fragments in on activity should have different loader ids!
	private static final int BOOKBORROW_BORROW_LOADER = 2;
	private static BooksBorrowedFragment instance;
	private static String[] bookborrowsProjection = new String[] {
			BookBorrows.TABLE_NAME + "." + BookBorrows.GUID,
			BookBorrows.BOOKOWNID, BookBorrows.BORROWERID,
			BookBorrows.BORROW_DATE, BookBorrows.PLANED_RETURN_DATE,
			BookBorrows.RETURNED_DATE, "owner", Books.TITLE, Books.COVER,
			"borrower" };

	public static BooksBorrowedFragment getInstance() {
		if (BooksBorrowedFragment.instance == null) {
			return new BooksBorrowedFragment();
		}
		return BooksBorrowedFragment.instance;
	}

	private BookLoanedListAdapter adapter;
	private SlidingActivity activity;
	private ISichuAPI api_client;
	private PullToRefreshListView lst_books_borrowed;
	private long userID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		activity = (SlidingActivity) getActivity();
		adapter = new BookLoanedListAdapter(activity);
		adapter.setAsBorrower(true);
		api_client = SichuAPI.getInstance(activity);
		userID = Preferences.getUserID(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_borrowedbooks, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_books_borrowed = (PullToRefreshListView) getActivity()
				.findViewById(R.id.lst_books_borrowed);
		lst_books_borrowed.setAdapter(adapter);
		lst_books_borrowed.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new GetBooksBorrowedTask().execute();
			}
		});
		activity.getSupportLoaderManager().initLoader(BOOKBORROW_BORROW_LOADER,
				null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == BOOKBORROW_BORROW_LOADER) {
			return new CursorLoader(activity, Uri.withAppendedPath(
					BookBorrows.CONTENT_URI, "borrower/" + userID),
					bookborrowsProjection, null, null,
					BookBorrows.RETURNED_DATE + " ASC");
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.clearBookBorrow();

		if (!data.moveToFirst()) {
			return;
		}

		do {
			adapter.addBookBorrow(new BookBorrow(data));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
		lst_books_borrowed.onRefreshComplete();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.clearBookBorrow();
	}

	private class GetBooksBorrowedTask extends
			AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 0) {
					ret = api_client.bookborrow(null, true, null);
				} else {
					ret = api_client.bookborrow(params[0], true, null);
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
			BookOwn own = null;

			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBooksLoaned;
				try {
					jBooksLoaned = result.getJSONArray("objects");
					for (int i = 0; i < jBooksLoaned.length(); i++) {
						BookBorrow borrow = new BookBorrow(
								jBooksLoaned.getJSONObject(i));
						own = new BookOwn(jBooksLoaned.getJSONObject(i)
								.getJSONObject("ownership"));
						if (own != null) {
							own.save(contentResolver);
						}
						borrow.setBookOwn(own);
						borrow.save(contentResolver);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(activity, "Get borrow record error!",
							Toast.LENGTH_SHORT).show();
				}
			}

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBooksBorrowedTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			activity.getSupportLoaderManager().restartLoader(BOOKBORROW_BORROW_LOADER, null,
					BooksBorrowedFragment.this);									
			lst_books_borrowed.onRefreshComplete();
			super.onPostExecute(result);
		} // onPostExecute
	} // GetBooksBorrowedTask

}
