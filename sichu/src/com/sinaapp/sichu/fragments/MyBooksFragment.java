package com.sinaapp.sichu.fragments;

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
import android.content.ContentValues;
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
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.sinaapp.sichu.R;
import com.sinaapp.sichu.adapters.BookOwnListAdapter;
import com.sinaapp.sichu.api.ISichuAPI;
import com.sinaapp.sichu.api.SichuAPI;
import com.sinaapp.sichu.models.Book.Books;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.models.BookOwn.BookOwns;
import com.sinaapp.sichu.utils.Preferences;

public class MyBooksFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final int BOOKOWN_LOADER = 0;
	private static MyBooksFragment instance;

	public static MyBooksFragment getInstance() {
		if (MyBooksFragment.instance == null) {
			return new MyBooksFragment();
		}
		return MyBooksFragment.instance;
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
			new GetBookOwnTask().execute();
		} else {
			new SyncTask().execute();
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

	private class GetBookOwnTask extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 0) {
					ret = api_client.bookown(null, null);
				} else {
					ret = api_client.bookown(params[0], null);
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
			Cursor cursor;

			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBookOwns;
				try {
					jBookOwns = result.getJSONArray("objects");
					for (int i = 0; i < jBookOwns.length(); i++) {
						BookOwn own = new BookOwn(jBookOwns.getJSONObject(i));
						adapter.addBookOwn(own);
						cursor = contentResolver.query(
								Uri.withAppendedPath(Books.CONTENT_URI, "guid/"
										+ own.getBook().getGuid()), null, null,
								null, null);
						ContentValues values = new ContentValues();
						if (cursor.getCount() == 0) {
							own.getBook().setContentValues(values);
							contentResolver.insert(Books.CONTENT_URI, values);
							values.clear();
						}

						cursor = contentResolver.query(Uri.withAppendedPath(
								BookOwns.CONTENT_URI, "guid/" + own.getGuid()),
								null, null, null, null);
						if (cursor.getCount() == 0) {
							own.setContentValues(values, userID);
							contentResolver
									.insert(BookOwns.CONTENT_URI, values);
						}
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
						new GetBookOwnTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} // onPostExecute
	} // GetBookOwnTask

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

	private class SyncTask extends AsyncTask<String, LinearLayout, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				if (params.length == 0) {
					return api_client.oplog(null, null);
				} else {
					return api_client.oplog(params[0], null);
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
		protected void onPreExecute() {
			super.onPreExecute();
			activity.setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			Cursor cursor = null;
			boolean updated = false;
			
			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = activity.getContentResolver();
				try {
					JSONArray jOplogs = result.getJSONArray("objects");
					for (int i = 0; i < jOplogs.length(); i++) {
						JSONObject log = jOplogs.getJSONObject(i);
						switch (log.getInt("opcode")) {
						case 1:
							// add
							if (log.getString("model").equals(
									"sichu.cabinet.models.BookOwnership")) {
								BookOwn own = new BookOwn(new JSONObject(
										log.getString("data")));
								cursor = contentResolver.query(
										Uri.withAppendedPath(Books.CONTENT_URI, "guid/"
												+ own.getBook().getGuid()), null, null,
										null, null);
								ContentValues values = new ContentValues();
								if (cursor.getCount() == 0) {
									own.getBook().setContentValues(values);
									contentResolver.insert(Books.CONTENT_URI, values);
									values.clear();
								}
								own.setContentValues(values, userID);
								contentResolver.insert(BookOwns.CONTENT_URI,
										values);
								Preferences.setSyncTime(activity,
										log.getLong("timestamp"));
								updated = true;
							}
							break;
						case 2:
							// update
							if (log.getString("model").equals(
									"sichu.cabinet.models.BookOwnership")) {
								BookOwn own = new BookOwn(new JSONObject(
										log.getString("data")));
								ContentValues values = new ContentValues();
								own.setContentValues(values, userID);
								contentResolver.update(Uri.withAppendedPath(
										BookOwns.CONTENT_URI,
										"/guid/" + own.getGuid()), values,
										null, null);
								Preferences.setSyncTime(activity,
										log.getLong("timestamp"));
								updated = true;
							}
							break;
						case 3:
							// delete
							if (log.getString("model").equals(
									"sichu.cabinet.models.BookOwnership")) {
								JSONObject ret = new JSONObject(
										log.getString("data"));
								contentResolver.delete(Uri.withAppendedPath(
										BookOwns.CONTENT_URI,
										"/guid/" + ret.getInt("id")), null,
										null);
								Preferences.setSyncTime(activity,
										log.getLong("timestamp"));
								updated = true;
							}
							break;
						default:
							break;
						} // endswitch						
					} // endfor
					contentResolver.notifyChange(Uri.withAppendedPath(
								BookOwns.CONTENT_URI, "owner/" + userID), null);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			} // endif

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new SyncTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} // endif
			activity.setSupportProgressBarIndeterminateVisibility(false);
		} // onPostExecute
	} // SyncTask

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
		
		int idx_guid = data.getColumnIndex(BookOwns.GUID);
		int idx_bookID = data.getColumnIndex(BookOwns.BOOKID);
		int idx_ownerID = data.getColumnIndex(BookOwns.OWNERID);
		int idx_status = data.getColumnIndex(BookOwns.STATUS);
		int idx_hasEBook = data.getColumnIndex(BookOwns.HASEBOOK);
		int idx_remark = data.getColumnIndex(BookOwns.REMARK);
		int idx_ISBN = data.getColumnIndex(Books.ISBN);
		int idx_title = data.getColumnIndex(Books.TITLE);
		int idx_author = data.getColumnIndex(Books.AUTHOR);
		int idx_doubanID = data.getColumnIndex(Books.DOUBAN_ID);
		int idx_cover = data.getColumnIndex(Books.COVER);

		while(data.moveToNext()) {
			BookOwn own = new BookOwn(data.getLong(idx_guid),
					data.getLong(idx_bookID), data.getLong(idx_ownerID),
					data.getInt(idx_status), data.getInt(idx_hasEBook),
					data.getString(idx_remark), data.getString(idx_ISBN),
					data.getString(idx_title), data.getString(idx_author),
					data.getString(idx_doubanID), data.getString(idx_cover));
			adapter.addBookOwn(own);
		};
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		// TODO Auto-generated method stub

	}
}