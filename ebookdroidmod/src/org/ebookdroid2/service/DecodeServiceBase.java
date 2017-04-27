package org.ebookdroid2.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.ebookdroid2.codec.CodecContext;
import org.ebookdroid2.codec.CodecDocument;
import org.ebookdroid2.codec.CodecFeatures;
import org.ebookdroid2.codec.CodecPage;
import org.ebookdroid2.codec.CodecPageHolder;
import org.ebookdroid2.codec.CodecPageInfo;
import org.ebookdroid2.codec.OutlineLink;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageTreeNode;
import org.ebookdroid2.page.PageTreeNodeComparator;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.artifex.mupdfdemo.MuPDFCore;

/**
 * FIXME:核心类
 * FIXME:这里的类需要分拆
 */
public class DecodeServiceBase implements DecodeService {
	private final static boolean D = false;
	private final static boolean DD = false;
	private final static String TAG = "DecodeServiceBase";
	private final static boolean USE_DRAW_DISABLE = false; //耗时间，正常的话设置为false
	
    private static final AtomicLong TASK_ID_SEQ = new AtomicLong();
    private final CodecContext codecContext;
    private final Executor executor = new Executor();
    private final AtomicBoolean isRecycled = new AtomicBoolean();
    private final AtomicReference<ViewState> viewState = new AtomicReference<ViewState>();
    private CodecDocument document;
    private final Map<Integer, CodecPageHolder> pages = new LinkedHashMap<Integer, CodecPageHolder>() {
        private static final long serialVersionUID = -8845124816503128098L;

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Integer, CodecPageHolder> eldest) {
            if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#removeEldestEntry");
            }
            if (this.size() > getCacheSize()) {
                final CodecPageHolder value = eldest != null ? eldest.getValue() : null;
                if (value != null) {
                    if (value.isInvalid(-1)) {
                        if (D) {
                            Log.e(TAG, Thread.currentThread().getName() + ": Remove auto-recycled codec page reference: "
                                    + eldest.getKey());
                        }
                        return true;
                    } else {
                        final boolean recycled = value.recycle(-1, false);
                        if (D) {
                            if (recycled) {
                            	Log.e(TAG, Thread.currentThread().getName() + ": Recycle and remove old codec page: "
                                        + eldest.getKey());
                            } else {
                            	Log.e(TAG, Thread.currentThread().getName()
                                        + ": Codec page locked and cannot be recycled: " + eldest.getKey());
                            }
                        }
                        return recycled;
                    }
                }
            }
            return false;
        }
    };

    public DecodeServiceBase(final CodecContext codecContext) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#DecodeServiceBase");
        }
        this.codecContext = codecContext;
    }

    @Override
    public boolean isFeatureSupported(final int feature) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#isFeatureSupported");
        }
        return codecContext.isFeatureSupported(feature);
    }

    @Override
    public int getPixelFormat() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getPixelFormat");
        }
        final Config cfg = getBitmapConfig();
        switch (cfg) {
        case ALPHA_8:
            return PixelFormat.A_8;
            
        case ARGB_4444:
            return PixelFormat.RGBA_4444;
            
        case RGB_565:
            return PixelFormat.RGB_565;
            
        case ARGB_8888:
            return PixelFormat.RGBA_8888;
            
        default:
            return PixelFormat.RGB_565;
        }
    }

    @Override
    public Config getBitmapConfig() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getBitmapConfig");
        }
        return this.codecContext.getBitmapConfig();
    }

    @Override
    public void open(MuPDFCore _core) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#open");
        }
        document = codecContext.openDocument(_core);
    }

    @Override
    public CodecPageInfo getUnifiedPageInfo() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getUnifiedPageInfo");
        }
        return document != null ? document.getUnifiedPageInfo() : null;
    }

    @Override
    public CodecPageInfo getPageInfo(final int pageIndex) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getPageInfo");
        }
        return document != null ? document.getPageInfo(pageIndex) : null;
    }

    @Override
    public void updateViewState(final ViewState viewState) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#updateViewState");
        }
        this.viewState.set(viewState);
    }

    @Override
    public void searchText(final Page page, final String pattern, final SearchCallback callback) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#searchText");
        }
        if (isRecycled.get()) {
            if (D) {
            	Log.e(TAG, "Searching not allowed on recycling");
            }
            return;
        }
        final SearchTask decodeTask = new SearchTask(page, pattern, callback);
        executor.add(decodeTask);
    }

    @Override
    public void stopSearch(final String pattern) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#stopSearch");
        }
        executor.stopSearch(pattern);
    }

    @Override
    public void decodePage(final ViewState viewState, final PageTreeNode node) {
    	//FIXME:解码线程
		if (D && DD) {
    		Log.e(TAG, ">>>DecodeServiceBase#decodePage", new Exception());
    	}
        if (isRecycled.get()) {
            if (D) {
            	Log.e(TAG, "Decoding not allowed on recycling");
            }
            return;
        }
        final DecodeTask decodeTask = new DecodeTask(viewState, node);
        updateViewState(viewState);
        executor.add(decodeTask);
    }

    @Override
    public void stopDecoding(final PageTreeNode node, final String reason) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#stopDecoding");
        }
        executor.stopDecoding(null, node, reason);
    }

    //FIXME:这个函数需要重要看
    //涉及图片剪裁（已经被删除）
    private void performDecode(final DecodeTask task) {
        if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#performDecode");
        }
        if (executor.isTaskDead(task)) {
            if (D) {
            	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Skipping dead decode task for "
                        + task.node);
            }
            return;
        }
        if (D) {
        	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Starting decoding for " + task.node);
        }
        CodecPageHolder holder = null;
        CodecPage vuPage = null;
        Rect r = null;
        RectF croppedPageBounds = null;
        try {
            holder = getPageHolder(task.id, task.pageNumber);
            vuPage = holder.getPage(task.id);
            if (executor.isTaskDead(task)) {
                if (D) {
                	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Abort dead decode task for "
                            + task.node);
                }
                return;
            }
            //
            //FIXME:这里原来是用于执行剪裁，现在没用了
            //考虑删除crop相关代码
            //shouldCrop,getCropping,
            //calculateNodeCropping,croppedPageBounds
            //
            if (executor.isTaskDead(task)) {
                if (D) {
                	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Abort dead decode task for "
                            + task.node);
                }
                return;
            }
            r = getScaledSize(task.node, task.viewState.zoom, croppedPageBounds, vuPage);
            if (D) {
            	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Rendering rect: " + r);
            }
            RectF cropping = task.node.page.getCropping(task.node);
            final RectF actualSliceBounds = cropping != null ? cropping : task.node.pageSliceBounds;
            final BitmapRef bitmap = vuPage.renderBitmap(task.viewState, r.width(), r.height(), actualSliceBounds);
            if (executor.isTaskDead(task)) {
                if (D) {
                	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Abort dead decode task for "
                            + task.node);
                }
                BitmapManager.release(bitmap);
                return;
            }
            if (task.node.page.links == null) {
                task.node.page.links = vuPage.getPageLinks();
                if (task.node.page.links != null && 
                	!task.node.page.links.isEmpty()) {
                    if (D) {
                    	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Found links on page "
                                + task.pageNumber + ": " + task.node.page.links);
                    }
                }
            }
            finishDecoding(task, vuPage, bitmap, r, croppedPageBounds);
        } catch (final OutOfMemoryError ex) {
        	ex.printStackTrace();
            if (D) {
            	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": No memory to decode " + task.node);
            }
            for (int i = 0; i <= AppSettings.current().pagesInMemory; i++) {
                pages.put(Integer.MAX_VALUE - i, null);
            }
            pages.clear();
            if (vuPage != null) {
                vuPage.recycle();
            }
            BitmapManager.clear("DecodeService OutOfMemoryError: ");
            abortDecoding(task, null, null);
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, Thread.currentThread().getName() + ": Task " + task.id + ": Decoding failed for " + task.node + ": "
                    + th.getMessage(), th);
            }
            abortDecoding(task, vuPage, null);
        } finally {
            if (holder != null) {
                holder.unlock();
            }
        }
    }

    private Rect getScaledSize(final PageTreeNode node, final float zoom, final RectF croppedPageBounds, final CodecPage vuPage) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getScaledSize");
        }
        final RectF pageBounds = zoom(croppedPageBounds != null ? croppedPageBounds : node.page.bounds, zoom);
        final RectF r = Page.getTargetRect(node.page.type, pageBounds, node.pageSliceBounds);
        return new Rect(0, 0, (int) r.width(), (int) r.height());
    }
    
    private static RectF zoom(final RectF rect, final float zoom) {
        return new RectF(zoom * rect.left, zoom * rect.top, zoom * rect.right, zoom * rect.bottom);
    }

    private void finishDecoding(final DecodeTask currentDecodeTask, final CodecPage page, final BitmapRef bitmap,
            final Rect bitmapBounds, final RectF croppedPageBounds) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#finishDecoding");
        }
        stopDecoding(currentDecodeTask.node, "complete");
        updateImage(currentDecodeTask, page, bitmap, bitmapBounds, croppedPageBounds);
    }

    private void abortDecoding(final DecodeTask currentDecodeTask, final CodecPage page, final BitmapRef bitmap) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#abortDecoding");
        }
        stopDecoding(currentDecodeTask.node, "failed");
        updateImage(currentDecodeTask, page, bitmap, null, null);
    }

    private CodecPage getPage(final int pageIndex) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getPage(pageIndex)");
        }
        return getPageHolder(-2, pageIndex).getPage(-2);
    }

    private synchronized CodecPageHolder getPageHolder(final long taskId, final int pageIndex) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getPageHolder");
        }
        if (D) {
        	Log.e(TAG, Thread.currentThread().getName() + "Task " + taskId + ": Codec pages in cache: " + pages.size());
        }
        for (final Iterator<Map.Entry<Integer, CodecPageHolder>> i = pages.entrySet().iterator(); i.hasNext();) {
            final Map.Entry<Integer, CodecPageHolder> entry = i.next();
            final int index = entry.getKey();
            final CodecPageHolder ref = entry.getValue();
            if (ref.isInvalid(-1)) {
                if (D) {
                	Log.e(TAG, Thread.currentThread().getName() + "Task " + taskId
                            + ": Remove auto-recycled codec page reference: " + index);
                }
                i.remove();
            }
        }
        CodecPageHolder holder = pages.get(pageIndex);
        if (holder == null) {
            holder = new CodecPageHolder(document, pageIndex);
            pages.put(pageIndex, holder);
        }
        //FIXME:防止MuPDF里面的问题
        if (!codecContext.isFeatureSupported(CodecFeatures.FEATURE_PARALLEL_PAGE_ACCESS)) {
            holder.getPage(taskId);
        }
        return holder;
    }

    private void updateImage(final DecodeTask currentDecodeTask, final CodecPage page, final BitmapRef bitmap,
            final Rect bitmapBounds, final RectF croppedPageBounds) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#updateImage");
        }
        currentDecodeTask.node.decodeComplete(page, bitmap, bitmapBounds, croppedPageBounds);
    }

    @Override
    public int getPageCount() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getPageCount");
        }
        return document != null ? document.getPageCount() : 0;
    }

    @Override
    public List<OutlineLink> getOutline() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getOutline");
        }
        return document != null ? document.getOutline() : null;
    }

    @Override
    public void recycle() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#recycle");
        }
        if (isRecycled.compareAndSet(false, true)) {
            executor.recycle();
        }
    }

    protected int getCacheSize() {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#getCacheSize");
        }
        final ViewState vs = viewState.get();
        int minSize = 1;
        if (vs != null) {
            minSize = vs.pages.lastVisible - vs.pages.firstVisible + 1;
        }
        final int pagesInMemory = AppSettings.current().pagesInMemory;
        return pagesInMemory == 0 ? 0 : Math.max(minSize, pagesInMemory);
    }

    //FIXME:考虑独立这个类出去
    private class Executor implements Runnable {
        final Map<PageTreeNode, DecodeTask> decodingTasks = new IdentityHashMap<PageTreeNode, DecodeTask>();
        final ArrayList<Task> tasks;
        final Thread[] threads;
        final ReentrantLock lock = new ReentrantLock();
        final AtomicBoolean run = new AtomicBoolean(true);

        Executor() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor");
            }
            tasks = new ArrayList<Task>();
            threads = new Thread[AppSettings.current().decodingThreads];
            if (D) {
            	Log.e(TAG, "Number of decoding threads: " + threads.length);
            }
            final int decodingThreadPriority = AppSettings.current().decodingThreadPriority;
            if (D) {
            	Log.e(TAG, "Decoding thread priority: " + decodingThreadPriority);
            }
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(this, "DecodingThread-" + i);
                threads[i].setPriority(decodingThreadPriority);
                threads[i].start();
            }
        }

        @Override
        public void run() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#run");
            }
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#run loop start===============> 1");
            }
        	try {
                while (run.get()) {
                	if (!USE_DRAW_DISABLE) {
	                	//FIXME:这里耗时间
	                	if (D) {
	                    	Log.e(TAG, ">>>DecodeServiceBase#Executor#run loop nextTask==========>>>>======== 2");
	                    }
	                    final Runnable r = nextTask();
	                    if (r != null) {
	                    	if (D) {
	                        	Log.e(TAG, ">>>DecodeServiceBase#Executor#run loop nextTask==========>>>>======== run 3");
	                        }
	                        BitmapManager.release();
	                        r.run();
	                    }
	                    if (D) {
	                    	Log.e(TAG, ">>>DecodeServiceBase#Executor#run loop nextTask==========>>>>======== over 4");
	                    }
                	}
                }
            } catch (final Throwable th) {
                th.printStackTrace();
            	if (D) {
            		Log.e(TAG, Thread.currentThread().getName() + ": Decoding service executor failed: " + th.getMessage(), th);
            	}
            	//FIXME:这里打印异常到日志文件
            } finally {
                BitmapManager.release();
            }
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#run loop end<=============== 5");
            }
        }

        Runnable nextTask() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#nextTask");
            }
            lock.lock();
            try {
                if (!tasks.isEmpty()) {
                    final TaskComparator comp = new TaskComparator(viewState.get());
                    Task candidate = null;
                    int cindex = 0;
                    int index = 0;
                    while (index < tasks.size() && candidate == null) {
                        candidate = tasks.get(index);
                        cindex = index;
                        index++;
                    }
                    if (candidate == null) {
                        if (D) {
                        	Log.e(TAG, Thread.currentThread().getName() + ": No tasks in queue");
                        }
                        tasks.clear();
                    } else {
                        while (index < tasks.size()) {
                            final Task next = tasks.get(index);
                            if (next != null && comp.compare(next, candidate) < 0) {
                                candidate = next;
                                cindex = index;
                            }
                            index++;
                        }
                        if (D) {
                        	Log.e(TAG, Thread.currentThread().getName() + ": <<<: " + cindex + "/" + tasks.size() + ": "
                                    + candidate);
                        }
                        tasks.set(cindex, null);
                    }
                    return candidate;
                }
            } finally {
                lock.unlock();
            }
            synchronized (run) {
                try {
                    run.wait(60000);
                } catch (final InterruptedException ex) {
                    Thread.interrupted();
                }
            }
            return null;
        }

        public void add(final SearchTask task) {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#add");
            }
            if (D) {
            	Log.e(TAG, Thread.currentThread().getName() + ": Adding search task: " + task + " for " + task.page.index);
            }
            lock.lock();
            try {
                boolean added = false;
                for (int index = 0; index < tasks.size(); index++) {
                    if (null == tasks.get(index)) {
                        tasks.set(index, task);
                        if (D) {
                        	Log.e(TAG, Thread.currentThread().getName() + ": >>>: " + index + "/" + tasks.size() + ": "
                                    + task);
                        }
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    if (D) {
                    	Log.e(TAG, Thread.currentThread().getName() + ": +++: " + tasks.size() + "/" + tasks.size() + ": "
                                + task);
                    }
                    tasks.add(task);
                }
                synchronized (run) {
                    run.notifyAll();
                }
            } finally {
                lock.unlock();
            }
        }

        public void stopSearch(final String pattern) {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#stopSearch");
            }
            if (D) {
            	Log.e(TAG, "Stop search tasks: " + pattern);
            }
            lock.lock();
            try {
                for (int index = 0; index < tasks.size(); index++) {
                    final Task task = tasks.get(index);
                    if (task instanceof SearchTask) {
                        final SearchTask st = (SearchTask) task;
                        if (st.pattern.equals(pattern)) {
                            tasks.set(index, null);
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void add(final DecodeTask task) {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task)");
            }
            if (D) {
            	Log.e(TAG, "Adding decoding task: " + task + " for " + task.node);
            }
            lock.lock();
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 1 start==========>");
            }
            try {
                final DecodeTask running = decodingTasks.get(task.node);
                if (running != null && running.equals(task) && !isTaskDead(running)) {
                    if (D) {
                    	Log.e(TAG, "The similar task is running: " + running.id + " for " + task.node);
                    }
                    return;
                } else if (running != null) {
                    if (D) {
                    	Log.e(TAG, "The another task is running: " + running.id + " for " + task.node);
                    }
                }
                if (D) {
                	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 2");
                }
                decodingTasks.put(task.node, task);
                if (D) {
                	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 3");
                }
                boolean added = false;
                for (int index = 0; index < tasks.size(); index++) {
                    if (null == tasks.get(index)) {
                        tasks.set(index, task);
                        if (D) {
                        	Log.e(TAG, ">>>: " + index + "/" + tasks.size() + ": " + task);
                        }
                        added = true;
                        break;
                    }
                }
                if (D) {
                	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 3");
                }
                if (!added) {
                    if (D) {
                    	Log.e(TAG, "+++: " + tasks.size() + "/" + tasks.size() + ": " + task);
                    }
                    tasks.add(task);
                }
                if (D) {
                	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 4");
                }
                synchronized (run) {
                    run.notifyAll();
                }
                if (D) {
                	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 5");
                }
                if (running != null) {
                    stopDecoding(running, null, "canceled by new one");
                }
                if (D) {
                	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 6");
                }
            } finally {
                lock.unlock();
            }
            if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#add(task) 7 end<==========");
            }
        }

        public void stopDecoding(final DecodeTask task, final PageTreeNode node, final String reason) {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#stopDecoding");
            }
            lock.lock();
            try {
                final DecodeTask removed = task == null ? decodingTasks.remove(node) : task;
                if (removed != null) {
                    removed.cancelled.set(true);
                    for (int i = 0; i < tasks.size(); i++) {
                        if (removed == tasks.get(i)) {
                            if (D) {
                            	Log.e(TAG, "---: " + i + "/" + tasks.size() + " " + removed);
                            }
                            tasks.set(i, null);
                            break;
                        }
                    }
                    if (D) {
                    	Log.e(TAG, Thread.currentThread().getName() + ": Task " + removed.id
                                + ": Stop decoding task with reason: " + reason + " for " + removed.node);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public boolean isTaskDead(final DecodeTask task) {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#isTaskDead");
            }
            return task.cancelled.get();
        }

        public void recycle() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#recycle");
            }
            lock.lock();
            try {
                for (final DecodeTask task : decodingTasks.values()) {
                    stopDecoding(task, null, "recycling");
                }
                tasks.add(new ShutdownTask());
                synchronized (run) {
                    run.notifyAll();
                }
            } finally {
                lock.unlock();
            }
        }

        void shutdown() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Executor#shutdown");
            }
            for (final CodecPageHolder ref : pages.values()) {
                ref.recycle(-3, true);
            }
            pages.clear();
            if (document != null) {
                document.recycle();
            }
            codecContext.recycle();
            run.set(false);
        }
    }

    class TaskComparator implements Comparator<Task> {
        final PageTreeNodeComparator cmp;

        public TaskComparator(final ViewState viewState) {
            cmp = viewState != null ? new PageTreeNodeComparator(viewState) : null;
        }

        @Override
        public int compare(final Task r1, final Task r2) {
            if (r1.priority < r2.priority) {
                return -1;
            }
            if (r2.priority < r1.priority) {
                return +1;
            }
            if (r1 instanceof DecodeTask && r2 instanceof DecodeTask) {
                final DecodeTask t1 = (DecodeTask) r1;
                final DecodeTask t2 = (DecodeTask) r2;
                if (cmp != null) {
                    return cmp.compare(t1.node, t2.node);
                }
                return 0;
            }
            return compareLong(r1.id, r2.id);
        }
        
    	public int compareLong(final long val1, final long val2) {
    		return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
    	}
    }

    abstract class Task implements Runnable {
        final long id = TASK_ID_SEQ.incrementAndGet();
        final AtomicBoolean cancelled = new AtomicBoolean();
        final int priority;

        Task(final int priority) {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#Task");
            }
            this.priority = priority;
        }
    }

    class ShutdownTask extends Task {
        public ShutdownTask() {
        	super(0);
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#ShutdownTask");
            }
        }

        @Override
        public void run() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#ShutdownTask#run");
            }
        	executor.shutdown();
        }
    }

    class SearchTask extends Task {
        final Page page;
        final String pattern;
        final SearchCallback callback;

        public SearchTask(final Page page, final String pattern, final SearchCallback callback) {
            super(1);
            this.page = page;
            this.pattern = pattern;
            this.callback = callback;
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#SearchTask");
            }
        }

        @Override
        public void run() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#SearchTask#run");
            }
            List<? extends RectF> regions = null;
            if (document != null) {
                try {
                    if (codecContext.isFeatureSupported(CodecFeatures.FEATURE_DOCUMENT_TEXT_SEARCH)) {
                        regions = document.searchText(page.index.docIndex, pattern);
                    } else if (codecContext.isFeatureSupported(CodecFeatures.FEATURE_PAGE_TEXT_SEARCH)) {
                        regions = getPage(page.index.docIndex).searchText(pattern);
                    }
                    callback.searchComplete(page, regions);
                } catch (final Throwable th) {
                    th.printStackTrace();
                	if (D) {
                		Log.e(TAG, "Unexpected error: ", th);
                	}
                    callback.searchComplete(page, null);
                }
            }
        }
    }

    class DecodeTask extends Task {
        final long id = TASK_ID_SEQ.incrementAndGet();
        final AtomicBoolean cancelled = new AtomicBoolean();

        final PageTreeNode node;
        final ViewState viewState;
        final int pageNumber;

        DecodeTask(final ViewState viewState, final PageTreeNode node) {
            super(2);
            this.pageNumber = node.page.index.docIndex;
            this.viewState = viewState;
            this.node = node;
            if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#DecodeTask");
            }
        }

        @Override
        public void run() {
        	if (D) {
            	Log.e(TAG, ">>>DecodeServiceBase#DecodeTask#run");
            }
            performDecode(this);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof DecodeTask) {
                final DecodeTask that = (DecodeTask) obj;
                return this.pageNumber == that.pageNumber
                        && this.viewState.viewRect.width() == that.viewState.viewRect.width()
                        && this.viewState.zoom == that.viewState.zoom;
            }
            return false;
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder("DecodeTask");
            buf.append("[");
            buf.append("id").append("=").append(id);
            buf.append(", ");
            buf.append("target").append("=").append(node);
            buf.append(", ");
            buf.append("width").append("=").append((int) viewState.viewRect.width());
            buf.append(", ");
            buf.append("zoom").append("=").append(viewState.zoom);
            buf.append("]");
            return buf.toString();
        }
    }

    @Override
    public BitmapRef createThumbnail(boolean useEmbeddedIfAvailable, int width, int height, final int pageNo,
            final RectF region) {
    	if (D) {
        	Log.e(TAG, ">>>DecodeServiceBase#createThumbnail");
        }
        if (document == null) {
            return null;
        }
        final Bitmap thumbnail = useEmbeddedIfAvailable ? document.getEmbeddedThumbnail() : null;
        if (thumbnail != null) {
            width = 200;
            height = 200;
            final int tw = thumbnail.getWidth();
            final int th = thumbnail.getHeight();
            if (th > tw) {
                width = width * tw / th;
            } else {
                height = height * th / tw;
            }
            final Bitmap scaled = Bitmap.createScaledBitmap(thumbnail, width, height, true);
            final BitmapRef ref = BitmapManager.addBitmap("Thumbnail", scaled);
            return ref;
        } else {
            final CodecPage page = getPage(pageNo);
            return page.renderBitmap(null, width, height, region);
        }
    }
}
