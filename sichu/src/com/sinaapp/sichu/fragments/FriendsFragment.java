package com.sinaapp.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sinaapp.sichu.R;

public class FriendsFragment extends Fragment {

    private static FriendsFragment instance;

    public static FriendsFragment getInstance() {
        if (FriendsFragment.instance == null) {
            return new FriendsFragment();
        }
        return FriendsFragment.instance;
    }		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_friends, container, false);
	}	
}
