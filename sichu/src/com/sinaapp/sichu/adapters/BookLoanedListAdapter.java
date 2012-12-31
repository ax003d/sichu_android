package com.sinaapp.sichu.adapters;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sinaapp.sichu.R;
import com.sinaapp.sichu.models.BookBorrow;
import com.sinaapp.sichu.models.BookOwn;
import com.sinaapp.sichu.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookLoanedListAdapter extends BaseAdapter {

	private ArrayList<BookBorrow> bookowns;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private Context context;
	private int col_not_returned;
	private int col_returned;	
	
	public BookLoanedListAdapter(Context context) {
		bookowns = new ArrayList<BookBorrow>();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);
		col_not_returned = context.getResources().getColor(R.color.col_value);
		col_returned = 0xFF000000;
	}	
	
	public void addBookBorrow(BookBorrow borrow) {
		bookowns.add(borrow);
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
						R.layout.item_bookborrow, null);
		
		BookBorrow borrow = (BookBorrow) getItem(position);
		ImageView img_cover = (ImageView) view.findViewById(R.id.img_cover);
		TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
		TextView txt_borrower = (TextView) view.findViewById(R.id.txt_borrower);
		TextView txt_borrow_date = (TextView) view.findViewById(R.id.txt_borrow_date);
		TextView txt_planed_return_date = (TextView) view.findViewById(R.id.txt_planed_return_date);
		TextView txt_returned_date = (TextView) view.findViewById(R.id.txt_returned_date);
		BookOwn own = borrow.getBookOwn();
		img_loader.displayImage(own.getBook().getCover().replace("lpic", "spic"), img_cover, options);
		txt_title.setText(own.getBook().getTitle());
		txt_borrower.setText(borrow.getBorrower());
		if ( borrow.getBorrowDate() != null ) {
			txt_borrow_date.setText(Utils.formatDate(borrow.getBorrowDate()));
		} else {
			txt_borrow_date.setText("");
		}
		if ( borrow.getPlanedReturnDate() != null ) {
			txt_planed_return_date.setText(Utils.formatDate(borrow.getPlanedReturnDate()));
		} else {
			txt_planed_return_date.setText("");
		}
		if ( borrow.getReturnedDate() != null ) {
			txt_returned_date.setText(Utils.formatDate(borrow.getReturnedDate()));
			
			txt_borrower.setTextColor(col_returned);
			txt_borrow_date.setTextColor(col_returned);
			txt_planed_return_date.setTextColor(col_returned);
			txt_returned_date.setTextColor(col_returned);			
		} else {
			txt_returned_date.setText("Not returned yet!");
			
			txt_borrower.setTextColor(col_not_returned);
			txt_borrow_date.setTextColor(col_not_returned);
			txt_planed_return_date.setTextColor(col_not_returned);
			txt_returned_date.setTextColor(col_not_returned);
		}
		
		return view;
	}

}
