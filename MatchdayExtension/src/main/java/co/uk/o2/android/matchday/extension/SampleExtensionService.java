/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package co.uk.o2.android.matchday.extension;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.notification.NotificationUtil;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.registration.DisplayInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationAdapter;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * The Sample Extension Service handles registration and keeps track of all
 * controls on all accessories.
 */
public class SampleExtensionService extends ExtensionService {

    public static final String EXTENSION_KEY = "com.sonymobile.smartconnect.extension.samplecontrol.key";

    public static final String EXTENSION_SPECIFIC_ID = "EXTENSION_SPECIFIC_ID_SAMPLE_NOTIFICATION";

    public static final String LOG_TAG = "SampleControlExtension";

    private String hostAppPackageName = "";

    public SampleExtensionService() {
        super(EXTENSION_KEY);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(SampleExtensionService.LOG_TAG, "SampleControlService: onCreate");
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new SampleRegistrationInformation(this);
    }

    /*
     * (non-Javadoc)
     * @see com.sonyericsson.extras.liveware.aef.util.ExtensionService#
     * keepRunningWhenConnected()
     */
    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (MainActivity.SET_TEXT_INTENT.equals(intent.getAction())) {
            Bundle b = intent.getExtras();
            if (b != null) {
                if (!doActionOnControl(0,this.hostAppPackageName,intent.getExtras())) {
                    addData(intent.getExtras());
                    stopSelfCheck();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
        this.hostAppPackageName = hostAppPackageName;
        // First we check if the API level and screen size required for
        // SampleControlSmartWatch2 is supported
        // If not we return an API level 1 control based on screen size
        final int controlSWWidth = SimpleControlSmartWatch.getSupportedControlWidth(this);
        final int controlSWHeight = SimpleControlSmartWatch.getSupportedControlHeight(this);

        for (DeviceInfo device : RegistrationAdapter.getHostApplication(this,
                hostAppPackageName)
                .getDevices()) {
            for (DisplayInfo display : device.getDisplays()) {
                if (display.sizeEquals(controlSWWidth, controlSWHeight)) {
                    return new SimpleControlSmartWatch(hostAppPackageName, this, new Handler());
                }
            }
        }
        throw new IllegalArgumentException("No control for: " + hostAppPackageName);

    }

    private void addData(Bundle b) {
        String result = b.getString("homeScore") + " vs " + b.getString("awayScore");
        long time = System.currentTimeMillis();
        long sourceId = NotificationUtil
                .getSourceId(this, EXTENSION_SPECIFIC_ID);
        if (sourceId == NotificationUtil.INVALID_ID) {
            Log.e(LOG_TAG, "Failed to insert data");
            return;
        }
        String profileImage = ExtensionUtils.getUriString(this,
                R.drawable.widget_default_userpic_bg);

        ContentValues eventValues = new ContentValues();
        eventValues.put(Notification.EventColumns.EVENT_READ_STATUS, false);
        eventValues.put(Notification.EventColumns.DISPLAY_NAME, getString(R.string.app_name));
        eventValues.put(Notification.EventColumns.MESSAGE, result);
        eventValues.put(Notification.EventColumns.PERSONAL, 1);
        eventValues.put(Notification.EventColumns.PROFILE_IMAGE_URI, profileImage);
        eventValues.put(Notification.EventColumns.PUBLISHED_TIME, time);
        eventValues.put(Notification.EventColumns.SOURCE_ID, sourceId);

        try {
            getContentResolver().insert(Notification.Event.URI, eventValues);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Failed to insert event", e);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Failed to insert event, is Live Ware Manager installed?", e);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to insert event", e);
        }
    }


    @Override
    protected void onViewEvent(Intent intent) {
        String action = intent.getStringExtra(Notification.Intents.EXTRA_ACTION);
        String hostAppPackageName = intent
                .getStringExtra(Registration.Intents.EXTRA_AHA_PACKAGE_NAME);
        boolean advancedFeaturesSupported = DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(
                this, hostAppPackageName);

        int eventId = intent.getIntExtra(Notification.Intents.EXTRA_EVENT_ID, -1);
        if (Notification.SourceColumns.ACTION_1.equals(action)) {
            doAction1(eventId);
        } else if (Notification.SourceColumns.ACTION_2.equals(action)) {
            // Here we can take different actions depending on the device.
            if (advancedFeaturesSupported) {
                Toast.makeText(this, "Action 2 API level 2", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action 2", Toast.LENGTH_LONG).show();
            }
        } else if (Notification.SourceColumns.ACTION_3.equals(action)) {
            Toast.makeText(this, "Action 3", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show toast with event information
     *
     * @param eventId The event id
     */
    public void doAction1(int eventId) {
        Log.d(LOG_TAG, "doAction1 event id: " + eventId);
        Cursor cursor = null;
        try {
            String name = "";
            String message = "";
            cursor = getContentResolver().query(Notification.Event.URI, null,
                    Notification.EventColumns._ID + " = " + eventId, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(Notification.EventColumns.DISPLAY_NAME);
                int messageIndex = cursor.getColumnIndex(Notification.EventColumns.MESSAGE);
                name = cursor.getString(nameIndex);
                message = cursor.getString(messageIndex);
            }

            String toastMessage = getText(R.string.action_event_1) + ", Event: " + eventId
                    + ", Name: " + name + ", Message: " + message;
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to query event", e);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Failed to query event", e);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Failed to query event", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
