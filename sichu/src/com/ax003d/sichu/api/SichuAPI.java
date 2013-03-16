package com.ax003d.sichu.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ax003d.sichu.R;
import com.ax003d.sichu.net.ApiBase;
import com.ax003d.sichu.net.ApiRequest;
import com.ax003d.sichu.net.ApiResponse;
import com.ax003d.sichu.net.HttpEntityWithProgress.ProgressListener;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;

public class SichuAPI extends ApiBase implements ISichuAPI {

	private Context context;
	private static ISichuAPI INSTANCE;

	public SichuAPI(Context context) {
		super(context);
		this.context = context;
	}

	public static ISichuAPI getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new SichuAPI(context);
		}
		return INSTANCE;
	}

	@Override
	public ApiResponse execute(ApiRequest request, ProgressListener listener)
			throws ClientProtocolException, IOException {
		if (!Utils.isNetworkAvailable(context)) {
			throw new IOException("Network not available!");
		}

		if (!request.getPath().equals("/v1/account/login/")) {
			request.addHeader("AUTHORIZATION",
					"Bearer " + Preferences.getToken(context));
		}

		ApiResponse response = super.execute(request, listener);
		if (response.getStatusCode() == 401) {
			Preferences.expireToken(context);
		}

		return response;
	}

	@Override
	public JSONObject account_login(String username, String password,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/account/login/");
		request.addParameter("username", username);
		request.addParameter("password", password);
		request.addParameter("apikey", context.getString(R.string.apikey));

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject account_login_by_weibo(String uid, String screen_name,
			String profile_image_url, String access_token, String expires_in,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/account/login_by_weibo/");
		request.addParameter("uid", uid);
		request.addParameter("screen_name", screen_name);
		request.addParameter("profile_image_url", profile_image_url);
		request.addParameter("access_token", access_token);
		request.addParameter("expires_in", expires_in);
		request.addParameter("apikey", context.getString(R.string.apikey));

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject account_unbind_weibo(ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/account/unbind_weibo/");

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject account_bind_weibo(String uid, String screen_name,
			String profile_image_url, String access_token, String expires_in,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/account/bind_weibo/");
		request.addParameter("uid", uid);
		request.addParameter("screen_name", screen_name);
		request.addParameter("profile_image_url", profile_image_url);
		request.addParameter("access_token", access_token);
		request.addParameter("expires_in", expires_in);

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject bookown(String uid, boolean trim_owner, String next,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.GET,
				next == null ? "/v1/bookown/" : next);
		if (uid != null) {
			request.addParameter("uid", uid);
			Log.d("bookown", "uid: " + uid);
		}
		if (trim_owner) {
			request.addParameter("trim_owner", "1");
		}

		ApiResponse response = execute(request, progressListener);

		String resp = response.getContentAsString();
		Log.d("bookown", resp);
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookownAdd(String isbn, String status, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST, "/v1/bookown/add/");

		request.addParameter("isbn", isbn);
		request.addParameter("status", status == null ? "1" : status);
		if (remark != null) {
			request.addParameter("remark", remark);
		}

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject oplog(String next, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.GET,
				next == null ? "/v1/oplog/" : next);
		request.addParameter("timestamp__gt", Preferences.getSyncTime(context)
				+ "");

		ApiResponse response = execute(request, progressListener);

		String resp = response.getContentAsString();
		// Log.d("Sync", resp);
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookborrow(String next, boolean asBorrower,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		String url = null;
		if (next != null) {
			url = asBorrower ? next + "&as_borrower=1" : next;
		} else {
			url = asBorrower ? "/v1/bookborrow/?as_borrower=1"
					: "/v1/bookborrow/";
		}
		ApiRequest request = new ApiRequest(ApiRequest.GET, url);

		ApiResponse response = execute(request, progressListener);

		String resp = response.getContentAsString();
		// Log.d("Sync", resp);
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookownByID(String id, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.GET,
				"/v1/bookown/?id__exact=" + id);

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject follow(String next, boolean asFollower,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		String url = null;
		if (next != null) {
			url = asFollower ? next + "&as_follower=1" : next;
		} else {
			url = asFollower ? "/v1/follow/?as_follower=1" : "/v1/follow/";
		}
		ApiRequest request = new ApiRequest(ApiRequest.GET, url);

		ApiResponse response = execute(request, progressListener);

		String resp = response.getContentAsString();
		// Log.d("Follows", resp);
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookborrowrequest(String next,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.GET,
				next == null ? "/v1/bookborrowreq/" : next);

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject bookownEdit(String guid, String status, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST, "/v1/bookown/"
				+ guid + "/");
		request.addParameter("status", status);
		request.addParameter("remark", remark);
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookownDelete(String guid,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/bookown/delete/" + guid + "/");
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}

	@Override
	public JSONObject account__may_know(String wb_ids,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/account/may_know/");
		request.addParameter("wb_ids", wb_ids);
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}

	@Override
	public JSONObject account__update_gexinid(String client_id,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/account/update_gexinid/");
		request.addParameter("client_id", client_id);
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}

	@Override
	public JSONObject friends__follow(String wb_id,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/friends/follow/");
		request.addParameter("wb_id", wb_id);
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookborrow__add(String bo_ship,
			String planed_return_date, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST, "/v1/bookborrow/");
		request.addParameter("bo_ship", bo_ship);
		request.addParameter("planed_return_date", planed_return_date);
		request.addParameter("remark", remark);
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookborrowrequest__detail(String request_id,
			String status, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/bookborrowreq/" + request_id + "/");
		request.addParameter("status", status);
		ApiResponse response = execute(request, progressListener);
		String resp = response.getContentAsString();
		return new JSONObject(resp);
	}
}
