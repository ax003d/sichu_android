package com.sinaapp.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Book {
	private long guid;
	private String ISBN;
	private String title;
	private String author;
	private String doubanID;
	private String cover;
	
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
}
