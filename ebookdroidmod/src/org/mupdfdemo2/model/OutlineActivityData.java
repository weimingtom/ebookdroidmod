package org.mupdfdemo2.model;

import com.artifex.mupdfdemo.OutlineItem;

/**
 * 大纲的缓存，可以被清除（设置为null）
 *
 */
public class OutlineActivityData {
	public OutlineItem items[];
	public int position;
	
	private static OutlineActivityData singleton;

	public static void set(OutlineActivityData d) {
		singleton = d;
	}

	public static OutlineActivityData get() {
		if (singleton == null) {
			singleton = new OutlineActivityData();
		}
		return singleton;
	}
}
