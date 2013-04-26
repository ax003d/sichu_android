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
import com.ax003d.sichu.models.User.Users;
import com.ax003d.sichu.providers.SichuContentProvider;
import com.ax003d.sichu.utils.Utils;

public class BookBorrowReq {
	public static final String CATEGORY = "cabinet.models.BookBorrowRequest";

	private long guid;
	private Date datetime;
	private long requesterID;
	private long bookownID;
	private Date planned_return_date;
	private String remark;
	private int status;
	private User requester;
	private BookOwn bookown;

	public static final class BookBorrowReqs implements BaseColumns {
		private BookBorrowReqs() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/bookborrowreqs");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.bookborrowreqs";
		public static final String TABLE_NAME = "bookborrowreqs";
		public static final String GUID = "guid";
		public static final String DATETIME = "datetime";
		public static final String REQUESTERID = "requesterID";
		public static final String BOOKOWNID = "bookownID";
		public static final String PLANED_RETURN_DATE = "planedReturnDate";
		public static final String REMARK = "remark";
		public static final String STATUS = "status";
	}

	private void setContentValues(ContentValues values) {
		values.put(BookBorrowReqs.GUID, this.guid);
		values.put(BookBorrowReqs.DATETIME, Utils.formatDateTime(datetime));
		values.put(BookBorrowReqs.REQUESTERID, requesterID);
		values.put(BookBorrowReqs.BOOKOWNID, bookownID);
		values.put(BookBorrowReqs.PLANED_RETURN_DATE,
				Utils.formatDateTime(planned_return_date));
		values.put(BookBorrowReqs.REMARK, remark);
		values.put(BookBorrowReqs.STATUS, status);
	}

	public BookBorrowReq(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
			this.setDatetime(Utils.parseDateTimeString(jsonObject
					.getString("datetime")));
			this.requester = new User(jsonObject.getJSONObject("requester"));
			this.requesterID = this.requester.getGuid();
			this.setBookown(new BookOwn(jsonObject.getJSONObject("bo_ship")));
			this.bookownID = this.getBookown().getGuid();
			this.setPlannedReturnDate(Utils.parseDateString(jsonObject
					.getString("planed_return_date")));
			this.remark = jsonObject.getString("remark");
			this.setStatus(jsonObject.getInt("status"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public BookBorrowReq(Cursor data) {
		int idx_guid = data.getColumnIndex(BookBorrowReqs.GUID);
		int idx_datetime = data.getColumnIndex(BookBorrowReqs.DATETIME);
		int idx_requesterID = data.getColumnIndex(BookBorrowReqs.REQUESTERID);
		int idx_bookownID = data.getColumnIndex(BookBorrowReqs.BOOKOWNID);
		int idx_planedReturnDate = data
				.getColumnIndex(BookBorrowReqs.PLANED_RETURN_DATE);
		int idx_remark = data.getColumnIndex(BookBorrowReqs.REMARK);
		int idx_status = data.getColumnIndex(BookBorrowReqs.STATUS);
		int idx_username = data.getColumnIndex(Users.USERNAME);
		int idx_avatar = data.getColumnIndex(Users.AVATAR);
		int idx_title = data.getColumnIndex(Books.TITLE);
		int idx_cover = data.getColumnIndex(Books.COVER);

		this.guid = data.getLong(idx_guid);
		this.datetime = Utils.parseDateTimeString(data.getString(idx_datetime));
		this.requesterID = data.getLong(idx_requesterID);
		this.bookownID = data.getLong(idx_bookownID);
		this.planned_return_date = Utils.parseDateTimeString(data
				.getString(idx_planedReturnDate));
		this.remark = data.getString(idx_remark);
		this.status = data.getInt(idx_status);
		this.requester = new User();
		this.requester.setUsername(data.getString(idx_username));
		this.requester.setAvatar(data.getString(idx_avatar));
		Book book = new Book();
		book.setTitle(data.getString(idx_title));
		book.setCover(data.getString(idx_cover));
		this.bookown = new BookOwn();
		this.bookown.setBook(book);
	}

	public User getRequester() {
		return requester;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public BookOwn getBookown() {
		return bookown;
	}

	public void setBookown(BookOwn bookown) {
		this.bookown = bookown;
	}

	public Date getPlannedReturnDate() {
		return planned_return_date;
	}

	public void setPlannedReturnDate(Date planned_return_date) {
		this.planned_return_date = planned_return_date;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Uri save(ContentResolver contentResolver) {
		if (this.bookown != null) {
			this.bookown.save(contentResolver);
		}
		if (this.requester != null) {
			this.requester.save(contentResolver);
		}
		ContentValues values = new ContentValues();
		setContentValues(values);
		return contentResolver.insert(BookBorrowReqs.CONTENT_URI, values);
	}

	public long getGuid() {
		return guid;
	}

	public int update(ContentResolver contentResolver) {
		ContentValues values = new ContentValues();
		setContentValues(values);
		return contentResolver.update(
				Uri.withAppendedPath(BookBorrowReqs.CONTENT_URI, "guid/"
						+ this.guid), values, null, null);
	}
}
