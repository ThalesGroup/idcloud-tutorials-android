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

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;

import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.protector.ProtectorAuthInput;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.callbacks.AuthPinHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.Lifespan;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpLogic;
import com.thalesgroup.mobileprotector.gettingstarted.otp.R;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.uicomponents.ResultFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * Main entry point of the application for current flavor.
 */
public class OtpActivity extends ProvisioningActivity {

    //region Declaration

    private ResultFragment mResultFragment;

    private ImageButton mBtnGenerateOtp;

    //endregion

    //region AbstractBaseActivity


    @Override
    protected int contentViewID() {
        return R.layout.activity_otp;
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
    protected OathTokenDevice updateGui() {
        // Get stored token
        OathTokenDevice token = super.updateGui();

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
        userPin(callback, "Enter PIN");
    }

    public void userPin(AuthPinHandler callback, String title) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            hideKeyboard(input);

                            String pin = input.getText().toString();
                            if (!pin.isEmpty())
                                callback.onPinProvided(pin, null);
                        }
                )
                .show();
    }

    protected void generateAndDisplayOtpWithPin(OathTokenDevice token, String pin) {
        try {
            OtpValue otpValue = OtpLogic.generateOtp(token, pin);
            displayMessageResult(otpValue.getOtp(), otpValue.getLifespan());
        } catch (FastTrackException exception) {
            displayMessageDialog(exception);
        }
    }

    protected void generateAndDisplayOtpWithAuthInput(
            OathTokenDevice token,
            ProtectorAuthInput authInput
    ) {
        try {
            OtpValue otpValue = OtpLogic.generateOtpWithAuthInput(token, authInput);
            displayMessageResult(otpValue.getOtp(), otpValue.getLifespan());
        } catch (FastTrackException exception) {
            displayMessageDialog(exception);
        }
    }

    protected void generateAndDisplayOtpPinInput() {
        // Display pin input dialog.
        userPin((pin, error) -> generateAndDisplayOtpWithPin(ProvisioningLogic.getToken(), pin));
    }

    //endregion

    //region User Interface

    private void onButtonPressedGenerateOTPPin() {
        generateAndDisplayOtpPinInput();
    }

    //endregion

}
