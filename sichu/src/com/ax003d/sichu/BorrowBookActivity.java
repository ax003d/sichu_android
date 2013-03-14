package com.ax003d.sichu;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;

import android.os.Bundle;
import android.widget.ImageView;

import com.actionbarsherlock.view.Window;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.Book;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BorrowBookActivity extends Activity {
	private BookOwn mBookOwn;
	private ISichuAPI api_client;
	private DisplayImageOptions options;
	private ImageLoader img_loader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_borrow_book);

		api_client = SichuAPI.getInstance(getApplicationContext());
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);

		ImageView img_cover = (ImageView) findViewById(R.id.img_cover);
		TextView txt_title = (TextView) findViewById(R.id.txt_title);
		TextView txt_author = (TextView) findViewById(R.id.txt_author);
		TextView txt_isbn = (TextView) findViewById(R.id.txt_isbn);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}

		mBookOwn = extras.getParcelable("bookown");
		Book book = mBookOwn.getBook();
		img_loader.displayImage(book.getCover().replace("lpic", "spic"),
				img_cover, options);
		txt_title.setText(book.getTitle());
		txt_author.setText(book.getAuthor());
		txt_isbn.setText(book.getISBN());
	}
}
