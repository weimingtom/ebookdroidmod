package org.ebookdroid2.manager;


public enum PageAlign {
    WIDTH("By Width"),
    HEIGHT("By Height"),
    AUTO("Auto");

    private final String resValue;

    private PageAlign(final String resId) {
        this.resValue = resId;
    }

    public String getResValue() {
        return resValue;
    }
    
    public static PageAlign getByName(String resValue, PageAlign defValue) {
		if (resValue != null && resValue.length() > 0) {
			if ("By Width".equalsIgnoreCase(resValue)) {
				return WIDTH;
			} else if ("By Height".equalsIgnoreCase(resValue)) {
				return HEIGHT;
			} else if ("Auto".equalsIgnoreCase(resValue)) {
				return AUTO;
			}
		}
		return defValue;
    }
}
