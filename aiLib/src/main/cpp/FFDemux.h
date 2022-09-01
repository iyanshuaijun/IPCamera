//
// Created by Administrator on 2022/4/14/014.
//

#ifndef DEMUX_FFDEMUX_H
#define DEMUX_FFDEMUX_H

#include <thread>
#include "CCNDKLogDef.h"
extern "C"
{
    #include <libavutil/time.h>
    #include <libavcodec/avcodec.h>
    #include <libavcodec/packet.h>
    #include <libavutil/imgutils.h>
    #include <libswscale/swscale.h>
    #include <libavformat/avformat.h>
    #include <libswscale/swscale.h>
    #include <libavutil/opt.h>
};


using namespace std;

#define MAX_PATH   512

enum DecoderState {
    STATE_UNKNOWN,
    STATE_DEMUXING,
    STATE_ERROR,
    STATE_STOP
};

enum DecoderMsg {
    MSG_DECODER_INIT_ERROR,
    MSG_DECODER_READY,
    MSG_DECODER_DONE,
    MSG_REQUEST_RENDER,
    MSG_DECODING_TIME
};

typedef void (*MessageCallback)(void*, int, float);
typedef void (*PacketCallback)(void*,uint8_t *buf, int size);


class FFDemux {
public:
    FFDemux();
    ~FFDemux();
    void Start();
    void Stop();

    void SetMessageCallback(void* context, MessageCallback callback)
    {
        m_MsgContext = context;
        m_MsgCallback = callback;
    }

    void SetPacketCallback(void* context, PacketCallback callback)
    {
        m_MsgContext = context;
        m_PacketCallback = callback;
    }

    void * m_MsgContext = nullptr;
    MessageCallback m_MsgCallback = nullptr;
    PacketCallback m_PacketCallback = nullptr;

    int Init(const char *url);
    void UnInit();
    void OnReceivePacket(AVPacket * packet);


private:
    int InitDemux();
    void DeInitDemux();
    //启动解码线程
    void StartThread();

    void DemuxLoop();

    //解码一个packet编码数据
    int DecodeOnePacket();

    static void DoDemux(FFDemux *demux);

    //封装格式上下文
    AVFormatContext *m_AVFormatContext = nullptr;
    //解码器上下文
    AVPacket        *m_Packet = nullptr;
    //文件地址
    char       m_Url[MAX_PATH] = {0};

    //数据流索引
    int              m_StreamIndex = -1;
    //锁和条件变量
    mutex               m_Mutex;
    condition_variable  m_Cond;
    thread             *m_Thread = nullptr;
    //解码器状态
    volatile int  m_DecoderState = STATE_UNKNOWN;

};


#endif //DEMUX_FFDEMUX_H
