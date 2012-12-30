package com.sinaapp.sichu.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sinaapp.sichu.R;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.utils.Utils;

public class BookOwnListAdapter extends BaseAdapter {

	private ArrayList<BookOwn> bookowns;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	
	public BookOwnListAdapter(Context context) {
		bookowns = new ArrayList<BookOwn>();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);		
	}
	
	public void addBookOwn(BookOwn own) {
		bookowns.add(own);
	}
	
	public void clearBookOwn() {
		bookowns.clear();
	}
	
	@Override
	public int getCount() {
		return bookowns.size();
	}

	@Override
	public Object getItem(int position) {
		return bookowns.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup view = (convertView instanceof ViewGroup) ? (ViewGroup) convertView
				: (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(
						R.layout.item_bookown, null);
		
		BookOwn own = (BookOwn) getItem(position);
		ImageView img_cover = (ImageView) view.findViewById(R.id.img_cover);
		TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
		TextView txt_status = (TextView) view.findViewById(R.id.txt_status);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);
		img_loader.displayImage(own.getBook().getCover().replace("lpic", "spic"), img_cover, options);
		txt_title.setText(own.getBook().getTitle());
		txt_status.setText(own.getStatus());
		txt_remark.setText(own.getRemark());
		
		return view;
	}

}
