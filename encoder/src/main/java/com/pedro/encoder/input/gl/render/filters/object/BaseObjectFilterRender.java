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

package com.pedro.encoder.input.gl.render.filters.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.pedro.encoder.R;
import com.pedro.encoder.input.gl.ImageTexture;
import com.pedro.encoder.input.gl.Sprite;
import com.pedro.encoder.input.gl.TextureLoader;
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;
import com.pedro.encoder.utils.BitmapUtils;
import com.pedro.encoder.utils.gl.GlUtil;
import com.pedro.encoder.utils.gl.StreamObjectBase;
import com.pedro.encoder.utils.gl.TranslateTo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pedro on 03/08/18.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
abstract public class BaseObjectFilterRender extends BaseFilterRender {

    //rotation matrix
    private final float[] squareVertexDataFilter = {
            // X, Y, Z, U, V
            -1f, -1f, 0f, 0f, 0f, //bottom left
            1f, -1f, 0f, 1f, 0f, //bottom right
            -1f, 1f, 0f, 0f, 1f, //top left
            1f, 1f, 0f, 1f, 1f, //top right
    };

    private int program = -1;
    private int aPositionHandle = -1;
    private int aTextureHandle = -1;
    private int aTextureObjectHandle = -1;
    private int uMVPMatrixHandle = -1;
    private int uSTMatrixHandle = -1;
    private int uSamplerHandle = -1;
    private int uObjectHandle = -1;
    protected int uAlphaHandle = -1;

    private FloatBuffer squareVertexObject;

    protected int[] streamObjectTextureId = new int[]{-1};
    protected TextureLoader textureLoader = new TextureLoader();
    private ArrayList<ImageTexture> imageTextureList;
    protected StreamObjectBase streamObject;
    private Sprite sprite;
    protected float alpha = 1f;
    protected boolean shouldLoad = false;

    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int mStreamWidth;
    private int mStreamHeight;
    private int textureId;
    private int frameBuffer;
    private int mSize = 22;
    private String str;
    private ArrayList<Integer> bitmapWidthList;
    private ArrayList<Integer> bitmapHeightList;

    public BaseObjectFilterRender() {
        squareVertex = ByteBuffer.allocateDirect(squareVertexDataFilter.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        squareVertex.put(squareVertexDataFilter).position(0);
        sprite = new Sprite();
        float[] vertices = sprite.getTransformedVertices();
        squareVertexObject = ByteBuffer.allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        squareVertexObject.put(vertices).position(0);
        Matrix.setIdentityM(MVPMatrix, 0);
        Matrix.setIdentityM(STMatrix, 0);
    }

    @Override
    protected void initGlFilter(Context context) {
        String vertexShader = GlUtil.getStringFromRaw(context, R.raw.object_vertex);
        String fragmentShader = GlUtil.getStringFromRaw(context, R.raw.object_fragment);

        program = GlUtil.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        aTextureObjectHandle = GLES20.glGetAttribLocation(program, "aTextureObjectCoord");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        uSamplerHandle = GLES20.glGetUniformLocation(program, "uSampler");
        uObjectHandle = GLES20.glGetUniformLocation(program, "uObject");
        uAlphaHandle = GLES20.glGetUniformLocation(program, "uAlpha");
    }

    protected void initImageTexture() {
        imageTextureList = new ArrayList<>();
        bitmapWidthList = new ArrayList<>();
        bitmapHeightList = new ArrayList<>();
        ImageTexture imageTexture;
        for (int i = 0; i < 22; i++) {
            imageTexture = new ImageTexture(width, height);
            if (i < 19) {   //左上角时间水印（textureId：0~18）
                if (i == 10) {
                    imageTexture.loadBitmap(BitmapUtils.textToBitmap("-"));
                } else if (i == 11) {
                    imageTexture.loadBitmap(BitmapUtils.textToBitmap(":"));
                } else if (i <= 9) {
                    imageTexture.loadBitmap(BitmapUtils.textToBitmap(i + ""));
                } else {
                    imageTexture.loadBitmap(BitmapUtils.textToBitmap("*"));
                }
            } else if (i == 19) {   //右上角自定义水印（textureId：19）
                imageTexture.loadBitmap(BitmapUtils.textToBitmap("晴转多云，xx℃"));
            } else if (i == 20) {   //左下角自定义水印（textureId：20）
                imageTexture.loadBitmap(BitmapUtils.textToBitmap("自定义符号~！@#￥%……&*（）"));
            } else {   //右下角自定义水印（textureId：21）
                imageTexture.loadBitmap(BitmapUtils.textToBitmap("自定义符号{}：”《》？+-*/"));
            }
            imageTextureList.add(imageTexture);
            bitmapWidthList.add(imageTexture.getImageWidth());
            bitmapHeightList.add(imageTexture.getImageHeight());
        }
        mSize = imageTextureList.size();
    }

    @Override
    protected void drawFilter() {
        GlUtil.checkGlError("drawFilter start");
        GLES20.glViewport(0, 0, width, height);

        if (shouldLoad) {
            releaseTexture();
            initImageTexture();
            shouldLoad = false;
        }
        ImageTexture preImageTexture = null;
        String time = formatter.format(new Date());
        if ("".equals(time)) {
            return;
        }

        for (int i = 0; i < mSize; i++) {
            if (preImageTexture == null) {
                textureId = previousTexId;
            } else {
                textureId = preImageTexture.getTextureId();
            }
            if (i == mSize - 1) {
                frameBuffer = renderHandler.getFboId()[0];
            } else {
                frameBuffer = imageTextureList.get(i).getFrameBuffer();
            }
            setDefaultScale(mStreamWidth, mStreamHeight, i);
            if (i < 19) {
//                setPosition(TranslateTo.TOP_LEFT);
                setPosition(0f + i * 1.5f, 0f);
                str = time.substring(i, i + 1);
                if (str.equals("-")) {
                    str = "10";
                } else if (str.equals(":")) {
                    str = "11";
                } else if (str.equals(" ") || str.equals("*")) {
                    continue;
                }
            } else if (i == 19) {
                setPosition(TranslateTo.TOP_RIGHT);
                str = "19";
            } else if (i == 20) {
                setPosition(TranslateTo.BOTTOM_LEFT);
                str = "20";
            } else if (i == 21) {
                setPosition(TranslateTo.BOTTOM_RIGHT);
                str = "21";
            }

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);

            GLES20.glUseProgram(program);

            squareVertex.position(SQUARE_VERTEX_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                    SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
            GLES20.glEnableVertexAttribArray(aPositionHandle);

            squareVertex.position(SQUARE_VERTEX_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false,
                    SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
            GLES20.glEnableVertexAttribArray(aTextureHandle);

            squareVertexObject.position(SQUARE_VERTEX_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(aTextureObjectHandle, 2, GLES20.GL_FLOAT, false,
                    2 * FLOAT_SIZE_BYTES, squareVertexObject);
            GLES20.glEnableVertexAttribArray(aTextureObjectHandle);

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, MVPMatrix, 0);
            GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, STMatrix, 0);

            //Sampler
            GLES20.glUniform1i(uSamplerHandle, 4);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
//            Log.i("cc", "previousTexId = " + textureId);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            //Object
            GLES20.glUniform1i(uObjectHandle, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //child
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imageTextureList.get(Integer.parseInt(str)).getImageTextureId());
//            Log.i("cc", "streamObjectTextureId[i] = " + imageTextureList.get(Integer.parseInt(str)).getImageTextureId());
            //Set alpha. 0f if no image loaded.
            GLES20.glUniform1f(uAlphaHandle, imageTextureList.get(Integer.parseInt(str)).getImageTextureId() == -1 ? 0f : alpha);

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//            GLES20.glUseProgram(0);
//            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GlUtil.checkGlError("drawFilter end");
            preImageTexture = imageTextureList.get(i);
        }
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(program);
        releaseTexture();
        sprite.reset();
    }

    private void releaseTexture() {
        GLES20.glDeleteTextures(streamObjectTextureId.length, streamObjectTextureId, 0);
        streamObjectTextureId = new int[]{-1};
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setScale(float scaleX, float scaleY) {
        sprite.scale(scaleX, scaleY);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public void setPosition(float x, float y) {
        sprite.translate(x, y);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public void setPosition(TranslateTo positionTo) {
        sprite.translate(positionTo);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public PointF getScale() {
        return sprite.getScale();
    }

    public PointF getPosition() {
        return sprite.getTranslation();
    }

    public void setDefaultScale(int streamWidth, int streamHeight, int num) {
        sprite.scale(bitmapWidthList.get(num) * 100 / streamWidth,
                bitmapHeightList.get(num) * 100 / streamHeight);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public void setDefaultScale(int streamWidth, int streamHeight) {
        mStreamWidth = streamWidth;
        mStreamHeight = streamHeight;
//        setDefaultScale(streamWidth, streamHeight, 0);
    }
}
