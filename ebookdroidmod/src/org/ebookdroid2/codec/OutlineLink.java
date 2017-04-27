package org.ebookdroid2.codec;

import android.graphics.RectF;

public class OutlineLink {
    public final String title;
    public final int level;
    
    public String targetUrl;
    public int targetPage = -1; //FIXME:注意这个是页码，从1开始数起
    public RectF targetRect;

    public OutlineLink(final String title, final String link, final int level) {
        this.title = title;
        this.level = level;

        if (link != null) {
            if (link.startsWith("#")) {
                try {
                    targetPage = Integer.parseInt(link.substring(1).replace(" ", ""));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } else if (link.startsWith("http:")) {
                targetUrl = link;
            }
        }
    }
}
