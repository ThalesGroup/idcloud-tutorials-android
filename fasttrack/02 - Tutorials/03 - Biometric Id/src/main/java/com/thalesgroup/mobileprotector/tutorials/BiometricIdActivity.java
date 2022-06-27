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

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.Switch;

import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthPinHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.TokenStatus;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.biometricid.BiometricIdLogic;
import com.thalesgroup.mobileprotector.tutorials.biometricid.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Main entry point of the application for current flavor.
 */
public class BiometricIdActivity extends QrCodeBasicActivity {

    //region Declaration

    private ImageButton mBtnGenerateOtpTouch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mSwitchTouch;

    //endregion

    //region AbstractBaseActivity


    @Override
    protected int contentViewID() {
        return R.layout.activity_biometricid;
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
    protected OathTokenDevice updateGui() {
        // Get stored token
        final OathTokenDevice token = super.updateGui();
        final TokenStatus tokenStatus = BiometricIdLogic.getTokenStatus();

        if (mBtnGenerateOtpTouch != null && mSwitchTouch != null) {
            mBtnGenerateOtpTouch.setEnabled(tokenStatus.isTouchEnabled());
            mSwitchTouch.setEnabled(tokenStatus.isTouchSupported());
            mSwitchTouch.setChecked(tokenStatus.isTouchEnabled());
        }

        return token;
    }

    //endregion

    //region Private Helpers

    /**
     * Enables or disables an authentication mode.
     *
     * @param isEnabled {@code True} if {@code AuthMode} should be isEnabled, else {@code false}.
     */
    @SuppressWarnings("deprecation")
    private void enableOrDisableAuthMode(final boolean isEnabled) {
        // Get currently provisioned token.
        final OathTokenDevice token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }


        // Auth mode is currently enabled. We want to disable it.
        if (isEnabled) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    token.deActivateBioFingerprintMode();
                } else {
                    token.deActivateBiometricMode();
                }
                updateGui();
            } catch (final FastTrackException exception) {
                displayMessageDialog(exception);
            }
        } else {
            // We must enable multi-auth mode before activating any specific one.
            // Since we need pin for both of those operations this method will ask for it and return one directly.
            enableMultiAuth(token, (authInput, error) -> {
                if (authInput == null) {
                    displayMessageDialog(error);
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
    @SuppressWarnings("deprecation")
    private void enableMultiAuth(@NonNull final OathTokenDevice token, @NonNull final AuthPinHandler callback) {
        // Check whenever multi-auth mode is already enabled.
        final boolean isEnabled = BiometricIdLogic.isBioModeActivated(token);

        // In both cases we will need auth pin, because it's used for
        // multi-auth upgrade as well as enabling specific auth-modes.
        userPin((pin, error) -> {
            // If multi-auth is not enabled and we do have pin, we can try to upgrade it.
            if (!isEnabled) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        token.activateBioFingerprintMode(pin);
                    } else {
                        token.activateBiometricMode(pin);
                    }
                } catch (final FastTrackException exception) {
                    callback.onPinProvided(null, exception.getMessage());
                    return;
                }
            }

            callback.onPinProvided(pin, null);
        });
    }

    //endregion

    //region User Interface

    private void onButtonPressedGenerateOTPTouch() {
        // Get currently provisioned token.
        final OathTokenDevice token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        BiometricIdLogic.getUserTouchId(this, token,
                (authInput, error) -> {
                    if (authInput == null) {
                        displayMessageDialog(error);
                    } else {
                        generateAndDisplayOtpWithAuthInput(token, authInput);
                    }
                },
                (pin, error) -> {
                    if (pin == null) {
                        displayMessageDialog(error);
                    } else {
                        generateAndDisplayOtpWithPin(token, pin);
                    }
                }
        );
    }

    private void onSwitchPressedTouchId() {
        // Toggle button back, because cancel operation is not handled and success will reload UI.
        mSwitchTouch.setChecked(!mSwitchTouch.isChecked());

        enableOrDisableAuthMode(BiometricIdLogic.getTokenStatus().isTouchEnabled());
    }

    //endregion

}
