package org.ebookdroid2.event;

import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeNode;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class EventChildLoaded extends AbstractEvent {
	private final static boolean D = false;
	private final static String TAG = "EventChildLoaded";		
	
    private final Queue<EventChildLoaded> eventQueue;
    public Page page;
    public PageTree nodes;
    public PageTreeNode child;
    public Rect bitmapBounds;

    public EventChildLoaded(final Queue<EventChildLoaded> eventQueue) {
        this.eventQueue = eventQueue;
    }

    final void init(final AbstractViewController ctrl, final PageTreeNode child, final Rect bitmapBounds) {
        this.viewState = ViewState.get(ctrl);
        this.ctrl = ctrl;
        this.page = child.page;
        this.nodes = page.nodes;
        this.child = child;
        this.bitmapBounds = bitmapBounds;
    }

    final void release() {
        this.ctrl = null;
        this.viewState = null;
        this.child = null;
        this.nodes = null;
        this.page = null;
        this.bitmapsToRecycle.clear();
        this.nodesToDecode.clear();
        eventQueue.offer(this);
    }

    @Override
    public final ViewState process() {
    	if (D) {
        	Log.e(TAG, ">>>EventChildLoaded#process()");
        }
        try {
            if (ctrl == null || viewState.book == null) {
                return null;
            }
            final RectF bounds = viewState.getBounds(page);
            final PageTreeNode parent = child.parent;
            if (parent != null) {
                recycleParent(parent, bounds);
            }
            recycleChildren();
            ctrl.pageUpdated(viewState, page);
            if (viewState.isNodeVisible(child, viewState.getBounds(page))) {
                ctrl.redrawView(viewState);
            }
            return viewState;
        } finally {
            release();
        }
    }

    protected void recycleParent(final PageTreeNode parent, final RectF bounds) {
        final boolean hiddenByChildren = nodes.isHiddenByChildren(parent, viewState, bounds);
        if (!viewState.isNodeVisible(parent, bounds) || hiddenByChildren) {
            final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
            final boolean res = nodes.recycleParents(child, bitmapsToRecycle);
            BitmapManager.release(bitmapsToRecycle);
            if (res) {
                if (D) {
                    Log.e(TAG, "Recycle parent nodes for: " + child.fullId + " " + bitmapsToRecycle.size());
                }
            }
        }
    }

    protected void recycleChildren() {
        final boolean res = nodes.recycleChildren(child, bitmapsToRecycle);
        BitmapManager.release(bitmapsToRecycle);
        if (res) {
            if (D) {
            	Log.e(TAG, "Recycle children nodes for: " + child.fullId + " " + bitmapsToRecycle.size());
            }
        }
    }

    @Override
    public boolean process(final PageTree nodes) {
        return false;
    }

    @Override
    public boolean process(final PageTreeNode node) {
        return false;
    }
}
