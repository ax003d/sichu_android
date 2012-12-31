package com.sinaapp.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.sinaapp.sichu.R;
import com.sinaapp.sichu.adapters.BookOwnListAdapter;
import com.sinaapp.sichu.api.ISichuAPI;
import com.sinaapp.sichu.api.SichuAPI;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.models.BookOwn.BookOwns;
import com.sinaapp.sichu.utils.Preferences;

public class BooksMineFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final int BOOKOWN_LOADER = 0;
	private static BooksMineFragment instance;

	public static BooksMineFragment getInstance() {
		if (BooksMineFragment.instance == null) {
			return new BooksMineFragment();
		}
		return BooksMineFragment.instance;
	}

	private BookOwnListAdapter adapter;
	private ListView lst_bookown;
	private ISichuAPI api_client;
	private SlidingActivity activity;
	private long userID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		adapter = new BookOwnListAdapter(getActivity());
		api_client = SichuAPI.getInstance(getActivity());
		activity = (SlidingActivity) getActivity();
		userID = Preferences.getUserID(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mybooks, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_bookown = (ListView) getActivity().findViewById(R.id.lst_bookowns);
		lst_bookown.setAdapter(adapter);
		activity.setSupportProgressBarIndeterminateVisibility(false);
		activity.getSupportLoaderManager().initLoader(BOOKOWN_LOADER, null,
				this);
		if (Preferences.getSyncTime(activity.getApplicationContext()) == 0) {
			// new GetBookOwnTask().execute();
		} else {
			// new SyncTask().execute();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_mybooks, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_scan) {
			IntentIntegratorSupportV4 integrator = new IntentIntegratorSupportV4(
					this);
			integrator.initiateScan();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		IntentResult scanResult = IntentIntegratorSupportV4
				.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			new AddBookOwnTask().execute(scanResult.getContents());
		}
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
				adapter.addBookOwn(own);
				adapter.notifyDataSetChanged();
				Toast.makeText(activity, "Book added!", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(activity, "Adid Book failed!",
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
		
		if ( !data.moveToFirst() ) {
			return;
		}
		
		do {
			adapter.addBookOwn(new BookOwn(data));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		// TODO Auto-generated method stub

	}
}