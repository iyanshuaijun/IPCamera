package com.pedro.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.ubeesky.lib.ai.AIDetectResult;

public class OverlayView extends View {

    private final Paint paint;

    private AIDetectResult[] results;

    public OverlayView(Context context, final AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                15, getResources().getDisplayMetrics()));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results != null && results.length != 0) {
            for (int i = 0; i < results.length; i++) {
                Rect box = new Rect((int)results[i].startX, (int)results[i].startY, (int)results[i].endX, (int)results[i].endY);
                String name = results[i].getClassName();
                String confidence = results[i].getConfidence() + "%";
                canvas.drawRect(box, paint);
                canvas.drawText(name, box.left, box.top + 50, paint);
                canvas.drawText(confidence, box.left, box.top + 100, paint);
            }
        }
    }

    public void setResults(final AIDetectResult[] results) {
        this.results = results;
        postInvalidate();
    }
}
