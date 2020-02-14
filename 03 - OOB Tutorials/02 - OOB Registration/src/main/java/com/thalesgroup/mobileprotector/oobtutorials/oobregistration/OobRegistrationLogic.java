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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gemalto.idp.mobile.core.ApplicationContextHolder;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.oob.OobManager;
import com.gemalto.idp.mobile.oob.OobModule;
import com.gemalto.idp.mobile.oob.notification.OobNotificationManager;
import com.gemalto.idp.mobile.oob.notification.OobNotificationProfile;
import com.gemalto.idp.mobile.oob.registration.OobRegistrationManager;
import com.gemalto.idp.mobile.oob.registration.OobRegistrationRequest;
import com.gemalto.idp.mobile.oob.registration.OobUnregistrationManager;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of OOB registration and notification updates.
 */
public class OobRegistrationLogic extends AbstractBaseLogic {
    private static final String STORAGE = "STORAGE";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String PUSH_TOKEN = "PUSH_TOKEN";
    private static final String REGISTERED_PUSH_TOKEN = "REGISTERED_PUSH_TOKEN";

    /**
     * Initializes {@link OobManager} with parameters from {@link OobRegistrationConfig}, provided to application
     * developers.
     *
     * @return Returns {@link OobManager} object.
     */
    public static OobManager initializeOOBManager() {
        OobManager manager;
        try {
            manager = OobModule.create().createOobManager(new URL(OobRegistrationConfig.getOobRegistrationUrl()),
                    OobRegistrationConfig.getOobDomain(),
                    OobRegistrationConfig.getOobAppId(),
                    OobRegistrationConfig.getOobRsaKeyExponent(),
                    OobRegistrationConfig.getOobRsaKeyModulus());
        } catch (MalformedURLException e) {
            // bad url at compile time - should not happen
            throw new IllegalStateException(e);
        }

        return manager;
    }

    /**
     * Processes registration to OOB - Mobile secure messenger server.
     *
     * @param userId            User ID received from Bank portal.
     * @param regCode           Registration code received from Bank portal.
     * @param completionHandler Handled on which UI part should be notified about result.
     */
    public static void registerToMsm(@NonNull final String userId,
                                     @Nullable final SecureString regCode,
                                     @NonNull final GenericHandler completionHandler) {

        // Prepare notification profiles
        List<OobNotificationProfile> profiles = null;

        final String fcmPushToken = OobRegistrationLogic.readFcmPushToken();
        if (fcmPushToken != null) {
            profiles = Collections
                    .singletonList(new OobNotificationProfile(OobRegistrationConfig.getOobChannel(), fcmPushToken));
        }

        final OobRegistrationManager regManager = initializeOOBManager().getOobRegistrationManager();
        final OobRegistrationRequest request = new OobRegistrationRequest(userId,
                userId,
                OobRegistrationRequest.RegistrationMethod.REGISTRATION_CODE,
                regCode,
                profiles);

        regManager.register(request, oobRegistrationResponse -> {
            // Check MSM registration status
            if (oobRegistrationResponse != null && oobRegistrationResponse.isSucceeded()) {
                final String clientId = oobRegistrationResponse.getClientId();

                // Store client id to persistent storage
                OobRegistrationLogic.storeClientId(clientId);

                // Store FCM token
                OobRegistrationLogic.storeRegisteredPushToken(fcmPushToken);

                // Notify about failure in UI thread.
                completionHandler.onFinished(true, null);
            } else {
                // Notify about failure in UI thread.
                completionHandler.onFinished(false, oobRegistrationResponse.getMessage());
            }
        });
    }

    /**
     * @param completionHandler Handled on which UI part should be notified about result.
     */
    public static void unregisterFromMsm(@NonNull final GenericHandler completionHandler) {
        final String clientId = OobRegistrationLogic.readClientId();
        if (clientId == null) {
            // OOB Was not registered.
            completionHandler.onFinished(true, null);
        } else {
            final OobUnregistrationManager regManager = initializeOOBManager().getOobUnregistrationManager(clientId);
            regManager.unregister(oobResponse -> {
                final boolean success = oobResponse != null && oobResponse.isSucceeded();
                if (success) {
                    OobRegistrationLogic.storeClientId(null);
                }
                // Notify about result in UI thread.
                completionHandler.onFinished(success, oobResponse == null ? null : oobResponse.getMessage());
            });
        }
    }

    /**
     * Updates notification profiles on server side, so push message can reach the device.
     *
     * @param fcmPushToken Updated Firebase push token, which should be updated also on server side.
     */
    static void updateNotificationProfile(@NonNull final String fcmPushToken) {
        // Store token and update notification profile if user is registered
        OobRegistrationLogic.storeFcmPushToken(fcmPushToken);

        // Client id is null mean, that user is not yet registered.
        final String clientId = OobRegistrationLogic.readClientId();
        if (clientId == null) {
            return;
        }

        // Compare last registered token with current one.
        if (fcmPushToken.equalsIgnoreCase(OobRegistrationLogic.readRegisteredPushToken())) {
            return;
        }

        // Prepare notification profiles
        final List<OobNotificationProfile> profiles = new ArrayList<>();
        profiles.add(new OobNotificationProfile(OobRegistrationConfig.getOobChannel(), fcmPushToken));

        final OobNotificationManager manager = initializeOOBManager().getOobNotificationManager(clientId);
        manager.setNotificationProfiles(profiles, oobResponse -> {
            // Check MSM registration status
            if (oobResponse != null && oobResponse.isSucceeded()) {

                // Store FCM token
                OobRegistrationLogic.storeRegisteredPushToken(fcmPushToken);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////
    // Storage helper methods
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Stores the push token in to shared preferences.
     *
     * @param pushToken Push token to store.
     */
    private static void storeRegisteredPushToken(final String pushToken) {
        getSharedPrefs().edit().putString(REGISTERED_PUSH_TOKEN, pushToken).apply();
    }

    /**
     * Reads the push token from shared preferences.
     *
     * @return Push token if stored, else {@code null}.
     */
    private static @Nullable
    String readRegisteredPushToken() {
        return getSharedPrefs().getString(REGISTERED_PUSH_TOKEN, null);
    }

    /**
     * Stores the firebase push token in to shared preferences.
     *
     * @param pushToken Firebase push token to store.
     */
    private static void storeFcmPushToken(final String pushToken) {
        getSharedPrefs().edit().putString(PUSH_TOKEN, pushToken).apply();
    }

    /**
     * Reads the firebase push token from shared preferences.
     *
     * @return Firebase push token if stored, else {@code null}.
     */
    private static @Nullable
    String readFcmPushToken() {
        return getSharedPrefs().getString(PUSH_TOKEN, null);
    }

    /**
     * Stores the client id in to shared preferences.
     *
     * @param clientId Clint id to store.
     */
    private static void storeClientId(final String clientId) {
        getSharedPrefs().edit().putString(CLIENT_ID, clientId).apply();
    }

    /**
     * Reads the client id form shared preferences.
     *
     * @return Client id if already stored, else {@code false}.
     */
    public static @Nullable
    String readClientId() {
        return getSharedPrefs().getString(CLIENT_ID, null);
    }

    /**
     * Retrieves shared preferences.
     *
     * @return Shared preferences.
     */
    private static SharedPreferences getSharedPrefs() {
        return ApplicationContextHolder.getContext().getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
    }
}
