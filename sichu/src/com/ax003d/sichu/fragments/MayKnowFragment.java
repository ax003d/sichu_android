package com.ax003d.sichu.fragments;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
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

import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.MayKnowListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.MayKnow;
import com.ax003d.sichu.utils.AccessTokenKeeper;
import com.ax003d.sichu.utils.Preferences;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.net.RequestListener;

public class MayKnowFragment extends Fragment {
	private static MayKnowFragment instance;

	public static MayKnowFragment getInstance() {
		if (MayKnowFragment.instance == null) {
			MayKnowFragment.instance = new MayKnowFragment();
		}
		return MayKnowFragment.instance;
	}

	private SlidingActivity activity;
	private int mFriendCursor;
	private MayKnowListAdapter adapter;
	private ListView lst_may_know;
	private long mWBuid;
	private MayKnowHandler mHandler;
	private ISichuAPI api_client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (SlidingActivity) getActivity();
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
		if (mWBuid != -1) {
			loadMayKnows();
			activity.findViewById(R.id.lbl_bind_weibo).setVisibility(View.GONE);
		}
	}

	private void loadMayKnows() {
		FriendshipsAPI friendships = new FriendshipsAPI(
				AccessTokenKeeper.readAccessToken(activity));
		activity.setSupportProgressBarIndeterminateVisibility(true);
		friendships.friends(mWBuid, 200, mFriendCursor, true,
				new RequestListener() {

					@Override
					public void onComplete(String resp) {
						JSONObject json;
						try {
							json = new JSONObject(resp);
							if (!json.has("users")) {
								return;
							}
							StringBuilder wb_ids = new StringBuilder();
							JSONArray users = json.getJSONArray("users");
							for (int i = 0; i < users.length(); i++) {
								MayKnow mk = new MayKnow(users.getJSONObject(i));
								adapter.addMayKnow(mk);
								mFriendCursor++;
								wb_ids.append(mk.getID());
								wb_ids.append(",");
							}
							new GetMayKnowTask().execute(wb_ids.toString());
						} catch (JSONException e) {
							e.printStackTrace();
							mHandler.sendEmptyMessage(0);
						}
					}

					@Override
					public void onError(WeiboException e) {
						mHandler.sendEmptyMessage(0);
					}

					@Override
					public void onIOException(IOException e) {
						mHandler.sendEmptyMessage(0);
					}

				});
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
				fragment.activity.setSupportProgressBarIndeterminateVisibility(false);
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
			// Log.d("may_know", result.toString());
			JSONArray array;
			try {
				array = result.getJSONArray("may_know");
				for ( int i = 0; i < array.length(); i++ ) {
					adapter.setSichuUser(array.getString(i));
				}
				array = result.getJSONArray("friends");
				for ( int i = 0 ; i < array.length(); i++ ) {
					adapter.remove(array.getString(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			adapter.notifyDataSetChanged();
		}
	}
}
