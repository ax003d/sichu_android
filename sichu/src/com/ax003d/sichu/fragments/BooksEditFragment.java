package com.ax003d.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ax003d.sichu.MainActivity;
import com.ax003d.sichu.R;
import com.ax003d.sichu.models.Book;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BooksEditFragment extends Fragment {

	private static BooksEditFragment instance;
	private static BookOwn mBookOwn;

	public static BooksEditFragment getInstance() {
		if (BooksEditFragment.instance == null) {
			BooksEditFragment.instance = new BooksEditFragment();
		}
		return BooksEditFragment.instance;
	}

	private MainActivity activity;
	private DisplayImageOptions options;
	private ImageLoader img_loader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		;
		activity = (MainActivity) getActivity();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_booksedit, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ImageView img_cover = (ImageView) activity.findViewById(R.id.img_cover);
		TextView txt_title = (TextView) activity.findViewById(R.id.txt_title);
		TextView txt_author = (TextView) activity.findViewById(R.id.txt_author);
		TextView txt_isbn = (TextView) activity.findViewById(R.id.txt_isbn);
		Spinner spin_status = (Spinner) activity.findViewById(R.id.spin_status);
		EditText edit_remark = (EditText) activity
				.findViewById(R.id.edit_remark);

		Book book = mBookOwn.getBook();
		img_loader.displayImage(book.getCover().replace("lpic", "spic"),
				img_cover, options);
		txt_title.setText(book.getTitle());
		txt_author.setText(book.getAuthor());
		txt_isbn.setText(book.getISBN());
		spin_status.setSelection(mBookOwn.getStatus() - 1);
		edit_remark.setText(mBookOwn.getRemark());
	}

	public void setBookOwn(BookOwn own) {
		this.mBookOwn = own;
	}
}
