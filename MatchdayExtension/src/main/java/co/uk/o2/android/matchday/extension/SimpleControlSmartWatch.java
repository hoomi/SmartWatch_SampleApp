package co.uk.o2.android.matchday.extension;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;


class SimpleControlSmartWatch extends ControlExtension {


    /**
     * Create sample control.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context            The context.
     * @param handler            The handler to use
     */
    SimpleControlSmartWatch(final String hostAppPackageName, final Context context,
                            Handler handler) {
        super(context, hostAppPackageName);
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }

    }

    /**
     * Get supported control width.
     *
     * @param context The context.
     * @return the width.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
    }

    /**
     * Get supported control height.
     *
     * @param context The context.
     * @return the height.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
    }


    @Override
    public void onDestroy() {

        Log.d(SampleExtensionService.LOG_TAG, "SampleControlSmartWatch onDestroy");
    }

    @Override
    public void onStart() {
        // Nothing to do. Animation is handled in onResume.
        showLayout(R.layout.fragment_main, null);

    }

    @Override
    public void onStop() {
        // Nothing to do. Animation is handled in onPause.
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onObjectClick(ControlObjectClickEvent event) {

    }

    @Override
    public void onDoAction(int requestCode, Bundle bundle) {
        if (requestCode == 0 ) {
            String homeScore = bundle.getString("homeScore");
            String awayScore = bundle.getString("awayScore");
            sendText(R.id.home_score_TextView,homeScore);
            sendText(R.id.away_score_TextView,awayScore);
            if (hasVibrator()) {
                startVibrator(500,200,2);
            }
        } else {
            super.onDoAction(requestCode, bundle);
        }
    }



}

