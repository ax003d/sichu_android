package com.sinaapp.sichu;

import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.slidingmenu.SlidingMenu;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.sinaapp.sichu.widget.NavigationWidget;

public class MainActivity extends SlidingActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		NavigationWidget navigationWidget = new NavigationWidget(this);
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
