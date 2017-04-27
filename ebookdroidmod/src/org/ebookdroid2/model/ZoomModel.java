package org.ebookdroid2.model;

import java.util.ArrayList;
import java.util.List;

import org.ebookdroid2.listener.ZoomListener;

import android.util.FloatMath;

public class ZoomModel {
    public static final float MIN_ZOOM = 1.0f;
    public static final float MAX_ZOOM = 32.0f;

    private static int ZOOM_ROUND_FACTOR = 0;
    private float initialZoom = MIN_ZOOM;
    private float currentZoom = MIN_ZOOM;
    private boolean isCommited;
    private List<ZoomListener> listenerList = new ArrayList<ZoomListener>();
    
    public void addListener(ZoomListener listener) {
    	if (listener != null) {
	    	listenerList.remove(listener);
	    	listenerList.add(listener);
    	}
    }
    public void removeListener(ZoomListener listener) {
    	if (listener != null) {
    		listenerList.remove(listener);
    	}
    }
    
    public ZoomModel() {
    	
    }

    public void initZoom(final float zoom) {
        this.initialZoom = this.currentZoom = adjust(zoom);
        isCommited = true;
    }

    public void setZoom(final float zoom) {
        setZoom(zoom, false);
        final float newZoom = adjust(zoom);
        final float oldZoom = this.currentZoom;
        if (newZoom != oldZoom) {
            isCommited = false;
            this.currentZoom = newZoom;
            //FIXME:事件响应
            if (listenerList != null && listenerList.size() > 0) {
            	for (ZoomListener listener : listenerList) {
            		if (listener != null) {
            			listener.zoomChanged(oldZoom, newZoom, false);
            		}
            	}
            }
        }
    }

    public void setZoom(final float zoom, final boolean commitImmediately) {
        final float newZoom = adjust(zoom);
        final float oldZoom = this.currentZoom;
        if (newZoom != oldZoom || commitImmediately) {
            isCommited = commitImmediately;
            this.currentZoom = newZoom;
            //FIXME:事件响应
            if (listenerList != null && listenerList.size() > 0) {
            	for (ZoomListener listener : listenerList) {
            		if (listener != null) {
            			listener.zoomChanged(oldZoom, newZoom, commitImmediately);
            		}
            	}
            }
            if (commitImmediately) {
                this.initialZoom = this.currentZoom;
            }
        }
    }

    public void scaleZoom(final float factor) {
        setZoom(currentZoom * factor, false);
    }

    public void scaleAndCommitZoom(final float factor) {
        setZoom(currentZoom * factor, true);
    }

    public float getZoom() {
        return currentZoom;
    }

    public void commit() {
        if (!isCommited) {
            isCommited = true;
            //FIXME:事件响应
            if (listenerList != null && listenerList.size() > 0) {
            	for (ZoomListener listener : listenerList) {
            		if (listener != null) {
            			listener.zoomChanged(initialZoom, currentZoom, true);
            		}
            	}
            }
            initialZoom = currentZoom;
        }
    }

    private float adjust(final float zoom) {
        return adjust(ZOOM_ROUND_FACTOR <= 0 ? 
        	zoom : round(zoom, ZOOM_ROUND_FACTOR),
        	MIN_ZOOM, MAX_ZOOM);
    }
    
    private static float round(final float value, final float share) {
        return FloatMath.floor(value * share) / share;
    }
    
    private static float adjust(final float value, final float min, final float max) {
        return Math.min(Math.max(min, value), max);
    }
}
