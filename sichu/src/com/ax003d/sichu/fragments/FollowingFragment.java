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
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.models.Follow.Follows;
import com.ax003d.sichu.models.User.Users;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Sync;

public class FollowingFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	private static final int FOLLOWING_LOADER = 3;
	private static FollowingFragment instance;
	private static String[] followingProjection = new String[] {
			Follows.TABLE_NAME + "." + Follows.GUID, Follows.FOLLOWINGID,
			Follows.REMARK, Follows.USERID,
			Users.TABLE_NAME + "." + Users.USERNAME + " AS followingName",
			Users.TABLE_NAME + "." + Users.AVATAR + " AS followingAvatar" };

	public static FollowingFragment getInstance() {
		if (FollowingFragment.instance == null) {
			FollowingFragment.instance = new FollowingFragment();
		}
		return FollowingFragment.instance;
	}

	private FollowListAdapter adapter;
	private ListView lst_following;
	private ISichuAPI api_client;
	private SlidingActivity activity;
	private long userID;
	private boolean requery;
	private View lbl_no_following;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		api_client = SichuAPI.getInstance(getActivity());
		activity = (SlidingActivity) getActivity();
		userID = Preferences.getUserID(activity);
		adapter = new FollowListAdapter(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_following, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_following = (ListView) activity.findViewById(R.id.lst_following);
		lst_following.setAdapter(adapter);
		lst_following.setOnItemClickListener(this);
		lbl_no_following = activity.findViewById(R.id.lbl_no_following);
		activity.getSupportLoaderManager().initLoader(FOLLOWING_LOADER, null,
				this);
		onMenuSyncTriggered();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_sync:
			onMenuSyncTriggered();
			break;
		}		

		return super.onOptionsItemSelected(item);
	}

	private void onMenuSyncTriggered() {
		requery = false;
		if (Preferences.getSyncTime(activity, Follow.CATEGORY) == 0) {
			activity.getContentResolver().delete(
					Uri.withAppendedPath(Follows.CONTENT_URI, "user/"
							+ userID), null, null);
			new GetFollowingTask().execute();
		} else {
			new Sync(activity).start_sync_task(Follow.CATEGORY);
		}		
	}	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == FOLLOWING_LOADER) {
			return new CursorLoader(activity, Uri.withAppendedPath(
					Follows.CONTENT_URI, "user/" + userID),
					followingProjection, null, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.clearFollows();
		
		if (!data.moveToFirst()) {
			if (lbl_no_following != null) {
				lbl_no_following.setVisibility(View.VISIBLE);
			}
			return;
		}

		if (lbl_no_following != null) {
			lbl_no_following.setVisibility(View.GONE);
		}
		do {
			adapter.addFollow(new Follow(data));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		// TODO Auto-generated method stub

	}

	private class GetFollowingTask extends AsyncTask<String, Void, JSONObject> {
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
					ret = api_client.follow(null, false, null);
				} else {
					ret = api_client.follow(params[0], false, null);
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
						FOLLOWING_LOADER, null, FollowingFragment.this);
				requery = false;
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetFollowingTask().execute(next);
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
		Follow follow = (Follow) adapter.getItem(position);
		Intent intent = new Intent(activity, FriendDetailActivity.class);
		intent.putExtra("friend", follow.getFollowing());
		intent.putExtra("remark", follow.getRemark());
		activity.startActivity(intent);
		Log.d("friend", follow.getFollowing().getUsername());
	}
}
