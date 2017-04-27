package org.ebookdroid2.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.codec.PageLink;
import org.ebookdroid2.event.EventGotoPageCorner;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.listener.IProgressIndicator;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.manager.PageAlign;
import org.ebookdroid2.manager.SettingsManager;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.model.DocumentModel.PageIterator;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageIndex;
import org.ebookdroid2.touch.DefaultGestureDetector;
import org.ebookdroid2.touch.IGestureDetector;
import org.ebookdroid2.touch.IMultiTouchListener;
import org.ebookdroid2.touch.MultiTouchGestureDetector;
import org.ebookdroid2.view.IView;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class AbstractViewController implements IViewController {
	private final static boolean D = false;
	private final static String TAG = "AbstractViewController";	

    public static final int DOUBLE_TAP_TIME = 500;
    
    public final IActivityController base;
    public final DocumentModel model;
    public final DocumentViewMode mode;
    protected boolean isInitialized = false;
    protected boolean isShown = false;
    protected final AtomicBoolean inZoom = new AtomicBoolean();
    protected final AtomicBoolean inQuickZoom = new AtomicBoolean();
    protected final AtomicBoolean inZoomToColumn = new AtomicBoolean();
    protected final PageIndex pageToGo;
    public int firstVisiblePage;
    public int lastVisiblePage;
    protected boolean layoutLocked;
    private List<IGestureDetector> detectors;
    protected Object m_parent; //FIXME:这里应该改用具体类型
    protected Object m_managedComponent; //FIXME:这里应该改用具体类型

    public AbstractViewController(final IActivityController base, final DocumentViewMode mode) {
    	this.base = base;
        this.m_parent = base;
        this.m_managedComponent = base.getView();
    	this.mode = mode;
        this.model = base.getDocumentModel();
        this.firstVisiblePage = -1;
        this.lastVisiblePage = -1;
        this.pageToGo = base.getBookSettings().getCurrentPage();
    }

    public Object getManagedComponent() {
        return m_managedComponent;
    }
    
    protected List<IGestureDetector> getGestureDetectors() {
        if (detectors == null) {
            detectors = initGestureDetectors(new ArrayList<IGestureDetector>(4));
        }
        return detectors;
    }

    protected List<IGestureDetector> initGestureDetectors(final List<IGestureDetector> list) {
        final GestureListener listener = new GestureListener();
        list.add(new MultiTouchGestureDetector(listener));
        list.add(new DefaultGestureDetector(base.getContext(), listener));
        return list;
    }

    @Override
    public final IView getView() {
        return base.getView();
    }

    @Override
    public final IActivityController getBase() {
        return base;
    }

    @Override
    public final void init(final IProgressIndicator task) {
        if (!isInitialized) {
            try {
                model.initPages(base, task);
            } finally {
                isInitialized = true;
            }
        }
    }

    //FIXME:这里的注释？？？
    //isShown = false;
    @Override
    public final void onDestroy() {
        // isShown = false;
    }

    @Override
    public final void show() {
        if (!isInitialized) {
            if (D) {
                Log.e(TAG, "View is not initialized yet");
            }
            return;
        }
        if (!isShown) {
            isShown = true;
            if (D) {
            	Log.e(TAG, "Showing view content...");
            }
            invalidatePageSizes(InvalidateSizeReason.INIT, null);
            final BookSettings bs = base.getBookSettings();
            bs.lastChanged = System.currentTimeMillis();
            final Page page = pageToGo.getActualPage(model, bs);
            final int toPage = page != null ? page.index.viewIndex : 0;
            if (D) {
            	Log.e(TAG, ">>>>goToPage(...)");
            }
            goToPage(toPage, bs.offsetX, bs.offsetY);
        } else {
            if (D) {
            	Log.e(TAG, "View has been shown before");
            }
        }
    }

    public final void updatePosition(final Page page, final ViewState viewState) {
        final PointF pos = viewState.getPositionOnPage(page);
        SettingsManager.positionChanged(base.getBookSettings(), pos.x, pos.y);
    }

    @Override
    public final void zoomChanged(final float oldZoom, final float newZoom, final boolean committed) {
        if (!isShown) {
            return;
        }
        inZoom.set(!committed);
        EventPool.newEventZoom(this, oldZoom, newZoom, committed, this.getBase().getContext()).process().release();
        if (committed) {
        	if (this.getHelper() != null) {
        		this.getHelper().zoomChanged(newZoom, base.getManagedComponent());
        	}
        } else {
            inQuickZoom.set(false);
            inZoomToColumn.set(false);
        }
    }

    //FIXME:快速缩放
    //R.id.actions_quickZoom)
    public final void quickZoom() {
        if (inZoom.get()) {
            return;
        }
        float zoomFactor = 2.0f;
        if (inQuickZoom.compareAndSet(true, false)) {
            zoomFactor = 1.0f / zoomFactor;
        } else {
            inQuickZoom.set(true);
            inZoomToColumn.set(false);
        }
        base.getZoomModel().scaleAndCommitZoom(zoomFactor);
    }

    protected void scrollToColumn(final Page page, final RectF column, final PointF pos, final int screenHeight) {
        final ViewState vs = ViewState.get(AbstractViewController.this);
        final RectF pb = vs.getBounds(page);
        final RectF columnRegion = page.getPageRegion(pb, new RectF(column));
        columnRegion.offset(-vs.viewBase.x, -vs.viewBase.y);
        final float toX = columnRegion.left;
        final float toY = pb.top + pos.y * pb.height() - 0.5f * screenHeight;
        getView().scrollTo((int) toX, (int) toY);
        vs.release();
    }

    //FIXME:滚动到
    //R.id.actions_leftTopCorner
    //R.id.actions_leftBottomCorner
    //R.id.actions_rightTopCorner,
    //R.id.actions_rightBottomCorner
    public void scrollToCorner(final Integer offX, final Integer offY) {
        final float offsetX = offX != null ? offX.floatValue() : 0;
        final float offsetY = offY != null ? offY.floatValue() : 0;
        new EventGotoPageCorner(this, offsetX, offsetY).process().release();
    }

    @Override
    public final void updateMemorySettings() {
        EventPool.newEventReset(this, null, false, this.getBase().getContext()).process().release();
    }

    public final int getScrollX() {
        return getView().getScrollX();
    }

    public final int getWidth() {
        return getView().getWidth();
    }

    public final int getScrollY() {
        return getView().getScrollY();
    }

    public final int getHeight() {
        return getView().getHeight();
    }

    @Override
    public final boolean dispatchKeyEvent(final KeyEvent event) {
        return false;
    }

    @Override
    public final boolean onTouchEvent(final MotionEvent ev) {
        final int delay = AppSettings.current().touchProcessingDelay;
        if (delay > 0) {
            try {
                Thread.sleep(Math.min(250, delay));
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        }
        for (final IGestureDetector d : getGestureDetectors()) {
            if (d.enabled() && d.onTouchEvent(ev)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onLayoutChanged(final boolean layoutChanged, final boolean layoutLocked, final Rect oldLaout,
            final Rect newLayout) {
        if (D) {
        	Log.e(TAG, "onLayoutChanged(" + layoutChanged + ", " + layoutLocked + "," + oldLaout + ", " + newLayout + ")");
        }
        if (layoutChanged && !layoutLocked) {
            if (isShown) {
                EventPool.newEventReset(this, InvalidateSizeReason.LAYOUT, true, this.getBase().getContext()).process().release();
                return true;
            } else {
                if (D) {
                	Log.e(TAG, "onLayoutChanged(): view not shown yet");
                }
            }
        }
        return false;
    }

    @Override
    public final void toggleRenderingEffects() {
        EventPool.newEventReset(this, null, true, this.getBase().getContext()).process().release();
    }

    @Override
    public final void invalidateScroll() {
        if (!isShown) {
            return;
        }
        getView().invalidateScroll();
    }

    @Override
    public final void setAlign(final PageAlign align) {
        EventPool.newEventReset(this, InvalidateSizeReason.PAGE_ALIGN, false, this.getBase().getContext()).process().release();
    }

    protected final boolean isShown() {
        return isShown;
    }

    @Override
    public final int getFirstVisiblePage() {
        return firstVisiblePage;
    }

    @Override
    public final int getLastVisiblePage() {
        return lastVisiblePage;
    }

    @Override
    public final void redrawView() {
        getView().redrawView(ViewState.get(this));
    }

    @Override
    public final void redrawView(final ViewState viewState) {
        getView().redrawView(viewState);
    }

    //FIXME:上下滚动
    //R.id.actions_verticalConfigScrollUp
    //R.id.actions_verticalConfigScrollDown
    public final void verticalConfigScroll(final Integer direction) {
        verticalConfigScroll(direction);
    }

    protected final boolean processLinkTap(final float x, final float y) {
        final float zoom = base.getZoomModel().getZoom();
        final RectF rect = new RectF(x, y, x, y);
        rect.offset(getScrollX(), getScrollY());
        final PageIterator pages = model.getPages(firstVisiblePage, lastVisiblePage + 1);
        try {
            final RectF bounds = new RectF();
            for (final Page page : pages) {
                page.getBounds(zoom, bounds);
                if (RectF.intersects(bounds, rect)) {
                    if (page != null && 
                    	page.links != null && 
                    	!page.links.isEmpty()) {
                        for (final PageLink link : page.links) {
                            if (processLinkTap(page, link, bounds, rect)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
        } finally {
            pages.release();
        }
        return false;
    }

    protected final boolean processLinkTap(final Page page, final PageLink link, final RectF pageBounds,
            final RectF tapRect) {
        final RectF linkRect = page.getLinkSourceRect(pageBounds, link);
        if (linkRect == null || !RectF.intersects(linkRect, tapRect)) {
            return false;
        }
        if (D) {
        	Log.e(TAG, "Page link found under tap: " + link);
        }
        goToLink(link.targetPage, link.targetRect, AppSettings.current().storeLinkGotoHistory);
        return true;
    }

    @Override
    public void goToLink(final int pageDocIndex, final RectF targetRect, final boolean addToHistory) {
        if (pageDocIndex >= 0) {
            final PointF linkPoint = new PointF();
            final Page target = model.getLinkTargetPage(pageDocIndex, targetRect, linkPoint,
                    base.getBookSettings().splitRTL);
            if (D) {
            	Log.e(TAG, "Target page found: " + target); //FIXME:点击链接跳转到的页面信息 //FIXME:BUG这里重启后跳转的位置错了，可能是页面缓存导致错误
            }
            if (target != null) {
                base.jumpToPage(target.index.viewIndex, linkPoint.x, linkPoint.y, addToHistory);
            }
        }
    }

    protected class GestureListener extends SimpleOnGestureListener 
    	implements IMultiTouchListener {
        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onDoubleTap(" + e + ")");
            }
            //FIXME:这里处理双击
            return false;
        }
        
        @Override
        public boolean onDown(final MotionEvent e) {
            getView().forceFinishScroll();
            if (D) {
            	Log.e(TAG, "onDown(" + e + ")");
            }
            return true;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float vX, final float vY) {
            final Rect l = getScrollLimits();
            float x = vX, y = vY;
            if (Math.abs(vX / vY) < 0.5) {
                x = 0;
            }
            if (Math.abs(vY / vX) < 0.5) {
                y = 0;
            }
            if (D) {
            	Log.e(TAG, "onFling(" + x + ", " + y + ")");
            }
            getView().startFling(x, y, l);
            getView().redrawView();
            return true;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            float x = distanceX, y = distanceY;
            if (Math.abs(distanceX / distanceY) < 0.5) {
                x = 0;
            }
            if (Math.abs(distanceY / distanceX) < 0.5) {
                y = 0;
            }
            if (D) {
            	Log.e(TAG, "onScroll(" + x + ", " + y + ")");
            }
            getView().scrollBy((int) x, (int) y);
            return true;
        }
        
        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onSingleTapUp(" + e + ")");
            }
            return true;
        }
        
        @Override
        public boolean onSingleTapConfirmed(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onSingleTapConfirmed(" + e + ")");
            }
            //FIXME:这里处理单击
            ////////////////from processTap()
            final float x = e.getX();
            final float y = e.getY();
            if (processLinkTap(x, y)) {
            	return true;
            }
            ///////////////
            return false;
        }

        @Override
        public void onLongPress(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onLongPress(" + e + ")");
            }
            //FIXME:处理长按
        }

        @Override
        public void onTwoFingerPinch(final MotionEvent e, final float oldDistance, final float newDistance) {
            final float factor = FloatMath.sqrt(newDistance / oldDistance);
            if (D) {
            	Log.e(TAG, "onTwoFingerPinch(" + oldDistance + ", " + newDistance + "): " + factor);
            }
            base.getZoomModel().scaleZoom(factor);
        }

        @Override
        public void onTwoFingerPinchEnd(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onTwoFingerPinch(" + e + ")");
            }
            base.getZoomModel().commit();
        }

        @Override
        public void onTwoFingerTap(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onTwoFingerTap(" + e + ")");
            }
            //FIXME:这里处理双指点击
        }
    }
}
