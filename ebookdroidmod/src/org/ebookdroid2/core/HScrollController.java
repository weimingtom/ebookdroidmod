package org.ebookdroid2.core;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.manager.PageAlign;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.model.DocumentModel.PageIterator;
import org.ebookdroid2.page.Page;

import android.graphics.Rect;
import android.graphics.RectF;

public class HScrollController extends AbstractScrollController {
	private final ViewerActivityHelper helper;

	public HScrollController(final IActivityController base, ViewerActivityHelper helper) {
        super(base, DocumentViewMode.HORIZONTAL_SCROLL);
        this.helper = helper;
    }

    @Override
    public final int calculateCurrentPage(final ViewState viewState, final int firstVisible, final int lastVisible) {
        int result = 0;
        long bestDistance = Long.MAX_VALUE;
        final int viewX = Math.round(viewState.viewRect.centerX());
        final PageIterator pages = firstVisible != -1 ? 
        	viewState.model.getPages(firstVisible, lastVisible + 1) : 
        	viewState.model.getPages(0);
        try {
            final RectF bounds = new RectF();
            for (final Page page : pages) {
                viewState.getBounds(page, bounds);
                final int pageX = Math.round(bounds.centerX());
                final long dist = Math.abs(pageX - viewX);
                if (dist < bestDistance) {
                    bestDistance = dist;
                    result = page.index.viewIndex;
                }
            }
        } finally {
            pages.release();
        }
        return result;
    }

    @Override
    public final void verticalConfigScroll(final int direction) {
        final AppSettings app = AppSettings.current();
        final int dx = (int) (direction * getWidth() * (app.scrollHeight / 100.0));
        if (app.animateScrolling) {
            getView().startPageScroll(dx, 0);
        } else {
            getView().scrollBy(dx, 0);
        }
    }

    @Override
    public final Rect getScrollLimits() {
        final int width = getWidth();
        final int height = getHeight();
        final Page lpo = model.getLastPageObject();
        final float zoom = getBase().getZoomModel().getZoom();
        final int right = lpo != null ? (int) lpo.getBounds(zoom).right - width : 0;
        final int bottom = (int) (height * zoom) - height;
        return new Rect(0, 0, right, bottom);
    }

    @Override
    public synchronized final void invalidatePageSizes(final InvalidateSizeReason reason, final Page changedPage) {
        if (!isInitialized) {
            return;
        }
        if (reason == InvalidateSizeReason.PAGE_ALIGN) {
            return;
        }
        final int height = getHeight();
        final int width = getWidth();
        final BookSettings bookSettings = base.getBookSettings();
        final PageAlign pageAlign = DocumentViewMode.getPageAlign(bookSettings);
        if (changedPage == null) {
            float widthAccum = 0;
            for (final Page page : model.getPages()) {
                final RectF pageBounds = calcPageBounds(pageAlign, page.getAspectRatio(), width, height);
                pageBounds.offset(widthAccum, 0);
                page.setBounds(pageBounds);
                widthAccum += pageBounds.width() + 3;
            }
        } else {
            float widthAccum = changedPage.getBounds(1.0f).left;
            final PageIterator pages = model.getPages(changedPage.index.viewIndex);
            try {
                for (final Page page : pages) {
                    final RectF pageBounds = calcPageBounds(pageAlign, page.getAspectRatio(), width, height);
                    pageBounds.offset(widthAccum, 0);
                    page.setBounds(pageBounds);
                    widthAccum += pageBounds.width() + 3;
                }
            } finally {
                pages.release();
            }
        }
    }

    @Override
    public RectF calcPageBounds(final PageAlign pageAlign, final float pageAspectRatio, final int width,
            final int height) {
        return new RectF(0, 0, height * pageAspectRatio, height);
    }

	@Override
	public ViewerActivityHelper getHelper() {
		return helper;
	}
}
