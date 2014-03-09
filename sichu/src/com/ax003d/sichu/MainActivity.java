package com.ax003d.sichu;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.app.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.ax003d.sichu.events.FollowEvent;
import com.ax003d.sichu.events.FollowEvent.Action;
import com.ax003d.sichu.fragments.AccountFragment;
import com.ax003d.sichu.fragments.BooksBorrowedFragment;
import com.ax003d.sichu.fragments.BooksLoanedFragment;
import com.ax003d.sichu.fragments.BooksMineFragment;
import com.ax003d.sichu.fragments.FollowerFragment;
import com.ax003d.sichu.fragments.FollowingFragment;
import com.ax003d.sichu.fragments.MayKnowFragment;
import com.ax003d.sichu.fragments.MessagesFragment;
import com.ax003d.sichu.utils.Utils;
import com.ax003d.sichu.widget.NavigationItem;
import com.igexin.slavesdk.MessageManager;
import com.squareup.otto.Subscribe;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends Activity implements TabListener {
	public static final String TAG = "MainActivity";

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
			// getSlidingMenu().showAbove(true);
		}
	}

	public static class SectionsPagerAdapter extends FragmentPagerAdapter {

		private int page;
		private int[] sections;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			changePage(0);
		}

		public void changePage(int page_id) {
			page = page_id;
			switch (page_id) {
			// books
			case 0:
				sections = books_tabs;
				break;
			// friends
			case 1:
				break;
			// messages
			case 2:
				break;
			// settings
			case 3:
				break;
			default:
				sections = books_tabs;
				break;
			}
			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int i) {
			if (page == 0) {
				switch (i) {
				case 0:
					return BooksMineFragment.getInstance();
				case 1:
					return BooksLoanedFragment.getInstance();
				case 2:
					return BooksBorrowedFragment.getInstance();
				}
			}

			return null;
		}

		@Override
		public int getCount() {
			return sections.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Section " + (position + 1);
		}
	}

	private ListNavigationAdapter adapter;
	private boolean reallyExit;
	private static int[] pages = { R.string.page_books, R.string.page_friends,
			R.string.page_messages, R.string.page_account };

	private static int[] books_tabs = { R.string.books_mine,
			R.string.books_loaned, R.string.books_borrowed };
	private static int[] friends_tabs = { R.string.friends_following,
			R.string.friends_follower, R.string.friends_may_know };
	private static int[] messages_tabs = { R.string.messages_borrow_request };

	private Platform weibo;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] drawer_menus;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	private CharSequence mDrawerTitle;
	private ViewPager vp_contents;

	private SectionsPagerAdapter mSectionsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ShareSDK.initSDK(this.getApplicationContext());
		// umeng sdk
		MobclickAgent.onError(this);
		UmengUpdateAgent.update(this);
		// gexin sdk
		MessageManager.getInstance().initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();
		drawer_menus = getResources().getStringArray(R.array.drawer_menus);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.lv_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_menu_item, drawer_menus));
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				drawerItemSelected(position);
			}
		});

		vp_contents = (ViewPager) findViewById(R.id.vp_contents);
		mSectionsAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		vp_contents.setAdapter(mSectionsAdapter);
		vp_contents
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						getSupportActionBar().setSelectedNavigationItem(
								position);
					}
				});

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			drawerItemSelected(0);
		}
		//
		// adapter = new ListNavigationAdapter();
		// for (int i = 0; i < pages.length; i++) {
		// adapter.add(pages[i]);
		// }
		//
		// NavigationWidget navigationWidget = new NavigationWidget(this);
		// navigationWidget.setAdapter(adapter);
		// navigationWidget.setOnItemClickListener(adapter);
		// navigationWidget.performItemClick(0);
		// setBehindContentView(navigationWidget);
		//
		// final SlidingMenu si = getSlidingMenu();
		// si.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		// si.setBehindWidthRes(R.dimen.demo_menu_width);
		// si.setShadowWidth(0);
		//
		// Utils.getBus().register(this);
		// Bundle extras = getIntent().getExtras();
		// if (extras != null && extras.getBoolean("ask_following") &&
		// (!Utils.isFollower)) {
		// checkFollow();
		// }
	}

	private void drawerItemSelected(int position) {
		Log.d(TAG, "drawer item " + position);
		mSectionsAdapter.changePage(position);
		ActionBar actionBar = getSupportActionBar();
		actionBar.removeAllTabs();
		for (int i = 0; i < mSectionsAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		mDrawerList.setItemChecked(position, true);
		getSupportActionBar().setSubtitle(drawer_menus[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	public void bindWeibo() {
		initWeibo();
		weibo.authorize();
	}

	private void initWeibo() {
		if (weibo == null) {
			weibo = ShareSDK.getPlatform(this, SinaWeibo.NAME);
			weibo.setPlatformActionListener(Utils.paListener);
		}
	}

	public void checkFollow() {
		initWeibo();
		weibo.showUser(Utils.MICABINET_UID + "");
	}

	public void follow() {
		initWeibo();
		weibo.followFriend(Utils.MICABINET_UID + "");
	}

	public void listFriend() {
		initWeibo();
		weibo.listFriend(50, 0, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (page != R.string.page_account) {
			getSupportMenuInflater().inflate(R.menu.main, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	@SuppressLint("NewApi")
	public void onBackPressed() {
		if (reallyExit) {
			super.onBackPressed();
			return;
		}

		reallyExit = true;
		Toast.makeText(this, R.string.hint_exit, Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				reallyExit = false;
			}
		}, 2000);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// toggle();
		}
		return super.onKeyUp(keyCode, event);
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
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			for (int i = 0; i < messages_tabs.length; i++) {
				ActionBar.Tab tab = ab.newTab();
				tab.setText(messages_tabs[i]);
				tab.setTag(messages_tabs[i]);
				tab.setTabListener(this);
				ab.addTab(tab);
			}
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.replace(android.R.id.content, MessagesFragment.getInstance());
			ft.commit();
		} else if (page == R.string.page_account) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.replace(android.R.id.content, AccountFragment.getInstance());
			ft.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// toggle();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		vp_contents.setCurrentItem(tab.getPosition());
//		Integer tag = (Integer) tab.getTag();
//		switch (tag) {
//		case R.string.books_mine:
//			ft.replace(android.R.id.content, BooksMineFragment.getInstance());
//			break;
//		case R.string.books_loaned:
//			ft.replace(android.R.id.content, BooksLoanedFragment.getInstance());
//			break;
//		case R.string.books_borrowed:
//			ft.replace(android.R.id.content,
//					BooksBorrowedFragment.getInstance());
//			break;
//		case R.string.friends_following:
//			ft.replace(android.R.id.content, FollowingFragment.getInstance());
//			break;
//		case R.string.friends_follower:
//			ft.replace(android.R.id.content, FollowerFragment.getInstance());
//			break;
//		case R.string.friends_may_know:
//			ft.replace(android.R.id.content, MayKnowFragment.getInstance());
//			break;
//
//		default:
//			break;
//		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		Utils.getBus().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ShareSDK.stopSDK(this);
	}

	@Subscribe
	public void askFollow(FollowEvent event) {
		if (event.mAction == Action.FOLLOW) {
			follow();
		} else if (event.mAction == Action.ASK_FOLLOW) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Utils.askFollowing(MainActivity.this);
				}
			});
		}
	}
}
