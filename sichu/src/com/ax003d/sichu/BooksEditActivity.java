package com.ax003d.sichu;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.ax003d.sichu.models.Book;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BooksEditActivity extends Activity implements OnClickListener {
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private BookOwn mBookOwn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booksedit);
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);

		findViewById(R.id.btn_douban).setOnClickListener(this);

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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_douban:
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://book.douban.com/subject/"
							+ mBookOwn.getBook().getDoubanID() + "/"));
			startActivity(intent);
			break;

		default:
			break;
		}
	}
}
