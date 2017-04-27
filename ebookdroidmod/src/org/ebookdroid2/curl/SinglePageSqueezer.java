package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.page.Page;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

public class SinglePageSqueezer extends AbstractPageSlider {
    public SinglePageSqueezer(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.SQUEEZER, singlePageDocumentView);
    }

    @Override
    protected void drawForeground(final EventDraw event) {
        Page page = event.viewState.model.getPageObject(foreIndex);
        if (page == null) {
            page = event.viewState.model.getCurrentPageObject();
        }
        if (page != null) {
            updateForeBitmap(event, page);
            final Canvas canvas = event.canvas;
            final RectF viewRect = event.viewState.viewRect;
            final Rect src = new Rect(0, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(0, 0, viewRect.width() - mA.x, viewRect.height());
            foreBitmap.draw(canvas, src, dst, PAINT);
        }
    }
    
    @Override
    protected void drawBackground(final EventDraw event) {
        final Page page = event.viewState.model.getPageObject(backIndex);
        if (page != null) {
            updateBackBitmap(event, page);
            final Canvas canvas = event.canvas;
            final RectF viewRect = event.viewState.viewRect;
            final Rect src = new Rect(0, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(viewRect.width() - mA.x, 0, viewRect.width(), viewRect.height());
            backBitmap.draw(canvas, src, dst, PAINT);
        }

    }

}
