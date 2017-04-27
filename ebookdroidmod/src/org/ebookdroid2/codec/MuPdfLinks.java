package org.ebookdroid2.codec;

import java.util.ArrayList;
import java.util.List;

import android.graphics.RectF;
import android.util.Log;

import com.artifex.mupdfdemo.LinkInfo;
import com.artifex.mupdfdemo.MuPDFCore;

public class MuPdfLinks {
	private final static boolean D = false;
	private final static String TAG = "MuPdfLinks";
	
    public static List<PageLink> getPageLinks2(final RectF pageBounds, MuPDFCore core, int pageno) {
        final List<PageLink> links = new ArrayList<PageLink>();
        LinkInfo[] linkInfoArray = core.getPageLinks(pageno, false);
        if (linkInfoArray != null) {
        	for (LinkInfo info : linkInfoArray) {
        		final PageLink link = new PageLink();
        		if (info.type == LinkInfo.TYPE_INTERNAL) {
        			//FZ_LINK_GOTO
                    link.rectType = 1;
                    
                    link.sourceRect = new RectF();
                    link.sourceRect.left = (info.rect.left - pageBounds.left) / pageBounds.width();
                    link.sourceRect.top = (info.rect.top - pageBounds.top) / pageBounds.height();
                    link.sourceRect.right = (info.rect.right - pageBounds.left) / pageBounds.width();
                    link.sourceRect.bottom = (info.rect.bottom - pageBounds.top) / pageBounds.height();
                    
                    link.targetPage = info.pageNumber;
                    if (link.targetPage > 0) {
                        int flags = info.flags;
                        link.targetRect = new RectF();
                        link.targetRect.left = info.targetx;
                        link.targetRect.top = info.targety;
                        MuPdfOutline.normalizeLinkTargetRect(MuPdfOutline.getCodecPageInfo(core, link.targetPage), link.targetRect, flags);
                    }
                    links.add(link);
        		} else if (info.type == LinkInfo.TYPE_EXTERNAL) {
        			//FZ_LINK_REMOTE
                    link.url = info.url;
                    links.add(link);
        		} else if (info.type == LinkInfo.TYPE_REMOTE) {
        			//FZ_LINK_GOTOR
        		}
        	}
        }
        if (D) {
        	Log.e(TAG, "================> getPageLinks2: " + links.size());
        }
        if (D) {
	        for (int i = 0; i < links.size(); i++) {
	        	PageLink item = links.get(i);
	        	Log.e(TAG, ">>>>getPageLinks item :[" + i + "] = " + item);
	        }
        }
        return links;
    }
    
}
