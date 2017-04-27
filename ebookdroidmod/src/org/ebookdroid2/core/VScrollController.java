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

public class VScrollController extends AbstractScrollController {
	private ViewerActivityHelper helper;
    public VScrollController(final IActivityController base, ViewerActivityHelper helper) {
        super(base, DocumentViewMode.VERTICALL_SCROLL);
        this.helper = helper;
    }

    @Override
    public final int calculateCurrentPage(final ViewState viewState, final int firstVisible, final int lastVisible) {
        int result = 0;
        long bestDistance = Long.MAX_VALUE;
        final int viewY = Math.round(viewState.viewRect.centerY());
        final PageIterator pages = firstVisible != -1 ? 
        	viewState.model.getPages(firstVisible, lastVisible + 1) : 
        	viewState.model.getPages(0);
        try {
            RectF bounds = new RectF();
            for (final Page page : pages) {
                viewState.getBounds(page, bounds);
                final int pageY = Math.round(bounds.centerY());
                final long dist = Math.abs(pageY - viewY);
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
        final int dy = (int) (direction * getHeight() * (app.scrollHeight / 100.0));
        if (app.animateScrolling) {
            getView().startPageScroll(0, dy);
        } else {
            getView().scrollBy(0, dy);
        }
    }
    
    @Override
    public final Rect getScrollLimits() {
        final int width = getWidth();
        final int height = getHeight();
        final Page lpo = model.getLastPageObject();
        final float zoom = getBase().getZoomModel().getZoom();
        final int bottom = lpo != null ? (int) lpo.getBounds(zoom).bottom - height : 0;
        final int right = (int) (width * zoom) - width;
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
        final int width = getWidth();
        final int height = getHeight();
        final BookSettings bookSettings = base.getBookSettings();
        final PageAlign pageAlign = DocumentViewMode.getPageAlign(bookSettings);
        if (changedPage == null) {
            float heightAccum = 0;
            for (final Page page : model.getPages()) {
                final RectF pageBounds = calcPageBounds(pageAlign, page.getAspectRatio(), width, height);
                pageBounds.offset(0, heightAccum);
                page.setBounds(pageBounds);
                heightAccum += pageBounds.height() + 3;
            }
        } else {
            float heightAccum = changedPage.getBounds(1.0f).top;
            final PageIterator pages = model.getPages(changedPage.index.viewIndex);
            try {
                for (final Page page : pages) {
                    final RectF pageBounds = calcPageBounds(pageAlign, page.getAspectRatio(), width, height);
                    pageBounds.offset(0, heightAccum);
                    page.setBounds(pageBounds);
                    heightAccum += pageBounds.height() + 3;
                }
            } finally {
                pages.release();
            }
        }
    }

    @Override
    public RectF calcPageBounds(final PageAlign pageAlign, final float pageAspectRatio, final int width,
            final int height) {
        return new RectF(0, 0, width, width / pageAspectRatio);
    }

	@Override
	public ViewerActivityHelper getHelper() {
		return this.helper;
	}
}
