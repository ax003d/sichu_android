package com.sinaapp.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sinaapp.sichu.providers.SichuContentProvider;

public class BookOwn {
	private long guid;
	private long bookID;
	private Book book;
	private long ownerID;
	private int status;
	private boolean hasEbook;
	private String remark;
	
	private static final String[] book_status = {"Available", "Not Available", "Loaned", "Lost"};

	public static final class BookOwns implements BaseColumns {
		private BookOwns() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/bookowns");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.bookowns";
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}	

	public BookOwn(long guid, long bookID, long ownerID, int status, int hasEbook,
			String remark, String ISBN, String title, String author,
			String doubanID, String cover) {
		this.guid = guid;
		this.book = new Book(bookID, ISBN, title, author, doubanID, cover);
		this.bookID = bookID;
		this.ownerID = ownerID;
		this.status = status;
		this.hasEbook = (hasEbook == 1);
		this.remark = remark;
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

	public void setContentValues(ContentValues values, long userID) {
		values.put(BookOwns.GUID, this.guid);
		values.put(BookOwns.BOOKID, this.bookID);
		values.put(BookOwns.OWNERID, userID);
		values.put(BookOwns.STATUS, this.status);
		values.put(BookOwns.HASEBOOK, this.hasEbook);
		values.put(BookOwns.REMARK, this.remark);
	}

	public long getGuid() {
		return this.guid;
	}
}
