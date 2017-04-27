package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;

public class SinglePageSimpleCurler extends AbstractSinglePageCurler {
    public SinglePageSimpleCurler(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.CURLER, singlePageDocumentView);
    }

    @Override
    protected int getInitialXForBackFlip(final int width) {
        return width;
    }

    @Override
    protected void updateValues() {
        final int width = view.getWidth();
        final int height = view.getHeight();
        mA.x = width - mMovement.x;
        mA.y = height;
        mD.x = 0;
        mD.y = 0;
        if (mA.x > width / 2) {
            mD.x = width;
            mD.y = height - (width - mA.x) * height / mA.x;
        } else {
            mD.x = 2 * mA.x;
            mD.y = 0;
        }
        final float tanA = (height - mD.y) / (mD.x + mMovement.x - width);
        final float _cos = (1 - tanA * tanA) / (1 + tanA * tanA);
        final float _sin = 2 * tanA / (1 + tanA * tanA);
        mF.x = (float) (width - mMovement.x + _cos * mMovement.x);
        mF.y = (float) (height - _sin * mMovement.x);
        if (mA.x > width / 2) {
            mE.x = mD.x;
            mE.y = mD.y;
        } else {
            mE.x = (float) (mD.x + _cos * (width - mD.x));
            mE.y = (float) -(_sin * (width - mD.x));
        }
    }
}
