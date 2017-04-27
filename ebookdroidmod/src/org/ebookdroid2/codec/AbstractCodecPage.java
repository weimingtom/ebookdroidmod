package org.ebookdroid2.codec;

import android.graphics.RectF;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCodecPage implements CodecPage {
    @Override
    public List<PageLink> getPageLinks() {
        return Collections.emptyList();
    }

    @Override
    public List<PageTextBox> getPageText() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends RectF> searchText(String pattern) {
        return Collections.emptyList();
    }
}
