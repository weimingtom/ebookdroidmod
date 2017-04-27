package org.ebookdroid2.event;

import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.core.IViewController.InvalidateSizeReason;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeLevel;
import org.ebookdroid2.page.PageTreeNode;

import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class EventReset extends AbstractEvent {
	private final static boolean D = false;
	private final static String TAG = "EventReset";
	
    private final Queue<EventReset> eventQueue;

    protected PageTreeLevel level;
    protected InvalidateSizeReason reason;
    protected boolean clearPages;

    EventReset(final Queue<EventReset> eventQueue) {
        this.eventQueue = eventQueue;
    }

    void init(final AbstractViewController ctrl, final InvalidateSizeReason reason, final boolean clearPages) {
        this.viewState = ViewState.get(ctrl);
        this.ctrl = ctrl;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
        this.reason = reason;
        this.clearPages = clearPages;
    }

    void release() {
        this.ctrl = null;
        this.viewState = null;
        this.level = null;
        this.reason = null;
        this.bitmapsToRecycle.clear();
        this.nodesToDecode.clear();
        eventQueue.offer(this);
    }

    @Override
    public ViewState process() {
    	if (D) {
        	Log.e(TAG, ">>>EventReset#process()");
        }
        try {
            if (clearPages) {
                final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
                for (final Page page : ctrl.model.getPages()) {
                    page.nodes.recycleAll(bitmapsToRecycle, true);
                }
                BitmapManager.release(bitmapsToRecycle);
            }
            if (reason != null) {
                ctrl.invalidatePageSizes(reason, null);
                ctrl.invalidateScroll();
                viewState.update();
            }
            return super.process();
        } finally {
            release();
        }
    }

    @Override
    public boolean process(final PageTree nodes) {
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
