/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.ebookdroid2.curl;

import org.ebookdroid2.core.SinglePageController;
import org.ebookdroid2.event.EventDraw;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.model.Vector2D;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.FloatMath;

public class SinglePageNaturalCurler extends AbstractPageAnimator {
    final Path forePath = new Path();
    final Path edgePath = new Path();
    final Path quadPath = new Path();

    private final Paint backPaint = new Paint();
    private final Paint edgePaint = new Paint();

    public SinglePageNaturalCurler(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.CURLER_DYNAMIC, singlePageDocumentView);
        backPaint.setAntiAlias(false);
        backPaint.setAlpha(0x40);
        edgePaint.setAntiAlias(true);
        edgePaint.setStyle(Paint.Style.FILL);
        edgePaint.setShadowLayer(15, 0, 0, 0xC0000000);
    }

    @Override
    protected int getInitialXForBackFlip(final int width) {
        return width << 1;
    }

    @Override
    protected void updateValues() {
        final int width = view.getWidth();
        final int height = view.getHeight();
        mA.x = width - mMovement.x + 0.1f;
        mA.y = height - mMovement.y + 0.1f;
    }

    @Override
    protected void drawInternal(final EventDraw event) {
        drawBackground(event);
        final Canvas canvas = event.canvas;
        final BitmapRef fgBitmap = BitmapManager.getBitmap("Foreground", canvas.getWidth(), canvas.getHeight(),
                Bitmap.Config.RGB_565);
        try {
            fgBitmap.eraseColor(Color.BLACK);
            drawForeground(EventPool.newEventDraw(event, fgBitmap.getCanvas(), this.view.getBase().getContext()));
            final int myWidth = canvas.getWidth();
            final int myHeight = canvas.getHeight();
            final int cornerX = myWidth;
            final int cornerY = myHeight;
            final int oppositeX = -cornerX;
            final int oppositeY = -cornerY;
            final int dX = Math.max(1, Math.abs((int) mA.x - cornerX));
            final int dY = Math.max(1, Math.abs((int) mA.y - cornerY));
            final int x1 = cornerX == 0 ? (dY * dY / dX + dX) / 2 : cornerX - (dY * dY / dX + dX) / 2;
            final int y1 = cornerY == 0 ? (dX * dX / dY + dY) / 2 : cornerY - (dX * dX / dY + dY) / 2;
            float sX, sY;
            {
                final float d1 = (int) mA.x - x1;
                final float d2 = (int) mA.y - cornerY;
                sX = FloatMath.sqrt(d1 * d1 + d2 * d2) / 2;
                if (cornerX == 0) {
                    sX = -sX;
                }
            }
            {
                final float d1 = (int) mA.x - cornerX;
                final float d2 = (int) mA.y - y1;
                sY = FloatMath.sqrt(d1 * d1 + d2 * d2) / 2;
                if (cornerY == 0) {
                    sY = -sY;
                }
            }
            forePath.rewind();
            forePath.moveTo((int) mA.x, (int) mA.y);
            forePath.lineTo(((int) mA.x + cornerX) / 2, ((int) mA.y + y1) / 2);
            forePath.quadTo(cornerX, y1, cornerX, y1 - sY);
            if (Math.abs(y1 - sY - cornerY) < myHeight) {
                forePath.lineTo(cornerX, oppositeY);
            }
            forePath.lineTo(oppositeX, oppositeY);
            if (Math.abs(x1 - sX - cornerX) < myWidth) {
                forePath.lineTo(oppositeX, cornerY);
            }
            forePath.lineTo(x1 - sX, cornerY);
            forePath.quadTo(x1, cornerY, ((int) mA.x + x1) / 2, ((int) mA.y + cornerY) / 2);
            quadPath.moveTo(x1 - sX, cornerY);
            quadPath.quadTo(x1, cornerY, ((int) mA.x + x1) / 2, ((int) mA.y + cornerY) / 2);
            canvas.drawPath(quadPath, edgePaint);
            quadPath.rewind();
            quadPath.moveTo(((int) mA.x + cornerX) / 2, ((int) mA.y + y1) / 2);
            quadPath.quadTo(cornerX, y1, cornerX, y1 - sY);
            canvas.drawPath(quadPath, edgePaint);
            quadPath.rewind();
            canvas.save();
            canvas.clipPath(forePath);
            fgBitmap.draw(canvas, 0, 0, null);
            canvas.restore();
            edgePaint.setColor(fgBitmap.getAverageColor());
            edgePath.rewind();
            edgePath.moveTo((int) mA.x, (int) mA.y);
            edgePath.lineTo(((int) mA.x + cornerX) / 2, ((int) mA.y + y1) / 2);
            edgePath.quadTo(((int) mA.x + 3 * cornerX) / 4, ((int) mA.y + 3 * y1) / 4, ((int) mA.x + 7 * cornerX) / 8,
                    ((int) mA.y + 7 * y1 - 2 * sY) / 8);
            edgePath.lineTo(((int) mA.x + 7 * x1 - 2 * sX) / 8, ((int) mA.y + 7 * cornerY) / 8);
            edgePath.quadTo(((int) mA.x + 3 * x1) / 4, ((int) mA.y + 3 * cornerY) / 4, ((int) mA.x + x1) / 2,
                    ((int) mA.y + cornerY) / 2);
            canvas.drawPath(edgePath, edgePaint);
            canvas.save();
            canvas.clipPath(edgePath);
            final Matrix m = getMatrix();
            m.postScale(1, -1);
            m.postTranslate((int) mA.x - cornerX, (int) mA.y + cornerY);
            final float angle;
            if (cornerY == 0) {
                angle = -180 / 3.1416f * (float) atan2((int) mA.x - cornerX, (int) mA.y - y1);
            } else {
                angle = 180 - 180 / 3.1416f * (float) atan2((int) mA.x - cornerX, (int) mA.y - y1);
            }
            m.postRotate(angle, (int) mA.x, (int) mA.y);
            fgBitmap.draw(canvas, m, backPaint);
            canvas.restore();
        } finally {
            BitmapManager.release(fgBitmap);
        }
    }

    @Override
    protected float getLeftBound() {
        return 1 - view.getWidth();
    }

    @Override
    protected void resetClipEdge() {
        mMovement.x = 0;
        mMovement.y = 0;
        mOldMovement.x = 0;
        mOldMovement.y = 0;
        mA.set(0, 0);
    }

    @Override
    protected Vector2D fixMovement(final Vector2D point, final boolean bMaintainMoveDir) {
        return point;
    }

    @Override
    protected void drawBackground(final EventDraw event) {
        final Canvas canvas = event.canvas;
        final ViewState viewState = event.viewState;
        Page page = event.viewState.model.getPageObject(backIndex);
        if (page == null) {
            page = event.viewState.model.getCurrentPageObject();
        }
        if (page != null) {
            canvas.save();
            canvas.clipRect(viewState.getBounds(page));
            event.process(page);
            canvas.restore();
        }
    }

    @Override
    protected void drawForeground(final EventDraw event) {
        final Canvas canvas = event.canvas;
        final ViewState viewState = event.viewState;
        Page page = event.viewState.model.getPageObject(foreIndex);
        if (page == null) {
            page = event.viewState.model.getCurrentPageObject();
        }
        if (page != null) {
            canvas.save();
            canvas.clipRect(viewState.getBounds(page));
            event.process(page);
            canvas.restore();
        }
    }

    @Override
    protected void drawExtraObjects(final EventDraw event) {
    	//FIXME:绘画额外图形showAnimIcon
    }

    @Override
    protected void onFirstDrawEvent(final Canvas canvas, final ViewState viewState) {
        resetClipEdge();
        lock.writeLock().lock();
        try {
            updateValues();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public static Matrix getMatrix() {
    	Matrix matrix = new Matrix();
    	matrix.reset();
    	return matrix;
    }
    
    //------------------------------------
    //FastMath
    
    private static final double TANGENT_TABLE_A[] =
        {
        +0.0d,
        +0.1256551444530487d,
        +0.25534194707870483d,
        +0.3936265707015991d,
        +0.5463024377822876d,
        +0.7214844226837158d,
        +0.9315965175628662d,
        +1.1974215507507324d,
        +1.5574076175689697d,
        +2.092571258544922d,
        +3.0095696449279785d,
        +5.041914939880371d,
        +14.101419448852539d,
        -18.430862426757812d,
    };
    
    private static final double TANGENT_TABLE_B[] =
        {
        +0.0d,
        -7.877917738262007E-9d,
        -2.5857668567479893E-8d,
        +5.2240336371356666E-9d,
        +5.206150291559893E-8d,
        +1.8307188599677033E-8d,
        -5.7618793749770706E-8d,
        +7.848361555046424E-8d,
        +1.0708593250394448E-7d,
        +1.7827257129423813E-8d,
        +2.893485277253286E-8d,
        +3.1660099222737955E-7d,
        +4.983191803254889E-7d,
        -3.356118100840571E-7d,
    };
    private static final double EIGHTHS[] = {0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0, 1.125, 1.25, 1.375, 1.5, 1.625};

    private static final double F_3_4 = 3d / 4d;
    private static final double F_1_2 = 1d / 2d;
    private static final double F_1_4 = 1d / 4d;
    public static final double SAFE_MIN = 0x1.0p-1022;
    private static final long HEX_40000000 = 0x40000000L; // 1073741824L
    private static final long MASK_30BITS = -1L - (HEX_40000000 -1); // 0xFFFFFFFFC0000000L;
    private static double atan2(double y, double x) {
        if (x != x || y != y) {
            return Double.NaN;
        }
        if (y == 0) {
            final double result = x * y;
            final double invx = 1d / x;
            final double invy = 1d / y;
            if (invx == 0) { 
            	if (x > 0) {
                    return y; 
                } else {
                    return copySign(Math.PI, y);
                }
            }
            if (x < 0 || invx < 0) {
                if (y < 0 || invy < 0) {
                    return -Math.PI;
                } else {
                    return Math.PI;
                }
            } else {
                return result;
            }
        }
        if (y == Double.POSITIVE_INFINITY) {
            if (x == Double.POSITIVE_INFINITY) {
                return Math.PI * F_1_4;
            }
            if (x == Double.NEGATIVE_INFINITY) {
                return Math.PI * F_3_4;
            }
            return Math.PI * F_1_2;
        }
        if (y == Double.NEGATIVE_INFINITY) {
            if (x == Double.POSITIVE_INFINITY) {
                return -Math.PI * F_1_4;
            }
            if (x == Double.NEGATIVE_INFINITY) {
                return -Math.PI * F_3_4;
            }
            return -Math.PI * F_1_2;
        }
        if (x == Double.POSITIVE_INFINITY) {
            if (y > 0 || 1 / y > 0) {
                return 0d;
            }
            if (y < 0 || 1 / y < 0) {
                return -0d;
            }
        }
        if (x == Double.NEGATIVE_INFINITY) {
            if (y > 0.0 || 1 / y > 0.0) {
                return Math.PI;
            }
            if (y < 0 || 1 / y < 0) {
                return -Math.PI;
            }
        }
        if (x == 0) {
            if (y > 0 || 1 / y > 0) {
                return Math.PI * F_1_2;
            }
            if (y < 0 || 1 / y < 0) {
                return -Math.PI * F_1_2;
            }
        }
        final double r = y / x;
        if (Double.isInfinite(r)) { 
        	return atan(r, 0, x < 0);
        }
        double ra = doubleHighPart(r);
        double rb = r - ra;
        final double xa = doubleHighPart(x);
        final double xb = x - xa;
        rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
        final double temp = ra + rb;
        rb = -(temp - ra - rb);
        ra = temp;
        if (ra == 0) { // Fix up the sign so atan works correctly
            ra = copySign(0d, y);
        }
        final double result = atan(ra, rb, x < 0);
        return result;
    }
    
    private static double doubleHighPart(double d) {
        if (d > -SAFE_MIN && d < SAFE_MIN){
            return d; // These are un-normalised - don't try to convert
        }
        long xl = Double.doubleToLongBits(d);
        xl = xl & MASK_30BITS; // Drop low order bits
        return Double.longBitsToDouble(xl);
    }
    
    private static double copySign(double magnitude, double sign){
        long m = Double.doubleToLongBits(magnitude);
        long s = Double.doubleToLongBits(sign);
        if ((m >= 0 && s >= 0) || (m < 0 && s < 0)) { // Sign is currently OK
            return magnitude;
        }
        return -magnitude; // flip sign
    }
    
    private static double atan(double xa, double xb, boolean leftPlane) {
        boolean negate = false;
        int idx;
        if (xa == 0.0) { 
        	return leftPlane ? copySign(Math.PI, xa) : xa;
        }
        if (xa < 0) {
            xa = -xa;
            xb = -xb;
            negate = true;
        }
        if (xa > 1.633123935319537E16) { // Very large input
            return (negate ^ leftPlane) ? (-Math.PI * F_1_2) : (Math.PI * F_1_2);
        }
        if (xa < 1) {
            idx = (int) (((-1.7168146928204136 * xa * xa + 8.0) * xa) + 0.5);
        } else {
            final double oneOverXa = 1 / xa;
            idx = (int) (-((-1.7168146928204136 * oneOverXa * oneOverXa + 8.0) * oneOverXa) + 13.07);
        }
        double epsA = xa - TANGENT_TABLE_A[idx];
        double epsB = -(epsA - xa + TANGENT_TABLE_A[idx]);
        epsB += xb - TANGENT_TABLE_B[idx];
        double temp = epsA + epsB;
        epsB = -(temp - epsA - epsB);
        epsA = temp;
        temp = xa * HEX_40000000;
        double ya = xa + temp - temp;
        double yb = xb + xa - ya;
        xa = ya;
        xb += yb;
        if (idx == 0) {
            final double denom = 1d / (1d + (xa + xb) * (TANGENT_TABLE_A[idx] + TANGENT_TABLE_B[idx]));
            ya = epsA * denom;
            yb = epsB * denom;
        } else {
            double temp2 = xa * TANGENT_TABLE_A[idx];
            double za = 1d + temp2;
            double zb = -(za - 1d - temp2);
            temp2 = xb * TANGENT_TABLE_A[idx] + xa * TANGENT_TABLE_B[idx];
            temp = za + temp2;
            zb += -(temp - za - temp2);
            za = temp;
            zb += xb * TANGENT_TABLE_B[idx];
            ya = epsA / za;
            temp = ya * HEX_40000000;
            final double yaa = (ya + temp) - temp;
            final double yab = ya - yaa;
            temp = za * HEX_40000000;
            final double zaa = (za + temp) - temp;
            final double zab = za - zaa;
            yb = (epsA - yaa * zaa - yaa * zab - yab * zaa - yab * zab) / za;
            yb += -epsA * zb / za / za;
            yb += epsB / za;
        }
        epsA = ya;
        epsB = yb;
        final double epsA2 = epsA * epsA;
        yb = 0.07490822288864472;
        yb = yb * epsA2 + -0.09088450866185192;
        yb = yb * epsA2 + 0.11111095942313305;
        yb = yb * epsA2 + -0.1428571423679182;
        yb = yb * epsA2 + 0.19999999999923582;
        yb = yb * epsA2 + -0.33333333333333287;
        yb = yb * epsA2 * epsA;
        ya = epsA;
        temp = ya + yb;
        yb = -(temp - ya - yb);
        ya = temp;
        yb += epsB / (1d + epsA * epsA);
        double za = EIGHTHS[idx] + ya;
        double zb = -(za - EIGHTHS[idx] - ya);
        temp = za + yb;
        zb += -(temp - za - yb);
        za = temp;
        double result = za + zb;
        double resultb = -(result - za - zb);
        if (leftPlane) {
            final double pia = 1.5707963267948966 * 2;
            final double pib = 6.123233995736766E-17 * 2;
            za = pia - result;
            zb = -(za - pia + result);
            zb += pib - resultb;
            result = za + zb;
            resultb = -(result - za - zb);
        }
        if (negate ^ leftPlane) {
            result = -result;
        }
        return result;
    }
}
