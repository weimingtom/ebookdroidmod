package com.artifex.mupdfdemo;

import android.graphics.RectF;

/**
 * that->cls = (*(that->jenv))->FindClass(that->jenv, PACKAGENAME "/PageTextBox2");
 */
public class PageTextBox2 extends RectF {
    public String text;

    @Override
    public String toString() {
        return "PageTextBox2(" + 
        	left + ", " + 
        	top + ", " + 
        	right + ", " + 
        	bottom + ": " + 
        	text + 
        	")";
    }
}
