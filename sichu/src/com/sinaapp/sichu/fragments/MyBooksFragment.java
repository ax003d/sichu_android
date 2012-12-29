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
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sinaapp.sichu.R;
import com.sinaapp.sichu.adapters.BookOwnListAdapter;
import com.sinaapp.sichu.api.ISichuAPI;
import com.sinaapp.sichu.api.SichuAPI;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.utils.Preferences;

public class MyBooksFragment extends Fragment {
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mybooks, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new BookOwnListAdapter();
		lst_bookown = (ListView) getActivity().findViewById(R.id.lst_bookowns);
		lst_bookown.setAdapter(adapter);
		api_client = SichuAPI.getInstance(getActivity());
		activity = (SlidingActivity) getActivity();
		new GetBookOwnTask().execute();
	}

	private class GetBookOwnTask extends
			AsyncTask<String, LinearLayout, JSONObject> {
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

			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBookOwns;
				try {
					jBookOwns = result.getJSONArray("objects");
					for (int i = 0; i < jBookOwns.length(); i++) {
						BookOwn own = new BookOwn(jBookOwns.getJSONObject(i));
						adapter.addBookOwn(own);
						ContentValues values = new ContentValues();
						// values.put(Tasks.GUID, task.getGuid());
						// values.put(Tasks.NAME, task.getName());
						// values.put(Tasks.TYPE, task.getTypeAsString());
						// values.put(Tasks.FINISH, task.getFinish());
						// values.put(Tasks.REMARK, task.getRemark());
						// contentResolver.insert(Tasks.CONTENT_URI, values);
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
}