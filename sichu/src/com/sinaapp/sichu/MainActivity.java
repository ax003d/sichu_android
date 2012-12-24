package com.sinaapp.sichu;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.slidingmenu.SlidingMenu;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.sinaapp.sichu.fragments.AboutFragment;
import com.sinaapp.sichu.fragments.BookCabinetFragment;
import com.sinaapp.sichu.fragments.FriendsFragment;
import com.sinaapp.sichu.widget.NavigationItem;
import com.sinaapp.sichu.widget.NavigationWidget;

public class MainActivity extends SlidingActivity {
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
    		Fragment fragment = null;
    		String title = null;
    		switch (position) {
    		case 0:
    			Toast.makeText(MainActivity.this, "Books", Toast.LENGTH_SHORT).show();
    			lastSelectedItem = 0;
    			fragment = BookCabinetFragment.getInstance();
    			title = "Books";
    			break;
    		case 1:
    			Toast.makeText(MainActivity.this, "Friends", Toast.LENGTH_SHORT).show();
    			lastSelectedItem = 1;
    			fragment = FriendsFragment.getInstance();
    			title = "Friends";
    			break;
    		case 2:
    			Toast.makeText(MainActivity.this, "About", Toast.LENGTH_SHORT).show();
    			lastSelectedItem = 2;
    			fragment = AboutFragment.getInstance();
    			title = "About";
    			break;
    		default:
    			break;
    		}
    		notifyDataSetInvalidated();
            replaceFragment(fragment);
            getSupportActionBar().setSubtitle(title);

            getSlidingMenu().showAbove(true);
    	}    
    } 
	
    private ListNavigationAdapter adapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        adapter = new ListNavigationAdapter();
        adapter.add("Books");
        adapter.add("Friends");
        adapter.add("About");
        
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
	}

    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, null);
    }

    public void replaceFragment(Fragment fragment,
            String backStackName) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(android.R.id.content, fragment);
        if (backStackName != null) {
            ft.addToBackStack(backStackName);
        }
        ft.commit();
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
}
