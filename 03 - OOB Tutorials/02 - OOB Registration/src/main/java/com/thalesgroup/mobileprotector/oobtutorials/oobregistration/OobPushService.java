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
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Handle of incoming push notifications and token changes.
 */
public class OobPushService extends FirebaseMessagingService {

    private static final String C_DATA_KEY_MESSAGE = "message";

    public static OobPushCallback sOobPushCallback;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        // Ignore non data notifications.
        if (remoteMessage.getData() == null) {
            return;
        }

        if (sOobPushCallback != null) {
            sOobPushCallback.informPushMessageReceived(remoteMessage.getData());
        }

        // Process notification
        sendNotification(remoteMessage.getData());
    }

    @Override
    public void onNewToken(final String token) {
        OobRegistrationLogic.updateNotificationProfile(token);
    }

    /**
     * Parses the incoming message data and shows a notification in the status bar.
     *
     * @param data
     *         Received data.
     */
    private void sendNotification(final Map<String, String> data) {
        // First try to get notification manager.
        final NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        final String channelId = getString(R.string.push_fcm_channel_id);
        final String message = data.containsKey(C_DATA_KEY_MESSAGE) ? data.get(C_DATA_KEY_MESSAGE)
                                                                    : getString(R.string.push_approve_question);

        // New android does require channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel notificationChannel = new NotificationChannel(channelId,
                                                                                    getString(R.string.push_fcm_channel_name),
                                                                                    NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel."MSP Frame Channel"
            notificationChannel.setDescription(getString(R.string.push_fcm_channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Build final notification.
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        notificationBuilder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                           .setWhen(System.currentTimeMillis())
                           .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                           .setContentTitle(getString(R.string.notification_caption)).setAutoCancel(true).setContentText(message);

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }
}