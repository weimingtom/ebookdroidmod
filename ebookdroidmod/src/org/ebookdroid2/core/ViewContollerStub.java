package org.ebookdroid2.core;

import org.ebookdroid2.activity.ActivityControllerStub;
import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.listener.IProgressIndicator;
import org.ebookdroid2.manager.PageAlign;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.view.IView;
import org.ebookdroid2.view.ViewStub;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ViewContollerStub implements IViewController {
    public static final ViewContollerStub STUB = new ViewContollerStub();

    @Override
    public void zoomChanged(final float oldZoom, final float newZoom, final boolean committed) {
    }

    @Override
    public void goToPage(final int page) {
    }

    @Override
    public void goToPage(final int page, final float offsetX, final float offsetY) {
    }

    @Override
    public void invalidatePageSizes(final InvalidateSizeReason reason, final Page changedPage) {
    }

    @Override
    public int getFirstVisiblePage() {
        return 0;
    }

    @Override
    public int calculateCurrentPage(final ViewState viewState, final int firstVisible, final int lastVisible) {
        return 0;
    }

    @Override
    public int getLastVisiblePage() {
        return 0;
    }

    @Override
    public void verticalConfigScroll(final int i) {
    }

    @Override
    public void redrawView() {
    }

    @Override
    public void redrawView(final ViewState viewState) {
    }

    @Override
    public void setAlign(final PageAlign byResValue) {
    }

    @Override
    public IActivityController getBase() {
        return ActivityControllerStub.STUB;
    }

    @Override
    public IView getView() {
        return ViewStub.STUB;
    }

    @Override
    public void updateAnimationType() {
    }

    @Override
    public void updateMemorySettings() {
    }

    @Override
    public boolean onLayoutChanged(final boolean layoutChanged, final boolean layoutLocked, final Rect oldLaout,
            final Rect newLayout) {
        return false;
    }

    @Override
    public Rect getScrollLimits() {
        return new Rect(0, 0, 0, 0);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        return false;
    }

    @Override
    public void onScrollChanged(final int dX, final int dY) {
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        return false;
    }

    @Override
    public void show() {
    }

    @Override
    public final void init(final IProgressIndicator task) {
    }

    @Override
    public void toggleRenderingEffects() {
    }

    @Override
    public void drawView(final EventDraw eventDraw) {
    }

    @Override
    public boolean isPageVisible(final Page page, final ViewState viewState, final RectF outBounds) {
        viewState.getBounds(page, outBounds);
        return false;
    }

    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {
    }

    @Override
    public void invalidateScroll() {
    }

    @Override
    public final void onDestroy() {
    }

    @Override
    public RectF calcPageBounds(final PageAlign pageAlign, final float pageAspectRatio, final int width,
            final int height) {
        return new RectF();
    }

    @Override
    public void goToLink(final int pageDocIndex, final RectF targetRect, final boolean addToHistory) {
    }

	@Override
	public ViewerActivityHelper getHelper() {
		return null;
	}
}
