package org.ebookdroid2.page;

import java.util.Comparator;

import org.ebookdroid2.model.ViewState;

import android.graphics.RectF;

public class PageTreeNodeComparator implements Comparator<PageTreeNode> {
    private final ViewState viewState;
    private final RectF bounds1 = new RectF();
    private final RectF bounds2 = new RectF();
    
    public PageTreeNodeComparator(final ViewState viewState) {
        this.viewState = viewState;
    }

    @Override
    public int compare(final PageTreeNode node1, final PageTreeNode node2) {
        final int cp = viewState.pages.currentIndex;
        final int viewIndex1 = node1.page.index.viewIndex;
        final int viewIndex2 = node2.page.index.viewIndex;
        viewState.getBounds(node1.page, bounds1);
        viewState.getBounds(node2.page, bounds2);
        final boolean v1 = viewState.isNodeVisible(node1, bounds1);
        final boolean v2 = viewState.isNodeVisible(node2, bounds2);
        final RectF s1 = node1.pageSliceBounds;
        final RectF s2 = node2.pageSliceBounds;
        int res = 0;
        if (viewIndex1 == cp && viewIndex2 == cp) {
        	res = compareFloat(s1.top, s2.top);
            if (res == 0) {
            	res = compareFloat(s1.left, s2.left);
            }
        } else if (v1 && !v2) {
            res = -1;
        } else if (!v1 && v2) {
            res = 1;
        } else {
            final float d1 = viewIndex1 + s1.centerY() - (cp + 0.5f);
            final float d2 = viewIndex2 + s2.centerY() - (cp + 0.5f);
            final int dist1 = Math.abs((int) (d1 * node1.level.zoom));
            final int dist2 = Math.abs((int) (d2 * node2.level.zoom));
            res = compareInt(dist1, dist2);
            if (res == 0) {
            	res = -compareInt(viewIndex1, viewIndex2);
            }
        }
        return res;
    }
    
	public static int compareFloat(final float val1, final float val2) {
		return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
	}
    
	public static int compareInt(final int val1, final int val2) {
		return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
	}
}
