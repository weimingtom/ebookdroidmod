package org.ebookdroid2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ebookdroid2.listener.DecodingProgressListener;

public class DecodingProgressModel {
    private AtomicInteger currentlyDecoding = new AtomicInteger();

    private List<DecodingProgressListener> listenerList = new ArrayList<DecodingProgressListener>();
    public void addListener(DecodingProgressListener listener) {
    	if (listener != null) {
    		listenerList.remove(listener);
    		listenerList.add(listener);
    	}
    }
    public void removeListener(DecodingProgressListener listener) {
    	if (listener != null) {
    		listenerList.remove(listener);
    	}
    }
    
    public DecodingProgressModel() {
    	
    }

    public void increase() {
    	//FIXME:事件响应
    	int cur = currentlyDecoding.incrementAndGet();
        if (listenerList != null && listenerList.size() > 0) {
	    	for (DecodingProgressListener listener : listenerList) {
	    		if (listener != null) {
	    			listener.decodingProgressChanged(cur);
	    		}
	    	}
        }
    }

    public void increase(int increment) {
    	//FIXME:事件响应
    	int cur = currentlyDecoding.addAndGet(increment);
    	if (listenerList != null && listenerList.size() > 0) {
	    	for (DecodingProgressListener listener : listenerList) {
	    		if (listener != null) {
	    			listener.decodingProgressChanged(cur);
	    		}
	    	}
        }
    }

    public void decrease() {
    	//FIXME:事件响应
    	int cur = currentlyDecoding.decrementAndGet();
    	if (listenerList != null && listenerList.size() > 0) {
	    	for (DecodingProgressListener listener : listenerList) {
	    		if (listener != null) {
	    			listener.decodingProgressChanged(cur);
	    		}
	    	}
        }
    }
}
