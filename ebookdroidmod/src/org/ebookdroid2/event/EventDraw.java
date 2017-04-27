package org.ebookdroid2.event;

import java.util.List;
import java.util.Queue;

import org.ebookdroid2.codec.PageLink;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.model.SearchModel;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.model.SearchModel.Matches;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeLevel;
import org.ebookdroid2.page.PageTreeNode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

public class EventDraw implements IEvent {
	private final static boolean D = false;
	private final static boolean DD = false;
	private final static String TAG = "EventDraw";
	private final static boolean USE_DRAW_DISABLE = false; //耗时间，正常的话设置为false
    private static final Paint LINK_PAINT = new Paint();
    private static final Paint BRIGHTNESS_FILTER = new Paint();

    private final Queue<EventDraw> eventQueue;
    public ViewState viewState;
    public PageTreeLevel level;
    public Canvas canvas;
    private Paint brightnessFilter;
    private RectF pageBounds;
    private final RectF fixedPageBounds = new RectF();

    EventDraw(final Queue<EventDraw> eventQueue) {
        this.eventQueue = eventQueue;
    }

    void init(final ViewState viewState, final Canvas canvas) {
        this.viewState = viewState;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
        this.canvas = canvas;
    }

    void init(final EventDraw event, final Canvas canvas) {
        this.viewState = event.viewState;
        this.level = event.level;
        this.canvas = canvas;
    }

    void release() {
        this.canvas = null;
        this.level = null;
        this.pageBounds = null;
        this.viewState = null;
        eventQueue.offer(this);
    }

    @Override
    public ViewState process() {
    	if (D) {
        	Log.e(TAG, ">>>EventDraw#process()");
        }
        try {
            if (canvas == null || viewState == null) {
                return viewState;
            }
            if (!USE_DRAW_DISABLE) {
            	canvas.drawRect(canvas.getClipBounds(), viewState.paint.backgroundFillPaint);
            } else {
            	canvas.drawColor(Color.BLACK);
            }
            //FIXME:这里耗时间
            //AbstractScrollController#drawView
            viewState.ctrl.drawView(this);
            return viewState;
        } finally {
            release();
        }
    }

    @Override
    public boolean process(final Page page) {
    	//FIXME:绘画线程
    	pageBounds = viewState.getBounds(page);
    	if (D) {
            Log.e(TAG, "process(" + page.index + "): view=" + viewState.viewRect + ", page=" + pageBounds);
        }
        //这是画再底部的白色背景和页面n的提示
        drawPageBackground(page);
        if (!USE_DRAW_DISABLE) {
	        //FIXME:这里耗时间，显示一个页面下面的结点（页面片段）
	        final boolean res = process(page.nodes);
	        drawPageLinks(page);
	        drawHighlights(page);
	        return res;
    	} else {
    		return true;
    	}
    }

    @Override
    public boolean process(final PageTree nodes) {
        return process(nodes, level);
    }

    @Override
    public boolean process(final PageTree nodes, final PageTreeLevel level) {
        return nodes.process(this, level, false);
    }

    @Override
    public boolean process(final PageTreeNode node) {
    	//FIXME:绘画线程2
    	if (D && DD) {
            Log.e(TAG, "EventDraw#process(node)", new Exception());
        }
    	
        final RectF nodeRect = node.getTargetRect(pageBounds);
        if (D && DD) {
        	Log.e(TAG, "process(" + node.fullId + "): view=" + viewState.viewRect + ", page=" + pageBounds + ", node="
                    + nodeRect);
        }

        if (!viewState.isNodeVisible(nodeRect)) {
            return false;
        }
        try {
            if (node.holder.drawBitmap(canvas, viewState.paint, viewState.viewBase, nodeRect, nodeRect)) {
                return true;
            }
            if (node.parent != null) {
                final RectF parentRect = node.parent.getTargetRect(pageBounds);
                if (node.parent.holder.drawBitmap(canvas, viewState.paint, viewState.viewBase, parentRect, nodeRect)) {
                    return true;
                }
            }
            return node.page.nodes.paintChildren(this, node, nodeRect);
        } finally {
            drawBrightnessFilter(nodeRect);
        }
    }

    public boolean paintChild(final PageTreeNode node, final PageTreeNode child, final RectF nodeRect) {
        final RectF childRect = child.getTargetRect(pageBounds);
        return child.holder.drawBitmap(canvas, viewState.paint, viewState.viewBase, childRect, nodeRect);
    }

    //这里是画底层，加载中时显示页面n
    protected void drawPageBackground(final Page page) {
        fixedPageBounds.set(pageBounds);
        fixedPageBounds.offset(-viewState.viewBase.x, -viewState.viewBase.y);
        //FIXME:这里耗时间
        if (!USE_DRAW_DISABLE) {
        	canvas.drawRect(fixedPageBounds, viewState.paint.fillPaint);
        }
        final TextPaint textPaint = viewState.paint.textPaint;
        textPaint.setTextSize(24 * viewState.zoom);
        textPaint.setTextAlign(Align.CENTER);
        if (USE_DRAW_DISABLE) {
        	textPaint.setColor(Color.RED); //FIXME:
        }
        final int offset = viewState.book != null ? viewState.book.firstPageOffset : 1;
        final String text = "页面"/*EBookDroidApp.context.getString(R.string.text_page)*/ + " " + (page.index.viewIndex + offset);
        canvas.drawText(text, fixedPageBounds.centerX(), fixedPageBounds.centerY(), textPaint);
    }

    private void drawPageLinks(final Page page) {
        if (page.links == null || page.links.isEmpty()) {
            return;
        }
        for (final PageLink link : page.links) {
            final RectF rect = page.getLinkSourceRect(pageBounds, link);
            if (rect != null) {
                rect.offset(-viewState.viewBase.x, -viewState.viewBase.y);
                LINK_PAINT.setColor(AppSettings.current().linkHighlightColor);
                canvas.drawRect(rect, LINK_PAINT);
            }
        }
    }

    private void drawHighlights(final Page page) {
        final SearchModel sm = viewState.ctrl.getBase().getSearchModel();
        final Matches matches = sm.getMatches(page);
        final List<? extends RectF> mm = matches != null ? matches.getMatches() : null;
        if (mm == null || mm.isEmpty()) {
            return;
        }
        final AppSettings app = AppSettings.current();
        final Paint p = new Paint();
        final Page cp = sm.getCurrentPage();
        final int cmi = sm.getCurrentMatchIndex();
        for (int i = 0; i < mm.size(); i++) {
            final boolean current = page == cp && i == cmi;
            final RectF link = mm.get(i);
            final RectF rect = page.getPageRegion(pageBounds, new RectF(link));
            rect.offset(-viewState.viewBase.x, -viewState.viewBase.y);
            p.setColor(current ? app.currentSearchHighlightColor : app.searchHighlightColor);
            canvas.drawRect(rect, p);
        }
    }

    protected void drawBrightnessFilter(final RectF nodeRect) {
        if (viewState.app.brightnessInNightModeOnly && !viewState.nightMode) {
            return;
        }
        if (viewState.app.brightness >= 100) {
            return;
        }
        final int alpha = 255 - viewState.app.brightness * 255 / 100;
        BRIGHTNESS_FILTER.setColor(Color.BLACK);
        BRIGHTNESS_FILTER.setAlpha(alpha);
        final float offX = viewState.viewBase.x;
        final float offY = viewState.viewBase.y;
        canvas.drawRect(nodeRect.left - offX, nodeRect.top - offY, nodeRect.right - offX + 1, nodeRect.bottom - offY
                + 1, BRIGHTNESS_FILTER);
    }
}
