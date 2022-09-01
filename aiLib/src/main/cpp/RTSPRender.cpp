//
// Created by 王军军 on 2022/4/9.
//

#include <jni.h>
#include <string>

#include "FFBridge.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/time.h>
#include <libavcodec/avcodec.h>
#include <libavcodec/packet.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>


JNIEXPORT jlong JNICALL Java_com_ubeesky_rtspffmpeg_RTSPGetStreamNative_native_1Init
        (JNIEnv *env, jobject obj, jstring jurl)
{
    const char* url = env->GetStringUTFChars(jurl, nullptr);
    FFBridge *bridge = new FFBridge();
    bridge->Init(env, obj, const_cast<char *>(url));
    env->ReleaseStringUTFChars(jurl, url);
    return reinterpret_cast<jlong>(bridge);
}

JNIEXPORT void JNICALL Java_com_ubeesky_rtspffmpeg_RTSPGetStreamNative_native_1Start
        (JNIEnv *env, jobject obj, jlong handle)
{
    if(handle != 0)
    {
        FFBridge *bridge = reinterpret_cast<FFBridge *>(handle);
        bridge->Start();
    }

}

JNIEXPORT void JNICALL Java_com_ubeesky_rtspffmpeg_RTSPGetStreamNative_native_1Stop
        (JNIEnv *env, jobject obj, jlong handle)
{
    if(handle != 0)
    {
        FFBridge *bridge = reinterpret_cast<FFBridge *>(handle);
        bridge->Stop();
    }
}


JNIEXPORT void JNICALL Java_com_ubeesky_rtspffmpeg_RTSPGetStreamNative_native_1UnInit
        (JNIEnv *env, jobject obj, jlong handle)
{
    if(handle != 0)
    {
        FFBridge *bridge = reinterpret_cast<FFBridge *>(handle);
        bridge->UnInit();
        delete bridge;
    }
}
#ifdef __cplusplus
}
#endif