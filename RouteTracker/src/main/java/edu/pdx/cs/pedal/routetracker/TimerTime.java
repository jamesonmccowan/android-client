package edu.pdx.cs.pedal.routetracker;

/**
 * Created on 2/28/14.
 */




import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;

public class TimerTime extends Activity {
    Chronometer mChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button button;


        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(new OnChronometerTickListener() {
            public void onChronometerTick(Chronometer times) {
                long timeElapsed = SystemClock.elapsedRealtime() - times.getBase();

                int hours = (int) (timeElapsed / 3600000);
                int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
                int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;

                times.setText(hours+":"+minutes+":"+seconds);

            }
        });

        // Watch for button clicks.
        button = (Button) findViewById(R.id.trackingToggleButton);
        button.setOnClickListener(mStartListener);

    }

    View.OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            mChronometer.start();

            mChronometer.setBase(SystemClock.elapsedRealtime());


        }
    };

    View.OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            mChronometer.stop();


        }
    };

    View.OnClickListener mResetListener = new OnClickListener() {
        public void onClick(View v) {
            mChronometer.setBase(SystemClock.elapsedRealtime());
        }
    };
}
