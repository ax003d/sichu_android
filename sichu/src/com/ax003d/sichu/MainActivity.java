package com.ax003d.sichu;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.slidingmenu.SlidingMenu;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.fragments.AccountFragment;
import com.ax003d.sichu.fragments.BooksBorrowedFragment;
import com.ax003d.sichu.fragments.BooksLoanedFragment;
import com.ax003d.sichu.fragments.BooksMineFragment;
import com.ax003d.sichu.fragments.FollowerFragment;
import com.ax003d.sichu.fragments.FollowingFragment;
import com.ax003d.sichu.fragments.MessagesFragment;
import com.ax003d.sichu.models.BookOwn;
import com.ax003d.sichu.models.BookOwn.BookOwns;
import com.ax003d.sichu.utils.Preferences;
import com.ax003d.sichu.utils.Utils;
import com.ax003d.sichu.utils.WeiboAuthDialogListener;
import com.ax003d.sichu.utils.WeiboUtils;
import com.ax003d.sichu.widget.NavigationItem;
import com.ax003d.sichu.widget.NavigationWidget;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.sso.SsoHandler;


public class MainActivity extends SlidingActivity implements TabListener {
	public int pre_page = -1;
	public int page = -1;

	private final class ListNavigationAdapter extends ArrayAdapter<Integer>
			implements OnItemClickListener {
		private int lastSelectedItem = 0;

		public ListNavigationAdapter() {
			this(new ArrayList<Integer>());
		}

		public ListNavigationAdapter(List<Integer> list) {
			super(MainActivity.this, android.R.id.text1, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			NavigationItem view;
			if (convertView == null) {
				view = new NavigationItem(MainActivity.this);
				view.setSelectionHandlerColorResource(R.color.holo_blue_light);
			} else {
				view = (NavigationItem) convertView;
			}
			int item = getItem(position);
			view.setLabel(item);
			view.setSelectionHandlerVisiblity(lastSelectedItem == position ? View.VISIBLE
					: View.INVISIBLE);
			return view;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			lastSelectedItem = position;
			notifyDataSetInvalidated();
			pre_page = page;
			page = pages[lastSelectedItem];
			replaceTabs();
			getSupportActionBar().setSubtitle(page);
			getSlidingMenu().showAbove(true);
		}
	}

	private ListNavigationAdapter adapter;
	private ISichuAPI api_client;
	SsoHandler mSsoHandler;
	private long userID;
	private UpdatePreferHandler preferHandler;
	private ProgressDialog mDialog;
	private static int[] pages = { R.string.page_books, R.string.page_friends,
			R.string.page_messages, R.string.page_account };
	
	private static int[] books_tabs = { R.string.books_mine,
			R.string.books_loaned, R.string.books_borrowed };
	private static int[] friends_tabs = { R.string.friends_following,
			R.string.friends_follower }; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		api_client = SichuAPI.getInstance(this);
		userID = Preferences.getUserID(this);

		adapter = new ListNavigationAdapter();
		for (int i = 0; i < pages.length; i++) {
			adapter.add(pages[i]);
		}

		NavigationWidget navigationWidget = new NavigationWidget(this);
		navigationWidget.setAdapter(adapter);
		navigationWidget.setOnItemClickListener(adapter);
		navigationWidget.performItemClick(0);
		setBehindContentView(navigationWidget);

		final SlidingMenu si = getSlidingMenu();
		si.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		si.setBehindWidthRes(R.dimen.demo_menu_width);
		si.setShadowWidth(0);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(true);

		preferHandler = new UpdatePreferHandler(this);
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getBoolean("ask_following") && (!WeiboUtils.isFollower())) {
			WeiboUtils.askFollowing(this);
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (page != R.string.page_account) {
			getSupportMenuInflater().inflate(R.menu.main, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void replaceTabs() {
		if (page == pre_page) {
			return;
		}
		final ActionBar ab = getSupportActionBar();
		ab.removeAllTabs();

		if (page == R.string.page_books) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			for (int i = 0; i < books_tabs.length; i++) {
				ActionBar.Tab tab = ab.newTab();
				tab.setText(books_tabs[i]);
				tab.setTag(books_tabs[i]);
				tab.setTabListener(this);
				ab.addTab(tab);
			}
		} else if (page == R.string.page_friends) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			for (int i = 0; i < friends_tabs.length; i++) {
				ActionBar.Tab tab = getSupportActionBar().newTab();
				tab.setText(friends_tabs[i]);
				tab.setTag(friends_tabs[i]);
				tab.setTabListener(this);
				ab.addTab(tab);
			}
		} else if (page == R.string.page_messages) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	        ft.replace(android.R.id.content, MessagesFragment.getInstance());
	        ft.commit();
		} else if (page == R.string.page_account) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	        ft.replace(android.R.id.content, AccountFragment.getInstance());
	        ft.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void bindWeibo() {
		mSsoHandler = new SsoHandler(this,
				WeiboUtils.getWeiboInstance());
		mSsoHandler.authorize(new WeiboAuthDialogListener(this));		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
			if (mDialog == null) {
				mDialog = Utils.createLoginDialog(this);			
			}
			if (resultCode == RESULT_OK) {
				mDialog.show();
			}
		}
	}
	
	public void closeDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Integer tag = (Integer) tab.getTag();
		switch (tag) {
		case R.string.books_mine:			
			ft.replace(android.R.id.content, BooksMineFragment.getInstance());
			break;
		case R.string.books_loaned:
			ft.replace(android.R.id.content, BooksLoanedFragment.getInstance());
			break;
		case R.string.books_borrowed:
			ft.replace(android.R.id.content,
					BooksBorrowedFragment.getInstance());
			break;
		case R.string.friends_following:
			ft.replace(android.R.id.content, FollowingFragment.getInstance());
			break;
		case R.string.friends_follower:
			ft.replace(android.R.id.content, FollowerFragment.getInstance());
			break;
		default:
			break;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	private class SyncTask extends AsyncTask<String, LinearLayout, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				if (params.length == 0) {
					return api_client.oplog(null, null);
				} else {
					return api_client.oplog(params[0], null);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			if (result != null && result.has("objects")) {
				ContentResolver contentResolver = getContentResolver();
				try {
					JSONArray jOplogs = result.getJSONArray("objects");
					for (int i = 0; i < jOplogs.length(); i++) {
						JSONObject log = jOplogs.getJSONObject(i);
						switch (log.getInt("opcode")) {
						case 1:
							// add
							if (log.getString("model").equals(
									"sichu.cabinet.models.BookOwnership")) {
								BookOwn own = new BookOwn(new JSONObject(
										log.getString("data")));
								if (own != null) {
									own.save(contentResolver);
								}
								Preferences.setSyncTime(MainActivity.this,
										log.getLong("timestamp"));
							}
							break;
						case 2:
							// update
							if (log.getString("model").equals(
									"sichu.cabinet.models.BookOwnership")) {
								BookOwn own = new BookOwn(new JSONObject(
										log.getString("data")));
								if (own != null) {
									own.save(contentResolver);
								}
								Preferences.setSyncTime(MainActivity.this,
										log.getLong("timestamp"));
							}
							break;
						case 3:
							// delete
							if (log.getString("model").equals(
									"sichu.cabinet.models.BookOwnership")) {
								JSONObject ret = new JSONObject(
										log.getString("data"));
								contentResolver.delete(Uri.withAppendedPath(
										BookOwns.CONTENT_URI,
										"/guid/" + ret.getInt("id")), null,
										null);
								Preferences.setSyncTime(MainActivity.this,
										log.getLong("timestamp"));
							}
							break;
						default:
							break;
						} // endswitch
					} // endfor
					contentResolver.notifyChange(
							Uri.withAppendedPath(BookOwns.CONTENT_URI, "owner/"
									+ userID), null);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			} // endif

			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new SyncTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} // endif
			setSupportProgressBarIndeterminateVisibility(false);
		} // onPostExecute
	} // SyncTask
	
	private static class UpdatePreferHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;
		
		public UpdatePreferHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MainActivity activity = mActivity.get();
			if (activity != null) {
				activity.closeDialog();
				AccountFragment.getInstance().setScreenName();
				if (!WeiboUtils.isFollower()) {
					WeiboUtils.askFollowing(activity);
				}
			}
		}
	}
	
	public void sendMessage() {
		preferHandler.sendEmptyMessage(0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
