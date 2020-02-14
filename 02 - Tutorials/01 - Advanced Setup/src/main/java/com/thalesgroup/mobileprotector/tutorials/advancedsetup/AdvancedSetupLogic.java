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

package com.thalesgroup.mobileprotector.tutorials.advancedsetup;

import android.content.Intent;
import android.widget.Toast;

import com.gemalto.idp.mobile.authentication.IdpAuthException;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthInitializeCallback;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthLicense;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthLicenseConfigurationCallback;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthService;
import com.gemalto.idp.mobile.authentication.mode.face.ui.FaceManager;
import com.gemalto.idp.mobile.core.ApplicationContextHolder;
import com.gemalto.idp.mobile.core.IdpConfiguration;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.passwordmanager.PasswordManagerException;
import com.gemalto.idp.mobile.msp.MspConfiguration;
import com.gemalto.idp.mobile.otp.OtpConfiguration;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;

/**
 * Logic for Mobile Protector SDK setup. This setup is used for advanced Mobile Protector features (FaceID, OOB, MSP).
 */
public class AdvancedSetupLogic extends AbstractBaseLogic {

    public static final String NOTIFICATION_ID_FACE_STATE_CHANGED_ACTION = "NotificationIdFaceStateChanged";
    public static final String NOTIFICATION_ID_FACE_STATE_CHANGED_DATA = "NotificationIdFaceStateChanged";

    // Default face id state is undefined. It will be updated after calling setup in updateProtectorFaceIdStatus method.
    private static AdvancedSetupConfig.ProtectorFaceIdState sFaceAuthStatus
            = AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateUndefined;

    /**
     * Setups Mobile Protector SDK.
     */
    public static void setup(final boolean includeFaceId) {
        if (!IdpCore.isConfigured()) {
            IdpCore.configure(AdvancedSetupConfig.getActivationCode(), getModuleConfigurations());

            // Login so we can use secure storage, OOB etc..
            try {
                IdpCore.getInstance().getPasswordManager().login();
            } catch (final PasswordManagerException exception) {
                // Usually it means, that someone try to login with different password than last time.
                // Password was changes etc..
                throw new IllegalStateException(exception);
            }

            // This will also register and activate license.
            if (includeFaceId) {
                FaceManager.initInstance();
                updateProtectorFaceIdStatus();
            }
        }
    }

    private static void logError(final Exception exception) {
        Toast.makeText(ApplicationContextHolder.getContext(),
                exception.getCause() + " : " + exception.getMessage(),
                Toast.LENGTH_LONG).show();
    }

    /**
     * Retrieves the face auth status.
     *
     * @return Face auth status.
     */
    public static AdvancedSetupConfig.ProtectorFaceIdState getFaceAuthStatus() {
        return sFaceAuthStatus;
    }

    /**
     * Updates the face auth status.
     */
    public static void updateProtectorFaceIdStatus() {
        // Get protector face id service instance.
        final FaceAuthService faceAuthService = FaceManager.getInstance().getFaceAuthService();

        // Initial service state as no operation was done yet.
        sFaceAuthStatus = AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateUndefined;

        // Ensure, that device can support protector face id feature.
        if (!faceAuthService.isSupported()) {
            setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateNotSupported);
            logError(new IllegalStateException(getString(R.string.protector_face_id_not_supported)));
            return;
        }

        // Allow app run without protector face id functionality.
        if (AdvancedSetupConfig.getFaceIdProductKey() == null || AdvancedSetupConfig.getFaceIdProductKey().isEmpty()
                || AdvancedSetupConfig.getFaceIdServerUrl() == null || AdvancedSetupConfig.getFaceIdServerUrl().isEmpty()) {
            setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateUnlicensed);
            return;
        }

        // Register license. Based on current license state this call might require internet connection.
        final FaceAuthLicense faceAuthLicense = new FaceAuthLicense.Builder()
                .setProductKey(AdvancedSetupConfig.getFaceIdProductKey())
                .setServerUrl(AdvancedSetupConfig.getFaceIdServerUrl()).build();
        faceAuthService.configureLicense(faceAuthLicense, new FaceAuthLicenseConfigurationCallback() {
            @Override
            public void onLicenseConfigurationSuccess() {
                setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateLicensed);

                // After licensing it's time to initialise protector module.
                if (faceAuthService.isInitialized()) {
                    // Module is loaded. Check user face enrollment.
                    updateProtectorFaceIdStatusConfigured(faceAuthService);
                } else {
                    // With license we can activate face id service.
                    faceAuthService.initialize(new FaceAuthInitializeCallback() {
                        @Override
                        public String onInitializeCamera(final String[] strings) {
                            // Return null to select the default front camera
                            return null;
                        }

                        @Override
                        public void onInitializeSuccess() {
                            // Module is loaded. Check user face enrollment.
                            updateProtectorFaceIdStatusConfigured(faceAuthService);
                        }

                        @Override
                        public void onInitializeError(final IdpException exception) {
                            setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateInitFailed);
                            logError(exception);
                        }
                    });
                }
            }

            @Override
            public void onLicenseConfigurationFailure(final IdpAuthException exception) {
                setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateUnlicensed);
                logError(new IllegalStateException(getString(R.string.protector_face_id_license_issue)));
            }
        });
    }

    /**
     * Gets the configurations for all the required features.
     *
     * @return Feature configurations.
     */
    private static IdpConfiguration[] getModuleConfigurations() {
        // OTP module is required for token management and OTP calculation.
        final OtpConfiguration otpConfiguration = new OtpConfiguration.Builder().setRootPolicy(AdvancedSetupConfig.getTokenRootPolicy()).build();

        // Mobile Signing Protocol QR parsing, push messages etc..
        final MspConfiguration.Builder mspBuilder = new MspConfiguration.Builder();
        if (AdvancedSetupConfig.getMspObfuscationCode() != null) {
            mspBuilder.setObfuscationKeys(AdvancedSetupConfig.getMspObfuscationCode());
        }

        if (AdvancedSetupConfig.getMspSignKeys() != null) {
            mspBuilder.setSignatureKeys(AdvancedSetupConfig.getMspSignKeys());
        }

        // Return all configurations.
        return new IdpConfiguration[]{otpConfiguration, mspBuilder.build()};
    }

    /**
     * Checks if {@code FaceAuthService} is configured - at least one user enrolled.
     *
     * @param faceAuthService {@code FaceAuthService}.
     */
    private static void updateProtectorFaceIdStatusConfigured(final FaceAuthService faceAuthService) {
        if (faceAuthService.isConfigured()) {
            // User face is enrolled.
            setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateReadyToUse);
        } else {
            // User face is not yet enrolled.
            setFaceIdState(AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateInitialized);
        }
    }

    /**
     * Sends the notification about face id state changed.
     *
     * @param state State of face id.
     */
    private static void setFaceIdState(final AdvancedSetupConfig.ProtectorFaceIdState state) {
        // Update face id state
        sFaceAuthStatus = state;

        final Intent intent = new Intent();
        intent.setAction(NOTIFICATION_ID_FACE_STATE_CHANGED_ACTION);
        intent.putExtra(NOTIFICATION_ID_FACE_STATE_CHANGED_DATA, sFaceAuthStatus);
        ApplicationContextHolder.getContext().sendBroadcast(intent);
    }
}
