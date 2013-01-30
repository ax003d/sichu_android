package com.ax003d.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.ax003d.sichu.providers.SichuContentProvider;

public class User implements Parcelable {
	private long guid;
	private String username;
	private String lastName;
	private String firstName;
	private String avatar;
	
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
		public static final String AVATAR = "avatar";
	}
	
	private void setContentValues(ContentValues values) {
		values.put(Users.GUID, guid);
		values.put(Users.USERNAME, getUsername());
		values.put(Users.LAST_NAME, lastName);
		values.put(Users.FIRST_NAME, firstName);
		values.put(Users.AVATAR, getAvatar());
	}	
	
	public User(JSONObject jsonObject) {
		try {
			this.setGuid(jsonObject.getLong("id"));
			this.setUsername(jsonObject.getString("username"));
			this.lastName = jsonObject.getString("last_name");
			this.firstName = jsonObject.getString("first_name");
			this.setAvatar(jsonObject.getString("avatar"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public User() {
	}

	public User(Parcel source) {
		guid = source.readLong();
		username = source.readString();
		lastName = source.readString();
		firstName = source.readString();
		avatar = source.readString();
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

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {		
		dest.writeLong(guid);
		dest.writeString(username);
		dest.writeString(lastName);
		dest.writeString(firstName);
		dest.writeString(avatar);
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
		
	};
}
