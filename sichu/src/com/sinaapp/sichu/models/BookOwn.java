package com.sinaapp.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sinaapp.sichu.models.Book.Books;
import com.sinaapp.sichu.providers.SichuContentProvider;

public class BookOwn {
	private long guid;
	private long bookID;
	private Book book;
	private long ownerID;
	private int status;
	private boolean hasEbook;
	private String remark;
	private User owner;

	private static final String[] book_status = { "Available", "Not Available",
			"Loaned", "Lost" };

	public static final class BookOwns implements BaseColumns {
		private BookOwns() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/bookowns");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.bookowns";
		public static final String TABLE_NAME = "bookowns";
		public static final String GUID = "guid";
		public static final String BOOKID = "bookID";
		public static final String OWNERID = "ownerID";
		public static final String STATUS = "status";
		public static final String HASEBOOK = "hasEbook";
		public static final String REMARK = "remark";
	}

	public BookOwn(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
			this.setBook(new Book(jsonObject.getJSONObject("book")));
			this.bookID = this.getBook().getGuid();
			this.setStatus(jsonObject.getString("status"));
			this.hasEbook = jsonObject.getBoolean("has_ebook");
			this.setRemark(jsonObject.getString("remark"));
			this.owner = new User(jsonObject.getJSONObject("owner"));
			this.ownerID = this.owner.getGuid();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public BookOwn(long guid, long bookID, long ownerID, int status,
			int hasEbook, String remark, String ISBN, String title,
			String author, String doubanID, String cover) {
		this.guid = guid;
		this.book = new Book(bookID, ISBN, title, author, doubanID, cover);
		this.bookID = bookID;
		this.ownerID = ownerID;
		this.status = status;
		this.hasEbook = (hasEbook == 1);
		this.remark = remark;
	}

	public BookOwn(Cursor data) {
		int idx_guid = data.getColumnIndex(BookOwns.GUID);
		int idx_bookID = data.getColumnIndex(BookOwns.BOOKID);
		int idx_ownerID = data.getColumnIndex(BookOwns.OWNERID);
		int idx_status = data.getColumnIndex(BookOwns.STATUS);
		int idx_hasEBook = data.getColumnIndex(BookOwns.HASEBOOK);
		int idx_remark = data.getColumnIndex(BookOwns.REMARK);
		int idx_ISBN = data.getColumnIndex(Books.ISBN);
		int idx_title = data.getColumnIndex(Books.TITLE);
		int idx_author = data.getColumnIndex(Books.AUTHOR);
		int idx_doubanID = data.getColumnIndex(Books.DOUBAN_ID);
		int idx_cover = data.getColumnIndex(Books.COVER);

		this.guid = data.getLong(idx_guid);
		this.bookID = data.getLong(idx_bookID);
		this.ownerID = data.getLong(idx_ownerID);
		this.status = data.getInt(idx_status);
		this.hasEbook = data.getInt(idx_hasEBook) == 1;
		this.remark = data.getString(idx_remark);
		this.book = new Book(this.bookID, data.getString(idx_ISBN),
				data.getString(idx_title), data.getString(idx_author),
				data.getString(idx_doubanID), data.getString(idx_cover));
	}

	public long getBookID() {
		return bookID;
	}

	public void setBookID(long bookID) {
		this.bookID = bookID;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getStatus() {
		return book_status[this.status - 1];
	}

	public void setStatus(String status) {
		this.status = Integer.parseInt(status);
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public static final String GUID = "guid";
	public static final String BOOKID = "bookID";
	public static final String OWNERID = "ownerID";
	public static final String STATUS = "status";
	public static final String HASEBOOK = "hasEbook";
	public static final String REMARK = "remark";

	public void setContentValues(ContentValues values) {
		values.put(BookOwns.GUID, this.guid);
		values.put(BookOwns.BOOKID, this.bookID);
		values.put(BookOwns.OWNERID, this.ownerID);
		values.put(BookOwns.STATUS, this.status);
		values.put(BookOwns.HASEBOOK, this.hasEbook);
		values.put(BookOwns.REMARK, this.remark);
	}

	public long getGuid() {
		return this.guid;
	}

	public void save(ContentResolver contentResolver) {
		if (this.book != null) {
			this.book.save(contentResolver);
		}
		if (this.owner != null) {
			this.owner.save(contentResolver);
		}
		ContentValues values = new ContentValues();
		setContentValues(values);
		contentResolver.insert(BookOwns.CONTENT_URI, values);
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
}
