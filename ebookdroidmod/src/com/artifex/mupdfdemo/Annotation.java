package com.artifex.mupdfdemo;

import android.graphics.RectF;

/**
 * annotClass = (*env)->FindClass(env, PACKAGENAME "/Annotation");
 */
public class Annotation extends RectF {
	public final static int TEXT = 0; 
	public final static int LINK = 1; 
	public final static int FREETEXT = 2; 
	public final static int LINE = 3;
	public final static int SQUARE = 4;
	public final static int CIRCLE = 5;
	public final static int POLYGON = 6;
	public final static int POLYLINE = 7;
	public final static int HIGHLIGHT = 8;
	public final static int UNDERLINE = 9;
	public final static int SQUIGGLY = 10;
	public final static int STRIKEOUT = 11;
	public final static int STAMP = 12;
	public final static int CARET = 13;
	public final static int INK = 14;
	public final static int POPUP = 15;
	public final static int FILEATTACHMENT = 16;
	public final static int SOUND = 17;
	public final static int MOVIE = 18;
	public final static int WIDGET = 19;
	public final static int SCREEN = 20;
	public final static int PRINTERMARK = 21; 
	public final static int TRAPNET = 22;
	public final static int WATERMARK = 23;
	public final static int A3D = 23;
	public final static int UNKNOWN = -1;

	public final int type;

	public Annotation(float x0, float y0, 
			float x1, float y1, int _type) {
		super(x0, y0, x1, y1);
		type = _type;
	}
}
