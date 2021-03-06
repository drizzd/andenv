package com.example.andenv;

import java.lang.Short;
import java.lang.Math;
import java.lang.Thread;
import java.util.Arrays;
import java.lang.InterruptedException;
import java.lang.String;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.os.Bundle;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.media.AudioRecord;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    /* Update rate in multiples per second. */
    final int updateRate = 10;
    /* Averaging period in seconds. */
    final double avgPeriod = 1.0/updateRate/2.0;
    /* Graph width in seconds. */
    final double graphWidth = 20;

    private static String TAG = "Andenv";
    final int audioSource = MediaRecorder.AudioSource.MIC;
    final int sampleRate = 44100;
    final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    final int bytesPerSample = 2;
    final int readBufferLen = sampleRate/updateRate;
    int bufferSizeInBytes;
    Thread mConsumer;
    AudioRecord mAudioRecord;
    volatile boolean mActive = true;
    Chart mChart;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChart = new Chart(this, graphWidth, "MIC", "sec", "dB");
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        layout.addView(mChart.getView());

        bufferSizeInBytes = AudioRecord.getMinBufferSize(
                sampleRate, channelConfig, audioFormat);
        if (bufferSizeInBytes < 0) {
            Log.e(TAG, "AudioRecord.getMinBufferSize: " + bufferSizeInBytes);
            return;
        }
        Log.i(TAG, "minimum buffer size: " + bufferSizeInBytes);
        bufferSizeInBytes = Math.max(readBufferLen * bytesPerSample, bufferSizeInBytes);
        Log.i(TAG, "selected buffer size: " + bufferSizeInBytes);

        startRecording();
    }

    public void startRecording() {
        try {
            mAudioRecord = new AudioRecord(
                    audioSource,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSizeInBytes);

            mAudioRecord.startRecording();
        } catch (Throwable t) {
            Log.e(TAG, "AudioRecord", t);
            return;
        }

        mConsumer = new Thread(new Runnable() {
            public void run() {
                double timestamp = 0;
                double avgLen = Math.max(1.0, sampleRate * avgPeriod);
                double alpha = 2/(avgLen+1);
                double power = 0;
                short[] readBuffer = new short[readBufferLen];

                while (mActive) {
                    int ret = mAudioRecord.read(readBuffer, 0, readBuffer.length);
                    if (ret < 0) {
                        Log.e(TAG, "AudioRecord.read: " + ret);
                        break;
                    }
                    int nSamples = ret;

                    timestamp += (double)nSamples/sampleRate;

                    for (int i = 0; i < nSamples; i++) {
                        double a = (double)readBuffer[i]/Short.MAX_VALUE;
                        double p = 2 * a * a;
                        power = alpha * p + (1-alpha) * power;
                    }

                    double logPower = 10 * Math.log10(power);
                    if (power < 1e-10) {
                        logPower = -100;
                    }
                    mChart.publishUpdate(timestamp, logPower);
                }
            }
        });
        mConsumer.start();
    }

    public void onDestroy() {
        mActive = false;
        try {
            if (mConsumer != null)
                mConsumer.join();
        } catch (Throwable t) {
            Log.e(TAG, "Thread.join", t);
        }
        if (mAudioRecord != null)
            mAudioRecord.stop();

        super.onDestroy();
    }
}