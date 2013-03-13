package com.ax003d.sichu;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ax003d.sichu.models.User;
import com.ax003d.sichu.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FriendDetailActivity extends Activity {

	private User mFriend;
	private String mRemark;
	private DisplayImageOptions options;
	private ImageLoader img_loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);
		
		options = Utils.getCloudOptions();
		img_loader = Utils.getImageLoader(this);
		
		ImageView img_avatar = (ImageView) findViewById(R.id.img_avatar);
		TextView txt_username = (TextView) findViewById(R.id.txt_username);
		TextView txt_remark = (TextView) findViewById(R.id.txt_remark);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
		} 
		mFriend = (User) extras.getParcelable("friend");
		mRemark = (String) extras.getString("remark");
		
		img_loader.displayImage(mFriend.getAvatar(), img_avatar, options);
		txt_username.setText(mFriend.getUsername());
		if (mRemark != null) {
			txt_remark.setText(" (" + mRemark + ")");
		} else {
			txt_remark.setText("");
		}
	}

}
