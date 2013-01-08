package com.ax003d.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ax003d.sichu.providers.SichuContentProvider;

public class Book {
	private long guid;
	private String ISBN;
	private String title;
	private String author;
	private String doubanID;
	private String cover;

	public static final class Books implements BaseColumns {
		private Books() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/books");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.books";
		public static final String TABLE_NAME = "books";
		public static final String GUID = "guid";
		public static final String ISBN = "ISBN";
		public static final String TITLE = "title";
		public static final String AUTHOR = "author";
		public static final String DOUBAN_ID = "doubanID";
		public static final String COVER = "cover";
	}	
	
	public Book(JSONObject jsonObject) {
		try {
			this.setGuid(jsonObject.getLong("id"));
			this.ISBN = jsonObject.getString("isbn");
			this.setTitle(jsonObject.getString("title"));
			this.author = jsonObject.getString("author");
			this.doubanID = jsonObject.getString("douban_id");
			this.setCover(jsonObject.getString("cover"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Book(long bookID, String ISBN, String title, String author,
			String doubanID, String cover) {
		this.guid = bookID;
		this.ISBN = ISBN;
		this.title = title;
		this.author = author;
		this.doubanID = doubanID;
		this.cover = cover;
	}

	public Book() {
	}

	public long getGuid() {
		return guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public void setContentValues(ContentValues values) {
		values.put(Books.GUID, this.guid);
		values.put(Books.ISBN, this.ISBN);
		values.put(Books.TITLE, this.title);
		values.put(Books.AUTHOR, this.author);
		values.put(Books.DOUBAN_ID, this.doubanID);
		values.put(Books.COVER, this.cover);
	}

	public void save(ContentResolver contentResolver) {
		ContentValues values = new ContentValues();
		setContentValues(values);
		contentResolver.insert(Books.CONTENT_URI, values);				
	}	
}
