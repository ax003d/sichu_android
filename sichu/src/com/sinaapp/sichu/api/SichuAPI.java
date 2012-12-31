package com.sinaapp.sichu.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.sinaapp.sichu.R;
import com.sinaapp.sichu.net.ApiBase;
import com.sinaapp.sichu.net.ApiRequest;
import com.sinaapp.sichu.net.ApiResponse;
import com.sinaapp.sichu.net.HttpEntityWithProgress.ProgressListener;
import com.sinaapp.sichu.utils.Preferences;
import com.sinaapp.sichu.utils.Utils;

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
	public JSONObject bookown(String next, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.GET,
				next == null ? "/v1/bookown/" : next);

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}

	@Override
	public JSONObject bookownAdd(String isbn, String status,
			String remark, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.POST,
				"/v1/bookown/add/");

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
	public JSONObject bookborrow(String next, boolean asBorrower, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		String url = null;
		if ( next != null ) {
			url = asBorrower ? next + "&as_borrower=1" : next;
		} else {
			url = asBorrower ? "/v1/bookborrow/?as_borrower=1" : "/v1/bookborrow/";
		}
		ApiRequest request = new ApiRequest(ApiRequest.GET, url);

		ApiResponse response = execute(request, progressListener);

		String resp = response.getContentAsString();
		Log.d("Sync", resp);
		return new JSONObject(resp);
	}

	@Override
	public JSONObject bookownByID(String id, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException {
		ApiRequest request = new ApiRequest(ApiRequest.GET, "/v1/bookown/?id__exact=" + id);

		ApiResponse response = execute(request, progressListener);

		return new JSONObject(response.getContentAsString());
	}
}
