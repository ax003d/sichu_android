package com.ax003d.sichu.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.ax003d.sichu.net.HttpEntityWithProgress.ProgressListener;

public interface ISichuAPI {
	JSONObject account_login(String username, String password,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject account_login_by_weibo(String uid, String screen_name,
			String profile_image_url, String access_token, String expires_in,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject account_unbind_weibo(ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject account_bind_weibo(String uid, String screen_name,
			String profile_image_url, String access_token, String expires_in,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject account__may_know(String wb_ids,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject account__update_gexinid(String client_id,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject account__numbers(String uid, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookown(String uid, boolean trim_owner, String next,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject bookownByID(String id, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookownAdd(String isbn, String status, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject bookownDelete(String guid, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookownEdit(String guid, String status, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject oplog(String next, String category, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookborrow(String next, boolean asBorrower,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject bookborrow__detail(String rec_id,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject follow(String next, String asFollower,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject bookborrowrequest(String next, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookborrowrequest__detail(String request_id, String status,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject bookborrow__add(String bo_ship, String planed_return_date,
			String remark, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject friends__follow(String wb_id, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;
}
