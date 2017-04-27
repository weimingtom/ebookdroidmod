package org.ebookdroid2.event;

import java.util.Queue;

import org.ebookdroid2.page.PageTreeNode;

import android.graphics.RectF;

public class EventZoomOut extends AbstractEventZoom<EventZoomOut> {
    EventZoomOut(final Queue<EventZoomOut> eventQueue) {
        super(eventQueue);
    }

    @Override
    public boolean process(final PageTreeNode node) {
        final RectF pageBounds = viewState.getBounds(node.page);
        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycle(bitmapsToRecycle);
            return false;
        }
        if (!node.holder.hasBitmaps() || committed) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }
        return true;
    }
}
