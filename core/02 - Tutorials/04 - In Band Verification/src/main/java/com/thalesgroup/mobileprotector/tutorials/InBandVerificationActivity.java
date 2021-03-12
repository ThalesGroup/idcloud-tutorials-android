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

import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupLogic;
import com.thalesgroup.mobileprotector.tutorials.inbandverification.InBandVerificationLogic;
import com.thalesgroup.mobileprotector.tutorials.inbandverification.R;

/**
 * Main entry point of the application for current flavor.
 */
public class InBandVerificationActivity extends BiometricIdActivity {

    //region Declaration

    private ImageButton mBtnVerifyOtp;

    //endregion

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_inbandverification);

        // Initialise Mobile Protector SDK.
        AdvancedSetupLogic.setup();
    }

    @Override
    protected int caption() {
        return R.string.tutorials_in_band_verification_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        mBtnVerifyOtp = findViewById(R.id.activity_inbendverification_btn_verify_otp);
        if (mBtnVerifyOtp != null) {
            mBtnVerifyOtp.setOnClickListener(sender -> onButtonPressedVerifyOtp());
        }
    }

    @Nullable
    @Override
    protected SoftOathToken updateGui() {
        // Get stored token
        final SoftOathToken token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnVerifyOtp != null) {
            mBtnVerifyOtp.setEnabled(token != null);
        }

        return token;
    }

    //endregion

    //region User Interface

    private void onButtonPressedVerifyOtp() {
        // Get currently provisioned token.
        final SoftOathToken token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        userPin(pin -> {

            // Show loading progress
            loadingBarShow(R.string.loading_verifying);

            InBandVerificationLogic.verifyWithToken(token, pin, (success, result, lifespan) -> {
                // Wipe auth input.
                pin.wipe();
                // Hide loading progress.
                loadingBarHide();
                // Update result view with last otp lifespan.
                displayMessageResult(result, lifespan);
            });
        });
    }

    //endregion
}
