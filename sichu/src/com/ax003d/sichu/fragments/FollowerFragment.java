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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.MenuItem;
import com.ax003d.sichu.FriendDetailActivity;
import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.FollowListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.models.Follow.Follows;
import com.ax003d.sichu.models.User.Users;
import com.ax003d.sichu.utils.Preferences;

public class FollowerFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	private static final int FOLLOWER_LOADER = 4;
	private static FollowerFragment instance;
	private static String[] followerProjection = new String[] {
			Follows.TABLE_NAME + "." + Follows.GUID, Follows.FOLLOWINGID,
			Follows.REMARK, Follows.USERID,
			"Follower." + Users.USERNAME + " AS followerName",
			"Follower." + Users.AVATAR + " AS followerAvatar" };

	public static FollowerFragment getInstance() {
		if (FollowerFragment.instance == null) {
			FollowerFragment.instance = new FollowerFragment();
		}
		return FollowerFragment.instance;
	}

	private FollowListAdapter adapter;
	private ListView lst_follower;
	private ISichuAPI api_client;
	private SlidingActivity activity;
	private long userID;
	private boolean requery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		api_client = SichuAPI.getInstance(getActivity());
		activity = (SlidingActivity) getActivity();
		userID = Preferences.getUserID(activity);
		adapter = new FollowListAdapter(activity);
		adapter.setAsFollower(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_follower, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_follower = (ListView) activity.findViewById(R.id.lst_follower);
		lst_follower.setAdapter(adapter);
		lst_follower.setOnItemClickListener(this);
		activity.getSupportLoaderManager().initLoader(FOLLOWER_LOADER, null,
				this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_sync:
			requery = false;
			new GetFollowerTask().execute();
			break;
		}		

		return super.onOptionsItemSelected(item);
	}	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == FOLLOWER_LOADER) {
			return new CursorLoader(activity, Uri.withAppendedPath(
					Follows.CONTENT_URI, "following/" + userID),
					followerProjection, null, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.clearFollows();

		if (!data.moveToFirst()) {
			activity.findViewById(R.id.lbl_no_follower).setVisibility(View.VISIBLE);
			return;
		}

		activity.findViewById(R.id.lbl_no_follower).setVisibility(View.GONE);
		do {
			adapter.addFollow(new Follow(data));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
	}

	private class GetFollowerTask extends AsyncTask<String, Void, JSONObject> {
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
					ret = api_client.follow(null, true, null);
				} else {
					ret = api_client.follow(params[0], true, null);
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
				JSONArray jFollows;
				try {
					jFollows = result.getJSONArray("objects");
					for (int i = 0; i < jFollows.length(); i++) {
						Follow follow = new Follow(jFollows.getJSONObject(i));
						if (follow.save(contentResolver) != null) {
							requery = true;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (requery) {
				activity.getSupportLoaderManager().restartLoader(
						FOLLOWER_LOADER, null, FollowerFragment.this);
				requery = false;
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetFollowerTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		} // onPostExecute
	} // GetFollowerTask

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Follow follow = (Follow) adapter.getItem(position);
		Intent intent = new Intent(activity, FriendDetailActivity.class);
		intent.putExtra("friend", follow.getUser());
		activity.startActivity(intent);		
		Log.d("friend", follow.getUser().getUsername());
	}
}
