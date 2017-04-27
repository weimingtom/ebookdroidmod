package org.ebookdroid2.event;

import java.util.Queue;

import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeLevel;
import org.ebookdroid2.page.PageTreeNode;

import android.graphics.RectF;
import android.util.Log;

public abstract class AbstractEventScroll<E extends AbstractEventScroll<E>> extends AbstractEvent {
	private final static boolean D = false;
	private final static String TAG = "AbstractEventScroll";
	
    private final Queue<E> eventQueue;
    protected PageTreeLevel level;

    protected AbstractEventScroll(final Queue<E> eventQueue) {
        this.eventQueue = eventQueue;
    }

    public final void init(final AbstractViewController ctrl) {
        this.viewState = ViewState.get(ctrl);
        this.ctrl = ctrl;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
    }

    @SuppressWarnings("unchecked")
    final void release() {
        this.ctrl = null;
        this.viewState = null;
        this.level = null;
        this.bitmapsToRecycle.clear();
        this.nodesToDecode.clear();
        eventQueue.offer((E) this);
    }

    @Override
    public final ViewState process() {
        if (D) {
        	Log.e(TAG, ">>>AbstractEventScroll#process()");
        }
        try {
            super.process();
            final Page page = viewState.pages.getCurrentPage();
            if (page != null) {
                if (ctrl.mode != DocumentViewMode.SINGLE_PAGE) {
                    ctrl.model.setCurrentPageIndex(page.index);
                }
                ctrl.updatePosition(page, viewState);
            }
            ctrl.getView().redrawView(viewState);
            return viewState;
        } finally {
            release();
        }
    }

    @Override
    public boolean process(final PageTree nodes) {
        if (level.next != null) {
            nodes.recycleNodes(level.next, bitmapsToRecycle);
        }
        return process(nodes, level);
    }

    @Override
    public boolean process(final PageTreeNode node) {
        final RectF pageBounds = viewState.getBounds(node.page);
        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycle(bitmapsToRecycle);
            return false;
        }
        if (!node.holder.hasBitmaps()) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }
        return true;
    }
}
