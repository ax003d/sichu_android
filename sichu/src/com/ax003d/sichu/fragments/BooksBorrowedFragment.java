package com.ax003d.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.widget.ListView;
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

import com.actionbarsherlock.view.MenuItem;
import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.BookLoanedListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.Book.Books;
import com.ax003d.sichu.models.BookBorrow;
import com.ax003d.sichu.models.BookBorrow.BookBorrows;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Sync;

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
			BooksBorrowedFragment.instance = new BooksBorrowedFragment();
		}
		return BooksBorrowedFragment.instance;
	}

	private BookLoanedListAdapter adapter;
	private SlidingActivity activity;
	private ISichuAPI api_client;
	private ListView lst_books_borrowed;
	private long userID;
	private boolean requery;
	private View lbl_no_borrowed;

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
		return inflater.inflate(R.layout.fragment_booksborrowed, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_books_borrowed = (ListView) getActivity().findViewById(
				R.id.lst_books_borrowed);
		lst_books_borrowed.setAdapter(adapter);
		lbl_no_borrowed = activity.findViewById(R.id.lbl_no_borrowed);
		activity.getSupportLoaderManager().initLoader(BOOKBORROW_BORROW_LOADER,
				null, this);
		onMenuSyncTriggered();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_sync:
			onMenuSyncTriggered();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void onMenuSyncTriggered() {
		requery = false;
		if (Preferences.getSyncTime(activity, BookBorrow.CATEGORY) == 0) {
			activity.getContentResolver().delete(
					Uri.withAppendedPath(BookBorrows.CONTENT_URI, "borrower/"
							+ userID), null, null);
			new GetBooksBorrowedTask().execute();
		} else {
			new Sync(activity).start_sync_task(BookBorrow.CATEGORY);
		}
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
			if (lbl_no_borrowed != null) {
				lbl_no_borrowed.setVisibility(View.VISIBLE);
			}
			return;
		}

		if (lbl_no_borrowed != null) {
			lbl_no_borrowed.setVisibility(View.GONE);
		}
		do {
			adapter.addBookBorrow(new BookBorrow(data));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.clearBookBorrow();
	}

	private class GetBooksBorrowedTask extends
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
					ret = api_client.bookborrow(null, null, null);
				} else {
					ret = api_client.bookborrow(params[0], null, null);
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
						if (borrow.save(contentResolver) != null) {
							requery = true;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(activity, "Get borrow record error!",
							Toast.LENGTH_SHORT).show();
				}
			}

			if (requery) {
				activity.getSupportLoaderManager().restartLoader(
						BOOKBORROW_BORROW_LOADER, null,
						BooksBorrowedFragment.this);
				requery = false;
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBooksBorrowedTask().execute(next);
					} else {
						Preferences.setSyncTime(activity, BookBorrow.CATEGORY);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		} // onPostExecute
	} // GetBooksBorrowedTask

}
