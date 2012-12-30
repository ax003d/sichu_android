package com.sinaapp.sichu.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class BookBorrow {
	private long guid;
	private long bookOwnID;
	private Date borrowDate;
	private Date planedReturnDate;
	private Date returnedDate;
	private BookOwn bookown;
	
	public BookBorrow(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
			this.setBookOwnID(jsonObject.getLong("ownership"));
			// this.borrowDate = jsonObject.getString("borrow_date"); // parse date string
			// this.planedReturnDate = jsonObject.getString("planed_return_date"); // parse date string
			// this.returnedDate = jsonObject.getString("returned_date"); // parse date string
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public BookOwn getBookOwn() {
		return bookown;
	}
	public void setBookOwn(BookOwn bookown) {
		this.bookown = bookown;
	}

	public long getBookOwnID() {
		return bookOwnID;
	}

	public void setBookOwnID(long bookOwnID) {
		this.bookOwnID = bookOwnID;
	}
}
