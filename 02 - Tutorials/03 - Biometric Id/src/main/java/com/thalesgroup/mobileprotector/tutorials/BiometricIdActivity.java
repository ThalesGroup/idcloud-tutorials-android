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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import android.widget.Switch;

import com.gemalto.idp.mobile.authentication.AuthService;
import com.gemalto.idp.mobile.authentication.AuthenticationModule;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintAuthService;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.otp.oath.OathToken;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthInputHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.TokenStatus;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
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

    //endregion

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_biometricid);

        // Initialise Mobile Protector SDK.
        AdvancedSetupLogic.setup();
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

    }

    @Nullable
    @Override
    protected SoftOathToken updateGui() {
        // Get stored token
        final SoftOathToken token = super.updateGui();
        final TokenStatus tokenStatus = BiometricIdLogic.getTokenStatus();

        if (mBtnGenerateOtpTouch != null && mSwitchTouch != null) {
            mBtnGenerateOtpTouch.setEnabled(tokenStatus.isTouchEnabled());
            mSwitchTouch.setEnabled(tokenStatus.isTouchSupported());
            mSwitchTouch.setChecked(tokenStatus.isTouchEnabled());
        }

        return token;
    }

    //endregion

    //region Life Cycle

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

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

    @SuppressWarnings("deprecation")
    private void onSwitchPressedTouchId() {
        // Toggle button back, because cancel operation is not handled and success will reload UI.
        mSwitchTouch.setChecked(!mSwitchTouch.isChecked());

        enableOrDisableAuthMode(BiometricIdLogic.getTokenStatus().isTouchEnabled(),
                BioFingerprintAuthService.create(AuthenticationModule.create()));
    }


    //endregion

}
