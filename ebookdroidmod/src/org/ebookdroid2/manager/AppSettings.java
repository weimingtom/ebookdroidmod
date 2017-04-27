package org.ebookdroid2.manager;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.curl.PageAnimationType;
import org.ebookdroid2.view.BaseView;
import org.ebookdroid2.view.IView;
import org.ebookdroid2.view.SurfaceView;

import android.content.pm.ActivityInfo;

/**
 * 
 * FIXME:核心类，请根据引用代码删除没用到的设置项
 * 额外图形画法，参考类DragMark的写法（已删除）
 * 搜索FIXME:绘画额外图形showAnimIcon
 * 
 */
public class AppSettings {
    private static AppSettings current;
    //界面
    public final String lang;
    public final boolean loadRecent;
    public final boolean confirmClose;
    public final boolean brightnessInNightModeOnly;
    public final int brightness;
    public final boolean keepScreenOn;
    public final RotationType rotation;
    public final boolean fullScreen;
    public final boolean showTitle;
    public final boolean pageInTitle;
    public final ToastPosition pageNumberToastPosition;
    public final ToastPosition zoomToastPosition;
    public final boolean showAnimIcon; //不需要了
    public final boolean showBookmarksInMenu;
    public final int linkHighlightColor;
    public final int searchHighlightColor;
    public final int currentSearchHighlightColor;
    public final boolean storeGotoHistory;
    public final boolean storeLinkGotoHistory;
    public final boolean storeOutlineGotoHistory;
    public final boolean storeSearchGotoHistory;
    //滚动
    public final boolean tapsEnabled;
    public final int scrollHeight;
    public final int touchProcessingDelay;
    public final boolean animateScrolling; //FIXME:这里切换滚动方式
    //性能
    public final int pagesInMemory;
    public final DocumentViewType viewType;
    public final int decodingThreads;
    public final int decodingThreadPriority;
    public final int drawThreadPriority;
    public final int bitmapSize;
    public final boolean bitmapFileringEnabled;
    public final boolean textureReuseEnabled;
    public final boolean useNativeTextures;
    public final boolean useBitmapHack;
    public final boolean useEarlyRecycling;
    public final boolean reloadDuringZoom;
    public final int heapPreallocate;
    public final int pdfStorageSize;
    //呈现
    public final boolean nightMode;
    public boolean positiveImagesInNightMode;
    final int contrast;
    final int exposure;
    final boolean autoLevels;
    final boolean splitPages;
    final boolean splitRTL;
    final boolean cropPages;
    public final DocumentViewMode viewMode;
    final PageAlign pageAlign;
    final PageAnimationType animationType;
    //
    public final boolean useCustomDpi;
    public final int xDpi;
    public final int yDpi;
    public final String monoFontPack;
    public final String sansFontPack;
    public final String serifFontPack;
    public final String symbolFontPack;
    public final String dingbatFontPack;
    public final boolean slowCMYK;

    private AppSettings() {
        //用户界面设置
        lang = "";//用户界面语言 (需要重启)//LANG.getPreferenceValue(prefs);
        loadRecent = false;//在应用程序上启动自动加载上次打开的书//LOAD_RECENT.getPreferenceValue(prefs);
        confirmClose = false;//关闭前询问确认//CONFIRM_CLOSE.getPreferenceValue(prefs);
        brightnessInNightModeOnly = false;//在夜间模式中应用的亮度设置//BRIGHTNESS_NIGHT_MODE_ONLY.getPreferenceValue(prefs);
        brightness = 100;//调整设置整体亮度(0–100)//BRIGHTNESS.getPreferenceValue(prefs);
        keepScreenOn = true;//始终保持屏幕点亮//KEEP_SCREEN_ON.getPreferenceValue(prefs);
        rotation = RotationType.AUTOMATIC;//如何改变旋转//ROTATION.getPreferenceValue(prefs);
        fullScreen = false;//隐藏状态栏//FULLSCREEN.getPreferenceValue(prefs);
        showTitle = true;//SHOW_TITLE.getPreferenceValue(prefs);
        pageInTitle = true;//在标题栏中显示页码//SHOW_PAGE_IN_TITLE.getPreferenceValue(prefs);
        pageNumberToastPosition = ToastPosition.LeftTop;//页码自定义位置//PAGE_NUMBER_TOAST_POSITION.getPreferenceValue(prefs);
        zoomToastPosition = ToastPosition.LeftBottom;//缩放自定义位置//ZOOM_TOAST_POSITION.getPreferenceValue(prefs);
        showAnimIcon = true;//如果启用动画或拖动，显示动画或拖动图标// SHOW_ANIM_ICON.getPreferenceValue(prefs);
        //触摸和滚动
        tapsEnabled = true;//定义使用点击区域和行动//TAPS_ENABLED.getPreferenceValue(prefs);
        scrollHeight = 50;//设置滚动条的高度//SCROLL_HEIGHT.getPreferenceValue(prefs);
        touchProcessingDelay = 50;//触摸处理延时（毫秒）//TOUCH_DELAY.getPreferenceValue(prefs);
        animateScrolling = true;//动画滚动滚动模式//ANIMATE_SCROLLING.getPreferenceValue(prefs);
        //导航与历史
        showBookmarksInMenu = false;//SHOW_BOOKMARKs_MENU.getPreferenceValue(prefs);
        linkHighlightColor = 0x80FFFF00;//选择颜色环节突出//LINK_HIGHLIGHT_COLOR.getPreferenceValue(prefs);
        searchHighlightColor = 0x3F00FF00;//选择颜色的搜索结果突出//SEARCH_HIGHLIGHT_COLOR.getPreferenceValue(prefs);
        currentSearchHighlightColor = 0x7F007F00;//选择颜色突出当前搜索结果//CURRENT_SEARCH_HIGHLIGHT_COLOR.getPreferenceValue(prefs);
        storeGotoHistory = true;//转到页面导航历史记录中的商店//STORE_GOTO_HISTORY.getPreferenceValue(prefs);
        storeLinkGotoHistory = true;//转到链接存储在导航历史记录//STORE_LINK_GOTO_HISTORY.getPreferenceValue(prefs);
        storeOutlineGotoHistory = true;//商店转到大纲环节在导航历史记录//STORE_OUTLINE_GOTO_HISTORY.getPreferenceValue(prefs);
        storeSearchGotoHistory = false;//到商店去搜索结果中的导航历史记录//STORE_SEARCH_GOTO_HISTORY.getPreferenceValue(prefs);
        //性能设置
        pagesInMemory = 0;//0-100,存储在内存中的页数//PAGES_IN_MEMORY.getPreferenceValue(prefs);
        viewType = DocumentViewType.SURFACE;//定义文档查看器使用的视图//VIEW_TYPE.getPreferenceValue(prefs);
        decodingThreads = 1;//1-16,运行解码线程的数量//DECODING_THREADS.getPreferenceValue(prefs);
        decodingThreadPriority = 5;//1-10,解码线程优先级//DECODE_THREAD_PRIORITY.getPreferenceValue(prefs);
        drawThreadPriority = 5;//1-10,表面绘图线程优先级//DRAW_THREAD_PRIORITY.getPreferenceValue(prefs);
        bitmapSize = 7;//6-10,设置位图尺寸//BITMAP_SIZE.getPreferenceValue(prefs);
        bitmapFileringEnabled = false;//启用线性位图过滤//BITMAP_FILTERING.getPreferenceValue(prefs);
        textureReuseEnabled = true;//页面解码时使用已经存在的位图//REUSE_TEXTURES.getPreferenceValue(prefs);
        useNativeTextures = false;//存储纹理在原生内存中//USE_NATIVE_TEXTURES.getPreferenceValue(prefs);
        useBitmapHack = false;//使用位图分配跟踪破解//USE_BITMAP_HACK.getPreferenceValue(prefs);
        useEarlyRecycling = false;//位图移除时，手工回收位图//EARLY_RECYCLING.getPreferenceValue(prefs);
        reloadDuringZoom = false;//如果页面缩放超过20%，则重新加载页面//RELOAD_DURING_ZOOM.getPreferenceValue(prefs);
        heapPreallocate = 0;//0-256,程序启动时预分配堆栈大小（兆字节）//HEAP_PREALLOCATE.getPreferenceValue(prefs);
        pdfStorageSize = 64;//16-128,MuPDF内部存储容量大小（兆字节）//PDF_STORAGE_SIZE.getPreferenceValue(prefs);
        //呈现
        //FIXME:背景色为白色
        nightMode = false;//在黑色背景上使用白色符号//NIGHT_MODE.getPreferenceValue(prefs);
        positiveImagesInNightMode = false;//在夜间模式下显示灰度图片(不反转图片颜色)//NIGHT_MODE_POS_IMAGES.getPreferenceValue(prefs);
        contrast = 100;//对比度校正值 (0——1000,默认为 100)：100//CONTRAST.getPreferenceValue(prefs);
        exposure = 100;//曝光校正值 (0 - 200, 默认为100)：100//EXPOSURE.getPreferenceValue(prefs);
        autoLevels = false;//自动色阶,使用自动水平矫正//AUTO_LEVELS.getPreferenceValue(prefs);
        splitPages = false;//将两个纵向页面拆分为横向页面//SPLIT_PAGES.getPreferenceValue(prefs);
        splitRTL = false;//分割横屏页面为两个竖屏页面，从右到左//SPLIT_RTL.getPreferenceValue(prefs);
        cropPages = false;//裁剪页面边框//CROP_PAGES.getPreferenceValue(prefs);
        viewMode = DocumentViewMode.VERTICALL_SCROLL;//页面视图模式//VIEW_MODE.getPreferenceValue(prefs);
        pageAlign = PageAlign.WIDTH;//如何对齐页//PAGE_ALIGN.getPreferenceValue(prefs);
        animationType = PageAnimationType.NONE;//选择过渡的动画类型//ANIMATION_TYPE.getPreferenceValue(prefs);
        //PDF特定格式
        useCustomDpi = false;//指定DPI值//PDF_CUSTOM_DPI.getPreferenceValue(prefs);
        xDpi = 120;//72-720,X DPI//PDF_CUSTOM_XDPI.getPreferenceValue(prefs);
        yDpi = 120;//72-720,Y DPI//PDF_CUSTOM_YDPI.getPreferenceValue(prefs);
        monoFontPack = "";//定义字体包使用等宽字体族//MONO_FONT_PACK.getPreferenceValue(prefs);
        sansFontPack = "";//定义字体包使用sans字体族//SANS_FONT_PACK.getPreferenceValue(prefs);
        serifFontPack = "";//定义字体包使用衬线字体族//SERIF_FONT_PACK.getPreferenceValue(prefs);
        symbolFontPack = "";//定义字体包使用符号字体//SYMBOL_FONT_PACK.getPreferenceValue(prefs);
        dingbatFontPack = "";//定义字体包使用图片字体//DINGBAT_FONT_PACK.getPreferenceValue(prefs);
        slowCMYK = false;//使用慢速但精确的CMYK到RGB算法//PDF_SLOW_CMYK.getPreferenceValue(prefs);
    }

    public float getXDpi(final float def) {
        return useCustomDpi ? xDpi : def;
    }

    public float getYDpi(final float def) {
        return useCustomDpi ? yDpi : def;
    }

    public static void init() {
        current = new AppSettings();
    }

    public static AppSettings current() {
        SettingsManager.lock.readLock().lock();
        try {
            return current;
        } finally {
            SettingsManager.lock.readLock().unlock();
        }
    }
    
    //这里是全局变量设置到书的设置中
    public static void setDefaultSettings(final BookSettings bs) {
        bs.nightMode = current.nightMode;
        bs.positiveImagesInNightMode = current.positiveImagesInNightMode;
        bs.contrast = current.contrast;
        bs.exposure = current.exposure;
        bs.autoLevels = current.autoLevels;
        bs.splitPages = current.splitPages;
        bs.splitRTL = current.splitRTL;
        bs.cropPages = current.cropPages;
        bs.viewMode = current.viewMode;
        bs.pageAlign = current.pageAlign;
        bs.animationType = current.animationType;
    }
    
    public static enum DocumentViewType {
        BASE("Base"),
        SURFACE("Surface");

        private final String resValue;

        private DocumentViewType(final String resId) {
            this.resValue = resId;
        }

        public String getResValue() {
            return resValue;
        }

        public static IView create(DocumentViewType _this, final IActivityController base) {
        	switch (_this) {
        	case BASE:
        		return new BaseView(base);
        		
        	case SURFACE:
        		return new SurfaceView(base);
        		
        	default:
        		return null;
        	}
        }
        
        public static DocumentViewType getByName(String resValue, DocumentViewType defValue) {
    		if (resValue != null && resValue.length() > 0) {
    			if ("Base".equalsIgnoreCase(resValue)) {
    				return BASE;
    			} else if ("Surface".equalsIgnoreCase(resValue)) {
    				return SURFACE;
    			}
    		}
    		return defValue;
        }    
    }
    
    public static enum RotationType {
        UNSPECIFIED("Unspecified", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, 3),
        LANDSCAPE("Force landscape", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, 3),
        PORTRAIT("Force portrait", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, 3),
        USER("User", ActivityInfo.SCREEN_ORIENTATION_USER, 3),
        BEHIND("Behind", ActivityInfo.SCREEN_ORIENTATION_BEHIND, 3),
        AUTOMATIC("Automatic", ActivityInfo.SCREEN_ORIENTATION_SENSOR, 3),
        NOSENSOR("No sensor", ActivityInfo.SCREEN_ORIENTATION_NOSENSOR, 3),
        SENSOR_LANDSCAPE("Sensor landscape", ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, 10), //FIXME:这里要API10支持
        SENSOR_PORTRAIT("Sensor portrait", ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, 10),
        REVERSE_LANDSCAPE("Reverse landscape", ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, 10),
        REVERSE_PORTRAIT("Reverse portrait", ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, 10),
        FULL_SENSOR("Full sensor", ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, 10);

        private final String resValue;
        private final int orientation;
        private final int version;

        private RotationType(final String resId, final int orientation, final int version) {
            this.resValue = resId;
            this.orientation = orientation;
            this.version = version;
        }

        public String getResValue() {
            return resValue;
        }

        public int getOrientation() {
            return orientation;
        }

        public int getVersion() {
            return version;
        }
        
        public static RotationType getByName(String resValue, RotationType defValue) {
    		if (resValue != null && resValue.length() > 0) {
    			if ("Unspecified".equalsIgnoreCase(resValue)) {
    				return UNSPECIFIED;
    			} else if ("Force landscape".equalsIgnoreCase(resValue)) {
    				return LANDSCAPE;
    			} else if ("Force portrait".equalsIgnoreCase(resValue)) {
    				return PORTRAIT;
    			} else if ("User".equalsIgnoreCase(resValue)) {
    				return USER;
    			} else if ("Behind".equalsIgnoreCase(resValue)) {
    				return BEHIND;
    			} else if ("Automatic".equalsIgnoreCase(resValue)) {
    				return AUTOMATIC;
    			} else if ("No sensor".equalsIgnoreCase(resValue)) {
    				return NOSENSOR;
    			} else if ("Sensor landscape".equalsIgnoreCase(resValue)) {
    				return SENSOR_LANDSCAPE;
    			} else if ("Sensor portrait".equalsIgnoreCase(resValue)) {
    				return SENSOR_PORTRAIT;
    			} else if ("Reverse landscape".equalsIgnoreCase(resValue)) {
    				return REVERSE_LANDSCAPE;
    			} else if ("Reverse portrait".equalsIgnoreCase(resValue)) {
    				return REVERSE_PORTRAIT;
    			} else if ("Full sensor".equalsIgnoreCase(resValue)) {
    				return FULL_SENSOR;
    			}
    		}
    		return defValue;
        }
    }

    public static enum ToastPosition {
        Invisible("Invisible", 0),
        LeftTop("LeftTop", LEFT | TOP),
        RightTop("RightTop", RIGHT | TOP),
        LeftBottom("LeftBottom", LEFT | BOTTOM),
        Bottom("Bottom", CENTER | BOTTOM),
        RightBottom("RightBottom", RIGHT | BOTTOM);

        public final int position;
        private final String resValue;

        private ToastPosition(String resId, int position) {
            this.resValue = resId;//EBookDroidApp.context.getString(resId);
            this.position = position;
        }

        public String getResValue() {
            return resValue;
        }
        
        public static ToastPosition getByName(String resValue, ToastPosition defValue) {
    		if (resValue != null && resValue.length() > 0) {
    			if ("Invisible".equalsIgnoreCase(resValue)) {
    				return Invisible;
    			} else if ("LeftTop".equalsIgnoreCase(resValue)) {
    				return LeftTop;
    			} else if ("RightTop".equalsIgnoreCase(resValue)) {
    				return RightTop;
    			} else if ("LeftBottom".equalsIgnoreCase(resValue)) {
    				return LeftBottom;
    			} else if ("Bottom".equalsIgnoreCase(resValue)) {
    				return Bottom;
    			} else if ("RightBottom".equalsIgnoreCase(resValue)) {
    				return RightBottom;
    			}
    		}
    		return defValue;
        }
    }
}
