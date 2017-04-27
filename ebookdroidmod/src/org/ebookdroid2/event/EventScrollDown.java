package org.ebookdroid2.event;

import java.util.Queue;

import org.ebookdroid2.page.Page;

import android.graphics.RectF;

public class EventScrollDown extends AbstractEventScroll<EventScrollDown> {
	public EventScrollDown(final Queue<EventScrollDown> eventQueue) {
        super(eventQueue);
    }

    @Override
    protected void calculatePageVisibility() {
        final Page[] pages = ctrl.model.getPages();
        if (pages == null || pages.length == 0) {
            return;
        }
        final int firstVisiblePage = viewState.pages.firstVisible;
        final int lastVisiblePage = viewState.pages.lastVisible;
        if (firstVisiblePage == -1) {
            super.calculatePageVisibility();
            return;
        }
        final RectF bounds = new RectF();
        if (ctrl.isPageVisible(pages[firstVisiblePage], viewState, bounds)) {
            findLastVisiblePage(pages, firstVisiblePage, true, bounds);
            return;
        }
        if (firstVisiblePage != lastVisiblePage && ctrl.isPageVisible(pages[lastVisiblePage], viewState, bounds)) {
            findFirstVisiblePage(pages, lastVisiblePage, true, bounds);
            return;
        }
        final int midIndex = firstVisiblePage;
        int delta = 0;
        int run = 2;
        while (run > 0) {
            run = 0;
            final int left = midIndex - delta;
            final int right = midIndex + delta;
            if (left >= 0) {
                run++;
                if (ctrl.isPageVisible(pages[left], viewState, bounds)) {
                    findFirstVisiblePage(pages, left, false, bounds);
                    return;
                }
            }
            if (right < pages.length - 1) {
                run++;
                if (ctrl.isPageVisible(pages[right], viewState, bounds)) {
                    findLastVisiblePage(pages, right, false, bounds);
                    return;
                }
            }
            delta++;
        }
        viewState.update(-1, -1);
    }

    protected void findLastVisiblePage(final Page[] pages, final int first, final boolean updateFirst,
            final RectF bounds) {
        int firstVisiblePage = first;
        int lastVisiblePage = firstVisiblePage;
        while (lastVisiblePage < pages.length - 1) {
            final int index = lastVisiblePage + 1;
            if (!ctrl.isPageVisible(pages[index], viewState, bounds)) {
                break;
            }
            lastVisiblePage = index;
        }
        if (updateFirst) {
            for (int index = firstVisiblePage - 1; index >= 0; index--) {
                if (!ctrl.isPageVisible(pages[index], viewState, bounds)) {
                    break;
                }
                firstVisiblePage = index;
            }
        }
        viewState.update(firstVisiblePage, lastVisiblePage);
    }

    protected void findFirstVisiblePage(final Page[] pages, final int last, final boolean updateLast, final RectF bounds) {
        int lastVisiblePage = last;
        int firstVisiblePage = lastVisiblePage;
        for (int index = lastVisiblePage - 1; index >= 0; index--) {
            if (!ctrl.isPageVisible(pages[index], viewState, bounds)) {
                break;
            }
            firstVisiblePage = index;
        }
        if (updateLast) {
            for (int index = lastVisiblePage + 1; index < pages.length; index++) {
                if (!ctrl.isPageVisible(pages[index], viewState, bounds)) {
                    break;
                }
                lastVisiblePage = index;
            }
        }
        viewState.update(firstVisiblePage, lastVisiblePage);
    }
}
