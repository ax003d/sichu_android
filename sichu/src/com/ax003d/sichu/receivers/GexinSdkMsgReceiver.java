package com.ax003d.sichu.receivers;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.igexin.sdk.Consts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class GexinSdkMsgReceiver extends BroadcastReceiver {

	private ISichuAPI api_client = null;
	
	public GexinSdkMsgReceiver () {}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		api_client = SichuAPI.getInstance(context);
		Bundle bundle = intent.getExtras();
		switch (bundle.getInt(Consts.CMD_ACTION)) {
		case Consts.GET_MSG_DATA:
			byte[] payload = bundle.getByteArray("payload");
			if (payload != null) {
				String data = new String(payload);
				Log.d("gexin", "Got Payload:" + data);
			}
			break;
		case Consts.GET_CLIENTID:
			String cid = bundle.getString("clientid");
			new UpdateGexinIDTask().execute(cid);
			Log.d("gexin", "Got ClientID:" + cid);
			break;

		default:
			break;
		}
	}

	private class UpdateGexinIDTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				api_client.account__update_gexinid(params[0], null);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
