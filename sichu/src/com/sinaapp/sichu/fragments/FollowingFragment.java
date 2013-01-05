package com.sinaapp.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sinaapp.sichu.R;

public class FollowingFragment extends Fragment {
	private static FollowingFragment instance;

	public static FollowingFragment getInstance() {
		if (FollowingFragment.instance == null) {
			return new FollowingFragment();
		}
		return FollowingFragment.instance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_following, container, false);
	}	
}
