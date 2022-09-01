// Tencent is pleased to support the open source community by making TNN available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

#ifndef ANDROID_OBJECTDETECTORNANODET_JNI_H
#define ANDROID_OBJECTDETECTORNANODET_JNI_H

#include <jni.h>

#define TNN_OBJECT_DETECTOR_NANODET(sig) Java_com_ubeesky_lib_ai_AINative_##sig
#ifdef __cplusplus
extern "C" {
#endif

/**
* 初始化模型
* @param env               jni环境
* @param thiz
* @param model_path        模型文件路径
* @param width             宽度
* @param height            高度
* @param score_threshold   分类得分阈值
* @param iou_threshold     NMS的iou阈值
* @param topk              保留前检测到的结果
* @param compute_type      计算类型（0:CPU、 1:GPU）
* @return                  是否初始成功
*/
JNIEXPORT JNICALL jint TNN_OBJECT_DETECTOR_NANODET(nativeInit)(JNIEnv *env, jobject thiz, jstring modelPath, jint computUnitType);

/**
 * 检查NPU是否支持
 * @param env
 * @param thiz
 * @param modelPath 模型文件路径
 * @return 是否支持结果
 */
JNIEXPORT JNICALL jboolean TNN_OBJECT_DETECTOR_NANODET(nativeCheckNpu)(JNIEnv *env, jobject thiz, jstring modelPath);

/**
 * 释放模型及内存
 * @param env   jni环境
 * @param thiz
 * @return      是否初始成功
 */
JNIEXPORT JNICALL jint TNN_OBJECT_DETECTOR_NANODET(nativeDeinit)(JNIEnv *env, jobject thiz);

/**
 * 视频流检测
 * @param env           jni环境
 * @param thiz
 * @param yuv420sp      摄像头数据包
 * @param width         宽度
 * @param height        高度
 * @param view_width
 * @param view_height
 * @param rotate        旋转角度
 * @return              检测结果数组
 */
JNIEXPORT JNICALL jobjectArray
TNN_OBJECT_DETECTOR_NANODET(nativeDetectFromStream)(JNIEnv *env, jobject thiz, jbyteArray yuv420sp, jint width, jint height, jint view_width, jint view_height, jint rotate);


/**
* 检测图像数据
*
* @param bitmap 图像
* @param width  宽度
* @param height 高度
* @return 检测结果
*/
JNIEXPORT JNICALL jobjectArray TNN_OBJECT_DETECTOR_NANODET(nativeDetectFromImage)(JNIEnv *env, jobject thiz, jobject imageSource, jint width, jint height);

#ifdef __cplusplus
}
#endif
#endif //ANDROID_OBJECTDETECTORNANODET_JNI_H
