package com.ax003d.sichu.models;

import org.json.JSONException;
import org.json.JSONObject;

public class MayKnow {
	private String wb_uid;
	private String username;
	private String avatar;
	private String remark;
	
	public MayKnow(JSONObject jsonObject) {
		try {
			wb_uid = jsonObject.getLong("id") + "";
			username = jsonObject.getString("screen_name");
			avatar = jsonObject.getString("profile_image_url");
			remark = jsonObject.getString("remark");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getAvatar() {
		return avatar;
	}

	public String getRemark() {
		return remark;
	}

	public String getUsername() {
		return username;
	}
}
