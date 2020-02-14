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

package com.thalesgroup.mobileprotector.tutorials;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import com.gemalto.idp.mobile.authentication.AuthService;
import com.gemalto.idp.mobile.authentication.AuthenticationModule;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintAuthService;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthService;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthStatus;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthUnenrollerCallback;
import com.gemalto.idp.mobile.authentication.mode.face.ui.EnrollmentCallback;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.otp.oath.OathToken;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthInputHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.TokenStatus;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupConfig;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupLogic;
import com.thalesgroup.mobileprotector.tutorials.biometricid.BiometricIdLogic;
import com.thalesgroup.mobileprotector.tutorials.biometricid.R;

/**
 * Main entry point of the application for current flavor.
 */
public class BiometricIdActivity extends QrCodeBasicActivity {

    //region Declaration

    private ImageButton mBtnGenerateOtpTouch;
    private Switch mSwitchTouch;

    private ImageButton mBtnGenerateOtpFace;
    private Switch mSwitchFace;
    private Button mBtnEnrollFace;

    // Listen to Face Id status changes.
    private final BroadcastReceiver mFaceIdStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateGui();
        }
    };

    private static final String DIALOG_FACE_ID_ENROLL = "DIALOG_FACE_ID_ENROLL";
    private static final String DIALOG_FACE_ID_VERIFY = "DIALOG_FACE_ID_VERIFY";

    //endregion

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_biometricid);

        // Initialise Mobile Protector SDK.
        AdvancedSetupLogic.setup(true);
    }

    @Override
    protected int caption() {
        return R.string.tutorials_biometric_id_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.

        mBtnGenerateOtpTouch = findViewById(R.id.activity_biometricid_btn_generate_otp_touch);
        if (mBtnGenerateOtpTouch != null) {
            mBtnGenerateOtpTouch.setOnClickListener(sender -> onButtonPressedGenerateOTPTouch());
        }

        mSwitchTouch = findViewById(R.id.activity_biometricid_switch_generate_otp_touch);
        if (mSwitchTouch != null) {
            mSwitchTouch.setOnClickListener(sender -> onSwitchPressedTouchId());
        }

        mBtnGenerateOtpFace = findViewById(R.id.activity_biometricid_btn_generate_otp_face);
        if (mBtnGenerateOtpFace != null) {
            mBtnGenerateOtpFace.setOnClickListener(sender -> onButtonPressedGenerateOTPFace());
        }
        mSwitchFace = findViewById(R.id.activity_biometricid_switch_generate_otp_face);
        if (mSwitchFace != null) {
            mSwitchFace.setOnClickListener(sender -> onSwitchPressedFaceId());
        }

        mBtnEnrollFace = findViewById(R.id.activity_biometricid_btn_enroll_face);
        if (mBtnEnrollFace != null) {
            mBtnEnrollFace.setOnClickListener(sender -> onButtonPressedEnrollFaceId());
        }
    }

    @Nullable
    @Override
    protected SoftOathToken updateGui() {
        // Get stored token
        final SoftOathToken token = super.updateGui();
        final TokenStatus tokenStatus = BiometricIdLogic.getTokenStatus();
        final AdvancedSetupConfig.ProtectorFaceIdState faceAuthStatus = AdvancedSetupLogic.getFaceAuthStatus();

        if (mBtnGenerateOtpTouch != null && mSwitchTouch != null) {
            mBtnGenerateOtpTouch.setEnabled(tokenStatus.isTouchEnabled());
            mSwitchTouch.setEnabled(tokenStatus.isTouchSupported());
            mSwitchTouch.setChecked(tokenStatus.isTouchEnabled());
        }

        if (mBtnGenerateOtpFace != null && mSwitchFace != null && mBtnEnrollFace != null) {
            mBtnGenerateOtpFace.setEnabled(tokenStatus.isProtectorFaceEnabled());
            mSwitchFace.setEnabled(tokenStatus.isProtectorFaceSupported());
            mSwitchFace.setChecked(tokenStatus.isProtectorFaceEnabled());

            mBtnEnrollFace.setEnabled(
                    faceAuthStatus == AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateReadyToUse ||
                            faceAuthStatus == AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateInitialized);
            if (faceAuthStatus == AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateReadyToUse) {
                mBtnEnrollFace.setText(R.string.protector_face_id_unenroll);
            } else {
                mBtnEnrollFace.setText(R.string.protector_face_id_enroll);
            }
        }

        return token;
    }

    //endregion

    //region Life Cycle

    @Override
    public void onResume() {
        super.onResume();

        // Register listener for Protector Face Id State change (licensing etc...)
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AdvancedSetupLogic.NOTIFICATION_ID_FACE_STATE_CHANGED_ACTION);
        registerReceiver(mFaceIdStateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister notification listener on pause.
        unregisterReceiver(mFaceIdStateReceiver);
    }

    //endregion

    //region Private Helpers

    /**
     * Enables or disables an authentication mode.
     *
     * @param isEnabled   {@code True} if {@code AuthMode} should be isEnabled, else {@code false}.
     * @param authService {@code AuthService}.
     */
    private void enableOrDisableAuthMode(final boolean isEnabled,
                                         @NonNull final AuthService authService) {
        // Get currently provisioned token.
        final SoftOathToken token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        // Auth mode is currently enabled. We want to disable it.
        if (isEnabled) {
            try {
                token.deactivateAuthMode(authService.getAuthMode());
                updateGui();
            } catch (final IdpException exception) {
                displayMessageDialog(exception);
            }
        } else {
            // We must enable multi-auth mode before activating any specific one.
            // Since we need pin for both of those operations this method will ask for it and return one directly.
            enableMultiauth(token, (authInput, error) -> {
                if (authInput == null) {
                    displayMessageDialog(error);
                } else {
                    try {
                        token.activateAuthMode(authService.getAuthMode(), authInput);
                    } catch (IdpException exception) {
                        displayMessageDialog(exception);
                    }
                }
                updateGui();
            });
        }
    }

    /**
     * Enables multi auth mode.
     *
     * @param token Token for which to enable multi auth mode.
     */
    private void enableMultiauth(@NonNull final OathToken token, @NonNull final AuthInputHandler callback) {
        try {
            // Check whenever multi-auth mode is already enabled.
            final boolean isEnabled = token.isMultiAuthModeEnabled();
            // In both cases we will need auth pin, because it's used for
            // multi-auth upgrade as well as enabling specific authmodes.
            userPin(pin -> {
                // If multi-auth is not enabled and we do have pin, we can try to upgrade it.
                if (!isEnabled) {
                    try {
                        token.upgradeToMultiAuthMode(pin);
                    } catch (final IdpException exception) {
                        callback.onFinished(null, exception.getMessage());
                        pin.wipe();
                        return;
                    }
                }

                callback.onFinished(pin, null);
                pin.wipe();
            });

        } catch (final IdpException exception) {
            callback.onFinished(null, exception.getMessage());
        }
    }

    /**
     * Enrolls the face for OTP generation.
     */
    private void enrollFace() {
        dialogFragmentShow(BiometricIdLogic.enrollFaceId(new EnrollmentCallback() {
            @Override
            public void onEnrollmentSuccess() {
                displayMessageDialog(R.string.protector_face_id_enroll_success);
                dialogFragmentHide();
                AdvancedSetupLogic.updateProtectorFaceIdStatus();
            }

            @Override
            public void onCancel() {
                dialogFragmentHide();
                AdvancedSetupLogic.updateProtectorFaceIdStatus();
            }

            @Override
            public void onEnrollmentFailed(final FaceAuthStatus status) {
                dialogFragmentHide();
                displayMessageDialog(R.string.protector_face_id_enroll_failed);
                AdvancedSetupLogic.updateProtectorFaceIdStatus();
            }

            @Override
            public void onEnrollmentRetry(final FaceAuthStatus status, final int remainingRetries) {
                AdvancedSetupLogic.updateProtectorFaceIdStatus();
            }

            @Override
            public void onError(final IdpException exception) {
                dialogFragmentHide();
                displayMessageDialog(exception);
                AdvancedSetupLogic.updateProtectorFaceIdStatus();
            }
        }), DIALOG_FACE_ID_ENROLL, false);
    }

    private void unenrollFace() {
        BiometricIdLogic.unenroll(new FaceAuthUnenrollerCallback() {
            @Override
            public void onUnenrollFinish(final FaceAuthStatus faceAuthStatus) {
                displayMessageDialog(R.string.protector_face_id_unenroll_success);
                AdvancedSetupLogic.updateProtectorFaceIdStatus();
            }

            @Override
            public void onUnenrollError(final IdpException exception) {
                displayMessageDialog(exception);
            }
        });
    }

    //endregion

    //region User Interface

    private void onButtonPressedGenerateOTPTouch() {
        // Get currently provisioned token.
        final SoftOathToken token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        BiometricIdLogic.getUserTouchId(this, token, (authInput, error) -> {
            if (authInput == null) {
                displayMessageDialog(error);
            } else {
                generateAndDisplayOtp(token, authInput);
            }
        });
    }

    private void onButtonPressedGenerateOTPFace() {
        // Get currently provisioned token.
        final SoftOathToken token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        dialogFragmentShow(BiometricIdLogic.getUserFaceId(token, (authInput, error) -> {
            dialogFragmentHide();
            if (authInput == null) {
                displayMessageDialog(error);
            } else {
                generateAndDisplayOtp(token, authInput);
            }
        }), DIALOG_FACE_ID_VERIFY, false);
    }

    private void onSwitchPressedTouchId() {
        // Toggle button back, because cancel operation is not handled and success will reload UI.
        mSwitchTouch.setChecked(!mSwitchTouch.isChecked());

        enableOrDisableAuthMode(BiometricIdLogic.getTokenStatus().isTouchEnabled(),
                BioFingerprintAuthService.create(AuthenticationModule.create()));
    }

    private void onSwitchPressedFaceId() {
        // Toggle button back, because cancel operation is not handled and success will reload UI.
        mSwitchFace.setChecked(!mSwitchFace.isChecked());

        enableOrDisableAuthMode(BiometricIdLogic.getTokenStatus().isProtectorFaceEnabled(),
                FaceAuthService.create(AuthenticationModule.create()));
    }

    private void onButtonPressedEnrollFaceId() {
        if (AdvancedSetupLogic.getFaceAuthStatus() == AdvancedSetupConfig.ProtectorFaceIdState.ProtectorFaceIdStateInitialized) {
            enrollFace();
        } else {
            unenrollFace();
        }
    }

    //endregion

}
