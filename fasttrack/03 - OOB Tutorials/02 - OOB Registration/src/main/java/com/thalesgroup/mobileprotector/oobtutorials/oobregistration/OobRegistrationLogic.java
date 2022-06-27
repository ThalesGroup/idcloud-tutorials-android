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

import android.content.SharedPreferences;

import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.messenger.MobileMessenger;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.MobileMessengerCallback;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.RegistrationCallback;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.RegistrationResponse;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericHandler;
import com.thalesgroup.mobileprotector.oobtutorials.oobsetup.OobSetupLogic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Implementation of OOB registration and notification updates.
 */
public class OobRegistrationLogic extends OobSetupLogic {

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String PUSH_TOKEN = "PUSH_TOKEN";

    /**
     * Callback to notify that FCM token has been configured
     */
    public static OobFCMTokenCallback mFCMTokenCallback = null;

    /**
     * Callback to notify that there is an incoming push message
     */
    public static OobPushCallback sOobPushCallback;

    /**
     * Check if FCM token is valid or not
     *
     * @return true if FCM token is valid, otherwise false
     */
    public static boolean isFCMTokenValid() {
        return readFcmPushToken() != null;
    }

    /**
     * Processes registration to OOB - Mobile secure messenger server.
     *
     * @param userId            User ID received from Bank portal.
     * @param regCode           Registration code received from Bank portal.
     * @param completionHandler Handled on which UI part should be notified about result.
     */
    public synchronized static void registerToMsm(@NonNull final String userId,
                                                  @NonNull final String regCode,
                                                  @NonNull final GenericHandler completionHandler) {
        final MobileMessenger messenger = getMobileMessenger();
        messenger.register(userId, userId, regCode, null,
                new RegistrationCallback() {
                    @Override
                    public void onRegistrationResponse(@NonNull final RegistrationResponse response) {
                        final String clientId = response.getClientId();
                        storeClientId(clientId);

                        // FCM token is valid?
                        final String fcmToken = readFcmPushToken();
                        if (fcmToken == null) {
                            completionHandler.onFinished(false, "FCM token is not available!");
                            return;
                        }

                        // Then register notification profile
                        registerOOBClientId(clientId, fcmToken, completionHandler);
                    }

                    @Override
                    public void onError(@NonNull final FastTrackException exception) {
                        completionHandler.onFinished(false, exception.getLocalizedMessage());
                    }
                }
        );
    }

    /**
     * @param completionHandler Handled on which UI part should be notified about result.
     */
    public static void unregisterFromMsm(@NonNull final GenericHandler completionHandler) {
        do {
            final String clientId = readClientId();
            if (clientId == null) {
                completionHandler.onFinished(true, null);
                break;
            }

            final MobileMessenger messenger = getMobileMessenger();
            messenger.unregister(clientId, null,
                    new MobileMessengerCallback() {
                        @Override
                        public void onSuccess() {
                            storeClientId(null);
                            completionHandler.onFinished(true, null);
                        }

                        @Override
                        public void onError(final FastTrackException exception) {
                            completionHandler.onFinished(false, exception.getLocalizedMessage());
                        }
                    }
            );
        } while (false);
    }

    /**
     * Updates notification profiles on server side, so push message can reach the device.
     *
     * @param fcmPushToken Updated Firebase push token, which should be updated also on server side.
     */
    static synchronized void updateNotificationProfile(@NonNull final String fcmPushToken) {
        // Store token and update notification profile if user is registered
        storeFcmPushToken(fcmPushToken);

        // Client id is null mean, that user is not yet registered.
        final String clientId = readClientId();
        if (clientId != null) {
            registerOOBClientId(clientId, fcmPushToken, null);
        }
    }

    private static void registerOOBClientId(@NonNull final String clientId,
                                            @NonNull final String pushToken,
                                            @Nullable final GenericHandler completionHandler) {
        final MobileMessenger messenger = getMobileMessenger();
        messenger.refreshToken(clientId, OobRegistrationConfig.getOobChannel(), pushToken, null,
                new MobileMessengerCallback() {
                    @Override
                    public void onSuccess() {
                        if (completionHandler != null) {
                            completionHandler.onFinished(true, null);
                        }
                    }

                    @Override
                    public void onError(final FastTrackException exception) {
                        if (completionHandler != null) {
                            completionHandler.onFinished(false, exception.getLocalizedMessage());
                        }
                    }
                }
        );
    }

    //region Storage helper methods

    /**
     * Stores the firebase push token in to shared preferences.
     *
     * @param pushToken Firebase push token to store.
     */
    private static void storeFcmPushToken(final String pushToken) {
        final SharedPreferences pref = getSharedPrefs();
        if (pref != null) {
            pref.edit().putString(PUSH_TOKEN, pushToken).apply();
        }
    }

    /**
     * Reads the firebase push token from shared preferences.
     *
     * @return Firebase push token if stored, else {@code null}.
     */
    private static @Nullable
    String readFcmPushToken() {
        final SharedPreferences pref = getSharedPrefs();
        return pref != null ? pref.getString(PUSH_TOKEN, null) : null;
    }

    /**
     * Stores the client id in to shared preferences.
     *
     * @param clientId Client id to store, null to remove the value
     */
    private static void storeClientId(final String clientId) {
        final SharedPreferences pref = getSharedPrefs();
        if (pref != null) {
            final SharedPreferences.Editor editor = pref.edit();

            if (clientId != null) {
                editor.putString(CLIENT_ID, clientId);
            } else {
                editor.remove(CLIENT_ID);
            }

            editor.apply();
        }
    }

    /**
     * Reads the client id form shared preferences.
     *
     * @return Client id if already stored, else {@code null}.
     */
    protected static @Nullable
    String readClientId() {
        final SharedPreferences pref = getSharedPrefs();
        return pref != null ? pref.getString(CLIENT_ID, null) : null;
    }
    //endregion
}
