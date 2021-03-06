package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.model.Vector2D;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public abstract class AbstractSinglePageCurler 
	extends AbstractPageAnimator {
    protected float mFlipRadius;
    protected Paint mCurlEdgePaint;
    protected final Vector2D mB, mC, mD, mE, mF, mOldF, mOrigin;

    protected final Vector2D[] foreBack;
    protected final Vector2D[] backClip;

    public AbstractSinglePageCurler(final PageAnimationType type, final SinglePageController singlePageDocumentView) {
        super(type, singlePageDocumentView);
        mB = new Vector2D();
        mC = new Vector2D();
        mD = new Vector2D();
        mE = new Vector2D();
        mF = new Vector2D();
        mOldF = new Vector2D();
        mOrigin = new Vector2D(view.getWidth(), 0);
        foreBack = new Vector2D[] { mA, mD, mE, mF };
        backClip = new Vector2D[] { mA, mB, mC, mD };
    }
    
    @Override
    public void init() {
        super.init();
        mCurlEdgePaint = new Paint();
        mCurlEdgePaint.setColor(Color.WHITE);
        mCurlEdgePaint.setAntiAlias(true);
        mCurlEdgePaint.setStyle(Paint.Style.FILL);
        mCurlEdgePaint.setShadowLayer(10, -5, 5, 0x99000000);

        mInitialEdgeOffset = 20;
    }
    
    @Override
    protected Vector2D fixMovement(Vector2D point, final boolean bMaintainMoveDir) {
        if (point.distance(mOrigin) > mFlipRadius) {
            if (bMaintainMoveDir) {
                point = mOrigin.sum(point.sub(mOrigin).normalize().mult(mFlipRadius));
            } else {
                if (point.x > (mOrigin.x + mFlipRadius)) {
                    point.x = (mOrigin.x + mFlipRadius);
                } else if (point.x < (mOrigin.x - mFlipRadius)) {
                    point.x = (mOrigin.x - mFlipRadius);
                }
                point.y = 0;
            }
        }
        return point;
    }

    @Override
    protected void onFirstDrawEvent(final Canvas canvas, final ViewState viewState) {
        mFlipRadius = viewState.viewRect.width();
        resetClipEdge();
        lock.writeLock().lock();
        try {
            updateValues();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void drawForeground(final EventDraw event) {
        Page page = event.viewState.model.getPageObject(foreIndex);
        if (page == null) {
            page = event.viewState.model.getCurrentPageObject();
        }
        if (page != null) {
            event.canvas.save();
            event.canvas.clipRect(event.viewState.getBounds(page));
            event.process(page);
            event.canvas.restore();
        }
    }

    @Override
    protected void drawBackground(final EventDraw event) {
        final Path mask = createBackgroundPath();
        final Page page = event.viewState.model.getPageObject(backIndex);
        if (page != null) {
            event.canvas.save();
            event.canvas.clipPath(mask);
            event.canvas.drawRect(event.canvas.getClipBounds(), event.viewState.paint.backgroundFillPaint);
            event.process(page);
            event.canvas.restore();
        }
    }

    @Override
    protected void drawExtraObjects(final EventDraw event) {
        final Path path = createCurlEdgePath();
        event.canvas.drawPath(path, mCurlEdgePaint);
    }

    @Override
    protected void resetClipEdge() {
        mMovement.x = mInitialEdgeOffset;
        mMovement.y = mInitialEdgeOffset;
        mOldMovement.x = 0;
        mOldMovement.y = 0;
        mA.set(mInitialEdgeOffset, 0);
        mB.set(view.getWidth(), view.getHeight());
        mC.set(view.getWidth(), 0);
        mD.set(0, 0);
        mE.set(0, 0);
        mF.set(0, 0);
        mOldF.set(0, 0);
        mOrigin.set(view.getWidth(), 0);
    }

    private Path createBackgroundPath() {
        final Path path = new Path();
        path.moveTo(mA.x, mA.y);
        path.lineTo(mB.x, mB.y);
        path.lineTo(mC.x, mC.y);
        path.lineTo(mD.x, mD.y);
        path.lineTo(mA.x, mA.y);
        return path;
    }

    private Path createCurlEdgePath() {
        final Path path = new Path();
        path.moveTo(mA.x, mA.y);
        path.lineTo(mD.x, mD.y);
        path.lineTo(mE.x, mE.y);
        path.lineTo(mF.x, mF.y);
        path.lineTo(mA.x, mA.y);
        return path;
    }

}
