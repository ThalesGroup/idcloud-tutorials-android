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

import android.widget.ImageButton;

import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthPinsHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.Lifespan;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.changepin.ChangePinLogic;
import com.thalesgroup.mobileprotector.tutorials.changepin.R;
import com.thalesgroup.mobileprotector.tutorials.inbandverification.InBandVerificationLogic;

import androidx.annotation.Nullable;

/**
 * Main entry point of the application for current flavor.
 */
public class ChangePinActivity extends InBandVerificationActivity {

    //region Declaration

    private ImageButton mBtnChangePin;

    //endregion

    //region AbstractBaseActivity


    @Override
    protected int contentViewID() {
        return R.layout.activity_changepin;
    }

    @Override
    protected int caption() {
        return R.string.tutorials_change_pin_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        mBtnChangePin = findViewById(R.id.activity_changepin_btn_change_pin);
        if (mBtnChangePin != null) {
            mBtnChangePin.setOnClickListener(sender -> onButtonPressedChangePin());
        }
    }

    @Nullable
    @Override
    protected OathTokenDevice updateGui() {
        // Get stored token
        final OathTokenDevice token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnChangePin != null) {
            mBtnChangePin.setEnabled(token != null);
        }

        return token;
    }
    //endregion

    //region Shared

    protected void userPins(final AuthPinsHandler callback) {
        userPin((pin1, error1) -> {
            if (pin1 != null) {
                userPin((pin2, error2) -> {
                    if (pin2 != null) {
                        callback.onPinProvided(pin1, pin2);
                    }
                }, "Confirm New PIN");
            }
        }, "Enter New PIN");
    }

    //endregion

    //region Private Helpers

    private void changePin_GetAndVerifyNewPin(final OathTokenDevice token,
                                              final String oldPin,
                                              final Lifespan lifespan) {
        // Display pin input dialog.
        userPins(
                (firstPin, secondPin) -> {
                    final String result = ChangePinLogic.changePin(token, oldPin, firstPin, secondPin);
                    displayMessageResult(result, lifespan);
                }
        );
    }

    //endregion

    //region User Interface

    private void onButtonPressedChangePin() {
        // Get currently provisioned token.
        final OathTokenDevice token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        // Get the original PIN value from user
        userPin((pin, error) -> {
            // Display progress bar.
            loadingBarShow(R.string.loading_verifying);

            // Verify old pin validity first. Incorrect entry will invalidate token.
            InBandVerificationLogic.verifyWithToken(token, pin, (success, result, lifespan) -> {
                // Hide progress bar.
                loadingBarHide();

                // Continue with change pin on successful verification.
                if (success) {
                    changePin_GetAndVerifyNewPin(token, pin, lifespan);
                } else {
                    // Update result view with last otp lifespan.
                    displayMessageResult(result, lifespan);
                }
            });
        });
    }

    //endregion

}
