package com.artifex.mupdfdemo;

import android.graphics.RectF;

/**
 * linkInfoClass = (*env)->FindClass(env, PACKAGENAME "/LinkInfo");
 */
public class LinkInfo {
	public final static int TYPE_INTERNAL = 1; //FZ_LINK_GOTO
	public final static int TYPE_EXTERNAL = 2; //FZ_LINK_URI
	public final static int TYPE_REMOTE = 3; //FZ_LINK_GOTOR
	
	public final int type;
	public final int pageNumber;
	public final String url;
	public final boolean newWindow;
	public final float targetx;
	public final float targety;
	public final int flags;
	public final RectF rect;

	public LinkInfo(float l, float t, float r, float b, 
		int _type, int _pageNumber, String _url, boolean _newWindow, 
		float _targetx, float _targety, int _flags) {
		rect = new RectF(l, t, r, b);
		type = _type;
		pageNumber = _pageNumber;
		url = _url;
		newWindow = _newWindow;
		targetx = _targetx;
		targety = _targety;
		flags = _flags;
	}
}
