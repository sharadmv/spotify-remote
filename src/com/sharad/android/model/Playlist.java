package com.sharad.android.model;

public class Playlist {
	private String name, uri;;
	private int length;

	public Playlist(String name, String uri, int length) {
		super();
		this.name = name;
		this.uri = uri;
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String toString() {
		return name;
	}
}
