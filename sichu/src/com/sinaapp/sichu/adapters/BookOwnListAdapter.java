package com.sinaapp.sichu.adapters;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sinaapp.sichu.R;
import com.sinaapp.sichu.models.BookOwn;

public class BookOwnListAdapter extends BaseAdapter {

	private ArrayList<BookOwn> bookowns;
	
	public BookOwnListAdapter() {
		bookowns = new ArrayList<BookOwn>();
	}
	
	public void addBookOwn(BookOwn own) {
		bookowns.add(own);
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
		TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
		TextView txt_status = (TextView) view.findViewById(R.id.txt_status);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);
		txt_title.setText(own.getBook().getTitle());
		txt_status.setText(own.getStatus());
		txt_remark.setText(own.getRemark());
		
		return view;
	}

}
