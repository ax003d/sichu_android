package com.ax003d.sichu.receivers;

import com.igexin.sdk.Consts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GexinSdkMsgReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
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
			Log.d("gexin", "Got ClientID:" + cid);
			break;

		default:
			break;
		}

	}

}
