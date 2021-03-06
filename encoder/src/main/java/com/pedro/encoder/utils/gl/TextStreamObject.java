/*
 * Copyright (C) 2021 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pedro.encoder.utils.gl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

/**
 * Created by pedro on 23/09/17.
 */

public class TextStreamObject extends StreamObjectBase {

    private static final String TAG = "TextStreamObject";

    private int numFrames;
    private Bitmap imageBitmap;
    private Paint paint;
    private Typeface font;

    public TextStreamObject() {
        font = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAlpha(255);
        paint.setTypeface(font);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public int[] getWidth() {
        return null;
    }

    @Override
    public int[] getHeight() {
        return null;
    }

    public void load(String text, float textSize, int textColor, Typeface typeface) {
        numFrames = 4;
        Log.i(TAG, "finish load text");
    }

    @Override
    public void recycle() {
        if (imageBitmap != null) imageBitmap.recycle();
    }

    private Bitmap textAsBitmap(String text, float textSize, int textColor, Typeface typeface) {
        paint.setTextSize(textSize);
        paint.setColor(textColor);

        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    @Override
    public int getNumFrames() {
        return numFrames;
    }

    @Override
    public Bitmap[] getBitmaps() {
        return null;
    }

    @Override
    public int updateFrame() {
        return 0;
    }
}
