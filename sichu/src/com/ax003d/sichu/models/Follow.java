package com.ax003d.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ax003d.sichu.models.User.Users;
import com.ax003d.sichu.providers.SichuContentProvider;

public class Follow {
	private long guid;
	private long followingID;
	private String remark;
	private long userID;
	private User following;
	private User user;
	
	public static final class Follows implements BaseColumns {
		private Follows() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SichuContentProvider.AUTHORITY + "/follows");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sichu.follows";
		public static final String TABLE_NAME = "follows";
		public static final String GUID = "guid";
		public static final String FOLLOWINGID = "followingID";
		public static final String REMARK = "remark";
		public static final String USERID = "userID";
	}
	
	public void setContentValues(ContentValues values) {
		values.put(Follows.GUID, this.guid);
		values.put(Follows.FOLLOWINGID, this.followingID);
		values.put(Follows.REMARK, this.remark);
		values.put(Follows.USERID, this.userID);		
	}	
	
	public Follow(JSONObject jsonObject) {
		try {
			this.guid = jsonObject.getLong("id");
			this.following = new User(jsonObject.getJSONObject("following"));
			this.followingID = this.following.getGuid();
			this.remark = jsonObject.getString("remark");
			this.setUser(new User(jsonObject.getJSONObject("user")));
			this.userID = this.getUser().getGuid();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Follow(Cursor data) {
		int idx_guid = data.getColumnIndex(Follows.GUID);
		int idx_following_id = data.getColumnIndex(Follows.FOLLOWINGID);
		int idx_remark = data.getColumnIndex(Follows.REMARK);
		int idx_user_id = data.getColumnIndex(Follows.USERID);
		int idx_following_username = data.getColumnIndex("followingName");
		int idx_following_avatar = data.getColumnIndex("followingAvatar");
		int idx_follower_username = data.getColumnIndex("followerName");
		int idx_follower_avatar = data.getColumnIndex("followerAvatar");
		
		this.guid = data.getLong(idx_guid);
		this.followingID = data.getLong(idx_following_id);
		this.remark = data.getString(idx_remark);
		this.userID = data.getLong(idx_user_id);
		
		if ( idx_following_username != -1 ) {
			this.following = new User();
			this.following.setUsername(data.getString(idx_following_username));
			this.following.setAvatar(data.getString(idx_following_avatar));
		}
		if ( idx_follower_username != -1 ) {
			this.setUser(new User());
			this.getUser().setUsername(data.getString(idx_follower_username));
			this.getUser().setAvatar(data.getString(idx_follower_avatar));
		}
	}

	public User getFollowing() {
		return following;
	}
	
	public void setFollowing(User following) {
		this.following = following;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void save(ContentResolver contentResolver) {
		if (this.following != null) {
			this.following.save(contentResolver);
		}
		if (this.getUser() != null) {
			this.getUser().save(contentResolver);
		}
		ContentValues values = new ContentValues();
		setContentValues(values);
		contentResolver.insert(Follows.CONTENT_URI, values);		
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
