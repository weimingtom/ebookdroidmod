package org.ebookdroid2.codec;

import com.artifex.mupdfdemo.MuPDFCore;

import android.graphics.Bitmap;

public interface CodecContext extends CodecFeatures {
    CodecDocument openDocument(MuPDFCore _core);
    long getContextHandle();
    void recycle();
    boolean isRecycled();
    Bitmap.Config getBitmapConfig();
}
