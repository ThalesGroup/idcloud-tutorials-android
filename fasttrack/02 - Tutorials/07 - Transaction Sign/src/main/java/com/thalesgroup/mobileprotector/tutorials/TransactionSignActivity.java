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

import android.widget.EditText;
import android.widget.ImageButton;

import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.tutorials.transactionsign.R;
import com.thalesgroup.mobileprotector.tutorials.transactionsign.TransactionSignLogic;

import androidx.annotation.Nullable;

/**
 * Main entry point of the application for current flavor.
 */
public class TransactionSignActivity extends SecureKeypadActivity {

    //region Declaration

    private ImageButton mBtnGenerateOtp;
    private EditText mEditAmount;
    private EditText mEditBeneficiary;

    //endregion

    //region AbstractBaseActivity

    @Override
    protected int contentViewID() {
        return R.layout.activity_transactionsign;
    }

    @Override
    protected int caption() {
        return R.string.tutorials_transaction_sign_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        mBtnGenerateOtp = findViewById(R.id.activity_transactionsign_btn_generate_otp);
        if (mBtnGenerateOtp != null) {
            mBtnGenerateOtp.setOnClickListener(sender -> onButtonPressedGenerateSignatureOtp());
        }
        mEditAmount = findViewById(R.id.activity_transactionsign_edit_amount);
        mEditBeneficiary = findViewById(R.id.activity_transactionsign_edit_beneficiary);
    }

    @Nullable
    @Override
    protected OathTokenDevice updateGui() {
        // Get stored token
        final OathTokenDevice token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnGenerateOtp != null) {
            mBtnGenerateOtp.setEnabled(token != null);
        }

        if (mEditAmount != null) {
            mEditAmount.setEnabled(token != null);
        }

        if (mEditBeneficiary != null) {
            mEditBeneficiary.setEnabled(token != null);
        }

        return token;
    }

    //endregion

    //region Shared

    protected void generateAndDisplaySignatureOtp(final String pin,
                                                  final String amount,
                                                  final String beneficiary) {
        try {
            final OtpValue otpValue = TransactionSignLogic.generateOtp(
                    pin,
                    amount,
                    beneficiary
            );

            displayMessageResult(otpValue.getOtp(), otpValue.getLifespan());
        } catch (final FastTrackException exception) {
            displayMessageDialog(exception);
        }
    }

    //endregion

    //region User Interface

    private void onButtonPressedGenerateSignatureOtp() {
        userPin((pin, error) -> generateAndDisplaySignatureOtp(
                pin,
                mEditAmount.getText().toString(),
                mEditBeneficiary.getText().toString())
        );
    }

    //endregion
}
