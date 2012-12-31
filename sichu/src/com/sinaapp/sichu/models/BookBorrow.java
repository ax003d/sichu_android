package com.sinaapp.sichu.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.provider.BaseColumns;

import com.sinaapp.sichu.providers.SichuContentProvider;
import com.sinaapp.sichu.utils.Utils;

public class BookBorrow {
	private long guid;
	private long bookOwnID;
	private String borrower;
	private Date borrowDate;
	private Date planedReturnDate;
	private Date returnedDate;
	private BookOwn bookown;
	
	public static final class BookBorrows implements BaseColumns {
		private BookBorrows() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/bookborrows");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.bookborrows";
		public static final String TABLE_NAME = "bookborrows";
		public static final String GUID = "guid";
		public static final String BOOKOWNID = "bookOwnID";
		public static final String BORROWERID = "borrowerID";
		public static final String BORROW_DATE = "borrowDate";
		public static final String PLANED_RETURN_DATE = "planedReturnDate";
		public static final String RETURNED_DATE = "returnedDate";
	}	
	
	public BookBorrow(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
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
		this.bookOwnID = bookown.getGuid();
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
