package at.drizzd.Andenv;

import java.lang.Thread;
import java.lang.String;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class Andenv extends Activity {
    private static String TAG = "Andenv";
    Chart mChart;
    volatile boolean mActive = true;
    Thread mGenerator;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mChart = new Chart(this);
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        layout.addView(mChart.getView());

        mGenerator = new Thread(new Runnable() {
            public void run() {
                try {
                    int x = 0;
                    int y = 0;
                    while (mActive) {
                        mChart.publishUpdate(x, y);
                        x++;
                        y = (y + 1) % 50;
                        sleep(50);
                    }
                } catch (Throwable t) {
                    Log.wtf(TAG, t);
                }
            }
        });
        mGenerator.start();
    }

    public void onDestroy() {
        mActive = false;
        try {
            if (mGenerator != null) {
                mGenerator.join();
            }
        } catch (Throwable t) {
            Log.wtf(TAG, t);
        }
        super.onDestroy();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable t) {
            Log.wtf(TAG, t);
        }
    }
}
