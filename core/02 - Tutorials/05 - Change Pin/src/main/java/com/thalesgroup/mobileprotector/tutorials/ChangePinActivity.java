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

import android.support.annotation.Nullable;
import android.widget.ImageButton;

import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.gemalto.idp.mobile.ui.UiModule;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputBuilder;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputService;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputUi;
import com.gemalto.idp.mobile.ui.secureinput.SecureKeypadListener;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthPinsHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.Lifespan;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupLogic;
import com.thalesgroup.mobileprotector.tutorials.changepin.ChangePinLogic;
import com.thalesgroup.mobileprotector.tutorials.changepin.R;
import com.thalesgroup.mobileprotector.tutorials.inbandverification.InBandVerificationLogic;

/**
 * Main entry point of the application for current flavor.
 */
public class ChangePinActivity extends InBandVerificationActivity {

    //region Declaration

    private ImageButton mBtnChangePin;
    private static final String DIALOG_TAG_CHANGE_PIN = "DIALOG_TAG_CHANGE_PIN";
    private SecureInputBuilder builder;

    //endregion

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_changepin);

        // Initialise Mobile Protector SDK.
        AdvancedSetupLogic.setup();
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
    protected SoftOathToken updateGui() {
        // Get stored token
        final SoftOathToken token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnChangePin != null) {
            mBtnChangePin.setEnabled(token != null);
        }

        return token;
    }
    //endregion

    //region Shared

    protected void userPins(final AuthPinsHandler callback) {
        builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();
        final SecureInputUi secureInputUi = builder.buildKeypad(false, true, true, new SecureKeypadListener() {
            @Override
            public void onKeyPressedCountChanged(final int count, final int inputField) {
                // Handle on key pressed if needed.
            }

            @Override
            public void onInputFieldSelected(final int inputField) {
                // Handle on input field selected if needed.
            }

            @Override
            public void onOkButtonPressed() {
                // Handle on Button pressed if needed.
            }

            @Override
            public void onDeleteButtonPressed() {
                // Handle on delete button pressed if needed.
            }

            @Override
            public void onFinish(final PinAuthInput pinAuthInput, final PinAuthInput pinAuthInput1) {
                dialogFragmentHide();

                callback.onPinProvided(pinAuthInput, pinAuthInput1);

                // Wipe the builder
                builder.wipe();
            }

            @Override
            public void onError(final String errorMessage) {
                dialogFragmentHide();

                // Notify user about possible error.
                displayMessageDialog(errorMessage);

                // Wipe the builder
                builder.wipe();
            }
        });

        // Display dialog using common method.
        dialogFragmentShow(secureInputUi.getDialogFragment(), DIALOG_TAG_CHANGE_PIN, false);
    }

    //endregion

    //region Private Helpers

    private void changePin_GetAndVerifyNewPin(final SoftOathToken token,
                                              final PinAuthInput oldPin,
                                              final Lifespan lifespan) {
        // Display pin input dialog.
        userPins((firstPin, secondPin) -> {
            final String result = ChangePinLogic.changePin(token, oldPin, firstPin, secondPin);
            displayMessageResult(result, lifespan);
        });
    }

    //endregion

    //region User Interface

    private void onButtonPressedChangePin() {
        // Get currently provisioned token.
        final SoftOathToken token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        // Get the original PIN value from user
        userPin(pin -> {
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
                    pin.wipe();

                    // Update result view with last otp lifespan.
                    displayMessageResult(result, lifespan);
                }
            });
        });
    }

    //endregion

}
