package org.ebookdroid2.curl;

import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.touch.IGestureDetector;

public interface PageAnimator extends IGestureDetector {
    PageAnimationType getType();
    void init();
    void resetPageIndexes(final int currentIndex);
    void draw(EventDraw event);
    void setViewDrawn(boolean b);
    void flipAnimationStep();
    boolean isPageVisible(final Page page, final ViewState viewState);
    void pageUpdated(ViewState viewState, Page page);
    void animate(int direction);
}
