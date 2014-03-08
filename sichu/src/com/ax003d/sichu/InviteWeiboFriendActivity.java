package com.ax003d.sichu;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.sina.weibo.SinaWeibo.ShareParams;

import com.ax003d.sichu.events.ShareEvent;
import com.ax003d.sichu.utils.Utils;
import com.squareup.otto.Subscribe;

public class InviteWeiboFriendActivity extends Activity implements
		OnClickListener {

	private EditText edit_invite;
	private Platform weibo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ShareSDK.initSDK(this.getApplicationContext());
		setContentView(R.layout.activity_invite_weibo_friend);

		findViewById(R.id.btn_send).setOnClickListener(this);
		edit_invite = (EditText) findViewById(R.id.edit_invite);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			edit_invite.setText(String.format(
					getString(R.string.msg_invite_weibo_friend),
					extras.getString("screen_name"), extras.getLong("uid")));
		}
		Utils.getBus().register(this);
	}

	private void initWeibo() {
		if (weibo == null) {
			weibo = ShareSDK.getPlatform(this, SinaWeibo.NAME);
			weibo.setPlatformActionListener(Utils.paListener);
		}
	}

	public void share(ShareParams param) {
		initWeibo();
		weibo.share(param);
	}

	@Override
	public void onClick(View v) {
		String status = edit_invite.getText().toString();
		if (TextUtils.isEmpty(status)) {
			return;
		}
		ShareParams params = new ShareParams();
		params.setText(status);
		share(params);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.getBus().register(this);
	}

	@Subscribe
	public void onShared(ShareEvent event) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(InviteWeiboFriendActivity.this, R.string.ok_invite_weibo_friend,
						Toast.LENGTH_SHORT).show();
				InviteWeiboFriendActivity.this.finish();				
			}
		});		
	}
}
