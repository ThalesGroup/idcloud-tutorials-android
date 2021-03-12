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

package com.thalesgroup.mobileprotector.gettingstarted;

import android.support.annotation.Nullable;
import android.widget.ImageButton;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.gemalto.idp.mobile.ui.UiModule;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputBuilder;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputService;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputUi;
import com.gemalto.idp.mobile.ui.secureinput.SecureKeypadListener;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthPinHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.Lifespan;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpLogic;
import com.thalesgroup.mobileprotector.gettingstarted.otp.R;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.gettingstarted.setup.SetupLogic;
import com.thalesgroup.mobileprotector.uicomponents.ResultFragment;

/**
 * Main entry point of the application for current flavor.
 */
public class OtpActivity extends ProvisioningActivity {

    //region Declaration

    private ResultFragment mResultFragment;

    private ImageButton mBtnGenerateOtp;

    private static final String DIALOG_TAG_PIN_ENTRY = "DIALOG_TAG_PIN_ENTRY";

    private SecureInputBuilder builder;

    //endregion

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_otp);

        // Initialise Mobile Protector SDK.
        SetupLogic.setup();
    }

    @Override
    protected int caption() {
        return R.string.gettingstarted_otp_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        mResultFragment = (ResultFragment) getSupportFragmentManager().findFragmentById(R.id.shared_fragment_result);

        mBtnGenerateOtp = findViewById(R.id.shared_btn_generate_otp_pin);
        if (mBtnGenerateOtp != null) {
            mBtnGenerateOtp.setOnClickListener(sender -> onButtonPressedGenerateOTPPin());
        }
    }


    @Nullable
    @Override
    protected SoftOathToken updateGui() {
        // Get stored token
        SoftOathToken token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnGenerateOtp != null) {
            mBtnGenerateOtp.setEnabled(token != null);
        }

        // Hide OTP Result related value if user remove token.
        if (mResultFragment != null && token == null) {
            mResultFragment.hide();
        }

        return token;
    }

    //endregion

    //region Shared

    protected void displayMessageResult(
            String message,
            Lifespan lifespan
    ) {
        if (mResultFragment == null) {
            throw new IllegalStateException(getString(R.string.missing_result_fragment));
        }

        mResultFragment.show(message, lifespan);
    }

    public void userPin(AuthPinHandler callback) {
        builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();
        SecureInputUi secureInputUi = builder.buildKeypad(false, false, true, new SecureKeypadListener() {
            @Override
            public void onKeyPressedCountChanged(int count, int inputField) {
                // Handle on key pressed if needed.
            }

            @Override
            public void onInputFieldSelected(int inputField) {
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
            public void onFinish(PinAuthInput pinAuthInput, PinAuthInput pinAuthInput1) {
                // Hide view and notify handler.
                dialogFragmentHide();
                callback.onPinProvided(pinAuthInput);

                // Wipe the builder
                builder.wipe();
            }

            @Override
            public void onError(String errorMessage) {
                // Hide view and notify handler.
                dialogFragmentHide();
                displayMessageDialog(errorMessage);

                // Wipe the builder
                builder.wipe();
            }
        });

        // Display dialog using common method.
        dialogFragmentShow(secureInputUi.getDialogFragment(), DIALOG_TAG_PIN_ENTRY, false);
    }

    protected void generateAndDisplayOtp(SoftOathToken token, AuthInput authInput) {
        try {
            OtpValue otpValue = OtpLogic.generateOtp(token, authInput);
            displayMessageResult(otpValue.getOtp().toString(), otpValue.getLifespan());
            otpValue.wipe();
        } catch (IdpException exception) {
            displayMessageDialog(exception);
        }

        authInput.wipe();
    }

    protected void generateAndDisplayOtp_PinInput() {
        // Display pin input dialog.
        userPin(pin -> generateAndDisplayOtp(ProvisioningLogic.getToken(), pin));
    }

    //endregion

    //region User Interface

    private void onButtonPressedGenerateOTPPin() {
        generateAndDisplayOtp_PinInput();
    }

    //endregion

}
