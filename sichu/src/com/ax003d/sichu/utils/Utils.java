package com.ax003d.sichu.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.CheckBox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;

import com.ax003d.sichu.R;
import com.ax003d.sichu.events.FollowEvent;
import com.ax003d.sichu.events.ListFriendsEvent;
import com.ax003d.sichu.events.ShareEvent;
import com.ax003d.sichu.events.FollowEvent.Action;
import com.ax003d.sichu.events.PlatformAuthorizeEvent;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class Utils {

	// private static String TAG = Utils.class.getSimpleName();
	private static ImageLoader img_loader;
	private static DisplayImageOptions cloud_options;
	private static SimpleDateFormat dateTimeFmt = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
	private static Bus bus;

	public static final long MICABINET_UID = 2749010082L;
	public static boolean isFollower = false;

	public static PlatformActionListener paListener = new PlatformActionListener() {

		@Override
		public void onError(Platform platform, int action, Throwable e) {
			if (action == Platform.ACTION_AUTHORIZING) {
				Utils.getBus().post(new PlatformAuthorizeEvent());
			}
		}

		@Override
		public void onComplete(Platform platform, int action,
				HashMap<String, Object> res) {
			if (action == Platform.ACTION_AUTHORIZING) {
				PlatformDb db = platform.getDb();
				String token = db.getToken();
				long expiresTime = db.getExpiresTime();
				String id = db.getUserId();
				String name = db.getUserName();
				String icon = db.getUserIcon();

				Utils.getBus().post(
						new PlatformAuthorizeEvent(token, expiresTime, id,
								name, icon));
				return;
			}

			if (action == Platform.ACTION_USER_INFOR) {
				long id = (Long) res.get("id");
				boolean following = (Boolean) res.get("following");
				if ((id == MICABINET_UID) && (!following)) {
					Utils.getBus().post(new FollowEvent(Action.ASK_FOLLOW));
				}
				return;
			}

			if (action == Platform.ACTION_FOLLOWING_USER) {
				isFollower = true;
				return;
			}

			if (action == Platform.ACTION_SHARE) {
				Utils.getBus().post(new ShareEvent());
				return;
			}

			if (action == Platform.ACTION_GETTING_FRIEND_LIST) {
				ArrayList<HashMap<String, Object>> users = (ArrayList<HashMap<String, Object>>) res
						.get("users");
				Utils.getBus().post(new ListFriendsEvent(users));
				return;
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			// Do nothing
		}
	};

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

	public static Bus getBus() {
		if (bus == null) {
			bus = new Bus(ThreadEnforcer.ANY);
		}
		return bus;
	}

	public static void askFollowing(Context context) {
		Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.following, null);
		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.chk_following);
		builder.setTitle(R.string.title_following);
		builder.setView(view);
		builder.setCancelable(false);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!checkBox.isChecked()) {
					return;
				}
				getBus().post(new FollowEvent(Action.FOLLOW));
			}
		};
		builder.setPositiveButton(android.R.string.ok, onClickListener);
		builder.create().show();
	}
}
