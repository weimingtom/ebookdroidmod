package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.Vector2D;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.graphics.Canvas;

public abstract class AbstractPageSlider extends AbstractPageAnimator {
    public AbstractPageSlider(final PageAnimationType type, 
    	final SinglePageController singlePageDocumentView) {
        super(type, singlePageDocumentView);
    }

    @Override
    public void init() {
        super.init();
        mInitialEdgeOffset = 0;
    }

    @Override
    protected void onFirstDrawEvent(final Canvas canvas, final ViewState viewState) {
        lock.writeLock().lock();
        try {
            updateValues();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void resetClipEdge() {
        mMovement.x = mInitialEdgeOffset;
        mMovement.y = mInitialEdgeOffset;
        mOldMovement.x = 0;
        mOldMovement.y = 0;
        mA.set(mInitialEdgeOffset, 0);
    }

    @Override
    protected void updateValues() {
        mA.x = mMovement.x;
        mA.y = 0;
    }

    protected final BitmapRef getBitmap(final ViewState viewState, final BitmapRef ref) {
        final float width = viewState.viewRect.width();
        final float height = viewState.viewRect.height();
        final BitmapRef bitmap = BitmapManager.checkBitmap(ref, width, height);
        bitmap.eraseColor(viewState.paint.backgroundFillPaint.getColor());
        return bitmap;
    }

    @Override
    protected final void drawExtraObjects(final EventDraw event) {
    	//FIXME:绘画额外图形showAnimIcon
    }

    @Override
    protected Vector2D fixMovement(final Vector2D movement, final boolean bMaintainMoveDir) {
        return movement;
    }

    protected final void updateForeBitmap(final EventDraw event, final Page page) {
        if (foreBitmapIndex != foreIndex || foreBitmap == null) {
            foreBitmap = getBitmap(event.viewState, foreBitmap);
            EventPool.newEventDraw(event, foreBitmap.getCanvas(), this.view.getBase().getContext()).process(page);
            foreBitmapIndex = foreIndex;
        }
    }

    protected final void updateBackBitmap(final EventDraw event, final Page page) {
        if (backBitmapIndex != backIndex || backBitmap == null) {
            backBitmap = getBitmap(event.viewState, backBitmap);
            EventPool.newEventDraw(event, backBitmap.getCanvas(), this.view.getBase().getContext()).process(page);
            backBitmapIndex = backIndex;
        }
    }
}
