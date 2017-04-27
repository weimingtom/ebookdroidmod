package org.ebookdroid2.service;

import java.util.List;

import org.ebookdroid2.codec.CodecFeatures;
import org.ebookdroid2.codec.CodecPage;
import org.ebookdroid2.codec.CodecPageInfo;
import org.ebookdroid2.codec.OutlineLink;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTreeNode;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import com.artifex.mupdfdemo.MuPDFCore;

//FIXME:分拆里面的接口类
//FIXME:不要用extends写法
public interface DecodeService extends CodecFeatures {
	void open(MuPDFCore _core);
    void decodePage(ViewState viewState, PageTreeNode node);
    void searchText(Page page, String pattern, SearchCallback callback);
    void stopSearch(String pattern);
    void stopDecoding(PageTreeNode node, String reason);
    int getPageCount();
    List<OutlineLink> getOutline();
    CodecPageInfo getUnifiedPageInfo();
    CodecPageInfo getPageInfo(int pageIndex);
    void recycle();
    void updateViewState(ViewState viewState);
    BitmapRef createThumbnail(boolean useEmbeddedIfAvailable, int width, int height, int pageNo, RectF region);
    int getPixelFormat();
    Bitmap.Config getBitmapConfig();

    interface DecodeCallback {
        void decodeComplete(CodecPage codecPage, BitmapRef bitmap, Rect bitmapBounds, RectF croppedPageBounds);
    }

    interface SearchCallback {
        void searchComplete(Page page, List<? extends RectF> regions);
    }
}
