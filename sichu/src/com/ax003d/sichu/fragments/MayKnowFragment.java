package com.ax003d.sichu.fragments;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.ax003d.sichu.MainActivity;
import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.MayKnowListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.events.ListFriendsEvent;
import com.ax003d.sichu.models.MayKnow;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;
import com.squareup.otto.Subscribe;

public class MayKnowFragment extends Fragment {
	private static MayKnowFragment instance;

	public static MayKnowFragment getInstance() {
		if (MayKnowFragment.instance == null) {
			MayKnowFragment.instance = new MayKnowFragment();
		}
		return MayKnowFragment.instance;
	}

	private MainActivity activity;
	private int mFriendCursor;
	private MayKnowListAdapter adapter;
	private ListView lst_may_know;
	private long mWBuid;
	private MayKnowHandler mHandler;
	private ISichuAPI api_client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		api_client = SichuAPI.getInstance(activity);
		adapter = new MayKnowListAdapter(activity);
		mHandler = new MayKnowHandler(this);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_may_know = (ListView) activity.findViewById(R.id.lst_may_know);
		lst_may_know.setAdapter(adapter);
		mWBuid = Preferences.getWeiboUID(activity);
		mFriendCursor = 0;
		Utils.getBus().register(this);
		if (mWBuid != -1) {
			loadMayKnows();
			activity.findViewById(R.id.lbl_bind_weibo).setVisibility(View.GONE);
		}
	}

	@Subscribe
	public void onFriendsList(ListFriendsEvent event) {
		StringBuilder wb_ids = new StringBuilder();
		for (int i = 0; i < event.users.size(); i++) {
			MayKnow mk = null;
			try {
				mk = new MayKnow(event.users.get(i));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			adapter.addMayKnow(mk);
			mFriendCursor++;
			wb_ids.append(mk.getID());
			wb_ids.append(",");
		}
		new GetMayKnowTask().execute(wb_ids.toString());
	}

	private void loadMayKnows() {
		activity.listFriend();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_may_know, container, false);
	}

	public void updateMayKnowList() {
		adapter.notifyDataSetChanged();
	}

	private static class MayKnowHandler extends Handler {
		private final WeakReference<MayKnowFragment> mFragment;

		public MayKnowHandler(MayKnowFragment fragment) {
			mFragment = new WeakReference<MayKnowFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MayKnowFragment fragment = mFragment.get();
			if (fragment != null) {
				fragment.activity
						.setSupportProgressBarIndeterminateVisibility(false);
			}
		}
	}

	private class GetMayKnowTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				return api_client.account__may_know(params[0], null);
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
			mHandler.sendEmptyMessage(0);
			JSONArray array;
			try {
				array = result.getJSONArray("may_know");
				for (int i = 0; i < array.length(); i++) {
					adapter.setSichuUser(array.getString(i));
				}
				array = result.getJSONArray("friends");
				for (int i = 0; i < array.length(); i++) {
					adapter.remove(array.getString(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.getBus().register(this);
	}
}
