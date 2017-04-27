package org.mupdfdemo2.view;

import org.mupdfdemo2.model.Hit;

import android.graphics.PointF;
import android.graphics.RectF;

import com.artifex.mupdfdemo.LinkInfo;

/**
 * FIXME:移除这个接口
 */
public interface MuPDFView {
	public void setPage(int page, PointF size);
	public void setScale(float scale);
	public int getPage();
	public void blank(int page);
	public Hit passClickEvent(float x, float y);
	public LinkInfo hitLink(float x, float y);
	public void selectText(float x0, float y0, float x1, float y1);
	public void deselectText();
	public boolean copySelection();
	public boolean markupSelection(int type);
	public void deleteSelectedAnnotation();
	public void setSearchBoxes(RectF searchBoxes[]);
	public void setLinkHighlighting(boolean f);
	public void deselectAnnotation();
	public void startDraw(float x, float y);
	public void continueDraw(float x, float y);
	public void cancelDraw();
	public boolean saveDraw();
	public void update();
	public void updateHq(boolean update);
	public void removeHq();
	public void releaseResources();
	public void releaseBitmaps();
}
