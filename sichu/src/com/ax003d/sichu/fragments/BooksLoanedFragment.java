package com.ax003d.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.MenuItem;
import com.ax003d.sichu.MainActivity;
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
import com.ax003d.sichu.utils.Utils;

public class BooksLoanedFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	// Fragments in on activity should have different loader ids!
	private static final int BOOKBORROW_LOADER = 1;
	private static BooksLoanedFragment instance;
	private static String[] bookborrowsProjection = new String[] {
			BookBorrows.TABLE_NAME + "." + BookBorrows.GUID,
			BookBorrows.BOOKOWNID, BookBorrows.BORROWERID,
			BookBorrows.BORROW_DATE, BookBorrows.PLANED_RETURN_DATE,
			BookBorrows.RETURNED_DATE, "owner", Books.TITLE, Books.COVER,
			"borrower" };

	public static BooksLoanedFragment getInstance() {
		if (BooksLoanedFragment.instance == null) {
			BooksLoanedFragment.instance = new BooksLoanedFragment();
		}
		return BooksLoanedFragment.instance;
	}

	private BookLoanedListAdapter adapter;
	private MainActivity activity;
	private ISichuAPI api_client;
	private ListView lst_books_loaned;
	private long userID;
	private boolean requery;
	private int mClickItemPosition;
	private View lbl_no_loaned;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		activity = (MainActivity) getActivity();
		adapter = new BookLoanedListAdapter(activity);
		adapter.setAsBorrower(false);
		api_client = SichuAPI.getInstance(activity);
		userID = Preferences.getUserID(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_booksloaned, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_books_loaned = (ListView) getActivity().findViewById(
				R.id.lst_books_loaned);
		lst_books_loaned.setAdapter(adapter);
		lst_books_loaned.setOnItemClickListener(this);
		lbl_no_loaned = activity.findViewById(R.id.lbl_no_loaned);
		activity.getSupportLoaderManager().initLoader(BOOKBORROW_LOADER, null,
				this);
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
		if (Preferences.getSyncID(activity, BookBorrow.CATEGORY) == 0) {
			activity.getContentResolver().delete(BookBorrows.CONTENT_URI, null,
					null);
			new GetBooksLoanedTask().execute();
		} else {
			new Sync(activity).start_sync_task(BookBorrow.CATEGORY);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == BOOKBORROW_LOADER) {
			return new CursorLoader(activity, Uri.withAppendedPath(
					BookBorrows.CONTENT_URI, "owner/" + userID),
					bookborrowsProjection, null, null,
					BookBorrows.RETURNED_DATE + " ASC");
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.clearBookBorrow();

		if (!data.moveToFirst()) {
			if (lbl_no_loaned != null) {
				lbl_no_loaned.setVisibility(View.VISIBLE);
			}
			return;
		}

		if (lbl_no_loaned != null) {
			lbl_no_loaned.setVisibility(View.GONE);
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

	private class GetBooksLoanedTask extends
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
					Toast.makeText(activity, R.string.err_get_loaned,
							Toast.LENGTH_SHORT).show();
				}
			}

			if (requery) {
				activity.getSupportLoaderManager().restartLoader(
						BOOKBORROW_LOADER, null, BooksLoanedFragment.this);
				requery = false;
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBooksLoanedTask().execute(next);
					} else {
						new Sync(activity).set_sync_id(BookBorrow.CATEGORY);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		} // onPostExecute
	} // GetBooksLoanedTask

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mClickItemPosition = position;
		BookBorrow borrow = (BookBorrow) adapter.getItem(position);
		if (borrow.getReturnedDate() != null) {
			return;
		}
		Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_chk_book_return);
		builder.setMessage(R.string.msg_chk_book_return);
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BookBorrow borrow = (BookBorrow) adapter
						.getItem(mClickItemPosition);
				new ReturnBookTask().execute(borrow.getGuid() + "");
			}
		});
		builder.setNegativeButton(android.R.string.no, null);
		builder.create().show();
	}

	private class ReturnBookTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				return api_client.bookborrow__detail(params[0], null);
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
			if ((result == null) || result.has("error_code")) {
				Toast.makeText(activity, R.string.err_chk_book_return,
						Toast.LENGTH_SHORT).show();
			} else {
				BookBorrow borrow = new BookBorrow(result);
				adapter.replaceItem(borrow);
				ContentValues values = new ContentValues();
				values.put(BookBorrows.RETURNED_DATE,
						Utils.formatDateTime(borrow.getReturnedDate()));
				activity.getContentResolver().update(
						Uri.withAppendedPath(BookBorrows.CONTENT_URI, "guid/"
								+ borrow.getGuid()), values, null, null);
				Toast.makeText(activity, R.string.ok_chk_book_return,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
