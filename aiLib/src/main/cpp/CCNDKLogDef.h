//
// Created by chenchao on 2021/8/10.
//

#ifndef CCOPENGLES_CCNDKLOGDEF_H
#define CCOPENGLES_CCNDKLOGDEF_H

#ifdef __cplusplus
extern "C" {
#endif

#include <android/log.h>
#include <sys/time.h>

#define LOG_TAG "NDK-LOG"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)


static long long GetSysCurrentTime() {
    struct timeval time;
    gettimeofday(&time, NULL);
    long long curTime = ((long long) (time.tv_sec)) * 1000 + time.tv_usec / 1000;
    return curTime;
}

#ifdef __cplusplus
}
#endif

#endif //CCOPENGLES_CCNDKLOGDEF_H
