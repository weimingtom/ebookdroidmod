package org.ebookdroid2.curl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.Vector2D;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

public abstract class AbstractPageAnimator extends SinglePageView implements PageAnimator {
    protected static final Paint PAINT = new Paint(
    	Paint.FILTER_BITMAP_FLAG | 
    	Paint.ANTI_ALIAS_FLAG | 
    	Paint.DITHER_FLAG);

    protected int mUpdateRate;
    protected FlipAnimationHandler mAnimationHandler;
    protected Vector2D mMovement;
    protected boolean bFlipRight;
    protected boolean bFlipping;
    protected boolean bBlockTouchInput = false;
    protected boolean bEnableInputAfterDraw = false;
    protected final Vector2D mA = new Vector2D(0, 0);
    protected int mInitialEdgeOffset;
    protected Vector2D mFinger;
    protected Vector2D mOldMovement;
    protected boolean bUserMoves;
    protected BitmapRef foreBitmap;
    protected int foreBitmapIndex = -1;
    protected BitmapRef backBitmap;
    protected int backBitmapIndex = -1;
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public AbstractPageAnimator(final PageAnimationType type, final SinglePageController singlePageDocumentView) {
        super(type, singlePageDocumentView);
    }

    @Override
    public void init() {
        super.init();
        mMovement = new Vector2D(0, 0);
        mFinger = new Vector2D(0, 0);
        mOldMovement = new Vector2D(0, 0);
        mAnimationHandler = new FlipAnimationHandler(this);
        mUpdateRate = 5;
    }

    @Override
    public boolean isPageVisible(final Page page, final ViewState viewState) {
        final int pageIndex = page.index.viewIndex;
        return pageIndex == this.foreIndex || pageIndex == this.backIndex;
    }

    protected ViewState nextView(final ViewState viewState) {
        if (viewState.model == null) {
            return viewState;
        }
        final int pageCount = viewState.model.getPageCount();
        if (pageCount == 0) {
            return viewState;
        }
        foreIndex = viewState.pages.currentIndex % pageCount;
        backIndex = (foreIndex + 1) % pageCount;
        final Page forePage = viewState.model.getPageObject(foreIndex);
        final Page backPage = viewState.model.getPageObject(backIndex);
        return view.invalidatePages(viewState, forePage, backPage);
    }

    protected ViewState previousView(final ViewState viewState) {
        if (viewState.model == null) {
            return viewState;
        }
        final int pageCount = viewState.model.getPageCount();
        if (pageCount == 0) {
            return viewState;
        }
        backIndex = viewState.pages.currentIndex % pageCount;
        foreIndex = (pageCount + backIndex - 1) % pageCount;
        final Page forePage = viewState.model.getPageObject(foreIndex);
        final Page backPage = viewState.model.getPageObject(backIndex);
        return view.invalidatePages(viewState, forePage, backPage);
    }

    @Override
    public synchronized void flipAnimationStep() {
        if (!bFlipping) {
            return;
        }
        final int width = view.getWidth();
        bBlockTouchInput = true;
        float curlSpeed = width / 5;
        if (!bFlipRight) {
            curlSpeed *= -1;
        }
        mMovement.x += curlSpeed;
        mMovement = fixMovement(mMovement, false);
        lock.writeLock().lock();
        try {
            updateValues();
            if (mA.x < getLeftBound()) {
                mA.x = getLeftBound() - 1;
            }
            if (mA.x > width - 1) {
                mA.x = width;
            }
        } finally {
            lock.writeLock().unlock();
        }
        if (mA.x <= getLeftBound() || mA.x >= width - 1) {
            bFlipping = false;
            if (bFlipRight) {
                view.goToPage(backIndex);
                foreIndex = backIndex;
            } else {
                view.goToPage(foreIndex);
                backIndex = foreIndex;
            }
            lock.writeLock().lock();
            try {
                resetClipEdge();
                updateValues();
            } finally {
                lock.writeLock().unlock();
            }
            bEnableInputAfterDraw = true;
        } else {
            mAnimationHandler.sleep(mUpdateRate);
        }
        view.redrawView();
    }

    protected float getLeftBound() {
        return 1;
    }

    protected abstract void resetClipEdge();
    protected abstract Vector2D fixMovement(Vector2D point, final boolean bMaintainMoveDir);
    protected abstract void drawBackground(EventDraw event);
    protected abstract void drawForeground(EventDraw event);
    protected abstract void drawExtraObjects(EventDraw event);
    protected abstract void updateValues();

    @Override
    public final synchronized void draw(final EventDraw event) {
        final Canvas canvas = event.canvas;
        final ViewState viewState = event.viewState;
        if (!enabled()) {
            BitmapManager.release(foreBitmap);
            BitmapManager.release(backBitmap);
            foreBitmap = null;
            backBitmap = null;
            super.draw(event);
            return;
        }
        if (!isViewDrawn()) {
            setViewDrawn(true);
            onFirstDrawEvent(canvas, viewState);
        }
        canvas.drawColor(Color.BLACK);
        lock.readLock().lock();
        try {
            drawInternal(event);
            drawExtraObjects(event);
        } finally {
            lock.readLock().unlock();
        }
        if (bEnableInputAfterDraw) {
            bBlockTouchInput = false;
            bEnableInputAfterDraw = false;
        }
    }

    protected void drawInternal(final EventDraw event) {
        drawForeground(event);
        if (foreIndex != backIndex) {
            drawBackground(event);
        }
    }

    protected abstract void onFirstDrawEvent(Canvas canvas, final ViewState viewState);

    @Override
    public final boolean enabled() {
        final Rect limits = view.getScrollLimits();
        return limits.width() <= 0 && limits.height() <= 0;
    }

    @Override
    public final boolean onTouchEvent(final MotionEvent event) {
        if (bBlockTouchInput) {
            return true;
        }
        mFinger.x = event.getX();
        mFinger.y = event.getY();
        final int width = view.getWidth();
        ViewState viewState = ViewState.get(view);
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOldMovement.x = mFinger.x;
                    mOldMovement.y = mFinger.y;
                    bUserMoves = false;
                    return false;
                
                case MotionEvent.ACTION_UP:
                    if (bUserMoves) {
                        bUserMoves = false;
                        bFlipping = true;
                        flipAnimationStep();
                    } else {
                        return false;
                    }
                    break;
                    
                case MotionEvent.ACTION_MOVE:
                    if ((mFinger.absdistancex(mOldMovement) > 25)) {
                        if (!bUserMoves) {
                            if (mFinger.x < mOldMovement.x) {
                                mMovement.x = mInitialEdgeOffset;
                                mMovement.y = mInitialEdgeOffset;
                                bFlipRight = true;
                                viewState = nextView(viewState);
                            } else {
                                bFlipRight = false;
                                viewState = previousView(viewState);
                                mMovement.x = getInitialXForBackFlip(width);
                                mMovement.y = mInitialEdgeOffset;
                            }
                        }
                        bUserMoves = true;
                    } else {
                        if (!bUserMoves) {
                            break;
                        }
                    }
                    mMovement.x -= mFinger.x - mOldMovement.x;
                    mMovement.y -= mFinger.y - mOldMovement.y;
                    mMovement = fixMovement(mMovement, true);
                    if (mMovement.y <= 1) {
                        mMovement.y = 1;
                    }
                    if (mFinger.x < mOldMovement.x) {
                        bFlipRight = true;
                    } else {
                        bFlipRight = false;
                    }
                    mOldMovement.x = mFinger.x;
                    mOldMovement.y = mFinger.y;
                    lock.writeLock().lock();
                    try {
                        updateValues();
                    } finally {
                        lock.writeLock().unlock();
                    }
                    view.redrawView(viewState);
                    return !bUserMoves;
            }
        } finally {
            viewState.release();
        }
        return true;
    }

    protected int getInitialXForBackFlip(final int width) {
        return width;
    }

    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {
        if (foreBitmapIndex == page.index.viewIndex) {
            foreBitmapIndex = -1;
        }
        if (backBitmapIndex == page.index.viewIndex) {
            backBitmapIndex = -1;
        }
    }

    @Override
    public void animate(final int direction) {
        resetClipEdge();
        mMovement = new Vector2D(direction < 0 ? 7 * view.getWidth() / 8 : view.getWidth() / 8, mInitialEdgeOffset);
        bFlipping = true;
        bFlipRight = direction > 0;
        final ViewState viewState = ViewState.get(view);
        final ViewState newState = bFlipRight ? nextView(viewState) : previousView(viewState);
        bUserMoves = false;
        flipAnimationStep();
        viewState.release();
        newState.release();
    }
}
