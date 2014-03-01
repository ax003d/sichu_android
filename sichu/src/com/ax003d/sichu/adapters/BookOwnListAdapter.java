package com.ax003d.sichu.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ax003d.sichu.R;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.utils.PinYin4j;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookOwnListAdapter extends BaseAdapter implements Filterable {

	private ArrayList<BookOwn> bookowns;
	private ArrayList<BookOwn> bookownsAll;
	private DisplayImageOptions options;
	private ImageLoader img_loader;
	private BookOwnFilter mFilter;
	private PinYin4j pinyin4j;

	public BookOwnListAdapter(Context context) {
		bookownsAll = new ArrayList<BookOwn>();
		bookowns = new ArrayList<BookOwn>();
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(context);
		pinyin4j = new PinYin4j();
	}

	public void addBookOwn(BookOwn own) {
		bookownsAll.add(own);
//		if (own.getBook() == null) {
//			return;
//		}
//
//		String title = own.getBook().getTitle();
//		if (own.getTitlePinyin() == null) {
//			own.setTitlePinyin(pinyin4j.getPinyin(title));
//		}
	}

	public void clearBookOwn() {
		bookownsAll.clear();
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
		ImageView img_status = (ImageView) view.findViewById(R.id.img_status);
		TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
		TextView txt_status = (TextView) view.findViewById(R.id.txt_status);
		TextView txt_remark = (TextView) view.findViewById(R.id.txt_remark);
		img_loader.displayImage(own.getBook().getCover()
				.replace("lpic", "spic"), img_cover, options);
		txt_title.setText(own.getBook().getTitle());
		switch (own.getStatus()) {
		case 1:
			img_status.setImageResource(R.drawable.ic_available);
			txt_status.setText(R.string.status_available);
			break;
		case 2:
			img_status.setImageResource(R.drawable.ic_unavailable);
			txt_status.setText(R.string.status_unavailable);
			break;
		case 3:
			img_status.setImageResource(R.drawable.ic_borrow);
			txt_status.setText(R.string.status_loaned);
			break;
		case 4:
			img_status.setImageResource(R.drawable.ic_lost);
			txt_status.setText(R.string.status_lost);
			break;
		default:
			break;
		}
		String remark = own.getRemark();
		if (remark != null && remark.length() > 0) {
			txt_remark.setText(remark);
		} else {
			txt_remark.setText(R.string.hint_no_remark);
		}

		return view;
	}

	public void prependBookOwn(BookOwn own) {
		bookownsAll.add(0, own);
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new BookOwnFilter();
		}
		return mFilter;
	}

	private class BookOwnFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			bookowns.clear();

			if (TextUtils.isEmpty(constraint)) {
				bookowns.addAll(bookownsAll);
			} else {
				String cons = constraint.toString().toLowerCase();
				for (BookOwn i : bookownsAll) {
					if (i.getBook() == null) {
						continue;
					}

					String title = i.getBook().getTitle();
					String author = i.getBook().getAuthor();
					String remark = i.getRemark();
//					if (i.getTitlePinyin() == null) {
//						i.setTitlePinyin(pinyin4j.getPinyin(title));
//					}
					if (title.toLowerCase().contains(cons) ||
							author.toLowerCase().contains(cons) ||
							remark.toLowerCase().contains(cons)) {
						bookowns.add(i);
						continue;
					}
//					for (String py : i.getTitlePinyin()) {
//						if (py.toLowerCase().contains(cons)) {
//							bookowns.add(i);
//							break;
//						}
//					}
				}
			}

			return null;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			if (bookowns.size() > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}
}
