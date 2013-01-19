package com.ax003d.sichu.utils;

import com.weibo.sdk.android.Weibo;

public class WeiboUtils {
	private static final String CONSUMER_KEY = "1157643996";
	private static final String REDIRECT_URL = "http://sichu.sinaapp.com/callback/weibo/authorize/";
	private static Weibo g_weibo;

	public static Weibo getWeiboInstance() {
		if (g_weibo == null) {
			g_weibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);
		}
		return g_weibo;
	}

}
