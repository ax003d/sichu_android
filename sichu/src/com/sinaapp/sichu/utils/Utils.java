package com.sinaapp.sichu.utils;

import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

	// private static String TAG = Utils.class.getSimpleName();

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	public static long getUTCTimeStamp() {
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		int offset = tz.getOffset(now.getTime());
		return (now.getTime() - offset) / 1000;
	}
	
	public static Date getDateFromUTCTimeStamp(long timestamp) {
		TimeZone tz = TimeZone.getDefault();
		int offset = tz.getOffset(timestamp * 1000);			
		long lcl_timestamp = timestamp * 1000 + offset; 		
		return new Date(lcl_timestamp);
	}	
}
