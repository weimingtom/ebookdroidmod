package org.ebookdroid2.codec;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.graphics.RectF;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.OutlineItem;

public class MuPdfOutline {
    public List<OutlineLink> getOutline2(MuPDFCore core) {
    	List<OutlineLink> result = new ArrayList<OutlineLink>();
    	OutlineItem[] items = core.getOutline();
    	if (items != null) {
        	for (OutlineItem item : items) {
        		if (item.type == OutlineItem.TYPE_FZ_LINK_GOTO) {
        			OutlineLink outlineLink = new OutlineLink(item.title, "#" + (item.page + 1), item.level);
        			outlineLink.targetRect = new RectF();
                    outlineLink.targetRect.left = item.pointx;
                    outlineLink.targetRect.top = item.pointy;
                    normalizeLinkTargetRect(getCodecPageInfo(core, outlineLink.targetPage), outlineLink.targetRect, item.flags);
                    result.add(outlineLink);
        		} else if (item.type == OutlineItem.TYPE_FZ_LINK_URI) {
        			result.add(new OutlineLink(item.title, item.uri, item.level));
            	}
        	}	
    	}
    	return result;
    }
    
    public static CodecPageInfo getCodecPageInfo(MuPDFCore core, final int targetPage) {
        PointF point = core.getPageSize(targetPage - 1); //FIXME: 参数是页码需要减一
        final CodecPageInfo cpi = new CodecPageInfo();
        cpi.width = (int)point.x;
        cpi.height = (int)point.y;
        cpi.rotation = 0;
        return cpi;
    }
    
    //FIXME:这个方法需要检查，暂时没有问题
    public static void normalizeLinkTargetRect(CodecPageInfo cpi, final RectF targetRect, final int flags) {
        if ((flags & 0x0F) == 0) {
            targetRect.right = targetRect.left = 0;
            targetRect.bottom = targetRect.top = 0;
            return;
        }

        final float left = targetRect.left;
        final float top = targetRect.top;

        if (((cpi.rotation / 90) % 2) != 0) {
            targetRect.right = targetRect.left = left / cpi.height;
            targetRect.bottom = targetRect.top = 1.0f - top / cpi.width;
        } else {
            targetRect.right = targetRect.left = left / cpi.width;
            targetRect.bottom = targetRect.top = 1.0f - top / cpi.height;
        }
    }
}
