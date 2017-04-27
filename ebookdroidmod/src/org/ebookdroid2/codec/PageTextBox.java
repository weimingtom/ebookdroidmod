package org.ebookdroid2.codec;

import android.graphics.RectF;

public class PageTextBox extends RectF {
    public String text;

    @Override
    public String toString() {
        return "PageTextBox(" + left + ", " + top + ", " + right + ", " + bottom + ": " + text + ")";
    }
}
