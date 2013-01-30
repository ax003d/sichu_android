package com.ax003d.sichu;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;

import android.os.Bundle;
import android.widget.ImageView;

import com.ax003d.sichu.models.Book;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BooksEditActivity extends Activity {
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private BookOwn mBookOwn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booksedit);
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);
		
		ImageView img_cover = (ImageView) findViewById(R.id.img_cover);
		TextView txt_title = (TextView) findViewById(R.id.txt_title);
		TextView txt_author = (TextView) findViewById(R.id.txt_author);
		TextView txt_isbn = (TextView) findViewById(R.id.txt_isbn);
		Spinner spin_status = (Spinner) findViewById(R.id.spin_status);
		EditText edit_remark = (EditText) findViewById(R.id.edit_remark);

		Bundle extras = getIntent().getExtras();
		mBookOwn = extras.getParcelable("bookown");
		Book book = mBookOwn.getBook();
		img_loader.displayImage(book.getCover().replace("lpic", "spic"),
				img_cover, options);
		txt_title.setText(book.getTitle());
		txt_author.setText(book.getAuthor());
		txt_isbn.setText(book.getISBN());
		spin_status.setSelection(mBookOwn.getStatus() - 1);
		edit_remark.setText(mBookOwn.getRemark());		
	}	
}
