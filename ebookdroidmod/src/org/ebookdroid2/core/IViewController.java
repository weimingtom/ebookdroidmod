package org.ebookdroid2.core;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.listener.IProgressIndicator;
import org.ebookdroid2.listener.ZoomListener;
import org.ebookdroid2.manager.PageAlign;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.view.IView;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;

public interface IViewController extends ZoomListener {
    void init(IProgressIndicator bookLoadTask);
    void show();
    //
    void goToPage(int page);
    void goToPage(int page, float offsetX, float offsetY);
    void goToLink(int pageDocIndex, RectF targetRect, boolean addToHistory);
    RectF calcPageBounds(PageAlign pageAlign, float pageAspectRatio, int width, int height);
    void invalidatePageSizes(InvalidateSizeReason reason, Page changedPage);
    int getFirstVisiblePage();
    int calculateCurrentPage(ViewState viewState, int firstVisible, int lastVisible);
    int getLastVisiblePage();
    void verticalConfigScroll(int i);
    void redrawView();
    void redrawView(ViewState viewState);
    void setAlign(PageAlign byResValue);
    //
    IActivityController getBase();
    IView getView();
    void updateAnimationType();
    void updateMemorySettings();
    public static enum InvalidateSizeReason {
        INIT, LAYOUT, PAGE_ALIGN, PAGE_LOADED;
    }
    boolean onLayoutChanged(boolean layoutChanged, boolean layoutLocked, Rect oldLaout, Rect newLayout);
    Rect getScrollLimits();
    boolean isPageVisible(Page page, ViewState viewState, RectF outBounds);
    boolean onTouchEvent(MotionEvent ev);
    void onScrollChanged(int dX, final int dY);
    boolean dispatchKeyEvent(final KeyEvent event);
    void toggleRenderingEffects();
    void drawView(EventDraw eventDraw);
    void pageUpdated(ViewState viewState, Page page);
    void invalidateScroll();
    void onDestroy();
    ViewerActivityHelper getHelper();
}
