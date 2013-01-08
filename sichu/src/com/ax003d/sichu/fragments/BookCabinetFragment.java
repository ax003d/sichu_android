package com.ax003d.sichu.fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ax003d.sichu.R;

public class BookCabinetFragment extends Fragment {

    private static BookCabinetFragment instance;

    public static BookCabinetFragment getInstance() {
        if (BookCabinetFragment.instance == null) {
            return new BookCabinetFragment();
        }
        return BookCabinetFragment.instance;
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_book_cabinet, container, false);
	}
}
