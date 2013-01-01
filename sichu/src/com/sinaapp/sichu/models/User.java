package com.sinaapp.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sinaapp.sichu.providers.SichuContentProvider;

public class User {
	private long guid;
	private String username;
	private String lastName;
	private String firstName;
	
	public static final class Users implements BaseColumns {
		private Users() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/users");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.users";
		public static final String TABLE_NAME = "users";
		public static final String GUID = "guid";
		public static final String USERNAME = "username";
		public static final String LAST_NAME = "lastName";
		public static final String FIRST_NAME = "firstName";
	}
	
	private void setContentValues(ContentValues values) {
		values.put(Users.GUID, guid);
		values.put(Users.USERNAME, getUsername());
		values.put(Users.LAST_NAME, lastName);
		values.put(Users.FIRST_NAME, firstName);
	}	
	
	public User(JSONObject jsonObject) {
		try {
			this.setGuid(jsonObject.getLong("id"));
			this.setUsername(jsonObject.getString("username"));
			this.lastName = jsonObject.getString("last_name");
			this.firstName = jsonObject.getString("first_name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public User() {
	}

	public long getGuid() {
		return guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public void save(ContentResolver contentResolver) {
		ContentValues values = new ContentValues();
		setContentValues(values);
		contentResolver.insert(Users.CONTENT_URI, values);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
