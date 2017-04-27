package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.view.MotionEvent;

public class SinglePageView implements PageAnimator {
    protected final PageAnimationType type;
    protected final SinglePageController view;
    protected boolean bViewDrawn;
    protected int foreIndex = -1;
    protected int backIndex = -1;

    public SinglePageView(final SinglePageController view) {
        this(PageAnimationType.NONE, view);
    }

    protected SinglePageView(final PageAnimationType type, final SinglePageController view) {
        this.type = type;
        this.view = view;
    }

    @Override
    public void init() {
    }

    @Override
    public final PageAnimationType getType() {
        return type;
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return false;
    }

    @Override
    public boolean isPageVisible(final Page page, final ViewState viewState) {
        final int pageIndex = page.index.viewIndex;
        return pageIndex == viewState.model.getCurrentViewPageIndex();
    }

    @Override
    public void draw(final EventDraw event) {
        final Page page = event.viewState.model.getCurrentPageObject();
        if (page != null) {
            event.process(page);
            //FIXME:绘画额外图形showAnimIcon
        }
    }

    @Override
    public final void resetPageIndexes(final int currentIndex) {
        if (foreIndex != currentIndex) {
            foreIndex = backIndex = currentIndex;
        }
    }

    @Override
    public void flipAnimationStep() {
    
    }
    
    @Override
    public final void setViewDrawn(final boolean bViewDrawn) {
        this.bViewDrawn = bViewDrawn;
    }

    public boolean isViewDrawn() {
        return bViewDrawn;
    }

    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {

    }

    @Override
    public void animate(final int direction) {
        view.goToPage(view.model.getCurrentViewPageIndex() + direction);
    }
}
