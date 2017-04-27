package org.ebookdroid2.codec;

import java.util.List;

import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.ViewState;

import android.graphics.RectF;

public interface CodecPage {
    int getWidth();
    int getHeight();
    BitmapRef renderBitmap(ViewState viewState, int width, int height, RectF pageSliceBounds);
    List<PageLink> getPageLinks();
    List<PageTextBox> getPageText();
    List<? extends RectF> searchText(final String pattern);
    void recycle();
    boolean isRecycled();
}
