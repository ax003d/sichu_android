package com.ax003d.sichu.fragments;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ax003d.sichu.AddBookActivity;
import com.ax003d.sichu.BooksEditActivity;
import com.ax003d.sichu.ExportActivity;
import com.ax003d.sichu.MainActivity;
import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.BookOwnListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.BookOwn.BookOwns;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Sync;

public class BooksMineFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	private static final int REQUEST_BARCODE = 0;
	private static final int BOOKOWN_LOADER = 0;
	private static BooksMineFragment instance;

	public static BooksMineFragment getInstance() {
		if (BooksMineFragment.instance == null) {
			BooksMineFragment.instance = new BooksMineFragment();
		}
		return BooksMineFragment.instance;
	}

	private BookOwnListAdapter adapter;
	private ListView lst_bookown;
	private ISichuAPI api_client;
	private MainActivity activity;
	private long userID;
	private boolean requery;
	private int mActionPosition;
	private View lbl_no_books;
	private EditText et_filter;
	private TextWatcher mOnFilterTextChanged = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			adapter.getFilter().filter(s.toString());
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		adapter = new BookOwnListAdapter(getActivity());
		api_client = SichuAPI.getInstance(getActivity());
		activity = (MainActivity) getActivity();
		userID = Preferences.getUserID(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_booksmine, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.setSupportProgressBarIndeterminateVisibility(false);
		et_filter = (EditText) activity.findViewById(R.id.et_filter);
		et_filter.addTextChangedListener(mOnFilterTextChanged );
		lst_bookown = (ListView) activity.findViewById(R.id.lst_bookowns);
		lst_bookown.setAdapter(adapter);
		lst_bookown.setOnItemClickListener(this);
		lbl_no_books = activity.findViewById(R.id.lbl_no_books);
		activity.getSupportLoaderManager().initLoader(BOOKOWN_LOADER, null,
				this);
		onMenuSyncTriggered();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_booksmine, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			Intent intent = new Intent("com.ax003d.sichu.SCAN");
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			PackageManager pm = activity.getPackageManager();
			List<ResolveInfo> availableApps = pm.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
			if (availableApps.size() > 0) {
				startActivityForResult(intent, REQUEST_BARCODE);
			}
			break;
		case R.id.menu_sync:
			onMenuSyncTriggered();
			break;
		case R.id.menu_export:
			startActivity(new Intent(activity, ExportActivity.class));
			break;
		case R.id.menu_add:
			startActivity(new Intent(activity, AddBookActivity.class));
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void onMenuSyncTriggered() {
		requery = false;
		if (Preferences.getSyncID(activity, BookOwn.CATEGORY) == 0) {
			activity.getContentResolver().delete(
					Uri.withAppendedPath(BookOwns.CONTENT_URI, "owner/"
							+ userID), null, null);
			new GetBookOwnTask().execute();
		} else {
			new Sync(activity).start_sync_task(BookOwn.CATEGORY);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_BARCODE) {
			if (resultCode == Activity.RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				String format = data.getStringExtra("SCAN_RESULT_FORMAT");
				Log.d("scan", contents + " " + format);
				new AddBookOwnTask().execute(contents);
			} else if (resultCode == Activity.RESULT_CANCELED) {
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.getSupportLoaderManager().restartLoader(BOOKOWN_LOADER, null,
				this);
	}

	private class AddBookOwnTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 1) {
					ret = api_client.bookownAdd(params[0], null, null, null);
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
			activity.setSupportProgressBarIndeterminateVisibility(false);
			if (result != null && result.has("id")) {
				BookOwn own = new BookOwn(result);
				adapter.prependBookOwn(own);
				own.save(activity.getContentResolver());
				adapter.notifyDataSetChanged();
				Toast.makeText(activity, R.string.success_add_book,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(activity, R.string.failed_add_book,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == BOOKOWN_LOADER) {
			return new CursorLoader(activity, Uri.withAppendedPath(
					BookOwns.CONTENT_URI, "owner/" + userID), null, null, null,
					BookOwns.TABLE_NAME + "." + BookOwns.GUID + " DESC");
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.clearBookOwn();

		if (!data.moveToFirst()) {
			if (lbl_no_books != null) {
				lbl_no_books.setVisibility(View.VISIBLE);
			}
			return;
		}

		if (lbl_no_books != null) {
			lbl_no_books.setVisibility(View.GONE);
		}
		do {
			adapter.addBookOwn(new BookOwn(data));
		} while (data.moveToNext());
		adapter.getFilter().filter("");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
	}

	private class GetBookOwnTask extends AsyncTask<String, Void, JSONObject> {
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
					ret = api_client.bookown(null, false, null, null);
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
				ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBookOwns;
				try {
					jBookOwns = result.getJSONArray("objects");
					for (int i = 0; i < jBookOwns.length(); i++) {
						BookOwn own = new BookOwn(jBookOwns.getJSONObject(i));
						if (own.save(contentResolver) != null) {
							requery = true;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (requery) {
				activity.getSupportLoaderManager().restartLoader(
						BOOKOWN_LOADER, null, BooksMineFragment.this);
				requery = false;
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBookOwnTask().execute(next);
					} else {
						new Sync(activity).set_sync_id(BookOwn.CATEGORY);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		} // onPostExecute
	} // GetBookOwnTask

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mActionPosition = position;
		Intent intent = new Intent(activity, BooksEditActivity.class);
		intent.putExtra("bookown", (BookOwn) adapter.getItem(mActionPosition));
		startActivity(intent);
	}
}