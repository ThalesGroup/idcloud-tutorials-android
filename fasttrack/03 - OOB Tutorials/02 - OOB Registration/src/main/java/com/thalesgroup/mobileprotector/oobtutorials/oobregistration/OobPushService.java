/*
 * MIT License
 *
 * Copyright (c) 2020 Thales DIS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * IMPORTANT: This source code is intended to serve training information purposes only.
 *            Please make sure to review our IdCloud documentation, including security guidelines.
 */

package com.thalesgroup.mobileprotector.oobtutorials.oobregistration;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.core.app.NotificationCompat;

/**
 * Handle of incoming push notifications and token changes.
 */
public class OobPushService extends FirebaseMessagingService {
    private static final String TAG = OobPushService.class.getSimpleName();

    private static final String C_DATA_KEY_MESSAGE = "message";

    //region FirebaseMessagingService

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        Log.w(TAG, "onMessageReceived --> FCM received message: " + data);

        // Ignore non data notifications.
        if (data == null)
            return;

        if (OobRegistrationLogic.sOobPushCallback != null)
            OobRegistrationLogic.sOobPushCallback.informPushMessageReceived(data);

        // Process notification
        sendNotification(data);
    }

    @Override
    public void onNewToken(String token) {
        Log.w(TAG, "OobPushService --> FCM token: " + token);

        // NOTE: Need to make sure the FCM token is stored BEFORE executing the provisioning phase!
        OobRegistrationLogic.updateNotificationProfile(token);

        if (OobRegistrationLogic.mFCMTokenCallback != null)
            OobRegistrationLogic.mFCMTokenCallback.onTokenValid();
    }

    //endregion

    //region Private Helpers

    /**
     * Parses the incoming message data and shows a notification in the status bar.
     *
     * @param data Received data.
     */
    private void sendNotification(Map<String, String> data) {
        // First try to get notification manager.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)
            return;

        String channelId = getString(R.string.push_fcm_channel_id);
        String message = data.containsKey(C_DATA_KEY_MESSAGE) ? data.get(C_DATA_KEY_MESSAGE)
                : getString(R.string.push_approve_question);

        // New android does require channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId,
                    getString(R.string.push_fcm_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel."MSP Frame Channel"
            notificationChannel.setDescription(getString(R.string.push_fcm_channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{ 0, 1000, 500, 1000 });
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Build final notification.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(getString(R.string.notification_caption))
                .setAutoCancel(true)
                .setContentText(message);

        notificationManager.notify(1, notificationBuilder.build());
    }
    //endregion
}