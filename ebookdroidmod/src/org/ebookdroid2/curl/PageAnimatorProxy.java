package org.ebookdroid2.curl;

import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.view.MotionEvent;

import java.util.concurrent.atomic.AtomicReference;

public class PageAnimatorProxy implements PageAnimator {
    private final AtomicReference<PageAnimator> orig;

    public PageAnimatorProxy(final PageAnimator pa) {
        orig = new AtomicReference<PageAnimator>(pa);
    }

    public void switchCurler(final PageAnimator orig) {
        this.orig.set(orig);
    }

    @Override
    public void init() {
    }

    @Override
    public boolean enabled() {
        return orig.get().enabled();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        return orig.get().onTouchEvent(ev);
    }

    @Override
    public PageAnimationType getType() {
        return orig.get().getType();
    }

    @Override
    public void resetPageIndexes(final int currentIndex) {
        orig.get().resetPageIndexes(currentIndex);
    }

    @Override
    public void draw(final EventDraw event) {
        orig.get().draw(event);
    }

    @Override
    public void setViewDrawn(final boolean b) {
        orig.get().setViewDrawn(b);
    }

    @Override
    public void flipAnimationStep() {
        orig.get().flipAnimationStep();
    }

    @Override
    public boolean isPageVisible(final Page page, final ViewState viewState) {
        return orig.get().isPageVisible(page, viewState);
    }

    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {
        orig.get().pageUpdated(viewState, page);
    }

    @Override
    public void animate(final int direction) {
        orig.get().animate(direction);
    }
}
