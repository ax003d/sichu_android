package com.ax003d.sichu.events;

import java.util.ArrayList;
import java.util.HashMap;

public class ListFriendsEvent {
	public ArrayList<HashMap<String, Object>> users;
	
	public ListFriendsEvent(ArrayList<HashMap<String, Object>> users) {
		this.users = users;
	}
}
