package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.page.Page;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class SinglePageFader extends AbstractPageSlider {
    private final Paint paint = new Paint(PAINT);

    public SinglePageFader(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.FADER, singlePageDocumentView);
    }

    @Override
    protected void drawForeground(final EventDraw event) {
        Page page = event.viewState.model.getPageObject(foreIndex);
        if (page == null) {
            page = event.viewState.model.getCurrentPageObject();
        }
        if (page != null) {
            event.process(page);
        }
    }

    @Override
    protected void drawBackground(final EventDraw event) {
        final Page page = event.viewState.model.getPageObject(backIndex);
        if (page != null) {
            updateBackBitmap(event, page);
            final RectF viewRect = event.viewState.viewRect;
            final Rect src = new Rect(0, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(0, 0, viewRect.width(), viewRect.height());
            paint.setAlpha(255 * (int) mA.x / (int) viewRect.width());
            backBitmap.draw(event.canvas, src, dst, paint);
        }
    }

}
