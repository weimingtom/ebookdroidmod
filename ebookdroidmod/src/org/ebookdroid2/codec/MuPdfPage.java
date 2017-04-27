package org.ebookdroid2.codec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.ViewState;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.PageTextBox2;

public class MuPdfPage extends AbstractCodecPage {
	private final static boolean D = false;
	private final static String TAG = "MuPdfPage";

	private boolean isRecycled = false; //新增，用来兼容旧代码
	
    private final MuPDFCore core;
    private final int pageno; //FIXME:注意这个是页码，从1开始数起
    
    final RectF pageBounds;
    final int actualWidth;
    final int actualHeight;

    private MuPdfPage(MuPDFCore core, int pageno) {
        //FIXME:需要提前到这里
    	this.core = core;
        this.pageno = pageno;
        
        this.pageBounds = getBounds();
        this.actualWidth = (int) pageBounds.width();
        this.actualHeight = (int) pageBounds.height();
    }

    @Override
    public int getWidth() {
        return actualWidth;
    }

    @Override
    public int getHeight() {
        return actualHeight;
    }

    @Override
    public BitmapRef renderBitmap(ViewState viewState, final int width, final int height, final RectF pageSliceBounds) {
        final float[] matrixArray = calculateFz(width, height, pageSliceBounds);
        return render(viewState, new Rect(0, 0, width, height), matrixArray);
    }

    private float[] calculateFz(final int width, final int height, final RectF pageSliceBounds) {
        final Matrix matrix = getMatrix();
        matrix.postScale(width / pageBounds.width(), height / pageBounds.height());
        matrix.postTranslate(-pageSliceBounds.left * width, -pageSliceBounds.top * height);
        matrix.postScale(1 / pageSliceBounds.width(), 1 / pageSliceBounds.height());

        final float[] matrixSource = new float[9];
        matrix.getValues(matrixSource);

        final float[] matrixArray = new float[6];

        matrixArray[0] = matrixSource[0];
        matrixArray[1] = matrixSource[3];
        matrixArray[2] = matrixSource[1];
        matrixArray[3] = matrixSource[4];
        matrixArray[4] = matrixSource[2];
        matrixArray[5] = matrixSource[5];

        return matrixArray;
    }

    static MuPdfPage createPage(final int pageno, MuPDFCore core) {
        return new MuPdfPage(core, pageno);
    }

    @Override
    protected void finalize() throws Throwable {
        recycle();
        super.finalize();
    }

    @Override
    public synchronized void recycle() {
    	isRecycled = true;
    }

    @Override
    public boolean isRecycled() {
    	return isRecycled; //FIXME:
    }

    private RectF getBounds() {
    	if (D) {
			Log.e(TAG, "getBoundsgetPageSize 000 ");
		}
    	if (D) {
			Log.e(TAG, "getBoundsgetPageSize 001 ");
		}
		//FIXME:这里调用会空白一片
		if (core != null) {
	    	if (D) {
				Log.e(TAG, "getBoundsgetPageSize 002-1 ");
			}
			return core.getBounds(pageno - 1);
		} else {
			if (D) {
				Log.e(TAG, "getBoundsgetPageSize 002-2 ");
			}
			return new RectF();
		}
    }

    public BitmapRef render(ViewState viewState, final Rect viewbox, final float[] ctm) {
    	if (D) {
    		Log.e(TAG, "render 001");
    	}
    	if (isRecycled()) {
            throw new RuntimeException("The page has been recycled before: " + this);
        }
        final int[] mRect = new int[4];
        mRect[0] = viewbox.left;
        mRect[1] = viewbox.top;
        mRect[2] = viewbox.right;
        mRect[3] = viewbox.bottom;

        final int width = viewbox.width();
        final int height = viewbox.height();
        final int nightmode = viewState != null && viewState.nightMode && viewState.positiveImagesInNightMode ? 1 : 0;
        final int slowcmyk = AppSettings.current().slowCMYK ? 1 : 0;

        if (D) {
    		Log.e(TAG, "render 002");
    	}
        final int[] bufferarray = new int[width * height];
        //FIXME:这里空白一片
    	if (D) {
    		Log.e(TAG, "render 003-2");
    	}
    	if (this.core != null) {
    		if (D) {
        		Log.e(TAG, "render 004");
        	}
    		this.core.renderPage(this.pageno - 1, mRect, ctm, bufferarray);
    	}
        final BitmapRef b = BitmapManager.getBitmap("PDF page", width, height, MuPdfContext.BITMAP_CFG);
        b.setPixels(bufferarray, width, height);
        if (D) {
    		Log.e(TAG, "render 005");
    	}
        return b; 
    }
                                            
    @Override                                     
    public List<PageLink> getPageLinks() {
    	//FIXME:这里切换新的链接获取方式
        return MuPdfLinks.getPageLinks2(pageBounds, core, pageno-1);
    }

    @Override
    public List<? extends RectF> searchText(final String pattern) {
        List<PageTextBox> rects = null;
        //FIXME:搜索文本
    	if (this.core != null) {
    		List<PageTextBox2> result = this.core.search(this.pageno - 1, pattern);
    		if (result != null) {
    			rects = new ArrayList<PageTextBox>();
    			for (PageTextBox2 item : result) {
    				PageTextBox box = new PageTextBox();
    				box.left = item.left;
    				box.top = item.top;
    				box.right = item.right;
    				box.bottom = item.bottom;
    				box.text = item.text;
    				rects.add(box);
    			}
    		} else {
    			rects = null;
    		}
    	} else {
    		rects = null;
    	}
        if (rects != null && !rects.isEmpty()) {
            final Set<String> temp = new HashSet<String>();
            final Iterator<PageTextBox> iter = rects.iterator();
            while (iter.hasNext()) {
                final PageTextBox b = iter.next();
                if (temp.add(b.toString())) {
                    b.left = (b.left - pageBounds.left) / pageBounds.width();
                    b.top = (b.top - pageBounds.top) / pageBounds.height();
                    b.right = (b.right - pageBounds.left) / pageBounds.width();
                    b.bottom = (b.bottom - pageBounds.top) / pageBounds.height();
                } else {
                    iter.remove();
                }
            }
        }
        return rects;
    }

	public static Matrix getMatrix() {
		Matrix matrix = new Matrix();
		matrix.reset();
		return matrix;
	}
}
