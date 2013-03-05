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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_may_know, container, false);
	}
}
