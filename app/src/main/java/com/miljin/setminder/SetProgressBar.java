/*
 * Copyright (c) 2015 Jim X. Lin
 *
 * This file is part of SetMinder.
 *
 *  SetMinder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SetMinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SetMinder.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.miljin.setminder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SetProgressBar extends View{

    int completedSets = 0;
    int totalSets = 0;
    float setProgress = 0f;
    float barTop;
    float barBottom;
    float barLength;
    Paint paint;
    RectF boundsF;

    public SetProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {

        //calculate height of a full bar (75% of view, centered)
        //getHeight() etc. not called until view is rendered, won't work in constructor
        barTop = getHeight()/8;
        barBottom = getHeight() - barTop;
        barLength = barBottom - barTop;

        //Draw progress bar
        boundsF.set(0f, barBottom - setProgress * barLength, getWidth() / 2, barBottom);
        if (setProgress < 0.5) { //bar changes color for <50%, <50%, one set left, >100%
            paint.setColor(0xFF4CAF50);             //green 500 (from material design color palette)
        } else if (totalSets - completedSets > 1){
            paint.setColor(0xFFFFC107);             //amber 500
        } else if (completedSets < totalSets) {
            paint.setColor(0xFFF44336);             //red 500
        } else {
            paint.setColor(0xFF2196F3);             //blue 500
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boundsF, paint);

        //draw quarter tick marks (in ascending order)
        //ticks turn black as they're covered by the progress bar
        paint.setStrokeWidth(getHeight() / 70);

        if (setProgress > 0) {paint.setColor(0x00000000);} else {paint.setColor(0xFFFFFFFF);}
        canvas.drawLine(0f, barBottom, getWidth()/2, barBottom, paint);

        if (setProgress > 0.25f) {paint.setColor(0xFF000000);} else {paint.setColor(0xFFFFFFFF);}
        canvas.drawLine(0f, barTop + barLength*3/4, getWidth()/5, barTop + barLength*3/4, paint);

        if (setProgress > 0.5f) {paint.setColor(0xFF000000);} else {paint.setColor(0xFFFFFFFF);}
        canvas.drawLine(0f, barTop + barLength/2, getWidth()/2, barTop + barLength/2, paint);

        if (setProgress > 0.75f) {paint.setColor(0xFF000000);} else {paint.setColor(0xFFFFFFFF);}
        canvas.drawLine(0f, barTop + barLength / 4, getWidth() / 5, barTop + barLength / 4, paint);

        if (setProgress == 1) {paint.setColor(0x00000000);} else {paint.setColor(0xFFFFFFFF);}
        canvas.drawLine(0f, barTop, getWidth()/2, barTop, paint);
    }

    public void updateSetProgress (int inputCompletedSets, int inputTotalSets) {
        completedSets = inputCompletedSets;
        totalSets = inputTotalSets;
        setProgress = 0f;
        //calculate progress; exceeding totalSets counts as 100%
        if (totalSets > 0) {
            setProgress = (float) completedSets / totalSets;
        }
        if (totalSets > 0 && completedSets > totalSets) {
            setProgress = 1f;
        }
        this.invalidate();
    }

    private void init() {
        paint = new Paint();
        boundsF = new RectF();
    }
}
