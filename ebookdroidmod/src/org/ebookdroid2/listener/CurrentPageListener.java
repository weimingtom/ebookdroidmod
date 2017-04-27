package org.ebookdroid2.listener;

import org.ebookdroid2.page.PageIndex;

public interface CurrentPageListener {
    void currentPageChanged(PageIndex oldIndex, PageIndex newIndex);
}
