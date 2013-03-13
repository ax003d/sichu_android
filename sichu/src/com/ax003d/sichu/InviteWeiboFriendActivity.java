package com.ax003d.sichu;

import java.io.IOException;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.ax003d.sichu.utils.AccessTokenKeeper;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

public class InviteWeiboFriendActivity extends Activity implements
		OnClickListener {

	private EditText edit_invite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_weibo_friend);

		findViewById(R.id.btn_send).setOnClickListener(this);
		edit_invite = (EditText) findViewById(R.id.edit_invite);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			edit_invite
					.setText("I have some books to share with you! You can see them in @MiCabinet! Link is: http://sichu.sinaapp.com "
							+ extras.getString("screen_name"));
		}
	}

	@Override
	public void onClick(View v) {
		String status = edit_invite.getText().toString();
		if (TextUtils.isEmpty(status)) {
			return;
		}
		StatusesAPI statusesAPI = new StatusesAPI(
				AccessTokenKeeper.readAccessToken(this));
		statusesAPI.update(status, null, null, new RequestListener() {

			@Override
			public void onIOException(IOException arg0) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(InviteWeiboFriendActivity.this,
								"Invite friends failed!", Toast.LENGTH_SHORT)
								.show();
					}
				});

			}

			@Override
			public void onError(WeiboException arg0) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(InviteWeiboFriendActivity.this,
								"Invite friends failed!", Toast.LENGTH_SHORT)
								.show();
					}
				});
			}

			@Override
			public void onComplete(String arg0) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(InviteWeiboFriendActivity.this,
								"Invite friends success!", Toast.LENGTH_SHORT)
								.show();
						InviteWeiboFriendActivity.this.finish();
					}
				});
			}
		});
	}
}
