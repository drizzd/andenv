package at.drizzd.Andenv;

import java.lang.Math;
import java.lang.Thread;
import java.util.Arrays;
import java.lang.InterruptedException;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.media.AudioRecord;

public class Andenv extends Activity
{
    private static String TAG = "Andenv";
    final int audioSource = MediaRecorder.AudioSource.MIC;
    final int sampleRate = 44100;
    final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    final int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
    final int bufferSizeInBytes = AudioRecord.getMinBufferSize(
            sampleRate, channelConfig, audioFormat);
    Thread runner;
    AudioRecord mAudioRecord;
    volatile boolean mActive = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (bufferSizeInBytes < 0) {
            Log.e(TAG, "getMinBufferSize error: " + bufferSizeInBytes);
        }
        mAudioRecord = new AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSizeInBytes);

        mAudioRecord.startRecording();
        new Thread(new Runnable() {
            public void run() {
                byte[] audioData = new byte[bufferSizeInBytes];
                while (mActive) {
                    int ret = mAudioRecord.read(audioData, 0, audioData.length);
                    if (ret < 0) {
                        Log.e(TAG, "AudioRecord.read: " + ret);
                        break;
                    }

                    String audioDataStr = Arrays.toString(audioData);
                    Log.i(TAG, ". " + audioDataStr.substring(0, Math.min(70, audioDataStr.length())));
                    nap(1000);
                }
            }
        });
    }

    public void onDestroy() {
        mActive = false;
        try {
            runner.join();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        mAudioRecord.stop();

        super.onDestroy();
    }

    private void nap(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
