package com.sinaapp.sichu;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.slidingmenu.SlidingMenu;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.sinaapp.sichu.fragments.FriendsFragment;
import com.sinaapp.sichu.fragments.MessagesFragment;
import com.sinaapp.sichu.fragments.MyBooksFragment;
import com.sinaapp.sichu.widget.NavigationItem;
import com.sinaapp.sichu.widget.NavigationWidget;

public class MainActivity extends SlidingActivity implements TabListener {
    private final class ListNavigationAdapter extends ArrayAdapter<String> implements OnItemClickListener {
    	private int lastSelectedItem = 0;
    	
        public ListNavigationAdapter() {
            this(new ArrayList<String>());
        }
        
        public ListNavigationAdapter(List<String> list) {
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
            String item = getItem(position);
            view.setLabel(item);
            view.setSelectionHandlerVisiblity(lastSelectedItem == position ? View.VISIBLE
                    : View.INVISIBLE);
            return view;
        }

    	@Override
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    		lastSelectedItem = position;
    		String title = fragments[lastSelectedItem];
    		notifyDataSetInvalidated();
            replaceTabs(title);
            getSupportActionBar().setSubtitle(title);
            getSlidingMenu().showAbove(true);
    	}    
    } 
	
    private ListNavigationAdapter adapter;
    private static String[] fragments = {"Books", "Friends", "Messages", "Account", "About"};
    private static String[] books_tabs = {"My", "Borrowed", "Loaned"};
    private static String[] friends_tabs = {"Following", "Follower"};
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        adapter = new ListNavigationAdapter();
        for (int i=0; i < fragments.length; i++) {
        	adapter.add(fragments[i]);
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
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayHomeAsUpEnabled(true);        
	}
	
	private void replaceTabs(String title) {
		final ActionBar ab = getSupportActionBar();
		ab.removeAllTabs();
		
		if (title.equals("Books")) {
			for (int i=0; i < books_tabs.length; i++) {
		        ActionBar.Tab tab = ab.newTab();
		        tab.setText(books_tabs[i]);
		        tab.setTabListener(this);
		        ab.addTab(tab);		
			}
		} else if (title.equals("Friends")) {
			for (int i=0; i < friends_tabs.length; i++) {
		        ActionBar.Tab tab = getSupportActionBar().newTab();
		        tab.setText(friends_tabs[i]);
		        tab.setTabListener(this);
		        ab.addTab(tab);		
			}			
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

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if ( tab.getText().equals("My") ) {
			ft.replace(android.R.id.content, MyBooksFragment.getInstance());
		} else if (tab.getText().equals("Borrowed")) {
			ft.replace(android.R.id.content, FriendsFragment.getInstance());
		} else if (tab.getText().equals("Loaned")) {
			ft.replace(android.R.id.content, MessagesFragment.getInstance());
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}    
}
