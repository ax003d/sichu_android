package com.ax003d.sichu.adapters;

import java.util.ArrayList;

import com.ax003d.sichu.R;
import com.ax003d.sichu.models.Follow;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends BaseAdapter {

	private ArrayList<BookBorrowReq> messages;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private int col_unread;
	private int col_read;

	public MessageListAdapter(Context context) {
		this.messages = new ArrayList<BookBorrowReq>();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);
		col_unread = context.getResources().getColor(R.color.col_value);
		col_read = 0xFF333333;
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup view = (convertView instanceof ViewGroup) ? (ViewGroup) convertView
				: (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(
						R.layout.item_message, null);

		BookBorrowReq msg = (BookBorrowReq) getItem(position);
		ImageView img_avatar = (ImageView) view.findViewById(R.id.img_avatar);
		TextView lbl_type = (TextView) view.findViewById(R.id.lbl_type);
		TextView txt_datetime = (TextView) view.findViewById(R.id.txt_datetime);
		TextView txt_requester = (TextView) view.findViewById(R.id.txt_requester);
		TextView txt_book = (TextView) view.findViewById(R.id.txt_book);
		TextView txt_planed_return_date = (TextView) view.findViewById(R.id.txt_planed_return_date);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);

		img_loader.displayImage(msg.getRequester().getAvatar(), img_avatar,
				options);
		txt_datetime.setText(Utils.formatDateTime(msg.getDatetime()));
		txt_requester.setText(msg.getRequester().getUsername());
		txt_book.setText(msg.getBookown().getBook().getTitle());
		txt_planed_return_date.setText(Utils.formatDate(msg.getPlannedReturnDate()));
		txt_remark.setText(msg.getRemark());

		int text_col = col_read;
		switch (msg.getStatus()) {
		case 0:
			text_col = col_unread;
			break;
		case 1:
			text_col = col_read;
			break;

		default:
			break;
		}

		lbl_type.setTextColor(text_col);
		txt_datetime.setTextColor(text_col);
		txt_book.setTextColor(text_col);
		txt_planed_return_date.setTextColor(text_col);
		txt_remark.setTextColor(text_col);
		
		return view;
	}

	public void addBookBorrowReq(BookBorrowReq req) {
		messages.add(req);
	}

	public void clearBookBorrowReq() {
		messages.clear();
	}
}
