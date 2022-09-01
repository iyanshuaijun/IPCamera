//
// Created by Administrator on 2022/4/14/014.
//

#ifndef DEMUX_FFBRIDGE_H
#define DEMUX_FFBRIDGE_H

#include <jni.h>
#include "FFDemux.h"

#define JAVA_PACKET_EVENT_CALLBACK_API_NAME "packetEventCallback"

#define JAVA_MESSAGE_EVENT_CALLBACK_API_NAME "messageEventCallback"

class FFBridge {

public:
    FFBridge(){};
    virtual ~FFBridge(){};

    void Init(JNIEnv *jniEnv, jobject obj, char *url);
    void UnInit();

    void Start();
    void Stop();


private:
    virtual JNIEnv *GetJNIEnv(bool *isAttach);
    virtual jobject GetJavaObj();
    virtual JavaVM *GetJavaVM();

    static void PostPacket(void *context, uint8_t *buf,int size);
    static void PostMessage(void *context, int msgType, float msgCode);
    JavaVM *m_JavaVM = nullptr;
    jobject m_JavaObj = nullptr;
    FFDemux *m_demux = nullptr;

};


#endif //DEMUX_FFBRIDGE_H
