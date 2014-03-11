package com.ax003d.sichu;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.app.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import com.igexin.slavesdk.MessageManager;
import com.squareup.otto.Subscribe;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";

	public int pre_page = -1;
	public int page = -1;

	public static class SectionsPagerAdapter extends FragmentPagerAdapter {

		private int[] sections;
		private Context mContext;

		public SectionsPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			mContext = context;
			changePage(0);
		}

		public void changePage(int page_id) {
			switch (page_id) {
			// books
			case 0:
				sections = books_tabs;
				break;
			// friends
			case 1:
				sections = friends_tabs;
				break;
			// messages
			case 2:
				sections = messages_tabs;
				break;
			// settings
			case 3:
				sections = settings_tabs;
				break;
			default:
				sections = books_tabs;
				break;
			}
			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int i) {
			switch (sections[i]) {
			case R.string.books_mine:
				return BooksMineFragment.getInstance();
			case R.string.books_loaned:
				return BooksLoanedFragment.getInstance();
			case R.string.books_borrowed:
				return BooksBorrowedFragment.getInstance();
			case R.string.friends_following:
				return FollowingFragment.getInstance();
			case R.string.friends_follower:
				return FollowerFragment.getInstance();
			case R.string.friends_may_know:
				return MayKnowFragment.getInstance();
			case R.string.messages_borrow_request:
				return MessagesFragment.getInstance();
			case R.string.settings:
				return AccountFragment.getInstance();
			default:
				break;
			}

			return null;
		}

		@Override
		public int getCount() {
			return sections.length;
		}

		@Override
		public long getItemId(int position) {
			return sections[position];
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mContext.getString(sections[position]);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			FragmentManager manager = ((Fragment) object).getFragmentManager();
			if (manager == null) {
				return;
			}
			FragmentTransaction trans = manager.beginTransaction();
			trans.remove((Fragment) object);
			trans.commit();
		}

		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	private boolean reallyExit;
	private static int[] books_tabs = { R.string.books_mine,
			R.string.books_loaned, R.string.books_borrowed };
	private static int[] friends_tabs = { R.string.friends_following,
			R.string.friends_follower, R.string.friends_may_know };
	private static int[] messages_tabs = { R.string.messages_borrow_request };
	private static int[] settings_tabs = { R.string.settings };

	private Platform weibo;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] drawer_menus;
	private ActionBarDrawerToggle mDrawerToggle;
	private ViewPager vp_contents;
	private SectionsPagerAdapter mSectionsAdapter;
	private PagerTitleStrip pager_title_strip;

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

		drawer_menus = getResources().getStringArray(R.array.drawer_menus);
		pager_title_strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
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
		mSectionsAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(), this);
		vp_contents.setAdapter(mSectionsAdapter);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			drawerItemSelected(0);
		}
	}

	private void drawerItemSelected(int position) {
		if (position == 3) {
			pager_title_strip.setVisibility(View.GONE);
		} else {
			pager_title_strip.setVisibility(View.VISIBLE);
		}
		vp_contents.setCurrentItem(0);
		mSectionsAdapter.changePage(position);
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

	private void toggle() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			mDrawerLayout.openDrawer(mDrawerList);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
		}
		return super.onKeyUp(keyCode, event);
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
