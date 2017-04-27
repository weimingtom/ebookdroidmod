package com.artifex.mupdfdemo;

/**
 * olClass = (*env)->FindClass(env, PACKAGENAME "/OutlineItem");
 */
public class OutlineItem {
	public final static int TYPE_FZ_LINK_GOTO = 1;
	public final static int TYPE_FZ_LINK_URI = 2;
	
	public final int level;
	public final String title;
	public final int page;
	public final int type;
	public final String uri;
	public final float pointx;
	public final float pointy;
	public final int flags;

	public OutlineItem(int _level, String _title, 
		int _page, int _type, String _uri, 
		float _pointx, float _pointy, int _flags) {
		level = _level;
		title = _title;
		page = _page;
		type = _type;
		uri = _uri;
		pointx = _pointx;
		pointy = _pointy;
		flags = _flags;
	}
}
