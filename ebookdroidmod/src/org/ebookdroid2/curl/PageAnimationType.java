package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;

public enum PageAnimationType {
    NONE("None", true),
    CURLER("Simple curler", false),
    CURLER_DYNAMIC("Dynamic curler", false),
    CURLER_NATURAL("Natural curler", false),
    SLIDER("Slider", true),
    SLIDER2("Another Slider", true),
    FADER("Fade in", true),
    SQUEEZER("Squeeze", true);

    private final String resValue;
    private final boolean hardwareAccelSupported;

    private PageAnimationType(final String resId, final boolean hardwareAccelSupported) {
        this.resValue = resId;
        this.hardwareAccelSupported = hardwareAccelSupported;
    }

    public String getResValue() {
        return resValue;
    }

    public boolean isHardwareAccelSupported() {
        return hardwareAccelSupported;
    }

    public static PageAnimator create(final PageAnimationType type, final SinglePageController singlePageDocumentView) {
        if (type != null) {
            switch (type) {
            case CURLER:
                return new SinglePageSimpleCurler(singlePageDocumentView);
                
            case CURLER_DYNAMIC:
            	return new SinglePageDynamicCurler(singlePageDocumentView);
              
            case CURLER_NATURAL:
                return new SinglePageNaturalCurler(singlePageDocumentView);
                
            case SLIDER:
                return new SinglePageSlider(singlePageDocumentView);
                
            case SLIDER2:
                return new SinglePageSlider2(singlePageDocumentView);
                
            case FADER:
                return new SinglePageFader(singlePageDocumentView);
                
            case SQUEEZER:
                return new SinglePageSqueezer(singlePageDocumentView);
                
            default:
                break;
            }
        }
        return new SinglePageDefaultSlider(singlePageDocumentView);
    }
    
    public static PageAnimationType getByName(String resValue, PageAnimationType defValue) {
		if (resValue != null && resValue.length() > 0) {
			if ("None".equalsIgnoreCase(resValue)) {
				return NONE;
			} else if ("Simple curler".equalsIgnoreCase(resValue)) {
				return CURLER;
			} else if ("Dynamic curler".equalsIgnoreCase(resValue)) {
				return CURLER_DYNAMIC;
			} else if ("Natural curler".equalsIgnoreCase(resValue)) {
				return CURLER_NATURAL;
			} else if ("Slider".equalsIgnoreCase(resValue)) {
				return SLIDER;
			} else if ("Another Slider".equalsIgnoreCase(resValue)) {
				return SLIDER2;
			} else if ("Fade in".equalsIgnoreCase(resValue)) {
				return FADER;
			} else if ("Squeeze".equalsIgnoreCase(resValue)) {
				return SQUEEZER;
			}
		}
		return defValue;
    }
    
    
}
