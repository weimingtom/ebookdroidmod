package org.ebookdroid2.codec;

import com.artifex.mupdfdemo.MuPDFCore;

import android.graphics.Bitmap;

public class MuPdfContext extends AbstractCodecContext {
	private final static boolean D = false;
	private final static String TAG = "MuPdfContext";	
	
    public static final int MUPDF_FEATURES = 
//    	FEATURE_CACHABLE_PAGE_INFO |  //FIXME:暂时设置为无缓存
    	FEATURE_EMBEDDED_OUTLINE | 
    	FEATURE_PAGE_TEXT_SEARCH | 
    	FEATURE_POSITIVE_IMAGES_IN_NIGHT_MODE;
    
    public static final Bitmap.Config BITMAP_CFG = Bitmap.Config.RGB_565;

    public MuPdfContext() {
        super(MUPDF_FEATURES);
    }

    @Override
    public Bitmap.Config getBitmapConfig() {
        return BITMAP_CFG;
    }

    @Override
    public CodecDocument openDocument(MuPDFCore _core) {
    	return new MuPdfDocument(this, _core);
    }
}
