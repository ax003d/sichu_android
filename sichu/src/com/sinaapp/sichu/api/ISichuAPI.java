package com.sinaapp.sichu.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.sinaapp.sichu.net.HttpEntityWithProgress.ProgressListener;

public interface ISichuAPI {
	JSONObject account_login(String username, String password,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;

	JSONObject bookown(String next, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookownAdd(String isbn, String status, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;
}
