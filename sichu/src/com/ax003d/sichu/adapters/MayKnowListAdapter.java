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
import com.ax003d.sichu.models.MayKnow;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MayKnowListAdapter extends BaseAdapter {

	private ArrayList<MayKnow> mMayKnows;
	private DisplayImageOptions options;
	private ImageLoader img_loader;

	public MayKnowListAdapter(Context context) {
		this.mMayKnows = new ArrayList<MayKnow>();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);
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
		ViewGroup view = (convertView instanceof ViewGroup) ? (ViewGroup) convertView
				: (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(
						R.layout.item_may_know, null);

		MayKnow mk = (MayKnow) getItem(position);
		ImageView img_avatar = (ImageView) view.findViewById(R.id.img_avatar);
		TextView txt_username = (TextView) view.findViewById(R.id.txt_username);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);

		img_loader.displayImage(mk.getAvatar(), img_avatar, options);
		txt_username.setText(mk.getUsername());
		txt_remark.setText(mk.getRemark());

		return view;
	}

	public void addMayKnow(MayKnow mk) {
		mMayKnows.add(mk);
	}

}
