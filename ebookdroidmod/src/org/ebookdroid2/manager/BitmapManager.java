package org.ebookdroid2.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.ebookdroid2.util.ArrayDeque;
import org.ebookdroid2.util.SparseArrayEx;
import org.ebookdroid2.util.TLIterator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;

/*
BitmapManager.setPartSize(1 << newSettings.bitmapSize);
BitmapManager.setUseEarlyRecycling(newSettings.useEarlyRecycling);
BitmapManager.setUseBitmapHack(newSettings.useBitmapHack);
BitmapManager.setUseNativeTextures(newSettings.useNativeTextures);
 */
public class BitmapManager {
	private final static boolean D = false;
	private final static String TAG = "BitmapManager";		
    
	private final static boolean USE_SIZE_SAFE = false;
	//FIXME:
    public static int partSize = 1 << 7;//FIXME:这个是全局设置
    public static boolean useEarlyRecycling = false;//FIXME:这个是全局设置
    public static boolean useBitmapHack = false;//FIXME:这个是全局设置
	
    private final static long BITMAP_MEMORY_LIMIT = Runtime.getRuntime().maxMemory() / 2;
    private static final int GENERATION_THRESHOLD = 10;

    private static SparseArrayEx<BitmapRef> used = new SparseArrayEx<BitmapRef>();
    private static ArrayDeque<BitmapRef> pool = new ArrayDeque<BitmapRef>();
    private static SparseArray<Bitmap> resources = new SparseArray<Bitmap>();
    private static Queue<Object> releasing = new ConcurrentLinkedQueue<Object>();
    private static final AtomicLong created = new AtomicLong();
    private static final AtomicLong reused = new AtomicLong();
    private static final AtomicLong memoryUsed = new AtomicLong();
    private static final AtomicLong memoryPooled = new AtomicLong();
    private static AtomicLong generation = new AtomicLong();
    private static ReentrantLock lock = new ReentrantLock();

    public static Bitmap getResource(final int resourceId, Context context) {
        synchronized (resources) {
            Bitmap bitmap = resources.get(resourceId);
            if (bitmap == null || bitmap.isRecycled()) {
                final Resources resources = context.getResources();
                bitmap = BitmapFactory.decodeResource(resources, resourceId);
            }
            return bitmap;
        }
    }

    public static BitmapRef addBitmap(final String name, final Bitmap bitmap) {
        lock.lock();
        try {
            final BitmapRef ref = new BitmapRef(bitmap, generation.get());
            used.append(ref.id, ref);
            created.incrementAndGet();
            memoryUsed.addAndGet(ref.size);
            if (D) {
                Log.e(TAG, "Added bitmap: [" + ref.id + ", " + name + ", " + ref.width + ", " + ref.height + "], created="
                        + created + ", reused=" + reused + ", memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
            ref.name = name;
            return ref;
        } finally {
            lock.unlock();
        }
    }

    public static BitmapRef getBitmap(final String name, int width, int height, final Bitmap.Config config) {
        lock.lock();
        try {
            if (D) {
                if (memoryUsed.get() + memoryPooled.get() == 0) {
                	Log.e(TAG, "!!! Bitmap pool size: " + (BITMAP_MEMORY_LIMIT / 1024) + "KB");
                }
            }
            final TLIterator<BitmapRef> it = pool.iterator();
            try {
                while (it.hasNext()) {
                    final BitmapRef ref = it.next();
                    if (!ref.isRecycled() && ref.config == config && ref.width == width && ref.height >= height) {
                        if (ref.used.compareAndSet(false, true)) {
                            it.remove();
                            ref.gen = generation.get();
                            used.append(ref.id, ref);
                            reused.incrementAndGet();
                            memoryPooled.addAndGet(-ref.size);
                            memoryUsed.addAndGet(ref.size);
                            if (D) {
                            	Log.e(TAG, "Reuse bitmap: [" + ref.id + ", " + ref.name + " => " + name + ", " + width
                                        + ", " + height + "], created=" + created + ", reused=" + reused
                                        + ", memoryUsed=" + used.size() + "/" + (memoryUsed.get() / 1024) + "KB"
                                        + ", memoryInPool=" + pool.size() + "/" + (memoryPooled.get() / 1024) + "KB");
                            }
                            ref.eraseColor(Color.CYAN);
                            ref.name = name;
                            return ref;
                        } else {
                            if (D) {
                            	Log.e(TAG, "Attempt to re-use used bitmap: " + ref);
                            }
                        }
                    }
                }
            } finally {
                it.release();
            }
            //FIXME:防止报错
            if (USE_SIZE_SAFE) {
	            if (width <= 0) {
	            	width = 1;
	            }
	            if (height <= 0) {
	            	height = 1;
	            }
            }
            final BitmapRef ref = new BitmapRef(Bitmap.createBitmap(width, height, config), generation.get());
            used.put(ref.id, ref);
            created.incrementAndGet();
            memoryUsed.addAndGet(ref.size);
            if (D) {
            	Log.e(TAG, "Create bitmap: [" + ref.id + ", " + name + ", " + width + ", " + height + "], created="
                        + created + ", reused=" + reused + ", memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
            shrinkPool(BITMAP_MEMORY_LIMIT);
            ref.name = name;
            return ref;
        } finally {
            lock.unlock();
        }
    }

    public static BitmapRef checkBitmap(final BitmapRef r, final float width, final float height) {
        final BitmapRef ref =  r;
        if (ref == null || ref.isRecycled() || ref.width != width || ref.height != height) {
            BitmapManager.release(ref);
            return BitmapManager.getBitmap("Page", (int) width, (int) height, Bitmap.Config.RGB_565);
        }
        return r;
    }
    
    public static BitmapRef getTexture(final String name, final Bitmap.Config config) {
        lock.lock();
        try {
            if (D) {
                if (memoryUsed.get() + memoryPooled.get() == 0) {
                	Log.e(TAG, "!!! Bitmap pool size: " + (BITMAP_MEMORY_LIMIT / 1024) + "KB");
                }
            }
            final TLIterator<BitmapRef> it = pool.iterator();
            try {
                while (it.hasNext()) {
                    final BitmapRef ref = it.next();
                    if (!ref.isRecycled() && ref.config == config && ref.width == partSize && ref.height == partSize) {
                        if (ref.used.compareAndSet(false, true)) {
                            it.remove();
                            ref.gen = generation.get();
                            used.append(ref.id, ref);
                            reused.incrementAndGet();
                            memoryPooled.addAndGet(-ref.size);
                            memoryUsed.addAndGet(ref.size);
                            if (D) {
                            	Log.e(TAG, "Reuse bitmap: [" + ref.id + ", " + ref.name + " => " + name + ", " + partSize
                                        + ", " + partSize + "], created=" + created + ", reused=" + reused
                                        + ", memoryUsed=" + used.size() + "/" + (memoryUsed.get() / 1024) + "KB"
                                        + ", memoryInPool=" + pool.size() + "/" + (memoryPooled.get() / 1024) + "KB");
                            }
                            ref.eraseColor(Color.CYAN);
                            ref.name = name;
                            return ref;
                        } else {
                            if (D) {
                            	Log.e(TAG, "Attempt to re-use used bitmap: " + ref);
                            }
                        }
                    }
                }
            } finally {
                it.release();
            }
            final BitmapRef ref =  new BitmapRef(Bitmap.createBitmap(partSize, partSize, config), generation.get());
            used.put(ref.id, ref);
            created.incrementAndGet();
            memoryUsed.addAndGet(ref.size);
            if (D) {
            	Log.e(TAG, "Create bitmap: [" + ref.id + ", " + name + ", " + partSize + ", " + partSize + "], created="
                        + created + ", reused=" + reused + ", memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
            shrinkPool(BITMAP_MEMORY_LIMIT);
            ref.name = name;
            return ref;
        } finally {
            lock.unlock();
        }
    }

    public static void clear(final String msg) {
        lock.lock();
        try {
            generation.addAndGet(GENERATION_THRESHOLD * 2);
            removeOldRefs();
            release();
            shrinkPool(0);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public static void release() {
        lock.lock();
        try {
            generation.incrementAndGet();
            removeOldRefs();
            int count = 0;
            final int queueBefore = D ? releasing.size() : 0;
            while (!releasing.isEmpty()) {
                final Object ref = releasing.poll();
                if (ref instanceof BitmapRef) {
                    releaseImpl((BitmapRef)ref);
                    count++;
                } else if (ref instanceof List) {
                    final List<Bitmaps> list = (List<Bitmaps>) ref;
                    for (final Bitmaps bmp : list) {
                        final BitmapRef[] bitmaps = bmp.clear();
                        if (bitmaps != null) {
                            for (final BitmapRef bitmap : bitmaps) {
                                if (bitmap != null) {
                                    releaseImpl(bitmap);
                                    count++;
                                }
                            }
                        }
                    }
                } else if (ref instanceof BitmapRef[]) {
                    final BitmapRef[] bitmaps = (BitmapRef[]) ref;
                    for (final BitmapRef bitmap : bitmaps) {
                        if (bitmap != null) {
                            releaseImpl(bitmap);
                            count++;
                        }
                    }
                } else {
                    if (D) {
                    	Log.e(TAG, "Unknown object in release queue: " + ref);
                    }
                }
            }
            shrinkPool(BITMAP_MEMORY_LIMIT);
            if (D) {
            	Log.e(TAG, "Return " + count + " bitmap(s) to pool: " + "memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB" + ", releasing queue size " + queueBefore + " => 0");
            }
        } finally {
            lock.unlock();
        }
    }

    public static void release(final BitmapRef ref) {
        if (ref != null) {
            if (D) {
            	Log.e(TAG, "Adding 1 ref to release queue");
            }
            releasing.add(ref);
        }
    }

    public static void release(final BitmapRef[] refs) {
        if (refs != null) {
            if (D) {
            	Log.e(TAG, "Adding " + refs.length + " refs to release queue");
            }
            releasing.add(refs);
        }
    }

    public static void release(final List<Bitmaps> bitmapsToRecycle) {
        if (bitmapsToRecycle != null &&
        	!bitmapsToRecycle.isEmpty()) {
            if (D) {
            	Log.e(TAG, "Adding  list of " + bitmapsToRecycle.size() + " bitmaps to release queue");
            }
            releasing.add(new ArrayList<Bitmaps>(bitmapsToRecycle));
        }
    }

    static void releaseImpl(final BitmapRef ref) {
        assert ref != null;
        if (ref.used.compareAndSet(true, false)) {
            if (used.get(ref.id, null) == ref) {
                used.remove(ref.id);
                memoryUsed.addAndGet(-ref.size);
            } else {
                if (D) {
                	Log.e(TAG, "The bitmap " + ref + " not found in used ones");
                }
            }
        } else {
            if (D) {
            	Log.e(TAG, "Attempt to release unused bitmap");
            }
        }
        pool.add(ref);
        memoryPooled.addAndGet(ref.size);
    }

    private static void removeOldRefs() {
        final long gen = generation.get();
        int recycled = 0;
        final Iterator<BitmapRef> it = pool.iterator();
        while (it.hasNext()) {
            final BitmapRef ref = it.next();
            if (gen - ref.gen > GENERATION_THRESHOLD) {
                it.remove();
                ref.recycle();
                recycled++;
                memoryPooled.addAndGet(-ref.size);
            }
        }
        if (recycled > 0) {
            if (D) {
            	Log.e(TAG, "Recycled " + recycled + " pooled bitmap(s): " + "memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
        }
    }

    private static void shrinkPool(final long limit) {
        int recycled = 0;
        while (memoryPooled.get() + memoryUsed.get() > limit && !pool.isEmpty()) {
            final BitmapRef ref = pool.poll();
            if (ref != null) {
                ref.recycle();
                memoryPooled.addAndGet(-ref.size);
                recycled++;
            }
        }
        if (recycled > 0) {
            if (D) {
            	Log.e(TAG, "Recycled " + recycled + " pooled bitmap(s): " + "memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
        }
    }

    public static int getBitmapBufferSize(final int width, final int height, final Bitmap.Config config) {
        return getPixelSizeInBytes(config) * width * height;
    }

    public static int getBitmapBufferSize(final Bitmap parentBitmap, final Rect childSize) {
        int bytes = 4;
        if (parentBitmap != null) {
            bytes = BitmapManager.getPixelSizeInBytes(parentBitmap.getConfig());
        }
        return bytes * childSize.width() * childSize.height();
    }

    public static int getPartSize() {
        return partSize;
    }

    public static void setPartSize(final int partSize) {
        BitmapManager.partSize = partSize;
    }

    public static boolean isUseEarlyRecycling() {
        return useEarlyRecycling;
    }

    public static void setUseEarlyRecycling(final boolean useEarlyRecycling) {
        BitmapManager.useEarlyRecycling = useEarlyRecycling;
    }

    public static void setUseBitmapHack(final boolean useBitmapHack) {
        BitmapManager.useBitmapHack = useBitmapHack;
    }

    public static int getPixelSizeInBytes(final Bitmap.Config config) {
        switch (config) {
        case ALPHA_8:
            return 1;
            
        case ARGB_4444:
        case RGB_565:
            return 2;
        
        case ARGB_8888:
        default:
            return 4;
        }
    }
}
