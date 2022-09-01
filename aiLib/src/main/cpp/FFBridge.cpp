//
// Created by Administrator on 2022/4/14/014.
//

#include "FFBridge.h"

void FFBridge::Init(JNIEnv *jniEnv, jobject obj, char *url) {
    jniEnv->GetJavaVM(&m_JavaVM);
    m_JavaObj = jniEnv->NewGlobalRef(obj);

    m_demux = new FFDemux();
    m_demux->Init(url);
    m_demux->SetMessageCallback(this, PostMessage);
    m_demux->SetPacketCallback(this, PostPacket);

}

void FFBridge::Start() {
    LOGD("FFBridge::Start");
    if(m_demux)
        m_demux->Start();
}

void FFBridge::UnInit() {
    LOGD("FFBridge::UnInit");
    if(m_demux) {
        Stop();
        delete m_demux;
        m_demux = nullptr;
    }

    bool isAttach = false;
    GetJNIEnv(&isAttach)->DeleteGlobalRef(m_JavaObj);
    if(isAttach)
        GetJavaVM()->DetachCurrentThread();

}



void FFBridge::Stop() {
    LOGD("FFBridge::Stop");
    if(m_demux)
    {
        m_demux->UnInit();
    }
}


JNIEnv *FFBridge::GetJNIEnv(bool *isAttach) {
    JNIEnv *env;
    int status;
    if (nullptr == m_JavaVM) {
        LOGD("FFBridge::GetJNIEnv m_JavaVM == nullptr");
        return nullptr;
    }
    *isAttach = false;
    status = m_JavaVM->GetEnv((void **)&env, JNI_VERSION_1_4);
    if (status != JNI_OK) {
        status = m_JavaVM->AttachCurrentThread(&env, nullptr);
        if (status != JNI_OK) {
            LOGD("FFBridge::GetJNIEnv failed to attach current thread");
            return nullptr;
        }
        *isAttach = true;
    }
    return env;
}


jobject FFBridge::GetJavaObj() {
    return m_JavaObj;
}

JavaVM *FFBridge::GetJavaVM() {
    return m_JavaVM;
}


void FFBridge::PostMessage(void *context, int msgType, float msgCode) {
    if(context != nullptr)
    {
        FFBridge *player = static_cast<FFBridge *>(context);
        bool isAttach = false;
        JNIEnv *env = player->GetJNIEnv(&isAttach);
       // LOGD("FFBridge::PostMessage env=%p", env);
        if(env == nullptr)
            return;
        jobject javaObj = player->GetJavaObj();
        jmethodID mid = env->GetMethodID(env->GetObjectClass(javaObj), JAVA_MESSAGE_EVENT_CALLBACK_API_NAME, "(IF)V");
        env->CallVoidMethod(javaObj, mid, msgType, msgCode);
        if(isAttach)
            player->GetJavaVM()->DetachCurrentThread();

    }
}



void FFBridge::PostPacket(void *context,  uint8_t *buf,int size) {
    if(context != nullptr)
    {
        FFBridge *player = static_cast<FFBridge *>(context);
        bool isAttach = false;
        JNIEnv *env = player->GetJNIEnv(&isAttach);
        //LOGD("FFBridge::PostPacket env=%p", env);
        if(env == nullptr)
            return;
        jobject javaObj = player->GetJavaObj();

        jbyteArray  array1 = env->NewByteArray(size);
        env->SetByteArrayRegion(array1,0,size,(jbyte*)buf);


        jmethodID mid = env->GetMethodID(env->GetObjectClass(javaObj), JAVA_PACKET_EVENT_CALLBACK_API_NAME, "([B)V");
        env->CallVoidMethod(javaObj, mid, array1);

        env->DeleteLocalRef(array1);

        if(isAttach)
            player->GetJavaVM()->DetachCurrentThread();

    }
}
