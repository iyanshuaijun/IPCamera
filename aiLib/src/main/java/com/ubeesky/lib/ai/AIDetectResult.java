package com.ubeesky.lib.ai;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 检测结果
 *
 * @author wangjunjun
 */
public class AIDetectResult {


    public static final String[] classes = {
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
            "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
            "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
            "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
            "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
            "hair drier", "toothbrush"};

    /**
     * 结果类型
     */
    public int classId;

    /**
     * 目标左上角坐标-X
     */
    public float startX;

    /**
     * 目标左上角坐标-Y
     */
    public float startY;

    /**
     * 目标左右下角坐标-X
     */
    public float endX;

    /**
     * 目标左右下角坐标-Y
     */
    public float endY;

    /**
     * 置信度
     */
    public float confidence;

    public float[] landmarks;


    public String getClassName() {
        return classes[classId];
    }

    final static BigDecimal bigDecimal = new BigDecimal(100);

    public String getConfidence() {
        return new BigDecimal(confidence).multiply(bigDecimal).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    @Override
    public String toString() {
        return "AIDetectResult{" +
                "物体:" + getClassName() +
                ", startX:" + startX + ", startY:" + startY +
                ", endX:" + endX + ", endY:" + endY +
                ", 置信度:" + getConfidence() + "%, landmarks=" + Arrays.toString(landmarks) +
                '}';
    }
}
