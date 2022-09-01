//
// Created by Administrator on 2022/4/14/014.
//

#include "FFDemux.h"


FFDemux::FFDemux() {

}

FFDemux::~FFDemux() {
    //UnInit();
}

void FFDemux::Start() {
    if(m_Thread == nullptr) {
        StartThread();
    } else {
        std::unique_lock<std::mutex> lock(m_Mutex);
        m_DecoderState = STATE_DEMUXING;
        m_Cond.notify_all();
    }
}


void FFDemux::Stop() {
    LOGD("FFDemux::Stop2222");
    std::unique_lock<std::mutex> lock(m_Mutex);
    m_DecoderState = STATE_STOP;
    m_Cond.notify_all();
}


int FFDemux::Init(const char *url) {
    strcpy(m_Url,url);
    return 0;
}

void FFDemux::UnInit() {

    if(m_Thread) {
        Stop();
        m_Thread->join();
        delete m_Thread;
        m_Thread = nullptr;
    }

}



int FFDemux::InitDemux() {
    int result = -1;
    do {
        //1.创建封装格式上下文
        m_AVFormatContext = avformat_alloc_context();

        //2.打开文件
        if(avformat_open_input(&m_AVFormatContext, m_Url, NULL, NULL) != 0)
        {
            LOGD("FFDemux::InitDemux avformat_open_input fail.");
            break;
        }

        //3.获取音视频流信息
        if(avformat_find_stream_info(m_AVFormatContext, NULL) < 0) {
            LOGD("FFDemux::InitDemux avformat_find_stream_info fail.");
            break;
        }

        //4.获取音视频流索引
        for(int i=0; i < m_AVFormatContext->nb_streams; i++) {
            if(m_AVFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                m_StreamIndex = i;
                break;
            }
        }

        if(m_StreamIndex == -1) {
            LOGD("FFDemux::InitFFDecoder Fail to find stream index.");
            break;
        }


        AVDictionary *pAVDictionary = nullptr;
        av_dict_set(&pAVDictionary, "buffer_size", "1024000", 0);
        av_dict_set(&pAVDictionary, "stimeout", "20000000", 0);
        av_dict_set(&pAVDictionary, "max_delay", "30000000", 0);
        av_dict_set(&pAVDictionary, "rtsp_transport", "tcp", 0);


        result = 0;

        m_Packet = av_packet_alloc();

    } while (false);

    if(result != 0 && m_MsgContext && m_MsgCallback)
        m_MsgCallback(m_MsgContext, MSG_DECODER_INIT_ERROR, 0);

    return result;
}

void FFDemux::StartThread() {
//    m_Thread = new thread(DoAVDecoding, this);
    m_Thread = new thread(DoDemux, this);
}



void FFDemux::DemuxLoop() {
    {
        std::unique_lock<std::mutex> lock(m_Mutex);
        m_DecoderState = STATE_DEMUXING;
        lock.unlock();
    }

    for(;;) {

        LOGD("111111111111111111111111111   m_DecoderState =%d", m_DecoderState);
        if(m_DecoderState == STATE_STOP) {
            break;
        }

        if(DecodeOnePacket() != 0) {
            //解码结束，暂停解码器
            std::unique_lock<std::mutex> lock(m_Mutex);
            m_DecoderState = STATE_ERROR;
        }
    }
}

int FFDemux::DecodeOnePacket() {

    int result = av_read_frame(m_AVFormatContext, m_Packet);
    while(result == 0) {
        if(m_DecoderState == STATE_STOP)
        {
            LOGD("111111111111111111111111111  m_DecoderState is stop");
            break;
        }
        if(m_Packet->stream_index == m_StreamIndex) {
            OnReceivePacket(m_Packet);
            //LOGD("111111111111111111111111111   BaseDecoder::DecodeOnePacket_Ex packet size =%d", m_Packet->size);
            //判断一个 packet 是否解码完成

        }
        av_packet_unref(m_Packet);
        result = av_read_frame(m_AVFormatContext, m_Packet);
    }

    __EXIT:
    av_packet_unref(m_Packet);
    return result;
}

void FFDemux::DoDemux(FFDemux *demux) {
    LOGD("FFDemux::DoDemux");
    do {
        if(demux->InitDemux() != 0) {
            break;
        }
        demux->DemuxLoop();
    } while (false);

    demux->DeInitDemux();
}



void FFDemux::DeInitDemux() {
    LOGD("FFDemux::DeInitDemux");


    if(m_Packet != nullptr) {
        av_packet_free(&m_Packet);
        m_Packet = nullptr;
    }


    if(m_AVFormatContext != nullptr) {
        avformat_close_input(&m_AVFormatContext);
        avformat_free_context(m_AVFormatContext);
        m_AVFormatContext = nullptr;
    }

}

void FFDemux::OnReceivePacket(AVPacket *packet) {

    if(m_MsgContext && m_MsgCallback)
        m_PacketCallback(m_MsgContext, packet->data,packet->size);
}