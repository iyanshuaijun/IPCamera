package com.pedro.sample

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.encoder.input.gl.render.filters.`object`.TextObjectFilterRender
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.encoder.utils.gl.TranslateTo
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerCamera2
import com.ubeesky.lib.ai.AIDetectResult
import com.ubeesky.lib.ai.AINative
import com.ubeesky.lib.ai.FileUtils
import kotlinx.android.synthetic.main.activity_camera_demo.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraDemoActivity : AppCompatActivity(), ConnectCheckerRtsp, View.OnClickListener,
    SurfaceHolder.Callback, AINative.AICallback {

    private lateinit var rtspServerCamera1: RtspServerCamera2
    private lateinit var button: Button
    private lateinit var bRecord: Button

    private var currentDateAndTime = ""
    private var currentDateAndTime_watermark = ""
    private lateinit var folder: File

    private val textObjectFilterRender = TextObjectFilterRender()
    private var strList_top_left : ArrayList<String> = arrayListOf()
    private var strList_top_right : ArrayList<String> = arrayListOf()
    private var strList_bottom_left : ArrayList<String> = arrayListOf()
    private var strList_bottom_right : ArrayList<String> = arrayListOf()

    private lateinit var aiNative: AINative
    private lateinit var overlayView: OverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera_demo)
        folder = File(getExternalFilesDir(null)!!.absolutePath + "/rtmp-rtsp-stream-client-java")
        button = findViewById(R.id.b_start_stop)
        button.setOnClickListener(this)
        bRecord = findViewById(R.id.b_record)
        bRecord.setOnClickListener(this)
        switch_camera.setOnClickListener(this)
        overlayView = findViewById(R.id.overlay)
        aiNative = AINative(this)
        modelInit()
        rtspServerCamera1 = RtspServerCamera2(surfaceView, this, 1935, "0", aiNative)
//        rtspServerCamera1.setVideoCodec(VideoCodec.H265)
//        surfaceView.holder.addCallback(this)
    }

    override fun onResume() {
        super.onResume()
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                //要推迟执行的方法
                textObjectFilterRender.updateStringList(
                    strList_top_left,
                    intArrayOf(0, 1),
                    arrayOf(System.currentTimeMillis().toString(), (System.currentTimeMillis() * 2).toString()),
                    TranslateTo.TOP_LEFT
                )
                textObjectFilterRender.updateStringList(
                    strList_bottom_right,
                    intArrayOf(1, 3),
                    arrayOf((System.currentTimeMillis() * 3).toString(), (System.currentTimeMillis() * 4).toString()),
                    TranslateTo.BOTTOM_RIGHT
                )
            }
        }
        timer.schedule(task, 1000, 5000)
    }

    override fun onNewBitrateRtsp(bitrate: Long) {

    }

    override fun onConnectionSuccessRtsp(cameraId: String) {
        runOnUiThread {
            Toast.makeText(this@CameraDemoActivity, "Connection success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailedRtsp(reason: String, cameraId: String) {
        runOnUiThread {
            Toast.makeText(
                this@CameraDemoActivity,
                "Connection failed. $reason",
                Toast.LENGTH_SHORT
            )
                .show()
            rtspServerCamera1.stopStream()
            button.setText(R.string.start_button)
        }
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {
    }

    override fun onDisconnectRtsp(cameraId: String) {
        runOnUiThread {
            Toast.makeText(this@CameraDemoActivity, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthErrorRtsp() {
        runOnUiThread {
            Toast.makeText(this@CameraDemoActivity, "Auth error", Toast.LENGTH_SHORT).show()
            rtspServerCamera1.stopStream()
            button.setText(R.string.start_button)
            tv_url.text = ""
        }
    }

    override fun onAuthSuccessRtsp() {
        runOnUiThread {
            Toast.makeText(this@CameraDemoActivity, "Auth success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.b_start_stop -> if (!rtspServerCamera1.isStreaming) {
                if (rtspServerCamera1.isRecording || rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo(1920, 1080)) {
                    button.setText(R.string.stop_button)
                    initTimeWaterMarkFormat()
                    rtspServerCamera1.startStream()
                    tv_url.text = rtspServerCamera1.getEndPointConnection()
                } else {
                    Toast.makeText(
                        this,
                        "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                button.setText(R.string.start_button)
                rtspServerCamera1.stopStream()
                tv_url.text = ""
                aiNative.deinit()
            }
            R.id.switch_camera -> try {
                rtspServerCamera1.switchCamera()
            } catch (e: CameraOpenException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }

            R.id.b_record -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (!rtspServerCamera1.isRecording) {
                        try {
                            if (!folder.exists()) {
                                folder.mkdir()
                            }
                            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            currentDateAndTime = sdf.format(Date())
                            if (!rtspServerCamera1.isStreaming) {
                                if (rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo(1920, 1080)) {
                                    rtspServerCamera1.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
                                    bRecord.setText(R.string.stop_record)
                                    Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        this, "Error preparing stream, This device cant do it",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                rtspServerCamera1.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
                                bRecord.setText(R.string.stop_record)
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            rtspServerCamera1.stopRecord()
                            bRecord.setText(R.string.start_record)
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        rtspServerCamera1.stopRecord()
                        bRecord.setText(R.string.start_record)
                        Toast.makeText(
                            this,
                            "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "You need min JELLY_BEAN_MR2(API 18) for do it...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
            }
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        rtspServerCamera1.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (rtspServerCamera1.isRecording) {
                rtspServerCamera1.stopRecord()
                bRecord.setText(R.string.start_record)
                Toast.makeText(
                    this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
                    Toast.LENGTH_SHORT
                ).show()
                currentDateAndTime = ""
            }
        }
        if (rtspServerCamera1.isStreaming) {
            rtspServerCamera1.stopStream()
            button.text = resources.getString(R.string.start_button)
            tv_url.text = ""
        }
        rtspServerCamera1.stopPreview()
    }

    private fun initTimeWaterMarkFormat() {
        rtspServerCamera1.glInterface.setFilter(textObjectFilterRender)
        textObjectFilterRender.setDefaultScale(640, 480)
        initStrList()
        textObjectFilterRender.setImageTextureList(strList_top_left, TranslateTo.TOP_LEFT)
        textObjectFilterRender.setImageTextureList(strList_top_right, TranslateTo.TOP_RIGHT)
        textObjectFilterRender.setImageTextureList(strList_bottom_left, TranslateTo.BOTTOM_LEFT)
        textObjectFilterRender.setImageTextureList(strList_bottom_right, TranslateTo.BOTTOM_RIGHT)
    }

    private fun initStrList() {
        strList_top_left.add("0.0V 0.0V 0.0A 0% 0℃ T")
        strList_top_left.add("SIM卡盖未拧紧")

        strList_top_right.add("水印 2.1")
        strList_top_right.add("水印 2.2")

        strList_bottom_left.add("水印 3.1")
        strList_bottom_left.add("水印 3.2")
        strList_bottom_left.add("水印 3.3")

        strList_bottom_right.add("水印 4.1")
        strList_bottom_right.add("水印 4.2")
        strList_bottom_right.add("水印 4.3")
        strList_bottom_right.add("水印 4.4")

    }

    private fun modelInit() {
        val modelPath: String = copyModel().toString()
        val ret: Int = aiNative.init(modelPath, 1)
        val msg = "模型初始化：" + if (ret == -1) "失败" else "成功"
    }

    private fun copyModel(): String? {
        val targetDir: String = this.filesDir.absolutePath
        val modelPathsDetector = arrayOf(
            "nanodet_m.tnnmodel",
            "nanodet_m.tnnproto"
        )
        for (i in modelPathsDetector.indices) {
            val modelFilePath = modelPathsDetector[i]
            val interModelFilePath = "$targetDir/$modelFilePath"
            FileUtils.copyAsset(
                this.assets,
                "model/$modelFilePath", interModelFilePath
            )
        }
        return targetDir
    }

    override fun steamAIResult(results: Array<AIDetectResult>?) {
        overlayView.setResults(results)
        printArray(results)
    }

    override fun imageAIResult(results: Array<AIDetectResult>?) {
        TODO("Not yet implemented")
    }

    private fun printArray(aiDetectResults: Array<AIDetectResult>?) {
        if (aiDetectResults == null) {
            return
        }
        for (result in aiDetectResults) {
            Log.d("cc", result.toString())

        }
    }


}
