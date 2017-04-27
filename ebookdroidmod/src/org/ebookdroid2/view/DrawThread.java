package org.ebookdroid2.view;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.util.Flag;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class DrawThread extends Thread {
	private final static boolean D = false;
	private final static boolean DD = false;
	private final static String TAG = "DrawThread";
	private final static boolean USE_SLEEP = false; //FIXME:使用Sleep
	private final static boolean CALC_TIME = false; //FIXME:计算耗时
	
    private final SurfaceHolder surfaceHolder;
    private final BlockingQueue<ViewState> queue = new ArrayBlockingQueue<ViewState>(16, true);
    private final ArrayList<ViewState> list = new ArrayList<ViewState>();
    private final Flag stop = new Flag();
    private Context mContext;

    public DrawThread(final SurfaceHolder surfaceHolder, Context context) {
        this.surfaceHolder = surfaceHolder;
        this.mContext = context;
    }

    public void finish() {
        stop.set();
        try {
            this.join();
        } catch (final InterruptedException e) {
        }
    }

    @Override
    public void run() {
    	//FIXME:这里打印绘画线程
        if (D && DD) {
        	Log.e(TAG, ">>>DrawThread#run()");
        }
    	if (D && DD) {
        	Log.e(TAG, ">>>DrawThread#run() loop ================> 1 start");
        }
        while (!stop.get()) {
        	if (D && DD) {
            	Log.e(TAG, ">>>DrawThread#run() loop ================> 2");
            }
        	draw(false);
        	if (USE_SLEEP) {
	            try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	if (D && DD) {
            	Log.e(TAG, ">>>DrawThread#run() loop ================> 3");
            }
        }
    	if (D && DD) {
        	Log.e(TAG, ">>>DrawThread#run() loop <================ 4 end");
        }
    }

    protected void draw(final boolean useLastState) {
    	//FIXME:绘画线程2
        if (D && DD) {
        	Log.e(TAG, ">>>DrawThread#draw()");
        }
        final ViewState viewState = takeTask(1, TimeUnit.SECONDS, useLastState);
        if (viewState == null) {
            return;
        }
        Canvas canvas = null;
        long t1 = 0;
        if (CALC_TIME) {
        	t1 = System.currentTimeMillis();
        }
        try {
            canvas = surfaceHolder.lockCanvas(null);
            //FIXME:SurfaceView的绘画入口(消费者)
            EventPool.newEventDraw(viewState, canvas, this.mContext).process().releaseAfterDraw();
        } catch (final Throwable th) {
        	th.printStackTrace();
        	if (D) {
        		Log.e(TAG, "Unexpected error on drawing: " + th.getMessage(), th);
        	}
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
        if (CALC_TIME) {
            long t2 = System.currentTimeMillis();
            if (D) {
            	Log.e(TAG, "DrawThread.draw(): " + (t2 - t1) + " ms");
            }
        }
    }

    public ViewState takeTask(final long timeout, final TimeUnit unit, final boolean useLastState) {
        ViewState task = null;
        try {
            task = queue.poll(timeout, unit);
            if (task != null && useLastState) {
                //FIXME:防止出现ConcurrentModificationException异常
            	while (true) {
                    list.clear();
                    try {
                        if (queue.drainTo(list) > 0) {
                            final int last = list.size() - 1;
                            task = list.get(last);
                            for (int i = 0; i < last; i++) {
                                final ViewState vs = list.get(i);
                                if (vs != null) {
                                    vs.releaseAfterDraw();
                                }
                            }
                        }
                        break;
                    } catch (final Throwable ex) {
                    	ex.printStackTrace();
                        if (D) {
                        	Log.e(TAG, "Unexpected error on retrieving last view state from draw queue: " + ex.getMessage());
                        }
                    }
                }
            }
        } catch (final InterruptedException e) {
        	e.printStackTrace();
            Thread.interrupted();
        } catch (final Throwable ex) {
        	ex.printStackTrace();
            if (D) {
            	Log.e(TAG, "Unexpected error on retrieving view state from draw queue: " + ex.getMessage());
            }
        }
        return task;
    }

    public void draw(final ViewState viewState) {
        if (D) {
        	Log.e(TAG, ">>>DrawThread#draw(viewState) : producer : queue.size(): " + queue.size());
        }
        if (viewState != null) {
            //FIXME:防止出现ConcurrentModificationException
        	viewState.addedToDrawQueue();
            while (true) {
                try {
                    queue.offer(viewState);
                    break;
                } catch (final Throwable ex) {
                	ex.printStackTrace();
                    if (D) {
                    	Log.e(TAG, "Unexpected error on adding view state to draw queue: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
