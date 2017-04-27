package org.ebookdroid2.view;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.manager.PageAlign;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.service.DecodeService;
import org.ebookdroid2.util.Flag;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public final class BaseView extends View implements IView {
    private static final PointF BASE_POINT = new PointF(0, 0);
    
    protected final IActivityController base;
    protected final Scroller scroller;
    protected PageAlign align;
    protected DrawThread drawThread;
    protected ScrollEventThread scrollThread;
    protected boolean layoutLocked;
    protected final AtomicReference<Rect> layout = 
    	new AtomicReference<Rect>();
    protected final Flag layoutFlag = new Flag();

    public BaseView(final IActivityController baseActivity) {
        super(baseActivity.getContext());
        this.base = baseActivity;
        this.scroller = new Scroller(getContext());
        setKeepScreenOn(AppSettings.current().keepScreenOn);
        setFocusable(true);
        setFocusableInTouchMode(true);
        drawThread = new DrawThread(null, this.getContext());
        scrollThread = new ScrollEventThread(base, this);
        scrollThread.start();
    }

    @Override
    public final View getView() {
        return this;
    }

    @Override
    public final IActivityController getBase() {
        return base;
    }

    @Override
    public final Scroller getScroller() {
        return scroller;
    }

    @Override
    public final void invalidateScroll() {
        stopScroller();
        final float scrollScaleRatio = getScrollScaleRatio();
        scrollTo((int) (getScrollX() * scrollScaleRatio), (int) (getScrollY() * scrollScaleRatio));
    }

    @Override
    public final void invalidateScroll(final float newZoom, final float oldZoom) {
        stopScroller();
        final float ratio = newZoom / oldZoom;
        scrollTo((int) ((getScrollX() + getWidth() / 2) * ratio - getWidth() / 2),
                (int) ((getScrollY() + getHeight() / 2) * ratio - getHeight() / 2));
    }

    @Override
    public void startPageScroll(final int dx, final int dy) {
        scroller.startScroll(getScrollX(), getScrollY(), dx, dy);
        redrawView();
    }

    @Override
    public void startFling(final float vX, final float vY, final Rect limits) {
        scroller.fling(getScrollX(), getScrollY(), -(int) vX, -(int) vY, limits.left, limits.right, limits.top,
                limits.bottom);
    }

    @Override
    public void continueScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
        }
    }

    @Override
    public void forceFinishScroll() {
        if (!scroller.isFinished()) { // is flinging
            scroller.forceFinished(true); // to stop flinging on touch
        }
    }

    @Override
    public final void onScrollChanged(final int curX, final int curY, final int oldX, final int oldY) {
        super.onScrollChanged(curX, curY, oldX, oldY);
        scrollThread.onScrollChanged(curX, curY, oldX, oldY);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        if (base.getDocumentController().onTouchEvent(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public final void scrollTo(final int x, final int y) {
        scrollThread.scrollTo(x, y);
    }

    @Override
    public void _scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public final RectF getViewRect() {
        return new RectF(getScrollX(), getScrollY(), getScrollX() + getWidth(), getScrollY() + getHeight());
    }

    @Override
    public void changeLayoutLock(final boolean lock) {
        post(new Runnable() {
            @Override
            public void run() {
                layoutLocked = lock;
            }
        });
    }

    @Override
    public boolean isLayoutLocked() {
        return layoutLocked;
    }

    @Override
    protected final void onLayout(final boolean layoutChanged, final int left, final int top, final int right,
            final int bottom) {
        super.onLayout(layoutChanged, left, top, right, bottom);
        final Rect oldLayout = layout.getAndSet(
        		new Rect(left, top, right, bottom));
        base.getDocumentController().onLayoutChanged(
        	layoutChanged, layoutLocked, oldLayout, layout.get());
        if (oldLayout == null) {
            layoutFlag.set();
        }
    }

    @Override
    public final void waitForInitialization() {
        while (!layoutFlag.get()) {
            layoutFlag.waitFor(TimeUnit.SECONDS, 1);
        }
    }

    @Override
    public void onDestroy() {
        layoutFlag.set();
        scrollThread.finish();
    }

    @Override
    public float getScrollScaleRatio() {
        final Page page = base.getDocumentModel().getCurrentPageObject();
        if (page == null) {
            return 0;
        }
        final float zoom = base.getZoomModel().getZoom();
        return getWidth() * zoom / page.getBounds(zoom).width();
    }

    @Override
    public void stopScroller() {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
    }

    @Override
    public final void redrawView() {
        redrawView(ViewState.get(base.getDocumentController()));
    }

    @Override
    public final void redrawView(final ViewState viewState) {
        if (viewState != null) {
            if (drawThread != null) {
                drawThread.draw(viewState);
            }
            final DecodeService ds = base.getDecodeService();
            if (ds != null) {
                ds.updateViewState(viewState);
            }
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
    	ViewState viewState = drawThread.takeTask(1, TimeUnit.MILLISECONDS, true);
        if (viewState == null) {
            viewState = ViewState.get(base.getDocumentController());
            viewState.addedToDrawQueue();
        }
        //FIXME:View的绘画入口(消费者)
        EventPool.newEventDraw(viewState, canvas, this.getContext()).process().releaseAfterDraw();
    }

    @Override
    public PointF getBase(final RectF viewRect) {
        return BASE_POINT;
    }

    @Override
    public Bitmaps createBitmaps(final String nodeId, final BitmapRef orig, final Rect bitmapBounds, final boolean invert) {
        return new Bitmaps(nodeId, orig, bitmapBounds, invert);
    }
}
