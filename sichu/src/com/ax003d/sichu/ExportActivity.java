package com.ax003d.sichu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ExportActivity extends Activity {

	private View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_export:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);

		findViewById(R.id.btn_export).setOnClickListener(onClickListener);
	}

}
