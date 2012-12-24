package com.sinaapp.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sinaapp.sichu.R;

public class MessagesFragment extends Fragment {
    private static MessagesFragment instance;

    public static MessagesFragment getInstance() {
        if (MessagesFragment.instance == null) {
            return new MessagesFragment();
        }
        return MessagesFragment.instance;
    }		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_messages, container, false);
	}	
}
