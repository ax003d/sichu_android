package com.ax003d.sichu.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ax003d.sichu.InviteWeiboFriendActivity;
import com.ax003d.sichu.R;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.MayKnow;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MayKnowListAdapter extends BaseAdapter {

	private ArrayList<MayKnow> mMayKnows;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private Context mContext;
	private ISichuAPI api_client;

	public MayKnowListAdapter(Context context) {
		this.mMayKnows = new ArrayList<MayKnow>();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);
		mContext = context;
		api_client = SichuAPI.getInstance(mContext);
	}

	@Override
	public int getCount() {
		return mMayKnows.size();
	}

	@Override
	public Object getItem(int position) {
		return mMayKnows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup view = null;

		if (convertView != null) {
			view = (ViewGroup) convertView;
		} else {
			view = (ViewGroup) LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_may_know, null);
		}

		MayKnow mk = (MayKnow) getItem(position);
		ImageView img_avatar = (ImageView) view.findViewById(R.id.img_avatar);
		TextView txt_username = (TextView) view.findViewById(R.id.txt_username);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);
		Button btn_action = (Button) view.findViewById(R.id.btn_action);

		img_loader.displayImage(mk.getAvatar(), img_avatar, options);
		txt_username.setText(mk.getUsername());
		txt_remark.setText(mk.getRemark());
		if (mk.getIsSichuUser()) {
			btn_action.setText(R.string.btn_action_follow);
			btn_action.setTag(mk);
			btn_action.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("follow", "follow");
					MayKnow mk = (MayKnow) v.getTag();
					new FollowTask().execute(mk.getID(), mk.getRemark());
				}
			});
		} else {
			btn_action.setText(R.string.btn_action_invite);
			btn_action.setTag(mk.getUsername());
			btn_action.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("follow", "invite");
					Intent intent = new Intent(mContext, InviteWeiboFriendActivity.class);
					intent.putExtra("uid", Preferences.getUserID(mContext));
					intent.putExtra("screen_name", "@" + (String) v.getTag());
					mContext.startActivity(intent);
				}
			});
		}

		return view;
	}

	public void addMayKnow(MayKnow mk) {
		mMayKnows.add(mk);
	}

	public void setSichuUser(String wb_id) {
		for (MayKnow mk : mMayKnows) {
			if (mk.getID().equals(wb_id)) {
				mk.setIsSichuUser(true);
			}
		}

		MayKnowComparator comparator = new MayKnowComparator();
		Collections.sort(mMayKnows, comparator);
	}

	private class MayKnowComparator implements Comparator<MayKnow> {
		@Override
		public int compare(MayKnow mk1, MayKnow mk2) {
			if (mk1.getIsSichuUser() && (!mk2.getIsSichuUser())) {
				return -1;
			} else if ((!mk1.getIsSichuUser()) && mk2.getIsSichuUser()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public void remove(String wb_id) {
		for (MayKnow mk : mMayKnows) {
			if (mk.getID().equals(wb_id)) {
				mMayKnows.remove(mk);
				break;
			}
		}
	}

	private class FollowTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				return api_client.friends__follow(params[0], params[1], null);
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
			Log.d("follow", result.toString());
			if (result != null && result.has("status")) {
				try {
					String wb_id = result.getString("uid");
					remove(wb_id);
					notifyDataSetChanged();
					Toast.makeText(mContext, R.string.ok_follow,
							Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(mContext, R.string.err_follow, Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
}
