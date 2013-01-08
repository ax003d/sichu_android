package com.ax003d.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ax003d.sichu.R;

public class AboutFragment extends Fragment {

    private static AboutFragment instance;

    public static AboutFragment getInstance() {
        if (AboutFragment.instance == null) {
            return new AboutFragment();
        }
        return AboutFragment.instance;
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about, container, false);
	}	
	
}
