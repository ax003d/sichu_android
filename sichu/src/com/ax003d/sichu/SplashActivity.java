package com.ax003d.sichu;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.holoeverywhere.app.Activity;

import com.ax003d.sichu.utils.Preferences;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				Date now = new Date();
				long expire = Preferences.getExpire(SplashActivity.this) * 1000;

				finish();
				if (Preferences.getToken(SplashActivity.this) != null
						&& now.getTime() < expire) {
					startActivity(new Intent(SplashActivity.this,
							MainActivity.class));
				} else {
					startActivity(new Intent(SplashActivity.this,
							LoginActivity.class));
				}
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(timerTask, 3000);
	}

}
