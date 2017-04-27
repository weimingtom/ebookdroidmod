package org.ebookdroid2.view;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.core.IViewController;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.util.Flag;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class ScrollEventThread extends Thread {
	private final static boolean D = false;
	private final static String TAG = "ScrollEventThread";
	
	//FIXME:计算耗时
    private final static boolean CALC_TIME = false; 
	
	private static boolean mergeEvents = false;
    private static Field SCROLL_X;
    private static Field SCROLL_Y;
    static {
        final Field[] fields = View.class.getDeclaredFields();
        for (final Field f : fields) {
            if ("mScrollX".equals(f.getName())) {
                SCROLL_X = f;
                SCROLL_X.setAccessible(true);
            } else if ("mScrollY".equals(f.getName())) {
                SCROLL_Y = f;
                SCROLL_Y.setAccessible(true);
            }
        }
    }
    
    private final IActivityController base;
    private final IView view;
    private final Flag stop = new Flag();
    private final BlockingQueue<OnScrollEvent> queue = new LinkedBlockingQueue<OnScrollEvent>();
    private final ConcurrentLinkedQueue<OnScrollEvent> pool = new ConcurrentLinkedQueue<OnScrollEvent>();

    public ScrollEventThread(final IActivityController base, IView view) {
        super("ScrollEventThread");
        this.base = base;
        this.view = view;
    }

    @Override
    public void run() {
        while (!stop.get()) {
            try {
                final OnScrollEvent event = 
                	queue.poll(1, TimeUnit.SECONDS);
                if (event == null) {
                    continue;
                }
                if (mergeEvents) {
                    for (OnScrollEvent event1 = queue.poll(); 
                    	event1 != null; 
                    	event1 = queue.poll()) {
                        event.reuse(event1.m_curX, event1.m_curY, 
                        	event.m_oldX, event.m_oldY);
                        pool.add(event1);
                    }
                }
                process(event);
            } catch (final InterruptedException e) {
                Thread.interrupted();
            } catch (final Throwable th) {
                th.printStackTrace();
            }
        }
        if (D) {
        	Log.e(TAG, "ScrollEventThread.run(): finished");
        }
    }

    public void finish() {
        stop.set();
    }

    public void scrollTo(final int x, final int y) {
        final IViewController dc = base.getDocumentController();
        final DocumentModel dm = base.getDocumentModel();
        final View w = view.getView();
    
        if (dc != null && dm != null) {
            final Rect l = dc.getScrollLimits();
            final int xx = adjust(x, l.left, l.right);
            final int yy = adjust(y, l.top, l.bottom);
    
            if (SCROLL_X == null || SCROLL_Y == null) {
                try {
                    view._scrollTo(xx, yy);
                } catch (Throwable th) {
                    System.err.println("(1) " + th.getMessage());
                }
            } else {
                try {
                    int mScrollX = SCROLL_X.getInt(w);
                    int mScrollY = SCROLL_Y.getInt(w);
                    if (mScrollX != xx || mScrollY != yy) {
                        int oldX = mScrollX;
                        int oldY = mScrollY;
                        SCROLL_X.setInt(w, xx);
                        SCROLL_Y.setInt(w, yy);
                        view.onScrollChanged(mScrollX, mScrollY, oldX, oldY);
                    }
                } catch (Throwable th) {
                    System.err.println("(2) " + th.getMessage());
                    try {
                        view._scrollTo(xx, yy);
                    } catch (Throwable thh) {
                        System.err.println("(3) " + thh.getMessage());
                    }
                }
            }
        }
    }

    private static int adjust(final int value, final int min, final int max) {
        return Math.min(Math.max(min, value), max);
    }
    
    public void onScrollChanged(final int curX, final int curY, final int oldX, final int oldY) {
        OnScrollEvent event = pool.poll();
        if (event != null) {
            event.reuse(curX, curY, oldX, oldY);
        } else {
            event = new OnScrollEvent(curX, curY, oldX, oldY);
        }
        queue.offer(event);
    }

    private void process(final OnScrollEvent event) {
        long t1 = 0;
        if (CALC_TIME) {
    		t1 = System.currentTimeMillis();
    	}
    	try {
            final int dX = event.m_curX - event.m_oldX;
            final int dY = event.m_curY - event.m_oldY;
            base.getDocumentController().onScrollChanged(dX, dY);
        } catch (final Throwable th) {
            th.printStackTrace();
        } finally {
            pool.add(event);
            if (CALC_TIME) {
	            final long t2 = System.currentTimeMillis();
	            Log.e(TAG,
	            	"ScrollEventThread.onScrollChanged(): " + 
	            		(t2 - t1) + " ms, " + pool.size());
            }
        }
    }

    private final static class OnScrollEvent {
        int m_oldX;
        int m_curY;
        int m_curX;
        int m_oldY;

        public OnScrollEvent(final int curX, final int curY, 
        		final int oldX, final int oldY) {
            reuse(curX, curY, oldX, oldY);
        }

        void reuse(final int curX, final int curY, 
        		final int oldX, final int oldY) {
            m_oldX = oldX;
            m_curY = curY;
            m_curX = curX;
            m_oldY = oldY;
        }
    }
}
