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

package com.thalesgroup.mobileprotector.gettingstarted.provisioning;

import android.content.SharedPreferences;

import com.gemalto.idp.mobile.fasttrack.FastTrack;
import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.MobileFingerprintSource;
import com.gemalto.idp.mobile.fasttrack.protector.MobileProtector;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathMobileProtector;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDeviceCreationCallback;
import com.gemalto.idp.mobile.fasttrack.protector.oath.TotpSettings;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.BaseLogic;
import com.thalesgroup.mobileprotector.gettingstarted.setup.SetupConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Logic for token provisioning using Mobile Protector SDK.
 */
public class ProvisioningLogic extends BaseLogic {

    private static final String PREF_IDCLOUD_USER_ID = "idCloudUserId";

    private static OathMobileProtector sOathTokenManager;

    /**
     * Provisions asynchronously a new token.
     *
     * @param userId           User id.
     * @param registrationCode Registration code.
     * @param callback         Callback back to the application - called on Main UI Thread.
     */
    public static void provisionWithUserId(
            @NonNull String userId,
            @NonNull String registrationCode,
            @NonNull GenericHandler callback
    ) {
        OathMobileProtector oathMobileProtector = getOathMobileProtector();
        oathMobileProtector.withDeviceFingerprintSource(
                new MobileFingerprintSource(
                        ProvisioningConfig.getCustomFingerprintData(),
                        MobileFingerprintSource.Type.SOFT
                )
        );

        TotpSettings totpSettings = new TotpSettings();
        totpSettings.setTimeStepSize(ProvisioningConfig.getOtpLifespan());

        oathMobileProtector.provision(userId,
                registrationCode,
                totpSettings,
                new OathTokenDeviceCreationCallback() {
                    @Override
                    public void onSuccess(
                            OathTokenDevice oathTokenDevice,
                            Map<String, String> map
                    ) {
                        saveIdCloudUserId(userId);

                        callback.onFinished(true,
                                getString(R.string.token_provision_success));
                    }

                    @Override
                    public void onError(FastTrackException exception) {
                        callback.onFinished(false, exception.getMessage());
                    }
                });
    }

    /**
     * Retrieves the first token.
     *
     * @return Token, or {@code null} if no Token available.
     */
    @Nullable
    public static OathTokenDevice getToken() {
        OathTokenDevice retValue = null;
        try {
            Set<String> tokenNames = getOathMobileProtector().getTokenDeviceNames();
            if (!tokenNames.isEmpty()) {
                retValue = getOathMobileProtector().getTokenDevice(tokenNames.iterator().next(), ProvisioningConfig.getCustomFingerprintData());
            }
        } catch (FastTrackException exception) {
            // Application might want to handle invalid token in specific way. For example remove old one and ask for new provision.
            // Sample code will simple throw exception and crash.
            // Error here usually mean wrong password or SDK configuration.
            throw new IllegalStateException(exception.getMessage());
        }

        return retValue;
    }

    /**
     * Removes the token.
     *
     * @return {@code True} if token was successfully removed, {@code false} if token was not removed successfully or no token was available.
     */
    public static boolean removeToken() {
        boolean retValue = false;

        try {
            Set<String> tokenNames = getOathMobileProtector().getTokenDeviceNames();
            if (!tokenNames.isEmpty()) {
                retValue = getOathMobileProtector().removeTokenDevice(tokenNames.iterator().next());
            }
        } catch (FastTrackException exception) {
            // Application might want to handle invalid token in specific way. For example remove old one and ask for new provision.
            // Sample code will simple throw exception and crash.
            // Error here usually mean wrong password or SDK configuration.
            throw new IllegalStateException(exception.getMessage());
        }

        if (retValue)
            saveIdCloudUserId(null);

        return retValue;
    }

    private static synchronized OathMobileProtector getOathMobileProtector() {
        if (sOathTokenManager == null) {
            try {
                sOathTokenManager = FastTrack.getInstance().getOathMobileProtectorBuilder(
                        new URL(ProvisioningConfig.getProvisioningUrl()),
                        ProvisioningConfig.getDomain(),
                        MobileProtector.ProvisioningProtocol.PROTOCOL_V5,
                        ProvisioningConfig.getRsaKeyId(),
                        ProvisioningConfig.getRsaKeyExponent(),
                        ProvisioningConfig.getRsaKeyModulus()
                )
                        .withProtectorRootPolicy(SetupConfig.getTokenRootPolicy())
                        .build();
            } catch (MalformedURLException e) {
                // This should not happen.
                throw new IllegalStateException(e.getMessage());
            }
        }

        return sOathTokenManager;
    }

    protected static void saveIdCloudUserId(String userId) {
        SharedPreferences pref = getSharedPrefs();
        if (pref != null) {
            SharedPreferences.Editor editor = pref.edit();

            if (userId != null)
                editor.putString(PREF_IDCLOUD_USER_ID, userId);
            else
                editor.remove(PREF_IDCLOUD_USER_ID);

            editor.apply();
        }
    }

    protected static String getIdCloudUserId() {
        SharedPreferences pref = getSharedPrefs();
        return pref != null ? pref.getString(PREF_IDCLOUD_USER_ID, null) : null;
    }
}
