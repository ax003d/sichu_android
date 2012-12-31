package com.sinaapp.sichu.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.sinaapp.sichu.utils.Utils;

public class BookBorrow {
	private long guid;
	private long bookOwnID;
	private String borrower;
	private Date borrowDate;
	private Date planedReturnDate;
	private Date returnedDate;
	private BookOwn bookown;
	
	public BookBorrow(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
			this.setBookOwnID(jsonObject.getLong("ownership"));
			this.setBorrower(jsonObject.getString("borrower"));
			this.setBorrowDate(Utils.parseDateTimeString(jsonObject.getString("borrow_date")));
			this.setPlanedReturnDate(Utils.parseDateString(jsonObject.getString("planed_return_date")));
			this.setReturnedDate(Utils.parseDateTimeString(jsonObject.getString("returned_date")));
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

	public Date getBorrowDate() {
		return borrowDate;
	}

	public void setBorrowDate(Date borrowDate) {
		this.borrowDate = borrowDate;
	}

	public Date getPlanedReturnDate() {
		return planedReturnDate;
	}

	public void setPlanedReturnDate(Date planedReturnDate) {
		this.planedReturnDate = planedReturnDate;
	}

	public Date getReturnedDate() {
		return returnedDate;
	}

	public void setReturnedDate(Date returnedDate) {
		this.returnedDate = returnedDate;
	}

	public String getBorrower() {
		return borrower;
	}

	public void setBorrower(String borrower) {
		this.borrower = borrower;
	}
}
