package com.ax003d.sichu.events;

public class FollowEvent {
	public enum Action {
		ASK_FOLLOW, FOLLOW
	}
	
	public Action mAction;
	
	public FollowEvent(Action action) {
		mAction = action;
	}
}
