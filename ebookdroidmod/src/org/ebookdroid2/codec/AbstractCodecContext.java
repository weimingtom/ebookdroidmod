package org.ebookdroid2.codec;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.manager.AppSettings;

import android.graphics.Bitmap;

public abstract class AbstractCodecContext implements CodecContext {
    private static final AtomicLong SEQ = new AtomicLong();
    private static Integer densityDPI;
    
    public final int supportedFeatures;
    private long contextHandle;

    protected AbstractCodecContext(final int supportedFeatures) {
        this(SEQ.incrementAndGet(), supportedFeatures);
    }
    
    protected AbstractCodecContext(final long contextHandle, final int supportedFeatures) {
        this.contextHandle = contextHandle;
        this.supportedFeatures = supportedFeatures;
    }

    @Override
    protected final void finalize() throws Throwable {
        recycle();
        super.finalize();
    }

    @Override
    public final void recycle() {
        if (!isRecycled()) {
            freeContext();
            contextHandle = 0;
        }
    }

    protected void freeContext() {
    	
    }

    @Override
    public final boolean isRecycled() {
        return contextHandle == 0;
    }

    @Override
    public final long getContextHandle() {
        return contextHandle;
    }

    @Override
    public boolean isFeatureSupported(final int feature) {
        return (supportedFeatures & feature) != 0;
    }

    @Override
    public Bitmap.Config getBitmapConfig() {
        return Bitmap.Config.RGB_565;
    }

    public static int getWidthInPixels(final float pdfWidth) {
        return getSizeInPixels(pdfWidth, AppSettings.current().getXDpi(ViewerActivityHelper.DM.xdpi));
    }

    public static int getHeightInPixels(final float pdfHeight) {
        return getSizeInPixels(pdfHeight, AppSettings.current().getYDpi(ViewerActivityHelper.DM.ydpi));
    }

    public static int getSizeInPixels(final float pdfHeight, float dpi) {
        if (dpi == 0) {
            dpi = getDensityDPI();
        }
        if (dpi < 72) { 
            dpi = 72; 
        }
        return (int) (pdfHeight * dpi / 72);
    }

    private static int getDensityDPI() {
        if (densityDPI == null) {
            try {
                final Field f = ViewerActivityHelper.DM.getClass().getDeclaredField("densityDpi");
                densityDPI = ((Integer) f.get(ViewerActivityHelper.DM));
            } catch (final Throwable ex) {
                densityDPI = Integer.valueOf(120);
            }
        }
        return densityDPI.intValue();
    }
}
