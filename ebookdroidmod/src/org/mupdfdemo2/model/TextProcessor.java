package org.mupdfdemo2.model;

import com.artifex.mupdfdemo.TextWord;

public interface TextProcessor {
	void onStartLine();
	void onWord(TextWord word);
	void onEndLine();
}
