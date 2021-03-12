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

package com.thalesgroup.mobileprotector.tutorials.biometricid;

import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;

import com.gemalto.idp.mobile.fasttrack.protector.BioFingerprintAuthenticationCallbacks;
import com.gemalto.idp.mobile.fasttrack.FastTrack;
import com.gemalto.idp.mobile.fasttrack.protector.ProtectorAuthInput;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathMobileProtector;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthInputHandler;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthPinHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.BaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.TokenStatus;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.BiometricIdActivity;
import com.thalesgroup.mobileprotector.tutorials.FragmentBioFingerprint;

/**
 * Logic for handling Biometric use cases.
 */
public class BiometricIdLogic extends BaseLogic {

    private static final String DIALOG_TAG_TOUCH_ID = "DIALOG_TAG_TOUCH_ID";

    /**
     * Returns token status summary with all biometric status details.
     *
     * @return Current status.
     */
    public static TokenStatus getTokenStatus() {
        TokenStatus tokenStatus = new TokenStatus();
        OathTokenDevice token = ProvisioningLogic.getToken();

        tokenStatus.setTouchSupported(token != null && canAuthenticate());
        tokenStatus.setTouchEnabled(token != null && tokenStatus.isTouchSupported() && isBioModeActivated(token));

        return tokenStatus;
    }

    @SuppressWarnings("deprecation")
    public static boolean canAuthenticate() {
        OathMobileProtector oathMobileProtector = FastTrack.getInstance().getOathMobileProtectorInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            return oathMobileProtector.isBioFingerprintModeSupported() && oathMobileProtector.isBioFingerprintModeConfigured();

        return oathMobileProtector.canBiometricAuthenticate() == 0;
    }

    @SuppressWarnings("deprecation")
    public static boolean isBioModeActivated(OathTokenDevice oathTokenDevice) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            return oathTokenDevice.isBioFingerprintModeActivated();

        return oathTokenDevice.isBiometricModeActivated();
    }

    /**
     * Returns user Touch Id auth input or error description for given token.
     *
     * @param activity         you want to use for authentication.
     * @param token            Token you want to use for authentication.
     * @param authInputHandler Auth / Error completion handler.
     */
    @SuppressWarnings("deprecation")
    public static void getUserTouchId(
            @NonNull final BiometricIdActivity activity,
            @NonNull OathTokenDevice token,
            @NonNull final AuthInputHandler authInputHandler,
            @NonNull final AuthPinHandler authPinHandler
    ) {
        CancellationSignal cancelSignal = new CancellationSignal();
        FragmentBioFingerprint fpFragment = FragmentBioFingerprint
                .create(new FragmentBioFingerprint.BioFpFragmentCallback() {
                    @Override
                    public void onPinFallback() {
                        // Hide current dialog
                        activity.dialogFragmentHide();

                        // Send canceling signal to stop fingerprint reader.
                        cancelSignal.cancel();

                        // Fallback to pin variant.
                        activity.userPin((pin, error) -> authPinHandler.onPinProvided(pin, null));
                    }

                    @Override
                    public void onCancel() {
                        cancelSignal.cancel();
                    }
                });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            token.authenticateWithBioFingerprint(cancelSignal,
                    new BioFingerprintAuthenticationCallbacks() {
                        @Override
                        public void onSuccess(ProtectorAuthInput bioFingerprintAuthInput) {
                            activity.dialogFragmentHide();
                            authInputHandler.onFinished(bioFingerprintAuthInput, null);
                        }

                        @Override
                        public void onStartFPSensor() {
                            activity.dialogFragmentShow(fpFragment, DIALOG_TAG_TOUCH_ID, false);
                        }

                        @Override
                        public void onAuthenticationStatus(int status, String message) {
                            authInputHandler.onFinished(null, message + " Status: " + status);
                        }
                    });
        } else {
            token.authenticateWithBiometric(
                    "Biometric Authentication",
                    "Authenticate with biometric",
                    "Please use your biometric to verify your identity",
                    "Cancel",
                    cancelSignal,
                    new com.gemalto.idp.mobile.fasttrack.protector.BiometricAuthenticationCallbacks() {
                        @Override
                        public void onSuccess(ProtectorAuthInput protectorAuthInput) {
                            activity.dialogFragmentHide();
                            authInputHandler.onFinished(protectorAuthInput, null);
                        }

                        @Override
                        public void onAuthenticationStatus(int status, String message) {
                            authInputHandler.onFinished(null, message + " Status: " + status);
                        }
                    });
        }
    }

}