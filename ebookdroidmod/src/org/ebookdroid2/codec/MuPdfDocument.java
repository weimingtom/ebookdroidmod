package org.ebookdroid2.codec;

import java.util.List;

import android.graphics.PointF;
import android.util.Log;

import com.artifex.mupdfdemo.MuPDFCore;

public class MuPdfDocument extends AbstractCodecDocument {
	private final static boolean D = false;
	private final static String TAG = "MuPdfDocument";
	
    public static final int FORMAT_PDF = 0;
    public static final int FORMAT_XPS = 1;
    
    private MuPDFCore core = null;

    public MuPdfDocument(final MuPdfContext context, MuPDFCore _core) {
        super(context);
    	if (D) {
        	Log.e(TAG, "MuPdfDocument 001");
        }
        this.core = _core;
    	if (D) {
        	Log.e(TAG, "MuPdfDocument 002 core == " + core);
        }
    }

    @Override
    public List<OutlineLink> getOutline() {
        final MuPdfOutline ou = new MuPdfOutline();
        if (core != null) {
        	return ou.getOutline2(core);
        } else {
        	return null;
        }
    }

    @Override
    public CodecPage getPage(final int pageNumber) {
        return MuPdfPage.createPage(pageNumber + 1, core);
    }

    @Override
    public int getPageCount() {
    	if (D) {
    		Log.e(TAG, "==================getPageCount = " + core.countPages());
    	}
    	if (core != null) {
    		return core.countPages();
    	} else {
    		return 0;
    	}
    }

    @Override
    public CodecPageInfo getPageInfo(final int pageNumber) {
        final CodecPageInfo info = new CodecPageInfo();
        PointF point = new PointF(100, 100);
        if (core != null) {
        	//FIXME:如果想制造缓存bug，可以修改这里的大小为0,0
        	point = core.getPageSize(pageNumber - 1);
        }
        info.width = (int)point.x;
        info.height = (int)point.y;
        info.rotation = 0;
        
        info.rotation = (360 + info.rotation) % 360;
        
        return info;
    }

    @Override
    protected void freeDocument() {
        if (core != null) {
        	core.onDestroy();
        }
    }
}
