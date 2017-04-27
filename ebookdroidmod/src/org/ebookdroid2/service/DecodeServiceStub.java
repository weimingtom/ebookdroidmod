package org.ebookdroid2.service;

import java.util.List;

import org.ebookdroid2.codec.CodecPageInfo;
import org.ebookdroid2.codec.OutlineLink;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTreeNode;

import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.graphics.RectF;

import com.artifex.mupdfdemo.MuPDFCore;

public class DecodeServiceStub implements DecodeService {
    private static final CodecPageInfo DEFAULT = new CodecPageInfo(0, 0);

    @Override
    public boolean isFeatureSupported(final int feature) {
        return false;
    }
    
    @Override
    public void open(MuPDFCore _core) {
    	
    }
    
    @Override
    public void decodePage(final ViewState viewState, final PageTreeNode node) {
    }

    @Override
    public void stopDecoding(final PageTreeNode node, final String reason) {

    }

    @Override
    public int getPageCount() {
        return 0;
    }

    @Override
    public List<OutlineLink> getOutline() {
        return null;
    }

    @Override
    public CodecPageInfo getUnifiedPageInfo() {
        return DEFAULT;
    }

    @Override
    public CodecPageInfo getPageInfo(final int pageIndex) {
        return DEFAULT;
    }

    @Override
    public void recycle() {
    	
    }

    @Override
    public void updateViewState(final ViewState viewState) {
    	
    }

    @Override
    public BitmapRef createThumbnail(boolean useEmbeddedIfAvailable, final int width, final int height, final int pageNo, final RectF region) {
        return null;
    }

    @Override
    public int getPixelFormat() {
        return PixelFormat.RGBA_8888;
    }

    @Override
    public Config getBitmapConfig() {
        return Config.ARGB_8888;
    }

    @Override
    public void searchText(final Page page, final String pattern, final SearchCallback callback) {
    }

    @Override
    public void stopSearch(final String pattern) {
    }

}
