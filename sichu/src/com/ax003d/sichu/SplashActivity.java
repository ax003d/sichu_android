package com.ax003d.sichu;

import java.util.Timer;
import java.util.TimerTask;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;

import cn.sharesdk.framework.ShareSDK;

import com.ax003d.sichu.utils.Utils;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ShareSDK.initSDK(this.getApplicationContext());
		setContentView(R.layout.activity_splash);
		UMFeedbackService.enableNewReplyNotification(this, NotificationType.NotificationBar);
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (!Utils.isExpired(SplashActivity.this)) {
					startActivity(new Intent(SplashActivity.this,
							MainActivity.class));
				} else {
					startActivity(new Intent(SplashActivity.this,
							LoginActivity.class));
				}
				finish();
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(timerTask, 3000);
	}

}
