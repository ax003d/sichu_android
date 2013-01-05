package com.sinaapp.sichu.adapters;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sinaapp.sichu.R;
import com.sinaapp.sichu.models.Follow;

public class FollowListAdapter extends BaseAdapter {

	private ArrayList<Follow> follows;
	
	public FollowListAdapter() {
		this.follows = new ArrayList<Follow>();
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
		TextView txt_username = (TextView) view.findViewById(R.id.txt_username);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);
		txt_username.setText(follow.getFollowing().getUsername());
		txt_remark.setText(follow.getRemark());
		
		return view;
	}

	public void clearFollows() {
		this.follows.clear();
	}

	public void addFollow(Follow follow) {
		this.follows.add(follow);
	}

}
