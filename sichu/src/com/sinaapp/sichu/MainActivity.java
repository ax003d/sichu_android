package com.sinaapp.sichu;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.slidingmenu.SlidingMenu;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.sinaapp.sichu.widget.NavigationItem;
import com.sinaapp.sichu.widget.NavigationWidget;

public class MainActivity extends SlidingActivity {
    private final class ListNavigationAdapter extends ArrayAdapter<String> {
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
    } 
	
    private ListNavigationAdapter adapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        adapter = new ListNavigationAdapter();
        adapter.add("Books");
        adapter.add("Friends");
        adapter.add("About");
        
		NavigationWidget navigationWidget = new NavigationWidget(this);
		navigationWidget.setAdapter(adapter);
		setBehindContentView(navigationWidget);
		
        final SlidingMenu si = getSlidingMenu();
        si.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        si.setBehindWidthRes(R.dimen.demo_menu_width);
        si.setShadowWidth(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
