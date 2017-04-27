package org.ebookdroid2.manager;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.core.HScrollController;
import org.ebookdroid2.core.IViewController;
import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.core.VScrollController;

import android.util.Log;

public enum DocumentViewMode {
	VERTICALL_SCROLL("Vertical scroll", PageAlign.WIDTH),
    HORIZONTAL_SCROLL("Horizontal scroll", PageAlign.HEIGHT),
    SINGLE_PAGE("Single page", null);

	private final static boolean D = false;
	private final static String TAG = "DocumentViewMode";
	
    private final String resValue;
    private final PageAlign pageAlign;

    private DocumentViewMode(final String resId, final PageAlign pageAlign) {
        this.resValue = resId;
        this.pageAlign = pageAlign;
    }

    public static IViewController create(DocumentViewMode _this, final IActivityController base, ViewerActivityHelper helper) {
    	switch (_this) {
    	case VERTICALL_SCROLL:
    		return new VScrollController(base, helper);
    		
    	case HORIZONTAL_SCROLL:
    		return new HScrollController(base, helper);
    		
    	case SINGLE_PAGE:
    		return new SinglePageController(base, helper);
    	
    	default:
    		if (D) {
    			Log.e(TAG, "Cannot find instanciate view controller: ");
    		}
    	}
    	return null;
    }

    public String getResValue() {
        return resValue;
    }

    public static PageAlign getPageAlign(final BookSettings bs) {
        if (bs == null || bs.viewMode == null) {
            return PageAlign.AUTO;
        }
        final PageAlign defAlign = bs.viewMode.pageAlign;
        return defAlign != null ? defAlign : bs.pageAlign;
    }

    public static DocumentViewMode getByOrdinal(final int ord) {
        if (0 <= ord && ord < values().length) {
            return values()[ord];
        }
        return VERTICALL_SCROLL;
    }
    
    public static DocumentViewMode getByName(String resValue, DocumentViewMode defValue) {
		if (resValue != null && resValue.length() > 0) {
			if ("Vertical scroll".equalsIgnoreCase(resValue)) {
				return VERTICALL_SCROLL;
			} else if ("Horizontal scroll".equalsIgnoreCase(resValue)) {
				return HORIZONTAL_SCROLL;
			} else if ("Single page".equalsIgnoreCase(resValue)) {
				return SINGLE_PAGE;
			}
		}
		return defValue;
    }
}
