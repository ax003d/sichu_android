package com.sinaapp.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sinaapp.sichu.R;

public class FollowerFragment extends Fragment {
	private static FollowerFragment instance;

	public static FollowerFragment getInstance() {
		if (FollowerFragment.instance == null) {
			return new FollowerFragment();
		}
		return FollowerFragment.instance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_follower, container, false);
	}	
}
