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

import android.os.CancellationSignal;
import android.support.annotation.NonNull;

import com.gemalto.idp.mobile.authentication.AuthenticationModule;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintAuthInput;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintAuthService;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintAuthenticationCallbacks;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintContainer;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.otp.oath.OathToken;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthInputHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.TokenStatus;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.BiometricIdActivity;
import com.thalesgroup.mobileprotector.tutorials.FragmentBioFingerprint;

import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_CANCELED;

/**
 * Logic for handling Biometric use cases.
 */
public class BiometricIdLogic extends AbstractBaseLogic {

    private static final String DIALOG_TAG_TOUCH_ID = "DIALOG_TAG_TOUCH_ID";

    /**
     * Returns token status summary with all biometric status details.
     *
     * @return Current status.
     */
    @SuppressWarnings("deprecation")
    public static TokenStatus getTokenStatus() {
        final TokenStatus tokenStatus = new TokenStatus();
        final SoftOathToken token = ProvisioningLogic.getToken();
        try {
            // Check all auth mode states so we can enable / disable proper buttons.
            AuthenticationModule authenticationModule = AuthenticationModule.create();
            // Create an object that represents fingerprint authentication service
            BioFingerprintAuthService touchService = BioFingerprintAuthService.create(authenticationModule);

            tokenStatus.setTouchSupported(token != null && touchService.isSupported() && touchService.isConfigured());
            tokenStatus.setTouchEnabled(token != null && tokenStatus.isTouchSupported() && token
                    .isAuthModeActive(touchService.getAuthMode()));

        } catch (IdpException exception) {
            // TODO: Error
        }
        return tokenStatus;
    }

    /**
     * Returns user Touch Id auth input or error description for given token.
     *
     * @param activity you want to use for authentication.
     * @param token    Token you want to use for authentication.
     * @param callback Auth / Error completion handler.
     */
    @SuppressWarnings("deprecation")
    public static void getUserTouchId(
            @NonNull final BiometricIdActivity activity,
            @NonNull OathToken token,
            @NonNull final AuthInputHandler callback
    ) {
        BioFingerprintAuthService service = BioFingerprintAuthService.create(AuthenticationModule.create());
        BioFingerprintContainer container = service.getBioFingerprintContainer();
        final CancellationSignal cancelSignal = new CancellationSignal();

        FragmentBioFingerprint fpFragment = FragmentBioFingerprint
                .create(new FragmentBioFingerprint.BioFpFragmentCallback() {
                    @Override
                    public void onPinFallback() {
                        // Hide current dialog
                        activity.dialogFragmentHide();

                        // Send canceling signal to stop fingerprint reader.
                        cancelSignal.cancel();

                        // Fallback to pin variant.
                        activity.userPin(pin -> callback.onFinished(pin, null));
                    }

                    @Override
                    public void onCancel() {
                        cancelSignal.cancel();
                    }
                });

        // Trigger system authentication
        container.authenticateUser(token, cancelSignal,
                new BioFingerprintAuthenticationCallbacks() {
                    @Override
                    public void onSuccess(final BioFingerprintAuthInput bioFingerprintAuthInput) {
                        activity.dialogFragmentHide();
                        callback.onFinished(bioFingerprintAuthInput, null);
                    }

                    @Override
                    public void onStartFPSensor() {
                        activity.dialogFragmentShow(fpFragment, DIALOG_TAG_TOUCH_ID, false);
                    }

                    @Override
                    public void onError(final IdpException exception) {
                        callback.onFinished(null, exception.getLocalizedMessage());
                    }

                    @Override
                    public void onAuthenticationError(final int errorCode,
                                                      final CharSequence charSequence) {
                        // We don't want to show cancel error, since it's obvious to user.
                        if (errorCode != FINGERPRINT_ERROR_CANCELED) {
                            activity.displayMessageDialog(charSequence.toString());
                            activity.dialogFragmentHide();
                        }
                    }

                    @Override
                    public void onAuthenticationHelp(final int helpCode,
                                                     final CharSequence charSequence) {
                        activity.displayMessageDialog(charSequence.toString());
                    }

                    @Override
                    public void onAuthenticationSucceeded() {
                        fpFragment.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        fpFragment.onFailure();
                    }
                });
    }

}