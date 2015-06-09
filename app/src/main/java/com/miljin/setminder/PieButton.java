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
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.Button;

public class PieButton extends Button {

    float progress = 1f;
    Paint paintCircle;
    Paint paintPie;
    RectF boundsFCircle;
    RectF boundsFPie;

    public PieButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {

        //Draw green circle
        boundsFCircle.set(getWidth() / 20, getHeight() / 20, getWidth() - getWidth() / 20, getHeight() - getHeight() / 20);
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(0xFF4CAF50);        //material 500 green
        canvas.drawOval(boundsFCircle, paintCircle);

        //draw "+1" when timer is off, draw black waxing pie when timer is ticking
        paintPie.setColor(0xFF000000);
        if (progress < 1f) {
            //draw quartering lines
            paintPie.setStyle(Paint.Style.STROKE);
            paintPie.setStrokeWidth(getHeight()/50);
            canvas.drawLine(0f, getHeight() / 2, getWidth(), getHeight() / 2, paintPie);
            canvas.drawLine(getWidth() / 2, 0f, getWidth() / 2, getHeight(), paintPie);
            //draw pie
            paintPie.setStyle(Paint.Style.FILL);
            boundsFPie.set(0f, 0f, getWidth(), getHeight());
            canvas.drawArc(boundsFPie, -90f, (1 - progress) * (-360), true, paintPie);

        } else {
            paintPie.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paintPie.setTextSize(getHeight()/3f);
            canvas.drawText("+1", getWidth()/3.5f,
                    getHeight()/2 -  ((paintPie.descent() + paintPie.ascent()) / 2), paintPie);
        }

    }

    public void updatePie (float inputProgress) {
        progress = inputProgress;
        this.invalidate();
    }

    private void init() {
        paintCircle = new Paint();
        paintCircle.setAntiAlias(true);
        paintPie = new Paint();
        paintPie.setAntiAlias(true);
        boundsFCircle = new RectF();
        boundsFPie = new RectF();
    }
}