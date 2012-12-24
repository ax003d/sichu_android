package com.sinaapp.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sinaapp.sichu.R;

public class AccountFragment extends Fragment {

    private static AccountFragment instance;

    public static AccountFragment getInstance() {
        if (AccountFragment.instance == null) {
            return new AccountFragment();
        }
        return AccountFragment.instance;
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_account, container, false);
	}
	
}
