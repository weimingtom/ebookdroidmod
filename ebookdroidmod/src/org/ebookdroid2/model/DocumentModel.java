package org.ebookdroid2.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.codec.CodecContext;
import org.ebookdroid2.codec.CodecFeatures;
import org.ebookdroid2.codec.CodecPageInfo;
import org.ebookdroid2.codec.MuPdfContext;
import org.ebookdroid2.listener.CurrentPageListener;
import org.ebookdroid2.listener.IProgressIndicator;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.CacheManager;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageIndex;
import org.ebookdroid2.page.PageType;
import org.ebookdroid2.service.DecodeService;
import org.ebookdroid2.service.DecodeServiceBase;
import org.ebookdroid2.service.DecodeServiceStub;
import org.ebookdroid2.util.SparseArrayEx;
import org.ebookdroid2.util.TLIterator;
import org.ebookdroid2.view.IView;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

import com.artifex.mupdfdemo.MuPDFCore;

/**
 * 文档模型，关键类
 * FIXME:分拆类
 */
public class DocumentModel {
	private final static boolean D = false;
	private final static String TAG = "DocumentModel";		
	
	private static final Page[] EMPTY_PAGES = {};
    private static final int TAG_PAGE_COUNTS = 0;
    private static final int TAG_CODEC_PAGE_INFO = 1;
    private static final int TAG_AUTO_CROPPING = 2;
    private static final int TAG_MANUAL_CROPPING = 3;
	//FIXME:解码器类型
    public final static int CODEC_UNKNOWN = 0;
    public final static int CODEC_TYPE_PDF = 1;
	//FIXME:模式类型
    public final static int SCHEME_UNKNOWN = 0;
	public final static int SCHEME_FILE = 1;
    
	private final ThreadLocal<PageIterator> iterators = 
    		new ThreadLocal<PageIterator>();
    public final DecodeService decodeService;
    protected PageIndex currentIndex = PageIndex.FIRST;
    private final CodecContext context;
    private Page[] pages = EMPTY_PAGES;
    private File cacheFile;
    public DocumentInfo docInfo;
    private List<CurrentPageListener> listenerList = new ArrayList<CurrentPageListener>();
    
    public void addListener(CurrentPageListener listener) {
    	if (listener != null) {
	    	listenerList.remove(listener);
	    	listenerList.add(listener);
    	}
    }
    public void removeListener(CurrentPageListener listener) {
    	if (listener != null) {
    		listenerList.remove(listener);
    	}
    }
    
    //FIXME:这里异常写得不好
    public DocumentModel(final int activityType) {
        if (activityType != 0) {
            try {
                context = newCodecContext(activityType);//activityType.getContextClass().newInstance();
                if (context == null) {
                	throw new Exception("context == null");
                }
                decodeService = new DecodeServiceBase(context);
            } catch (final Throwable th) {
                throw new RuntimeException(th);
            }
        } else {
            context = null;
            decodeService = new DecodeServiceStub();
        }
    }

    public void open(MuPDFCore _core) {
        decodeService.open(_core);
    }

    public Page[] getPages() {
        return pages;
    }

    public PageIterator getPages(final int start) {
        return getPages(start, pages.length);
    }

    public PageIterator getPages(final int start, final int end) {
        final PageIterator pi = iterators.get();
        if (pi == null) {
            return new PageIterator(start, Math.min(end, pages.length));
        }
        pi.index = start;
        pi.end = Math.min(end, pages.length);
        iterators.set(null);
        return pi;
    }

    public int getPageCount() {
        return (pages != null ? pages.length : 0);
    }

    public void recycle() {
        decodeService.recycle();
        recyclePages();
    }

    private void recyclePages() {
        if (pages != null && pages.length > 0) {
            saveDocumentInfo();
            final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
            for (final Page page : pages) {
                page.recycle(bitmapsToRecycle);
            }
            BitmapManager.release(bitmapsToRecycle);
            BitmapManager.release();
        }
        pages = EMPTY_PAGES;
    }

    public void saveDocumentInfo() {
        final boolean cacheable = decodeService.isFeatureSupported(CodecFeatures.FEATURE_CACHABLE_PAGE_INFO);
        if (cacheable) {
        	DocumentModel.save(cacheFile, docInfo);
        }
    }

    public Page getPageObject(final int viewIndex) {
        return pages != null && 0 <= viewIndex && viewIndex < pages.length ? pages[viewIndex] : null;
    }

    public Page getPageByDocIndex(final int docIndex) {
        for (final Page page : pages) {
            if (page.index.docIndex == docIndex) {
                return page;
            }
        }
        return null;
    }

    public Page getLinkTargetPage(final int pageDocIndex, final RectF targetRect, final PointF linkPoint,
            final boolean splitRTL) {
        Page target = getPageByDocIndex(pageDocIndex);
        if (target != null) {
            float offsetX = 0;
            float offsetY = 0;
            if (targetRect != null) {
                offsetX = targetRect.left;
                offsetY = targetRect.top;
                if (target.type == PageType.LEFT_PAGE && offsetX >= 0.5f) {
                    target = getPageObject(
                    	target.index.viewIndex + (splitRTL ? -1 : +1));
                    offsetX -= 0.5f;
                }
            }
            if (linkPoint != null) {
                linkPoint.set(offsetX, offsetY);
            }
        }
        return target;
    }

    public Page getCurrentPageObject() {
        return getPageObject(this.currentIndex.viewIndex);
    }

    public Page getLastPageObject() {
        return getPageObject(pages.length - 1);
    }

    public void setCurrentPageIndex(final PageIndex newIndex) {
    	if (!equalsObject(currentIndex, newIndex)) {
            if (D) {
                Log.e(TAG, "Current page changed: " + "currentIndex" + " -> " + newIndex);
            }
            final PageIndex oldIndex = this.currentIndex;
            this.currentIndex = newIndex;
            //FIXME:事件响应
            if (listenerList != null && listenerList.size() > 0) {
	        	for (CurrentPageListener listener : listenerList) {
	        		if (listener != null) {
	        			listener.currentPageChanged(oldIndex, newIndex);
	        		}
	        	}
            }
    	}
    }

	public static boolean equalsObject(final Object o1, final Object o2) {
		if (o1 == null) {
			return o2 == null ? true : false;
		}
		return o1.equals(o2);
	}
    
    public PageIndex getCurrentIndex() {
        return this.currentIndex;
    }

    public int getCurrentViewPageIndex() {
        return this.currentIndex.viewIndex;
    }

    public int getCurrentDocPageIndex() {
        return this.currentIndex.docIndex;
    }

    public void setCurrentPageByFirstVisible(final int firstVisiblePage) {
        final Page page = getPageObject(firstVisiblePage);
        if (page != null) {
            setCurrentPageIndex(page.index);
        }
    }

    public void initPages(final IActivityController base, final IProgressIndicator task) {
        recyclePages();
        final BookSettings bs = base.getBookSettings();
        if (base == null || bs == null || 
        	context == null || decodeService == null) {
            return;
        }
        final IView view = base.getView();
        final CodecPageInfo defCpi = new CodecPageInfo();
        defCpi.width = view.getWidth();
        defCpi.height = view.getHeight();
        int viewIndex = 0;
        final long start = System.currentTimeMillis();
        try {
            final ArrayList<Page> list = new ArrayList<Page>();
            if (docInfo == null) {
                retrieveDocumentInfo(base, bs, task);
            }
            for (int docIndex = 0; docIndex < docInfo.docPageCount; docIndex++) {
                final PageInfo pi = docInfo.docPages.get(docIndex, null);
                final CodecPageInfo info = pi != null ? pi.info : null;
                if (!bs.splitPages || info == null || (info.width < info.height)) {
                    viewIndex = createFullPage(base, docIndex, viewIndex, defCpi, info, pi, list);
                } else {
                    if (bs.splitRTL) {
                        viewIndex = createRightPage(base, docIndex, viewIndex, info, list);
                        viewIndex = createLeftPage(base, docIndex, viewIndex, info, list);
                    } else {
                        viewIndex = createLeftPage(base, docIndex, viewIndex, info, list);
                        viewIndex = createRightPage(base, docIndex, viewIndex, info, list);
                    }
                }
            }
            pages = list.toArray(new Page[list.size()]);
            if (pages.length > 0) {
            	//FIXME:这里原来是用来创建缩略图，已经被删除
            }
        } finally {
            if (D) {
            	Log.e(TAG, "Loading page info: " + (System.currentTimeMillis() - start) + " ms");
            }
        }
    }

    protected int createFullPage(final IActivityController base, final int docIndex, final int viewIndex,
            final CodecPageInfo defCpi, final CodecPageInfo info, final PageInfo pi, final ArrayList<Page> list) {
        final PageIndex index = new PageIndex(docIndex, viewIndex);
        final Page page = new Page(base, index, PageType.FULL_PAGE, info != null ? info : defCpi);
        list.add(page);
        page.nodes.root.setInitialCropping(pi);
        return index.viewIndex + 1;
    }

    protected int createLeftPage(final IActivityController base, final int docIndex, final int viewIndex,
            final CodecPageInfo info, final ArrayList<Page> list) {
        final PageIndex index = new PageIndex(docIndex, viewIndex);
        final Page left = new Page(base, index, PageType.LEFT_PAGE, info);
        left.nodes.root.setInitialCropping(docInfo.leftPages.get(docIndex, null));
        list.add(left);
        return index.viewIndex + 1;
    }

    protected int createRightPage(final IActivityController base, final int docIndex, final int viewIndex,
            final CodecPageInfo info, final ArrayList<Page> list) {
        final PageIndex index = new PageIndex(docIndex, viewIndex);
        final Page right = new Page(base, index, PageType.RIGHT_PAGE, info);
        right.nodes.root.setInitialCropping(docInfo.rightPages.get(docIndex, null));
        list.add(right);
        return index.viewIndex + 1;
    }
    
    public void updateAutoCropping(final Page page, final RectF r) {
        final PageInfo pageInfo = docInfo.getPageInfo(page);
        pageInfo.autoCropping = r != null ? new RectF(r) : null;
    }

    public void updateManualCropping(final Page page, final RectF r) {
        final PageInfo pageInfo = docInfo.getPageInfo(page);
        pageInfo.manualCropping = r != null ? new RectF(r) : null;
    }

    private DocumentInfo retrieveDocumentInfo(final IActivityController base, final BookSettings bs,
            final IProgressIndicator task) {
    	if (D) {
    		Log.e(TAG, "retrieveDocumentInfo start ");
    	}
    	//FIXME:暂时设置为无缓存
    	//FIXME:在另一个地方注释掉，搜索FEATURE_CACHABLE_PAGE_INFO
        final boolean cacheable = decodeService.isFeatureSupported(CodecFeatures.FEATURE_CACHABLE_PAGE_INFO);
        if (D) {
    		Log.e(TAG, "retrieveDocumentInfo start cacheable == " + cacheable);
    	}
        //FIXME:去除缓存文件
        if (cacheable) {
            cacheFile = CacheManager.getDocumentFile(bs.fileName);
            docInfo = cacheFile.exists() ? DocumentModel.load(cacheFile) : null;
            if (D) {
            	Log.e(TAG, "retrieveDocumentInfo start cacheFile == " + cacheFile);
            	Log.e(TAG, "retrieveDocumentInfo start docInfo == " + docInfo);
        	}
            if (docInfo != null) {
                return docInfo;
            }
        }
        if (D) {
        	Log.e(TAG, "Retrieving pages from document...");
        }
        docInfo = new DocumentInfo();
        docInfo.docPageCount = decodeService.getPageCount();
        docInfo.viewPageCount = -1;
        final CodecPageInfo unified = decodeService.getUnifiedPageInfo();
        for (int i = 0; i < docInfo.docPageCount; i++) {
            if (task != null) {
            	task.setProgressDialogMessage(
            		"获取页面大小 %d/%d", (i + 1), docInfo.docPageCount);
            }
            final PageInfo pi = new PageInfo(i);
            docInfo.docPages.append(i, pi);
            pi.info = unified != null ? unified : 
            	decodeService.getPageInfo(i);
        }
        this.saveDocumentInfo();
        return docInfo;
    }

    public final class PageIterator implements TLIterator<Page> {
        private int end;
        private int index;

        private PageIterator(final int start, final int end) {
            this.index = start;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return 0 <= index && index < end;
        }

        @Override
        public Page next() {
            return hasNext() ? pages[index++] : null;
        }

        @Override
        public void remove() {
        }

        @Override
        public Iterator<Page> iterator() {
            return this;
        }

        public void release() {
            iterators.set(this);
        }
    }
    
    public static DocumentInfo load(File _this) {
        try {
            if (D) {
            	Log.e(TAG, "Loading document info...");
            }
            final DocumentInfo info = new DocumentInfo();
            final DataInputStream in = new DataInputStream(new FileInputStream(_this));
            try {
                while (true) {
                    byte tag = -1;
                    try {
                        tag = in.readByte();
                    } catch (EOFException ex) {
                        return info;
                    }
                    final byte id = (byte) (tag & 0x3F);
                    final boolean docPage = (tag & 0x80) == 0;
                    final boolean leftPage = (tag & 0x40) == 0;
                    switch (id) {
                    case TAG_PAGE_COUNTS:
                        info.loadPageCounts(in);
                        break;
                        
                    case TAG_CODEC_PAGE_INFO:
                        info.loadCodePageInfo(in);
                        break;
                        
                    case TAG_AUTO_CROPPING:
                        info.loadAutoCropping(in, docPage, leftPage);
                        break;
                        
                    case TAG_MANUAL_CROPPING:
                        info.loadManualCropping(in, docPage, leftPage);
                        break;
                    }
                }
            } catch (final EOFException ex) {
            	ex.printStackTrace();
                if (D) {
                	Log.e(TAG, "Loading document info failed: " + ex.getMessage());
                }
            } catch (final IOException ex) {
            	ex.printStackTrace();
                if (D) {
                	Log.e(TAG, "Loading document info failed: " + ex.getMessage());
                }
            } finally {
                try {
                    in.close();
                } catch (final IOException ex) {
                }
            }
        } catch (final FileNotFoundException ex) {
        	ex.printStackTrace();
            if (D) {
            	Log.e(TAG, "Loading document info failed: " + ex.getMessage());
            }
        }
        return null;
    }

    public static void save(File _this, final DocumentInfo info) {
        try {
            if (D) {
            	Log.e(TAG, "Saving document info...");
            }
            final DataOutputStream out = new DataOutputStream(new FileOutputStream(_this));
            try {
                info.savePageCounts(out);
                info.saveCodePageInfo(out);
                info.saveAutoCropping(out);
                info.saveManualCropping(out);
                if (D) {
                	Log.e(TAG, "Saving document info finished");
                }
            } catch (final IOException ex) {
            	ex.printStackTrace();
            	if (D) {
            		Log.e(TAG, "Saving document info failed: " + ex.getMessage());
            	}
            } finally {
                try {
                    out.close();
                } catch (final IOException ex) {
                }
            }
        } catch (final IOException ex) {
        	ex.printStackTrace();
            if (D) {
            	Log.e(TAG, "Saving document info failed: " + ex.getMessage());
            }
        }
    }

    public static class DocumentInfo {
        public int docPageCount;
        public int viewPageCount;
        public final SparseArrayEx<PageInfo> docPages = new SparseArrayEx<PageInfo>();
        public final SparseArrayEx<PageInfo> leftPages = new SparseArrayEx<PageInfo>();
        public final SparseArrayEx<PageInfo> rightPages = new SparseArrayEx<PageInfo>();

        void loadPageCounts(final DataInputStream in) throws IOException {
            this.docPageCount = in.readShort();
            this.viewPageCount = in.readShort();
        }

        void savePageCounts(final DataOutputStream out) throws IOException {
            out.writeByte(TAG_PAGE_COUNTS);
            out.writeShort(this.docPageCount);
            out.writeShort(this.viewPageCount);
        }

        void loadCodePageInfo(final DataInputStream in) throws IOException {
            final int index = in.readShort();
            PageInfo pageInfo = this.docPages.get(index, null);
            if (pageInfo == null) {
                pageInfo = new PageInfo(index);
                this.docPages.append(index, pageInfo);
            }
            pageInfo.info = new CodecPageInfo(in.readInt(), in.readInt());
        }

        void saveCodePageInfo(final DataOutputStream out) throws IOException {
            for (final PageInfo info : this.docPages) {
                out.writeByte(TAG_CODEC_PAGE_INFO);
                out.writeShort(info.index);
                out.writeInt(info.info.width);
                out.writeInt(info.info.height);
            }
        }

        void loadAutoCropping(final DataInputStream in, final boolean docPage, final boolean leftPage)
                throws IOException {
            final int index = in.readShort();
            final SparseArrayEx<PageInfo> target = getPages(docPage, leftPage);
            PageInfo pageInfo = target.get(index, null);
            if (pageInfo == null) {
                pageInfo = new PageInfo(index);
                target.append(index, pageInfo);
            }
            pageInfo.autoCropping = new RectF(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        }

        void loadManualCropping(final DataInputStream in, final boolean docPage, final boolean leftPage)
                throws IOException {
            final int index = in.readShort();
            final SparseArrayEx<PageInfo> target = getPages(docPage, leftPage);
            PageInfo pageInfo = target.get(index, null);
            if (pageInfo == null) {
                pageInfo = new PageInfo(index);
                target.append(index, pageInfo);
            }
            pageInfo.manualCropping = new RectF(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        }

        void saveAutoCropping(final DataOutputStream out) throws IOException {
            for (final PageInfo info : this.docPages) {
                final RectF cropping = info.autoCropping;
                if (cropping != null) {
                    saveCropping(out, TAG_AUTO_CROPPING, info.index, cropping);
                }
            }
            for (final PageInfo info : this.leftPages) {
                final RectF cropping = info.autoCropping;
                if (cropping != null) {
                    saveCropping(out, TAG_AUTO_CROPPING | 0x80, info.index, cropping);
                }
            }
            for (final PageInfo info : this.rightPages) {
                final RectF cropping = info.autoCropping;
                if (cropping != null) {
                    saveCropping(out, TAG_AUTO_CROPPING | 0x80 | 0x40, info.index, cropping);
                }
            }
        }

        void saveManualCropping(final DataOutputStream out) throws IOException {
            for (final PageInfo info : this.docPages) {
                final RectF cropping = info.manualCropping;
                if (cropping != null) {
                    saveCropping(out, TAG_MANUAL_CROPPING, info.index, cropping);
                }
            }
            for (final PageInfo info : this.leftPages) {
                final RectF cropping = info.manualCropping;
                if (cropping != null) {
                    saveCropping(out, TAG_MANUAL_CROPPING | 0x80, info.index, cropping);
                }
            }
            for (final PageInfo info : this.rightPages) {
                final RectF cropping = info.manualCropping;
                if (cropping != null) {
                    saveCropping(out, TAG_MANUAL_CROPPING | 0x80 | 0x40, info.index, cropping);
                }
            }
        }

        void saveCropping(final DataOutputStream out, final int tag, final int index, final RectF cropping)
                throws IOException {
            out.writeByte(tag);
            out.writeShort(index);
            out.writeFloat(cropping.left);
            out.writeFloat(cropping.top);
            out.writeFloat(cropping.right);
            out.writeFloat(cropping.bottom);
        }

        SparseArrayEx<PageInfo> getPages(final boolean docPage, final boolean leftPage) {
            return docPage ? this.docPages : leftPage ? this.leftPages : this.rightPages;
        }

        public PageInfo getPageInfo(Page page) {
            SparseArrayEx<PageInfo> arr = null;
            switch (page.type) {
            case FULL_PAGE:
                arr = docPages;
                break;
                
            case LEFT_PAGE:
                arr = leftPages;
                break;
                
            case RIGHT_PAGE:
                arr = rightPages;
                break;
            }
            int key = page.index.docIndex;
            PageInfo pi = arr.get(key, null);
            if (pi == null) {
                pi = new PageInfo(key);
                arr.append(key, pi);
            }
            return pi;
        }
    }

    public static class PageInfo {
        public final int index;
        public CodecPageInfo info;
        public RectF autoCropping;
        public RectF manualCropping;

        public PageInfo(final int index) {
            this.index = index;
        }
    }

    public final static int getCodecByUri(final String uri) {
    	int codecType = CODEC_UNKNOWN;
    	final String uriString = uri.toLowerCase();
    	if (uriString.endsWith(".pdf")) { 
    		//"pdf"
    		codecType = CODEC_TYPE_PDF;
    	}
    	return codecType;
    }
    
    public final static int getCodecByMimeType(String type) {
    	int codecType = CODEC_UNKNOWN;
    	String type2 = type.toLowerCase();
    	if (type2.equals("application/pdf")) { 
    		//"pdf"
    		codecType = CODEC_TYPE_PDF;
    	}
    	return codecType;
    }
    
    public static CodecContext newCodecContext(int type) {
    	if (type == CODEC_TYPE_PDF) {
    		return new MuPdfContext();
    	} else {
    		return null;
    	}
    }
	public static String getDefaultResourceName(final Uri uri, final String defTitle) {
		String result = uri.getLastPathSegment();
		if (result == null || result.length() == 0) {
			result = defTitle;
		}
		return result;
	}
	
	public static int getScheme(final Intent intent) {
		return intent != null ? getScheme(intent.getScheme()) : SCHEME_UNKNOWN;
	}
	
	public static int getScheme(final Uri uri) {
		return uri != null ? getScheme(uri.getScheme()) : SCHEME_UNKNOWN;
	}
	
	public static int getScheme(final String scheme) {
		if ("file".equalsIgnoreCase(scheme)) {
			return SCHEME_FILE;
		}
		return SCHEME_UNKNOWN;
	}
	
	public static String getResourceName(final ContentResolver cr, final Uri uri) {
		return getDefaultResourceName(uri, "");
	}
}
