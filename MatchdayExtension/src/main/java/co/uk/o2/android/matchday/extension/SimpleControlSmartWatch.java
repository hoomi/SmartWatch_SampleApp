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
        sendText(R.id.v_home_team_name, "England");
        sendText(R.id.v_away_team_name,"Argentina");
        sendText(R.id.v_away_score2, 0 + "");
        sendText(R.id.v_away_score3, 0 + "");
        sendText(R.id.v_home_score2, 0 + "");
        sendText(R.id.v_home_score3, 0 + "");


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
            int homeScore = bundle.getInt("homeScore");
            updateScore(true,homeScore);
            int awayScore = bundle.getInt("awayScore");
            updateScore(false,awayScore);
            if (hasVibrator()) {
                startVibrator(500,200,2);
            }
        } else {
            super.onDoAction(requestCode, bundle);
        }
    }


    private void updateScore(boolean home, int score) {
        int hundreds, tens, ones;
        hundreds = score / 100;
        tens = (score - hundreds * 100) / 10;
        ones = score % 10;
        int id2,id3;
        if (home) {
            id2 = R.id.v_home_score2;
            id3 = R.id.v_home_score3;
        } else {
            id2 = R.id.v_away_score2;
            id3 = R.id.v_away_score3;
        }
        sendText(id2,tens+"");
        sendText(id3,ones+"");
    }

}

