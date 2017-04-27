package com.artifex.mupdfdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

/**
 * pdf文件读取器，跟界面解耦
 * 这个类也是MuPDFActivity的状态保存对象
 */
public class MuPDFCore {
	private final static boolean D = true;
	private final static String TAG = "MuPDFCore";
	
	//调试用，屏蔽reload功能，请设置为false
	private final static boolean DISABLE_RELOAD = false; 
	//FIXME:这里有bug，如果设置false的话，在内存不足时会崩溃，需要注释掉configChanges="keyboardHidden才能重现这个崩溃
	private final static boolean DISABLE_GET_PAGE_LINKS = true;
	
	//另一个hasChange的判断条件，避免异步退出时还没写入批注
	public boolean isModified = false; 
	public String mFilename = null;
	
	static {
		if (D) {
			Log.e(TAG, "Loading dll");
		}
		System.loadLibrary("mupdf_java");
		if (D) {
			Log.e(TAG, "Loaded dll");
		}
	}

	private int numPages = -1;
	private float pageWidth;
	private float pageHeight;
	private float[] bounds = new float[4]; //FIXME:新增
	private volatile long globals; //FIXME:volatile的作用是如果多个线程同时读这个变量时不会读到缓存值（改变之前的值）
	private String file_format;
	private boolean isUnencryptedPDF;

	private native long openFile(String filename);
	private native String fileFormatInternal();
	private native boolean isUnencryptedPDFInternal();
	private native int countPagesInternal();
	//FIXME:注意很多JNI都是依赖于这个API才能工作
	private native void gotoPageInternal(int localActionPageNum);
	//FIXME:新增,这个函数有问题，会导致获取到bounds变成0，
	//可能是内部出现错误，也可能是线程问题，暂时用getBounds2代替
	private native void getBoundsInternal(float[] bounds);
	private native float getPageWidth();
	private native float getPageHeight();
	//FIXME:绘画
	private native void drawPage(Bitmap bitmap, int pageW, int pageH,
			int patchX, int patchY, int patchW, int patchH, long cookiePtr); 
	//FIXME:绘画
	private native void updatePageInternal(Bitmap bitmap, int page, int pageW,
			int pageH, int patchX, int patchY, int patchW, int patchH,
			long cookiePtr);
	private native RectF[] searchPage(String text);
	private native TextChar[][][][] text();
	private native void addMarkupAnnotationInternal(PointF[] quadPoints, int type);
	private native void addInkAnnotationInternal(PointF[][] arcs);
	private native void deleteAnnotationInternal(int annot_index);
	private native LinkInfo[] getPageLinksInternal(int page, boolean isscale);
	private native Annotation[] getAnnotationsInternal(int page);	
	private native OutlineItem[] getOutlineInternal();
	private native boolean hasOutlineInternal();
	private native boolean needsPasswordInternal();
	private native void destroying();
	private native boolean hasChangesInternal();
	private native void saveInternal();
	private native long createCookie();
	private native void destroyCookie(long cookie);
	private native void abortCookie(long cookie);
	//FIXME:新增
	private native void getBounds2(int pagenum, float[] bounds);
	//FIXME:新增
	private native void renderPage2(int pagenum, int[] viewboxarray, float[] matrixarray, int[] bufferarray);
	//FIXME:新增 
	//FIXME:这个方法曾经卡死（向前搜索），可能是多线程问题
	private native List<PageTextBox2> search2(int pagenum, String pattern);
	
	public synchronized void renderPage(int pagenum, int[] viewboxarray, float[] matrixarray, int[] bufferarray) {
		if (D) {
			Log.e(TAG, "renderPage" + ",hashcode==" + this.hashCode());
		}
		//FIXME:这个方法存在异步问题，如果不加synchronized会导致C代码崩溃
		renderPage2(pagenum, viewboxarray, matrixarray, bufferarray);
	}
	
	public synchronized List<PageTextBox2> search(int pagenum, String pattern) {
		if (D) {
			Log.e(TAG, "search" + ",hashcode==" + this.hashCode());
		}
		return search2(pagenum, pattern);		
	}
	
	public class Cookie {
		private final long cookiePtr;

		public Cookie() {
			cookiePtr = createCookie();
			if (cookiePtr == 0) {
				throw new OutOfMemoryError();
			}
		}

		public void abort() {
			abortCookie(cookiePtr);
		}

		//FIXME:无法保证在core销毁时释放，所以只能在这里手工释放
		public void destroy() {
			destroyCookie(cookiePtr);
		}
	}

	public MuPDFCore() {
		if (D) {
			Log.e(TAG, "MuPDFCore()" + ",hashcode==" + this.hashCode());
		}
	}

	public MuPDFCore(String filename) throws Exception {
		if (D) {
			Log.e(TAG, "MuPDFCore(filename)" + ",hashcode==" + this.hashCode());
		}
		globals = openFile(filename);
		if (globals == 0) {
			throw new Exception(String.format("无法打开文件：%s", filename));
		}
		mFilename = filename;
		file_format = fileFormatInternal();
		isUnencryptedPDF = isUnencryptedPDFInternal();
	}
	
	public synchronized void reload() throws Exception {
		if (D) {
			Log.e(TAG, "reload " + ",hashcode==" + this.hashCode());
		}
		if (DISABLE_RELOAD) {
			return;
		} else {
			onDestroy();
			if (D) {
				Log.e(TAG, "reload pre globals == " + globals);
			}
			globals = openFile(mFilename);
			if (D) {
				Log.e(TAG, "reload globals == " + globals);
			}
			if (globals == 0) {
				throw new Exception(String.format("无法打开文件：%s", mFilename));
			}		
			file_format = fileFormatInternal();
			isUnencryptedPDF = isUnencryptedPDFInternal();
		}
	}

	public synchronized int countPages() {
		if (D) {
			Log.e(TAG, "countPages" + ",hashcode==" + this.hashCode());
		}
		if (numPages < 0) {
			numPages = countPagesSynchronized();
		}
		return numPages;
	}

	public synchronized String fileFormat() {
		if (D) {
			Log.e(TAG, "fileFormat" + ",hashcode==" + this.hashCode());
		}
		return file_format;
	}

	public synchronized boolean isUnencryptedPDF() {
		if (D) {
			Log.e(TAG, "isUnencryptedPDF" + ",hashcode==" + this.hashCode());
		}
		return isUnencryptedPDF;
	}

	private synchronized int countPagesSynchronized() {
		if (D) {
			Log.e(TAG, "countPagesSynchronized" + ",hashcode==" + this.hashCode());
		}
		return countPagesInternal();
	}

	/**
	 * FIXME:注意很多API依赖于这个API才能工作，
	 * 而且这是耗时操作
	 * （准确来说应该是加载页面而不是跳转这么简单）
	 * @param page
	 */
	private void gotoPage(int page) {
		if (D) {
			Log.e(TAG, "gotoPage 001 " + page + ",hashcode==" + this.hashCode());
		}
		if (page > numPages - 1) {
			page = numPages - 1;
		} else if (page < 0) {
			page = 0;
		}
		if (D) {
			Log.e(TAG, "gotoPage 002");
		}
		gotoPageInternal(page);
		if (D) {
			Log.e(TAG, "gotoPage 003");
		}
		this.pageWidth = getPageWidth();
		this.pageHeight = getPageHeight();
		if (D) {
			Log.e(TAG, "gotoPage 004 size = " + this.pageWidth + ", " + this.pageHeight);
		}
		//FIXME:在这里执行getBoundsInternal(this.bounds);
		//但因为有bug所以移除了
		if (D) {
			Log.e(TAG, "gotoPage 005 bounds" + Arrays.toString(bounds));
		}
		if (D) {
			Log.e(TAG, "==================gotoPage = " + Arrays.toString(bounds));
		}
	}

	//FIXME:注意这个是耗时操作
	public synchronized PointF getPageSize(int page) {
		if (D) {
			Log.e(TAG, "getPageSize" + ",hashcode==" + this.hashCode());
		}
		gotoPage(page);
		return new PointF(pageWidth, pageHeight);
	}

	//FIXME:新增，注意C代码这里没有做缩放
	//FIXME:请不要在这里调用getBoundsInternal，应该在gotoPage执行，这里只是读出缓存值
	public synchronized RectF getBounds(int page) {
		if (D) {
			Log.e(TAG, "getBoundsrect 000 " + Arrays.toString(bounds)  + ",hashcode==" + this.hashCode());
		}
		//FIXME:移除了gotoPage(page);
		getBounds2(page, bounds);
		if (D) {
			Log.e(TAG, "getBoundsrect 001 " + Arrays.toString(bounds));
		}
		return new RectF(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	
	public synchronized void onDestroy() {
		if (D) {
			Log.e(TAG, "onDestroy" + ",hashcode==" + this.hashCode());
		}
		destroying();
		globals = 0;
	}

	public synchronized void drawPage(Bitmap bm, int page, int pageW,
			int pageH, int patchX, int patchY, int patchW, int patchH,
			MuPDFCore.Cookie cookie) {
		if (D) {
			Log.e(TAG, "drawPage" + ",hashcode==" + this.hashCode());
		}
		gotoPage(page);
		drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr);
	}

	public synchronized void updatePage(Bitmap bm, int page, int pageW,
			int pageH, int patchX, int patchY, int patchW, int patchH,
			MuPDFCore.Cookie cookie) {
		if (D) {
			Log.e(TAG, "updatePage" + ",hashcode==" + this.hashCode());
		}
		updatePageInternal(bm, page, pageW, pageH, patchX, patchY, patchW,
				patchH, cookie.cookiePtr);
	}

	public synchronized LinkInfo[] getPageLinks(int page, boolean isscale) {
		if (D) {
			Log.e(TAG, "getPageLinks " + ",hashcode==" + this.hashCode());
		}
		if (DISABLE_GET_PAGE_LINKS) {
			return new LinkInfo[0];
		} else {
			return getPageLinksInternal(page, isscale);
		}
	}

	public synchronized Annotation[] getAnnoations(int page) {
		if (D) {
			Log.e(TAG, "getWidgetAreas" + ",hashcode==" + this.hashCode());
		}
		return getAnnotationsInternal(page);
	}

	public synchronized RectF[] searchPage(int page, String text) {
		if (D) {
			Log.e(TAG, "searchPage" + ",hashcode==" + this.hashCode());
		}
		gotoPage(page);
		return searchPage(text);
	}

	public synchronized TextWord[][] textLines(int page) {
		if (D) {
			Log.e(TAG, "textLines");
		}
		gotoPage(page);
		TextChar[][][][] chars = text();
		ArrayList<TextWord[]> lns = new ArrayList<TextWord[]>();
		for (TextChar[][][] bl : chars) {
			if (bl == null) {
				continue;
			}
			for (TextChar[][] ln : bl) {
				ArrayList<TextWord> wds = new ArrayList<TextWord>();
				TextWord wd = new TextWord();
				for (TextChar[] sp : ln) {
					for (TextChar tc : sp) {
						if (tc.c != ' ') {
							wd.Add(tc);
						} else if (wd.w.length() > 0) {
							wds.add(wd);
							wd = new TextWord();
						}
					}
				}
				if (wd.w.length() > 0) {
					wds.add(wd);
				}
				if (wds.size() > 0) {
					lns.add(wds.toArray(new TextWord[wds.size()]));
				}
			}
		}
		return lns.toArray(new TextWord[lns.size()][]);
	}

	public synchronized void addMarkupAnnotation(int page, PointF[] quadPoints, int type) {
		if (D) {
			Log.e(TAG, "addMarkupAnnotation");
		}
		gotoPage(page);
		addMarkupAnnotationInternal(quadPoints, type);
	}

	public synchronized void addInkAnnotation(int page, PointF[][] arcs) {
		if (D) {
			Log.e(TAG, "addInkAnnotation");
		}
		gotoPage(page);
		addInkAnnotationInternal(arcs);
	}

	public synchronized void deleteAnnotation(int page, int annot_index) {
		if (D) {
			Log.e(TAG, "deleteAnnotation");
		}
		gotoPage(page);
		deleteAnnotationInternal(annot_index);
	}

	public synchronized boolean hasOutline() {
		if (D) {
			Log.e(TAG, "hasOutline");
		}
		return hasOutlineInternal();
	}

	public synchronized OutlineItem[] getOutline() {
		if (D) {
			Log.e(TAG, "getOutline");
		}
		return getOutlineInternal();
	}

	public synchronized boolean needsPassword() {
		if (D) {
			Log.e(TAG, "needsPassword");
		}
		return needsPasswordInternal();
	}

	public synchronized boolean hasChanges() {
		if (D) {
			Log.e(TAG, "hasChanges");
		}
		return hasChangesInternal();
	}

	public synchronized void save() {
		if (D) {
			Log.e(TAG, "save");
		}
		saveInternal();
	}
}
