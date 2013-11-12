package co.uk.o2.android.matchday.extension;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final String SET_TEXT_INTENT = "uk.co.o2.android.matchday.score.updated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        Bundle b = new Bundle();
        b.putInt("homeScore", new Random().nextInt(100));
        b.putInt("awayScore", new Random().nextInt(100));
        startService(new Intent(SET_TEXT_INTENT).setClass(this, SampleExtensionService.class).putExtras(b));
    }
}
