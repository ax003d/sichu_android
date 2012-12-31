package com.sinaapp.sichu.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sinaapp.sichu.providers.SichuContentProvider;
import com.sinaapp.sichu.utils.Utils;

public class BookBorrow {
	private long guid;
	private long bookOwnID;
	private long borrowerID;
	private Date borrowDate;
	private Date planedReturnDate;
	private Date returnedDate;
	private BookOwn bookown;
	private User borrower;

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

	private void setContentValues(ContentValues values) {
		values.put(BookBorrows.GUID, this.guid);
		values.put(BookBorrows.BOOKOWNID, bookOwnID);
		values.put(BookBorrows.BORROWERID, borrowerID);
		values.put(BookBorrows.BORROW_DATE, Utils.formatDateTime(borrowDate));
		values.put(BookBorrows.PLANED_RETURN_DATE,
				Utils.formatDate(planedReturnDate));
		values.put(BookBorrows.RETURNED_DATE,
				returnedDate == null ? "" : Utils.formatDateTime(returnedDate));
	}

	public BookBorrow(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
			this.borrower = new User(jsonObject.getJSONObject("borrower"));
			this.borrowerID = this.borrower.getGuid();
			this.setBorrowDate(Utils.parseDateTimeString(jsonObject
					.getString("borrow_date")));
			this.setPlanedReturnDate(Utils.parseDateString(jsonObject
					.getString("planed_return_date")));
			this.setReturnedDate(Utils.parseDateTimeString(jsonObject
					.getString("returned_date")));
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

	public User getBorrower() {
		return borrower;
	}

	public void setBorrower(User borrower) {
		this.borrower = borrower;
	}

	public void save(ContentResolver contentResolver) {
		if (this.bookown != null) {
			this.bookown.save(contentResolver);
		}
		if (this.borrower != null) {
			this.borrower.save(contentResolver);
		}
		ContentValues values = new ContentValues();
		setContentValues(values);
		contentResolver.insert(BookBorrows.CONTENT_URI, values);
	}
}
