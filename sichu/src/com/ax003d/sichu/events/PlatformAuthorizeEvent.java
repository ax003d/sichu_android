package com.ax003d.sichu.events;

public class PlatformAuthorizeEvent {
	private boolean status;
	private String token;
	private long expiresTime;
	private String id;
	private String name;
	private String icon;
	
	public PlatformAuthorizeEvent() {
		setStatus(false);
	}
	
	public PlatformAuthorizeEvent(String token, long expiresTime, String id,
			String name, String icon) {
		setStatus(true);
		setToken(token);
		setExpiresTime(expiresTime);
		setId(id);
		setName(name);
		setIcon(icon);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getExpiresTime() {
		return expiresTime;
	}

	public void setExpiresTime(long expiresTime) {
		this.expiresTime = expiresTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
}
