package com.ax003d.sichu.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.ax003d.sichu.utils.Utils;

public class BookBorrowReq {
	private long guid;
	private Date datetime;
	private long requesterID;
	private long bookownID;
	private Date planned_return_date;
	private String remark;
	private int status;
	private User requester;
	private BookOwn bookown;
	
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
}
