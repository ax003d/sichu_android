package com.ax003d.sichu.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ax003d.sichu.models.Book.Books;
import com.ax003d.sichu.providers.SichuContentProvider;
import com.ax003d.sichu.utils.Utils;

public class BookBorrow {
	public static final String CATEGORY = "cabinet.models.BookBorrowRecord";

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
			this.bookown = new BookOwn(jsonObject.getJSONObject("ownership"));
			this.bookOwnID = this.bookown.getGuid();
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

	public BookBorrow(Cursor data) {
		int idx_guid = data.getColumnIndex(BookBorrows.GUID);
		int idx_bookOwnID = data.getColumnIndex(BookBorrows.BOOKOWNID);
		int idx_borrowerID = data.getColumnIndex(BookBorrows.BORROWERID);
		int idx_borrowDate = data.getColumnIndex(BookBorrows.BORROW_DATE);
		int idx_planedReturnDate = data
				.getColumnIndex(BookBorrows.PLANED_RETURN_DATE);
		int idx_returnedDate = data.getColumnIndex(BookBorrows.RETURNED_DATE);
		int idx_owner = data.getColumnIndex("owner");
		int idx_title = data.getColumnIndex(Books.TITLE);
		int idx_cover = data.getColumnIndex(Books.COVER);
		int idx_borrower = data.getColumnIndex("borrower");

		this.guid = data.getLong(idx_guid);
		this.bookOwnID = data.getLong(idx_bookOwnID);
		this.borrowerID = data.getLong(idx_borrowerID);
		this.borrowDate = Utils.parseDateTimeString(data
				.getString(idx_borrowDate));
		this.planedReturnDate = Utils.parseDateString(data
				.getString(idx_planedReturnDate));
		this.returnedDate = Utils.parseDateTimeString(data
				.getString(idx_returnedDate));

		Book book = new Book();
		book.setTitle(data.getString(idx_title));
		book.setCover(data.getString(idx_cover));
		User owner = new User();
		owner.setUsername(data.getString(idx_owner));
		this.bookown = new BookOwn();
		this.bookown.setBook(book);
		this.bookown.setOwner(owner);
		this.borrower = new User();
		this.borrower.setUsername(data.getString(idx_borrower));
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

	public Uri save(ContentResolver contentResolver) {
		if (this.bookown != null) {
			this.bookown.save(contentResolver);
		}
		if (this.borrower != null) {
			this.borrower.save(contentResolver);
		}
		ContentValues values = new ContentValues();
		setContentValues(values);
		return contentResolver.insert(BookBorrows.CONTENT_URI, values);
	}

	public long getGuid() {
		return guid;
	}

	public int update(ContentResolver contentResolver) {
		ContentValues values = new ContentValues();
		setContentValues(values);
		return contentResolver.update(
				Uri.withAppendedPath(BookBorrows.CONTENT_URI, "guid/"
						+ this.guid), values, null, null);
	}
}
