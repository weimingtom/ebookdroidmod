package org.ebookdroid2.curl;

import android.os.Handler;
import android.os.Message;

class FlipAnimationHandler extends Handler {
    private final PageAnimator animator;

    FlipAnimationHandler(PageAnimator singlePageCurler) {
        this.animator = singlePageCurler;
    }

    @Override
    public void handleMessage(final Message msg) {
        this.animator.flipAnimationStep();
    }

    public void sleep(final long millis) {
        this.removeMessages(0);
        sendMessageDelayed(obtainMessage(0), millis);
    }
}