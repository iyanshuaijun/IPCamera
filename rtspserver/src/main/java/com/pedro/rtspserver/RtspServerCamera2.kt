package com.pedro.rtspserver

import android.content.Context
import android.media.MediaCodec
import android.os.Build
import android.view.SurfaceView
import com.pedro.encoder.utils.CodecUtil
import com.pedro.rtplibrary.base.Camera2Base
import com.pedro.rtplibrary.view.LightOpenGlView
import com.pedro.rtplibrary.view.OpenGlView
import com.pedro.rtsp.rtsp.VideoCodec
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import java.nio.ByteBuffer

open class RtspServerCamera2 : Camera2Base {

  private val rtspServer: RtspServer
  private var mCameraId: String = "0"
  constructor(surfaceView: SurfaceView, connectCheckerRtsp: ConnectCheckerRtsp, port: Int, cameraId: String) : super(
          surfaceView) {
    mCameraId = cameraId

    rtspServer = RtspServer(connectCheckerRtsp, port, cameraId)
  }

  constructor(openGlView: OpenGlView, connectCheckerRtsp: ConnectCheckerRtsp, port: Int, cameraId: String) : super(
    openGlView) {
    mCameraId = cameraId

    rtspServer = RtspServer(connectCheckerRtsp, port, cameraId)
  }

  constructor(lightOpenGlView: LightOpenGlView, connectCheckerRtsp: ConnectCheckerRtsp,
    port: Int, cameraId: String) : super(lightOpenGlView) {
    rtspServer = RtspServer(connectCheckerRtsp, port, cameraId)
  }

  constructor(context: Context, useOpengl: Boolean, connectCheckerRtsp: ConnectCheckerRtsp,
    port: Int, cameraId: String) : super(context, useOpengl) {
    rtspServer = RtspServer(connectCheckerRtsp, port, cameraId)
  }

  fun setVideoCodec(videoCodec: VideoCodec) {
    videoEncoder.type =
      if (videoCodec == VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
  }

  fun getNumClients(): Int = rtspServer.getNumClients()

  fun getEndPointConnection(): String = "rtsp://${rtspServer.serverIp}:${rtspServer.port}/"

  override fun setAuthorization(user: String, password: String) {
    rtspServer.setAuth(user, password)
  }

  fun startStream() {
    super.startStream("")
    switchCamera(mCameraId)
  }

  fun startServer() {
    rtspServer.startServer()
  }

  fun stopServer() {
    if (rtspServer.isRunning()) {
      rtspServer.stopServer(false)
    }
  }

  fun isServering(): Boolean {
    return rtspServer.isRunning()
  }

  override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
    rtspServer.isStereo = isStereo
    rtspServer.sampleRate = sampleRate
  }

  override fun startStreamRtp(url: String) { //unused
  }

  override fun stopStreamRtp() {
    rtspServer.stopServer(false)
  }

  override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtspServer.sendAudio(aacBuffer, info)
  }

  override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
    val newSps = sps.duplicate()
    val newPps = pps.duplicate()
    val newVps = vps?.duplicate()
    rtspServer.setVideoInfo(newSps, newPps, newVps)
  }

  override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtspServer.sendVideo(h264Buffer, info)
  }

  override fun setLogs(enable: Boolean) {
    rtspServer.setLogs(enable)
  }

  override fun setCheckServerAlive(enable: Boolean) {
  }

  /**
   * Unused functions
   */
  @Throws(RuntimeException::class)
  override fun resizeCache(newSize: Int) {
  }

  override fun shouldRetry(reason: String?): Boolean = false

  override fun hasCongestion(): Boolean = rtspServer.hasCongestion()

  override fun setReTries(reTries: Int) {
  }

  override fun reConnect(delay: Long, backupUrl: String?) {
  }

  override fun getCacheSize(): Int = 0

  override fun getSentAudioFrames(): Long = 0

  override fun getSentVideoFrames(): Long = 0

  override fun getDroppedAudioFrames(): Long = 0

  override fun getDroppedVideoFrames(): Long = 0

  override fun resetSentAudioFrames() {
  }

  override fun resetSentVideoFrames() {
  }

  override fun resetDroppedAudioFrames() {
  }

  override fun resetDroppedVideoFrames() {
  }
}