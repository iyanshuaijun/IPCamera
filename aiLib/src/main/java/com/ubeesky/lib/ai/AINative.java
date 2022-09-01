package com.ubeesky.lib.ai;

import android.graphics.Bitmap;

/**
 * TNN推理模型计算
 *
 * @author wangjunjun
 */
public class AINative {

    static {
        System.loadLibrary("aiNative");
    }

    public interface AICallback {
        void steamAIResult(AIDetectResult[] results);

        void imageAIResult(AIDetectResult[] results);
    }

    private AICallback aiCallback;

    public AINative(AICallback aiCallback) {
        this.aiCallback = aiCallback;
    }

    public int init(String modelPath, int computeType) {
        return nativeInit(modelPath, computeType);
    }

    public int deinit() {
        return nativeDeinit();
    }

    public void detectFromStream(byte[] yuv420sp, int width, int height, int view_width, int view_height, int rotate) {
        AIDetectResult[] aiDetectResults = nativeDetectFromStream(yuv420sp, width, height, view_width, view_height, rotate);
        if (aiCallback != null) {
            aiCallback.steamAIResult(aiDetectResults);
        }
    }

    public void detectFromImage(Bitmap bitmap, int width, int height) {
        AIDetectResult[] aiDetectResults = nativeDetectFromImage(bitmap, width, height);
        if (aiCallback != null) {
            aiCallback.imageAIResult(aiDetectResults);
        }
    }

    public boolean checkNpu(String modelPath) {
        return nativeCheckNpu(modelPath);
    }


    /**
     * 模型初始化
     *
     * @param modelPath   模型文件路径
     * @param computeType 计算类型
     * @return 执行结果
     */
    public native int nativeInit(String modelPath, int computeType);

    /**
     * 释放模型
     *
     * @return 执行结果
     */
    public native int nativeDeinit();


    /**
     * 检测视频流数据
     *
     * @param yuv420sp    一帧数据
     * @param width       宽度
     * @param height      高度
     * @param view_width  可视范围宽
     * @param view_height 可视范围高
     * @param rotate      视频旋转角度
     * @return 检测结果集
     */
    public native AIDetectResult[] nativeDetectFromStream(byte[] yuv420sp, int width, int height, int view_width, int view_height, int rotate);


    /**
     * 检测图像数据
     *
     * @param bitmap 图像
     * @param width  宽度
     * @param height 高度
     * @return 检测结果
     */
    public native AIDetectResult[] nativeDetectFromImage(Bitmap bitmap, int width, int height);

    public native boolean nativeCheckNpu(String modelPath);
}
