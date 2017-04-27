package org.ebookdroid2.event;

import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTree;
import org.ebookdroid2.page.PageTreeLevel;
import org.ebookdroid2.page.PageTreeNode;

public interface IEvent {
    ViewState process();
    boolean process(Page page);
    boolean process(PageTree nodes);
    boolean process(PageTree nodes, PageTreeLevel level);
    boolean process(PageTreeNode node);
}
