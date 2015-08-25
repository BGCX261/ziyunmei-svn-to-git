package com.yunmei.frame.utils;

public class Forward {
	private String key;
	private String value;
	private String url;

	public Forward() {
	}

	public Forward(String url) {
		this.url = url;
	}

	public Forward(String url, String key, String obj) {
		this.url = url;
		this.key = key;
		this.value = obj;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
