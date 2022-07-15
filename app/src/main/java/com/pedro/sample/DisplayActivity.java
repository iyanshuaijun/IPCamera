/*
 * Copyright (C) 2021 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pedro.sample;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtpstreamer.displayexample.DisplayService;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.DisplayBase}
 * {@link com.pedro.rtplibrary.rtmp.RtmpDisplay}
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DisplayActivity extends AppCompatActivity
    implements ConnectCheckerRtsp, View.OnClickListener {

  private Button button;
  private EditText etUrl;
  private final int REQUEST_CODE_STREAM = 179; //random num
  private final int REQUEST_CODE_RECORD = 180; //random num

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_display);
    button = findViewById(R.id.start_stop_dis);
    button.setOnClickListener(this);
    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtsp);
    DisplayService displayService = DisplayService.Companion.getINSTANCE();
    //No streaming/recording start service
    if (displayService == null) {
      startService(new Intent(this, DisplayService.class));
    }
    if (displayService != null && displayService.isStreaming()) {
      button.setText(R.string.stop_button);
    } else {
      button.setText(R.string.start_button);
    }

    Uri uri = Uri.parse("rtsp://192.168.61.224:1234");
    VideoView videoView = (VideoView)this.findViewById(R.id.video_view);
    videoView.setMediaController(new MediaController(this));
    videoView.setVideoURI(uri);
    videoView.start();
    videoView.requestFocus();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    DisplayService displayService = DisplayService.Companion.getINSTANCE();
    if (displayService != null && !displayService.isStreaming() && !displayService.isRecording()) {
      //stop service only if no streaming or recording
      stopService(new Intent(this, DisplayService.class));
    }
  }

  @Override
  public void onConnectionStartedRtsp(String rtmpUrl) {
  }

  @Override
  public void onConnectionSuccessRtsp(String cameraId) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtsp(final String reason, String cameraId) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
            .show();
        DisplayService displayService = DisplayService.Companion.getINSTANCE();
        if (displayService != null) {
          displayService.stopStream();
        }
        button.setText(R.string.start_button);
      }
    });
  }

  @Override
  public void onNewBitrateRtsp(long bitrate) {

  }

  @Override
  public void onDisconnectRtsp(String cameraId) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtsp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtsp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null && (requestCode == REQUEST_CODE_STREAM
        || requestCode == REQUEST_CODE_RECORD && resultCode == Activity.RESULT_OK)) {
      DisplayService displayService = DisplayService.Companion.getINSTANCE();
      if (displayService != null) {
        displayService.prepareStreamRtp(resultCode, data);
        displayService.startStreamRtp();
        etUrl.setText(displayService.getPoint());
      }
    } else {
      Toast.makeText(this, "No permissions available", Toast.LENGTH_SHORT).show();
      button.setText(R.string.start_button);
    }
  }

  @Override
  public void onClick(View view) {
    DisplayService displayService = DisplayService.Companion.getINSTANCE();
    if (displayService != null) {
      switch (view.getId()) {
        case R.id.start_stop_dis:
          if (!displayService.isStreaming()) {
            button.setText(R.string.stop_button);
            startActivityForResult(displayService.sendIntent(), REQUEST_CODE_STREAM);
          } else {
            button.setText(R.string.start_button);
            displayService.stopStream();
          }
          break;
        default:
          break;
      }
    }
  }
}
