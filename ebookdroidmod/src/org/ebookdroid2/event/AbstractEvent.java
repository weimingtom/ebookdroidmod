package org.ebookdroid2.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeLevel;
import org.ebookdroid2.page.PageTreeNode;
import org.ebookdroid2.page.PageTreeNodeComparator;
import org.ebookdroid2.service.DecodeService;

import android.graphics.RectF;
import android.util.Log;

public abstract class AbstractEvent implements IEvent {
	private final static boolean D = false;
	private final static String TAG = "AbstractEvent";		
	
    protected final List<PageTreeNode> nodesToDecode = new ArrayList<PageTreeNode>();
    protected final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
    public AbstractViewController ctrl;
    protected ViewState viewState;

    protected AbstractEvent() {
    }

    @Override
    public ViewState process() {
        if (D) {
        	Log.e(TAG, "AbstractEvent#process");
        }
        calculatePageVisibility();
        ctrl.firstVisiblePage = viewState.pages.firstVisible;
        ctrl.lastVisiblePage = viewState.pages.lastVisible;
        for (final Page page : ctrl.model.getPages()) {
            process(page);
        }
        BitmapManager.release(bitmapsToRecycle);
        if (!nodesToDecode.isEmpty()) {
            ctrl.base.getDecodingProgressModel().increase(nodesToDecode.size());
            decodePageTreeNodes(viewState, nodesToDecode);
            if (D) {
                Log.e(TAG, viewState + " => " + nodesToDecode.size());
            }
        }
        return viewState;
    }

    @Override
    public final boolean process(final Page page) {
        if (page.recycled) {
            return false;
        }
        if (viewState.isPageKeptInMemory(page) || viewState.isPageVisible(page)) {
            return process(page.nodes);
        }
        recyclePage(viewState, page);
        return false;
    }

    @Override
    public boolean process(final PageTree nodes, final PageTreeLevel level) {
        return nodes.process(this, level, true);
    }

    protected void calculatePageVisibility() {
        int firstVisiblePage = -1;
        int lastVisiblePage = -1;
        final RectF bounds = new RectF();
        for (final Page page : ctrl.model.getPages()) {
            if (ctrl.isPageVisible(page, viewState, bounds)) {
                if (firstVisiblePage == -1) {
                    firstVisiblePage = page.index.viewIndex;
                }
                lastVisiblePage = page.index.viewIndex;
            } else if (firstVisiblePage != -1) {
                break;
            }
        }
        viewState.update(firstVisiblePage, lastVisiblePage);
    }

    protected final void decodePageTreeNodes(final ViewState viewState, final List<PageTreeNode> nodesToDecode) {
        if (D) {
        	Log.e(TAG, ">>>AbstractEvent#decodePageTreeNodes start<=============");
        }
        final PageTreeNode best = Collections.min(nodesToDecode, new PageTreeNodeComparator(viewState));
        final DecodeService ds = ctrl.getBase().getDecodeService();
        if (ds != null) {
            ds.decodePage(viewState, best);
            for (final PageTreeNode node : nodesToDecode) {
                if (node != best) {
                    ds.decodePage(viewState, node);
                }
            }
        }
        if (D) {
        	Log.e(TAG, ">>>AbstractEvent#decodePageTreeNodes end=============>");
        }
    }

    protected final void recyclePage(final ViewState viewState, final Page page) {
        final int oldSize = bitmapsToRecycle.size();
        final boolean res = page.nodes.recycleAll(bitmapsToRecycle, true);
        if (res) {
            if (D) {
            	Log.e(TAG, "Recycle page " + page.index + " " + viewState.pages + " = " + (bitmapsToRecycle.size() - oldSize));
            }
        }
    }
}
