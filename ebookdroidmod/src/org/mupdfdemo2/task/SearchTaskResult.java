package org.mupdfdemo2.task;

import android.graphics.RectF;

public class SearchTaskResult {
	private static SearchTaskResult singleton;

	public final String txt;
	public final int   pageNumber;
	public final RectF searchBoxes[];

	public SearchTaskResult(String _txt, int _pageNumber, RectF _searchBoxes[]) {
		txt = _txt;
		pageNumber = _pageNumber;
		searchBoxes = _searchBoxes;
	}

	public static SearchTaskResult get() {
		return singleton;
	}

	public static void set(SearchTaskResult r) {
		singleton = r;
	}
}
