package org.ebookdroid2.core;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.event.EventGotoPage;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.model.DocumentModel.PageIterator;
import org.ebookdroid2.page.Page;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public abstract class AbstractScrollController extends AbstractViewController {
	private final static boolean D = false;
	private final static String TAG = "AbstractScrollController";
	
    protected AbstractScrollController(final IActivityController base, final DocumentViewMode mode) {
        super(base, mode);
    }

    @Override
    public final void goToPage(final int toPage) {
        if (D) {
        	Log.e(TAG, ">>>AbstractScrollController#goToPage()");
        }
        new EventGotoPage(this, toPage).process().release();
    }
    
    @Override
    public final void goToPage(final int toPage, final float offsetX, final float offsetY) {
        if (D) {
        	Log.e(TAG, ">>>AbstractScrollController#goToPage(toPage, offsetX, offsetY)");
        }
    	new EventGotoPage(this, toPage, offsetX, offsetY).process().release();
    }

    @Override
    public final void drawView(final EventDraw eventDraw) {
        final ViewState viewState = eventDraw.viewState;
        if (viewState.model == null) {
            return;
        }
        final PageIterator pages = viewState.pages.getVisiblePages();
        try {
            for (final Page page : pages) {
                if (page != null) {
                    eventDraw.process(page);
                }
            }
        } finally {
            pages.release();
        }

        //FIXME:绘画额外图形showAnimIcon

        getView().continueScroll();
    }

    @Override
    public final boolean onLayoutChanged(final boolean layoutChanged, final boolean layoutLocked, final Rect oldLaout,
            final Rect newLayout) {
        final BookSettings bs = base.getBookSettings();
        final int page = model != null ? model.getCurrentViewPageIndex() : -1;
        final float offsetX = bs != null ? bs.offsetX : 0;
        final float offsetY = bs != null ? bs.offsetY : 0;

        if (super.onLayoutChanged(layoutChanged, layoutLocked, oldLaout, newLayout)) {
            if (isShown && layoutChanged && page != -1) {
                goToPage(page, offsetX, offsetY);
            }
            return true;
        }
        return false;
    }

    @Override
    public final void onScrollChanged(final int dX, final int dY) {
        if (inZoom.get()) {
            return;
        }

       EventPool.newEventScroll(this, mode == DocumentViewMode.VERTICALL_SCROLL ? dY : dX, this.getBase().getContext()).process().release();
    }

    @Override
    public final boolean isPageVisible(final Page page, final ViewState viewState, final RectF outBounds) {
        viewState.getBounds(page, outBounds);
        return RectF.intersects(viewState.viewRect, outBounds);
    }

    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {
    }

    @Override
    public void updateAnimationType() {
    }
}
