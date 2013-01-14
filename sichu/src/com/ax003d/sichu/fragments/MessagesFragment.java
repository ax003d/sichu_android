package com.ax003d.sichu.fragments;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.slidingmenu.SlidingActivity;
import org.holoeverywhere.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ax003d.sichu.R;
import com.ax003d.sichu.adapters.MessageListAdapter;
import com.ax003d.sichu.api.ISichuAPI;
import com.ax003d.sichu.api.SichuAPI;
import com.ax003d.sichu.models.BookBorrowReq;
import com.ax003d.sichu.utils.Preferences;

public class MessagesFragment extends Fragment {
    private static MessagesFragment instance;

    public static MessagesFragment getInstance() {
        if (MessagesFragment.instance == null) {
        	MessagesFragment.instance = new MessagesFragment();
        }
        return MessagesFragment.instance;
    }

	private ISichuAPI api_client;
	private SlidingActivity activity;
	private long userID;
	private MessageListAdapter adapter;
	private ListView lst_msg;		
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		api_client = SichuAPI.getInstance(getActivity());
		activity = (SlidingActivity) getActivity();
		userID = Preferences.getUserID(activity);
		adapter = new MessageListAdapter(activity);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_messages, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lst_msg = (ListView) activity
				.findViewById(R.id.lst_msg);
		lst_msg.setAdapter(adapter);
		new GetBookBorrowReqTask().execute();
	}
	
	private class GetBookBorrowReqTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject ret = null;
			try {
				if (params.length == 0) {
					ret = api_client.bookborrowrequest(null, null);
				} else {
					ret = api_client.bookborrowrequest(params[0], null);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null && result.has("objects")) {
				// ContentResolver contentResolver = activity.getContentResolver();
				JSONArray jBookBorrowRequests;
				try {
					jBookBorrowRequests = result.getJSONArray("objects");
					for (int i = 0; i < jBookBorrowRequests.length(); i++) {
						BookBorrowReq req = new BookBorrowReq(jBookBorrowRequests.getJSONObject(i));
						adapter.addBookBorrowReq(req);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			adapter.notifyDataSetChanged();
			if (result != null && result.has("meta")) {
				String next;
				try {
					next = result.getJSONObject("meta").getString("next");
					if (!next.equals("null")) {
						new GetBookBorrowReqTask().execute(next);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);			
		}

	} // GetBookBorrowReqTask
}
