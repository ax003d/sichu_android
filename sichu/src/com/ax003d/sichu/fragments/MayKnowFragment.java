package com.ax003d.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import com.ax003d.sichu.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class MayKnowFragment extends Fragment {
	private static MayKnowFragment instance;
	
	public static MayKnowFragment getInstance() {
		if (MayKnowFragment.instance == null) {
			MayKnowFragment.instance = new MayKnowFragment();
		}
		return MayKnowFragment.instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 1. get 200 weibo friends
		// 2. request sichu backend for these 200 ids
		// 3. iterate through all weibo friends, follow if the id in sichu, else invite
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_may_know, container, false);
	}
}
