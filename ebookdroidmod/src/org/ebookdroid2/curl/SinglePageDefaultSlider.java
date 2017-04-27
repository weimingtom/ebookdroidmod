package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.graphics.Rect;
import android.graphics.RectF;

public class SinglePageDefaultSlider extends AbstractPageSlider {
    public SinglePageDefaultSlider(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.NONE, singlePageDocumentView);
    }

    @Override
    public boolean isPageVisible(final Page page, final ViewState viewState) {
        final int pageIndex = page.index.viewIndex;
        return pageIndex == viewState.model.getCurrentViewPageIndex();
    }

    @Override
    protected void drawForeground(final EventDraw event) {
        final ViewState viewState = event.viewState;
        Page page = null;
        if (bFlipping) {
            page = viewState.model.getPageObject(
            	!bFlipRight ? foreIndex : backIndex);
        }
        if (page == null) {
            page = viewState.model.getCurrentPageObject();
        }
        if (page != null) {
            updateForeBitmap(event, page);
            final Rect src = new Rect(0, 0, 
            	(int) viewState.viewRect.width(), 
            	(int) viewState.viewRect.height());
            final RectF dst = new RectF(0, 0, 
            	viewState.viewRect.width(), 
            	viewState.viewRect.height());
            foreBitmap.draw(event.canvas, src, dst, PAINT);
        }
    }

    @Override
    protected void drawBackground(final EventDraw event) {
    	
    }
}
