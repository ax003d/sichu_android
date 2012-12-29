package com.sinaapp.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

public class BookOwn {
	private long guid;
	private long bookID;
	private Book book;
	private long ownerID;
	private int status;
	private boolean hasEbook;
	private String remark;
	
	private static final String[] book_status = {"Available", "Not Available", "Loaned", "Lost"};
	
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
}
