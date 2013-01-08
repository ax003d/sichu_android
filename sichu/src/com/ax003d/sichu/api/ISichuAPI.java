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

	JSONObject bookown(String next, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;

	JSONObject bookownByID(String id, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;
	
	JSONObject bookownAdd(String isbn, String status, String remark,
			ProgressListener progressListener) throws ClientProtocolException,
			IOException, JSONException;
	
	JSONObject oplog(String next, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;	
	
	JSONObject bookborrow(String next, boolean asBorrower, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;
	
	JSONObject follow(String next, boolean asFollower, ProgressListener progressListener)
			throws ClientProtocolException, IOException, JSONException;		
}