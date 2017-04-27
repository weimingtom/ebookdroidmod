package org.ebookdroid2.event;

import java.util.Queue;

import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.manager.SettingsManager;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeLevel;

import android.graphics.RectF;
import android.util.Log;

public abstract class AbstractEventZoom<E extends AbstractEventZoom<E>> extends AbstractEvent {
	private final static boolean D = false;
	private final static String TAG = "AbstractEventZoom";
	
    private final Queue<E> eventQueue;
    public float oldZoom;
    public float newZoom;
    public PageTreeLevel oldLevel;
    public PageTreeLevel newLevel;
    public boolean committed;

    protected AbstractEventZoom(final Queue<E> eventQueue) {
        this.eventQueue = eventQueue;
    }

    public final void init(final AbstractViewController ctrl, final float oldZoom, final float newZoom, final boolean committed) {
        this.viewState = ViewState.get(ctrl, newZoom);
        this.ctrl = ctrl;
        this.oldZoom = oldZoom;
        this.newZoom = newZoom;
        this.oldLevel = PageTreeLevel.getLevel(oldZoom);
        this.newLevel = PageTreeLevel.getLevel(newZoom);
        this.committed = committed;
    }

    final void release() {
        this.ctrl = null;
        this.viewState = null;
        this.oldLevel = null;
        this.newLevel = null;
        this.bitmapsToRecycle.clear();
        this.nodesToDecode.clear();
        eventQueue.offer((E) this);
    }

    @Override
    public final ViewState process() {
    	if (D) {
        	Log.e(TAG, ">>>AbstractEventZoom#process()");
        }
        try {
            if (!committed) {
                ctrl.getView().invalidateScroll(newZoom, oldZoom);
                viewState.update();
            }
            super.process();
            if (!committed) {
                ctrl.redrawView(viewState);
            } else {
                SettingsManager.zoomChanged(viewState.book, newZoom, true);
                ctrl.updatePosition(ctrl.model.getCurrentPageObject(), viewState);
            }
            return viewState;
        } finally {
            release();
        }
    }

    @Override
    public final boolean process(final PageTree nodes) {
        return process(nodes, newLevel);
    }

    @Override
    protected final void calculatePageVisibility() {
        final int viewIndex = ctrl.model.getCurrentViewPageIndex();
        int firstVisiblePage = viewIndex;
        int lastVisiblePage = viewIndex;
        final Page[] pages = ctrl.model.getPages();
        if (pages == null || pages.length == 0) {
            return;
        }
        final RectF bounds = new RectF();
        while (firstVisiblePage > 0) {
            final int index = firstVisiblePage - 1;
            if (!ctrl.isPageVisible(pages[index], viewState, bounds)) {
                break;
            }
            firstVisiblePage = index;
        }
        while (lastVisiblePage < pages.length - 1) {
            final int index = lastVisiblePage + 1;
            if (!ctrl.isPageVisible(pages[index], viewState, bounds)) {
                break;
            }
            lastVisiblePage = index;
        }
        viewState.update(firstVisiblePage, lastVisiblePage);
    }
}
