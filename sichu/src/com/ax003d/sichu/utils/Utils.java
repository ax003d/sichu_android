package com.ax003d.sichu.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.holoeverywhere.app.ProgressDialog;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;

import com.ax003d.sichu.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class Utils {

	// private static String TAG = Utils.class.getSimpleName();
	private static ImageLoader img_loader;
	private static DisplayImageOptions cloud_options;
	private static SimpleDateFormat dateTimeFmt = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");

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

	public static DisplayImageOptions getCloudOptions() {
		if (cloud_options == null) {
			cloud_options = new DisplayImageOptions.Builder()
					.showStubImage(R.drawable.ic_book)
					.showImageForEmptyUri(R.drawable.ic_book)
					.resetViewBeforeLoading().cacheInMemory().cacheOnDisc()
					.delayBeforeLoading(100).build();
		}
		return cloud_options;
	}

	public static ImageLoader getImageLoader(Context context) {
		if (img_loader == null) {
			img_loader = ImageLoader.getInstance();
			File cacheDir = StorageUtils.getOwnCacheDirectory(context,
					"sichu/cache");
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.showStubImage(R.drawable.ic_book)
					.showImageForEmptyUri(R.drawable.ic_book)
					.resetViewBeforeLoading().cacheInMemory().build();
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
					context).memoryCacheExtraOptions(80, 80)
					.discCache(new UnlimitedDiscCache(cacheDir))
					.defaultDisplayImageOptions(options).build();
			img_loader.init(config);
		}
		return img_loader;
	}

	public static Date parseDateTimeString(String date) {
		try {
			Date dt = dateTimeFmt.parse(date);
			return dt;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String formatDateTime(Date date) {
		return dateTimeFmt.format(date);
	}

	public static Date parseDateString(String date) {
		try {
			Date dt = dateFmt.parse(date);
			return dt;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String formatDate(Date date) {
		return dateFmt.format(date);
	}

	public static boolean isLogin(Context context) {
		return (Preferences.getUserID(context) != -1) && (!isExpired(context));
	}

	public static ProgressDialog createLoginDialog(Context context) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setCancelable(false);
		dialog.setMessage(context.getResources().getString(R.string.msg_login));
		dialog.setIndeterminate(true);
		return dialog;
	}

	public static boolean isExpired(Context context) {
		Date now = new Date();
		long expire = Preferences.getExpire(context) * 1000;
		if (Preferences.getToken(context) != null && now.getTime() < expire) {
			return false;
		} else {
			return true;
		}
	}

	public static PackageInfo getPackagetInfo(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
		}
		return null;
	}

	public static boolean isEmailValid(String email) {
		return Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public static void hideKeyboard(Context context, IBinder token) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(token, 0);
	}
}
