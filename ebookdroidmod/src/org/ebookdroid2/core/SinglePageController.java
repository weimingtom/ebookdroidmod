package org.ebookdroid2.core;

import java.util.List;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.curl.PageAnimationType;
import org.ebookdroid2.curl.PageAnimator;
import org.ebookdroid2.curl.PageAnimatorProxy;
import org.ebookdroid2.curl.SinglePageView;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.manager.PageAlign;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.touch.DefaultGestureDetector;
import org.ebookdroid2.touch.IGestureDetector;
import org.ebookdroid2.touch.MultiTouchGestureDetector;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class SinglePageController extends AbstractViewController {
	private final static boolean D = false;
	private final static String TAG = "SinglePageController";
    private final PageAnimatorProxy curler = new PageAnimatorProxy(new SinglePageView(this));
    private ViewerActivityHelper helper;
    
    public SinglePageController(final IActivityController baseActivity, ViewerActivityHelper helper) {
        super(baseActivity, DocumentViewMode.SINGLE_PAGE);
        updateAnimationType();
        this.helper = helper;
    }

    @Override
    public final void goToPage(final int toPage) {
        if (D) {
        	Log.e(TAG, ">>>SinglePageController#goToPage(toPage)");
        }
        if (toPage >= 0 && toPage < model.getPageCount()) {
            final Page page = model.getPageObject(toPage);
            model.setCurrentPageIndex(page.index);
            curler.setViewDrawn(false);
            curler.resetPageIndexes(page.index.viewIndex);
            final ViewState viewState = EventPool.newEventScrollTo(this, page.index.viewIndex, this.getBase().getContext()).process();
            getView().redrawView(viewState);
            viewState.release();
        }
    }

    @Override
    public void goToPage(final int toPage, final float offsetX, final float offsetY) {
    	if (D) {
        	Log.e(TAG, ">>>SinglePageController#goToPage(toPage, offsetX, offsetY)");
        }
        if (toPage >= 0 && toPage < model.getPageCount()) {
            final Page page = model.getPageObject(toPage);
            model.setCurrentPageIndex(page.index);
            curler.setViewDrawn(false);
            curler.resetPageIndexes(page.index.viewIndex);
            final RectF bounds = page.getBounds(getBase().getZoomModel().getZoom());
            final float left = bounds.left + offsetX * bounds.width();
            final float top = bounds.top + offsetY * bounds.height();
            getView().scrollTo((int) left, (int) top);
            final ViewState viewState = EventPool.newEventScrollTo(this, page.index.viewIndex, this.getBase().getContext()).process();
            pageUpdated(viewState, page);
            getView().redrawView(viewState);
            viewState.release();
        }
    }

    @Override
    public void onScrollChanged(final int dX, final int dY) {
        if (inZoom.get()) {
            return;
        }
        EventPool.newEventScroll(this, dX, this.getBase().getContext()).process().release();
    }

    @Override
    public final int calculateCurrentPage(final ViewState viewState, final int firstVisible, final int lastVisible) {
        return viewState.model.getCurrentViewPageIndex();
    }
    
    @Override
    public final void verticalConfigScroll(final int direction) {
        if (curler.enabled()) {
            curler.animate(direction);
        } else {
            final BookSettings bs = base.getBookSettings();
            final float offsetX = bs != null ? bs.offsetX : 0;
            final Page page = model.getCurrentPageObject();
            final RectF viewRect = base.getView().getViewRect();
            final RectF bounds = page.getBounds(getBase().getZoomModel().getZoom());
            if (Math.abs(viewRect.top - bounds.top) < 5 && direction < 0) {
                goToPage(page.index.viewIndex - 1, offsetX, 1);
                return;
            }
            if (Math.abs(viewRect.bottom - bounds.bottom) < 5 && direction > 0) {
                goToPage(page.index.viewIndex + 1, offsetX, 0);
                return;
            }
            final float pageHeight = bounds.height();
            final float viewHeight = viewRect.height();
            final float diff = direction * viewHeight * AppSettings.current().scrollHeight / 100.0f;
            final float oldTop = getScrollY();
            final float newTop = oldTop + diff;
            goToPage(model.getCurrentViewPageIndex(), offsetX, newTop / pageHeight);
        }
    }

    @Override
    public final Rect getScrollLimits() {
        final int width = getWidth();
        final int height = getHeight();
        final float zoom = getBase().getZoomModel().getZoom();
        final Page page = model.getCurrentPageObject();
        if (page != null) {
            final RectF bounds = page.getBounds(zoom);
            final int top = ((int) bounds.top > 0) ? 0 : (int) bounds.top;
            final int left = ((int) bounds.left > 0) ? 0 : (int) bounds.left;
            final int bottom = ((int) bounds.bottom < height) ? 0 : (int) bounds.bottom - height;
            final int right = ((int) bounds.right < width) ? 0 : (int) bounds.right - width;
            return new Rect(left, top, right, bottom);
        }
        return new Rect(0, 0, 0, 0);
    }

    @Override
    protected List<IGestureDetector> initGestureDetectors(final List<IGestureDetector> list) {
        final GestureListener listener = new GestureListener();
        list.add(new MultiTouchGestureDetector(listener));
        list.add(curler);
        list.add(new DefaultGestureDetector(base.getContext(), listener));
        return list;
    }

    @Override
    public void drawView(final EventDraw eventDraw) {
        curler.draw(eventDraw);
        getView().continueScroll();
    }

    public final ViewState invalidatePages(final ViewState oldState, final Page... pages) {
    	if (D) {
        	Log.e(TAG, ">>>SinglePageController#invalidatePages()");
        }
        if (pages != null && pages.length > 0 && pages[0] != null) {
            return EventPool.newEventScrollTo(this, pages[0].index.viewIndex, this.getBase().getContext()).process();
        }
        return oldState;
    }

    @Override
    public final void invalidatePageSizes(final InvalidateSizeReason reason, final Page changedPage) {
        if (!isShown()) {
            return;
        }
        final int width = getWidth();
        final int height = getHeight();
        final BookSettings bookSettings = base.getBookSettings();
        final PageAlign pageAlign = DocumentViewMode.getPageAlign(bookSettings);
        if (changedPage == null) {
            for (final Page page : model.getPages()) {
                invalidatePageSize(pageAlign, page, width, height);
            }
        } else {
            invalidatePageSize(pageAlign, changedPage, width, height);
        }
        curler.setViewDrawn(false);
    }

    private void invalidatePageSize(final PageAlign pageAlign, final Page page, final int width, final int height) {
        final RectF pageBounds = calcPageBounds(pageAlign, page.getAspectRatio(), width, height);
        final float pageWidth = pageBounds.width();
        if (width > pageWidth) {
            final float widthDelta = (width - pageWidth) / 2;
            pageBounds.offset(widthDelta, 0);
        }
        page.setBounds(pageBounds);
    }

    @Override
    public RectF calcPageBounds(final PageAlign pageAlign, final float pageAspectRatio, final int width,
            final int height) {
        PageAlign effective = pageAlign;
        if (effective == PageAlign.AUTO) {
            final float pageHeight = width / pageAspectRatio;
            effective = pageHeight > height ? PageAlign.HEIGHT : PageAlign.WIDTH;
        }
        if (effective == PageAlign.WIDTH) {
            final float pageHeight = width / pageAspectRatio;
            return new RectF(0, 0, width, pageHeight);
        } else {
            final float pageWidth = height * pageAspectRatio;
            return new RectF(0, 0, pageWidth, height);
        }
    }

    @Override
    public final boolean isPageVisible(final Page page, final ViewState viewState, final RectF outBounds) {
        viewState.getBounds(page, outBounds);
        return curler.isPageVisible(page, viewState);
    }

    @Override
    public final void updateAnimationType() {
        final PageAnimationType animationType = base.getBookSettings().animationType;
        final PageAnimator newCurler = PageAnimationType.create(animationType, this);
        newCurler.init();
        curler.switchCurler(newCurler);
    }

    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {
        curler.pageUpdated(viewState, page);
    }

	@Override
	public ViewerActivityHelper getHelper() {
		return this.helper;
	}
}
