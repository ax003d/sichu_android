package com.ax003d.sichu.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ax003d.sichu.R;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FollowListAdapter extends BaseAdapter {

	private ArrayList<Follow> follows;
	private boolean asFollower;
	private DisplayImageOptions options;
	private ImageLoader img_loader;			
	
	public FollowListAdapter(Context context) {
		this.follows = new ArrayList<Follow>();
		this.asFollower = false;
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);		
	}
	
	@Override
	public int getCount() {
		return follows.size();
	}

	@Override
	public Object getItem(int position) {
		return follows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup view = (convertView instanceof ViewGroup) ? (ViewGroup) convertView
				: (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(
						R.layout.item_follow, null);
		
		Follow follow = (Follow) getItem(position);
		ImageView img_avatar = (ImageView) view.findViewById(R.id.img_avatar);
		TextView txt_username = (TextView) view.findViewById(R.id.txt_username);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);
		if ( asFollower ) {
			img_loader.displayImage(follow.getUser().getAvatar(), img_avatar, options);
			txt_username.setText(follow.getUser().getUsername());
			txt_remark.setText("");
		} else {
			img_loader.displayImage(follow.getFollowing().getAvatar(), img_avatar, options);
			txt_username.setText(follow.getFollowing().getUsername());
			txt_remark.setText(follow.getRemark());
		}
		
		return view;
	}

	public void clearFollows() {
		this.follows.clear();
	}

	public void addFollow(Follow follow) {
		this.follows.add(follow);
	}

	public void setAsFollower(boolean asFollower) {
		this.asFollower = asFollower;
	}

}
